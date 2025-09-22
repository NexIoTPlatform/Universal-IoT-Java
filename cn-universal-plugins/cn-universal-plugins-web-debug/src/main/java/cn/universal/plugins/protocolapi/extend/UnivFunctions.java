/*
 *
 * Copyright (c) 2025, IoT-Universal. All Rights Reserved.
 *
 * @Description: 本文件由 Aleo 开发并拥有版权，未经授权严禁擅自商用、复制或传播。
 * @Author: Aleo
 * @Email: wo8335224@gmail.com
 * @Wechat: outlookFil
 *
 *
 */

package cn.universal.plugins.protocolapi.extend;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.net.URLDecoder;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.universal.common.constant.IoTConstant;
import cn.universal.core.engine.annotation.Comment;
import cn.universal.core.engine.annotation.Function;
import cn.universal.core.engine.functions.DateExtension;
import jakarta.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 封装定义的脚本
 *
 * @version 1.0 @Author Aleo
 * @since 2023/5/23
 */
@Component
public class UnivFunctions implements IdeMagicFunction {
  private final String imei = "86";
  private static final double X_PI = 3.14159265358979324 * 3000.0 / 180.0;
  private static final double PI = 3.14159265358979324;

  private static final double a = 6378245.0;

  private static final double EE = 0.00669342162296594323;
  private AtomicLong currentSerialNumber = new AtomicLong(1L);
  @Resource
  private StringRedisTemplate stringRedisTemplate;

  @Function
  @Comment("基于CSQ信号强度评估")
  public String csq(@Comment(name = "csq", value = "信号强度指示值(0-31)") Integer csq) {
    // 参数有效性校验
    if (csq == null || csq < 0 || csq > 31) {
      return "参数异常";
    }
    if (csq == 99) {
      return "信道无效"; // 特殊错误码处理
    }
    // CSQ转RSSI（参考网页2中CSQ与RSSI的换算关系）
    double rssi = csq * 2 - 113; // 公式：RSSI = CSQ*2 - 113

    // 信号等级划分（综合网页6中国移动标准与网页3行业实践）
    if (rssi > -65) {
      return "极好";
    }
    if (rssi > -75) {
      return "好";
    }
    if (rssi > -85) {
      return "中";
    }
    if (rssi > -95) {
      return "差";
    }
    return "极差";
  }

  @Function
  @Comment("基于RSRP和SNR的信号强度评估")
  public String rsrpSnr(
      @Comment(name = "rsrp", value = "参考信号接收功率(dBm)") Double rsrp,
      @Comment(name = "snr", value = "信噪比(可能为10倍值需转换)") Double snr) {
    // 参数有效性校验（网页6标准范围）
    if (rsrp == null || snr == null) {
      return "参数缺失";
    }
    if (rsrp > -40 || rsrp < -140) {
      return "RSRP超限";
    }
    if (snr < 0) {
      return "SNR异常";
    }
    // 处理SNR放大倍数（如模块返回30表示实际值3dB）
    double realSnr = snr >= 100 ? snr / 10.0 : snr;
    // RSRP等级判断（网页6标准）
    String rsrpLevel = "极差";
    if (rsrp > -85) {
      rsrpLevel = "极好";
    } else if (rsrp >= -95) {
      rsrpLevel = "好";
    } else if (rsrp >= -105) {
      rsrpLevel = "中";
    } else if (rsrp >= -115) {
      rsrpLevel = "差";
    }
    // SNR等级判断（网页6标准）
    String snrLevel = "极差";
    if (realSnr > 25) {
      snrLevel = "极好";
    } else if (realSnr >= 16) {
      snrLevel = "好";
    } else if (realSnr >= 11) {
      snrLevel = "中";
    } else if (realSnr >= 3) {
      snrLevel = "差";
    }
    return Stream.of(rsrpLevel, snrLevel)
        .min(
            Comparator.comparingInt(
                level -> {
                  // 手动构建等级优先级映射（网页1、网页5实现思路）
                  Map<String, Integer> priorityMap = new HashMap<>();
                  priorityMap.put("极好", 5);
                  priorityMap.put("好", 4);
                  priorityMap.put("中", 3);
                  priorityMap.put("差", 2);
                  priorityMap.put("极差", 1);
                  return priorityMap.get(level);
                }))
        .orElse("未知");
  }

  @Comment("获取指定key的缓存")
  @Function
  public String redisOfValue(@Comment(name = "key", value = "目标对象") String key) {
    // 固定前缀 magicRedisSign:
    key = IoTConstant.MAGIC_REDIS_SIGN + key;
    String value = stringRedisTemplate.opsForValue().get(key);
    return value;
  }

  @Comment("设置指定key的缓存")
  @Function
  public Boolean redisOfValueSet(
      @Comment(name = "key", value = "目标对象") String key,
      @Comment(name = "value", value = "目标对象") String value) {
    // 固定前缀 magicRedisSign:
    key = IoTConstant.MAGIC_REDIS_SIGN + key;
    stringRedisTemplate.opsForValue().set(key, value, 5, TimeUnit.MINUTES);
    return true;
  }

  @Comment("获取通用流水号")
  @Function
  public String randomSign() {
    long nextSerialNumber = currentSerialNumber.incrementAndGet();
    if (nextSerialNumber > 0xFFFFL) {
      currentSerialNumber.set(1L);
      nextSerialNumber = currentSerialNumber.incrementAndGet();
    }
    return String.valueOf(nextSerialNumber);
  }

  @Comment("base64解码")
  @Function
  public String base64Decode(@Comment(name = "value", value = "目标对象") String payload) {
    return Base64.decodeStr(payload);
  }

  @Comment("base64编码")
  @Function
  public String base64Encode(@Comment(name = "value", value = "目标对象") String payload) {
    return Base64.encode(payload);
  }

  @Comment("判断是否是json字符串")
  @Function
  public Boolean isJson(@Comment(name = "value", value = "目标对象") String payload) {
    try {
      return JSONUtil.isTypeJSON(payload);
    } catch (Exception e) {
      return false;
    }
  }

  @Comment("判断是否是json数组")
  @Function
  public Boolean isJsonArray(@Comment(name = "value", value = "目标对象") String payload) {
    try {
      return JSONUtil.isTypeJSON(payload);
    } catch (Exception e) {
      return false;
    }
  }

  @Comment("转JSON对象")
  @Function
  public Object toJson(@Comment(name = "value", value = "目标对象") Object payload) {
    if (payload == null) {
      return null;
    }
    // 已是JSON对象或Map，直接返回
    if (payload instanceof JSONObject || payload instanceof Map) {
      return payload;
    }
    // 字符串则尝试解析为JSON对象
    if (payload instanceof String) {
      String s = (String) payload;
      if (JSONUtil.isTypeJSON(s)) {
        return JSONUtil.parseObj(s);
      }
    }
    return payload;
  }

  @Comment("字符串转JSON数组")
  @Function
  public Object toJsonArray(@Comment(name = "value", value = "目标对象") Object payload) {
    if (payload == null) {
      return null;
    }
    // 已是JSON数组或List，直接返回
    if (payload instanceof cn.hutool.json.JSONArray || payload instanceof List) {
      return payload;
    }
    // 字符串则尝试解析为JSON数组
    if (payload instanceof String) {
      String s = (String) payload;
      if (JSONUtil.isTypeJSONArray(s)) {
        return JSONUtil.parseArray(s);
      }
    }
    return payload;
  }

  @Comment("JSON转字符串")
  @Function
  public String toJsonString(@Comment(name = "value", value = "目标对象") Object payload) {
    if (JSONUtil.isTypeJSON(payload + "")) {
      return JSONUtil.toJsonStr(payload);
    }
    return payload + "";
  }

  @Comment("JSON数组转字符串")
  @Function
  public String jsonArrayToStr(@Comment(name = "value", value = "目标对象") Object payload) {
    if (JSONUtil.isTypeJSONArray(payload + "")) {
      return JSONUtil.toJsonStr(payload);
    }
    return payload + "";
  }

  @Comment("base64转16进制")
  @Function
  public String base64ToHex(@Comment(name = "value", value = "目标对象") String payload) {
    return Convert.toHex(Base64.decode(payload));
  }

  @Comment("16进制转base64")
  @Function
  public String hexToBase64(@Comment(name = "value", value = "目标对象") String payload) {
    if (StrUtil.isBlank(payload)) {
      return "";
    }
    int len = payload.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      data[i / 2] =
          (byte)
              ((Character.digit(payload.charAt(i), 16) << 4)
                  + Character.digit(payload.charAt(i + 1), 16));
    }
    return Base64.encode(data);
  }

  @Comment("判断字符串为空")
  @Function
  public boolean isEmpty(@Comment(name = "value", value = "目标对象") String value) {
    return StrUtil.isEmpty(value);
  }

  @Function
  @Comment("日期格式化")
  public String date_format(
      @Comment(name = "target", value = "目标日期") String dateStr,
      @Comment(name = "pattern", value = "格式") String pattern) {
    return dateStr == null ? null : DateExtension.format(DateUtil.parse(dateStr), pattern);
  }

  @Function
  @Comment("时间戳转日期")
  public DateTime timestampToDate(@Comment(name = "target", value = "时间戳") String dateStr) {
    return dateStr == null ? null : DateUtil.date(Long.parseLong(dateStr));
  }

  @Function
  @Comment("字符串反转")
  public String reverse(@Comment(name = "target", value = "参数") String str) {
    return StrUtil.reverse(str);
  }

  @Function
  @Comment("字符串转Ascii")
  public String stringToAscii(@Comment(name = "target", value = "字符串") String value) {
    StringBuffer sbu = new StringBuffer();
    char[] chars = value.toCharArray();
    for (int i = 0; i < chars.length; i++) {
      if (i != chars.length - 1) {
        sbu.append((int) chars[i]).append(",");
      } else {
        sbu.append((int) chars[i]);
      }
    }
    return sbu.toString();
  }

  @Function
  @Comment("Ascii转字符串")
  public String asciiToString(@Comment(name = "target", value = "字符串") String value) {
    StringBuffer sbu = new StringBuffer();
    String[] chars = value.split(",");
    for (int i = 0; i < chars.length; i++) {
      sbu.append((char) Integer.parseInt(chars[i]));
    }
    return sbu.toString();
  }

  @Function
  @Comment("Ascii转字符串")
  public String convertAsciiToStr(@Comment(name = "target", value = "字符串") String value) {
    byte[] bytes = new byte[value.length() / 2];
    for (int i = 0; i < bytes.length; i++) {
      int index = i * 2;
      int j = Integer.parseInt(value.substring(index, index + 2), 16);
      bytes[i] = (byte) j;
    }
    String str = new String(bytes);
    return str;
  }

  @Function
  @Comment("字符串转utf-8")
  public String stringToUTF8(@Comment(name = "target", value = "字符串") String str)
      throws UnsupportedEncodingException {
    if (StrUtil.isEmpty(str)) {
      return str;
    }
    return URLEncoder.encode(str, "UTF-8");
  }

  @Function
  @Comment("URL解码")
  public String urlDecode(@Comment(name = "target", value = "字符串") String str) {
    if (StrUtil.isEmpty(str)) {
      return str;
    }
    return URLDecoder.decode(str, CharsetUtil.CHARSET_UTF_8);
  }



  /***
   * CRC每个字节异或
   * @param str
   * @return
   */
  @Function
  @Comment("CRC每个字节异或")
  public String checkCRCTwoDigit(@Comment(name = "target", value = "字符串") String str) {
    if (str == null || str.length() % 2 != 0) {
      return null;
    }
    Integer result = Integer.parseInt(str.substring(0, 2), 16);
    for (int i = 2; i < str.length() - 1; i += 2) {
      Integer c1 = Integer.parseInt(str.substring(i, i + 2), 16);
      result = (result ^ c1);
    }
    return Integer.toHexString(result);
  }

  @Function
  @Comment("字符串转unicode")
  public static String stringToUnicode(@Comment(name = "target", value = "字符串") String str) {
    if (StrUtil.isEmpty(str)) {
      return str;
    }
    final int len = str.length();
    final StrBuilder unicode = StrBuilder.create(str.length() * 6);
    char c;
    for (int i = 0; i < len; i++) {
      c = str.charAt(i);
      unicode.append(HexUtil.toUnicodeHex(c)); // 形如 \ue696，刚好占6个字符
    }
    return unicode.toString();
  }

  @Function
  @Comment("十六进制转GB2312编码汉字")
  public String hexToGB2312(@Comment(name = "target", value = "字符串") String hexStr) {
    if (StrUtil.isBlank(hexStr)) {
      return hexStr;
    }
    byte[] a = HexUtil.decodeHex(hexStr);
    return new String(a, Charset.forName("GB2312")).trim();
  }

  @Function
  @Comment("GB2312编码汉字转十六进制")
  public String gb2312ToHex(@Comment(name = "target", value = "字符串") String str)
      throws UnsupportedEncodingException {
    if (StrUtil.isEmpty(str)) {
      return str;
    }
    return URLEncoder.encode(str, "GB2312").replaceAll("%", "");
  }

  /***
   * CRC校验
   * @param crc
   * @return
   */
  @Function
  @Comment("CRC校验")
  public String checkCRC(@Comment(name = "target", value = "字符串") String crc) {
    if (crc.length() % 2 != 0) {
      return null;
    }
    int sum = 0;
    for (int i = 0; i < crc.length(); i += 2) {
      sum += Integer.parseInt(crc.substring(i, i + 2), 16);
    }
    String var = String.format("%02X", sum & 0xFF);
    return var;
  }

  /***
   * CRC-CCITT(0xFFFF) 校验
   */
  @Function
  @Comment("CRC-CCITT(0xFFFF) 校验")
  public static String checkCRCCCITT(@Comment(name = "target", value = "字符串") String crc) {
    if (StrUtil.isBlank(crc)) {
      return null;
    }
    byte[] test = HexUtil.decodeHex(crc);
    int v = getCRC(test);
    return String.format("%04X", v & 0xFFFF);
  }

  /**
   * 字符串每两位倒转
   */
  @Function
  @Comment("字符串每两位倒转")
  public String reverseTwoDigit(@Comment(name = "target", value = "字符串") String str) {
    String newInput = str.replaceAll("(.{2})", "$1,");
    List<String> inputList = Arrays.asList(newInput.split(","));
    Collections.reverse(inputList);
    return String.join("", inputList);
  }

  // =====================
  // 645/对接常用 十六进制工具
  // 命名统一以 hex_ 前缀，便于 Magic-API 检索
  // =====================

  @Function
  @Comment("hex按字节加0x33（DL/T645编码）")
  public String hex_add33(@Comment(name = "target", value = "十六进制字符串") String hex) {
    if (StrUtil.isBlank(hex)) {
      return hex;
    }
    if (hex.length() % 2 != 0) {
      return null;
    }
    StringBuilder sb = new StringBuilder(hex.length());
    for (int i = 0; i < hex.length(); i += 2) {
      int b = Integer.parseInt(hex.substring(i, i + 2), 16);
      int v = (b + 0x33) & 0xFF;
      sb.append(String.format("%02X", v));
    }
    return sb.toString();
  }

  @Function
  @Comment("hex按字节减0x33（DL/T645解码）")
  public String hex_sub33(@Comment(name = "target", value = "十六进制字符串") String hex) {
    if (StrUtil.isBlank(hex)) {
      return hex;
    }
    if (hex.length() % 2 != 0) {
      return null;
    }
    StringBuilder sb = new StringBuilder(hex.length());
    for (int i = 0; i < hex.length(); i += 2) {
      int b = Integer.parseInt(hex.substring(i, i + 2), 16);
      int v = (b - 0x33) & 0xFF;
      sb.append(String.format("%02X", v));
    }
    return sb.toString();
  }


  /**
   * int强转float
   */
  @Function
  @Comment("int强转float")
  public Float intToFloat(@Comment(name = "target", value = "字符串") Integer number) {
    if (number == null) {
      return null;
    }
    return Float.intBitsToFloat(number);
  }

  /**
   * long强转double
   */
  @Function
  @Comment("long强转double")
  public Double longToDouble(@Comment(name = "target", value = "字符串") Long number) {
    if (number == null) {
      return null;
    }
    return Double.longBitsToDouble(number);
  }

  /**
   * 地球坐标系转火星坐标系
   */
  @Function
  @Comment("地球坐标系转火星坐标系")
  public String wgs84ToGcj02(@Comment(name = "target", value = "字符串") String coordinate) {
    JSONObject obj = new JSONObject();
    String lng = "";
    String lat = "";
    if (coordinate == null || !coordinate.contains(",")) {
      return JSONUtil.toJsonStr(obj);
    }
    double wgLon = Double.parseDouble(coordinate.split(",")[0]);
    double wgLat = Double.parseDouble(coordinate.split(",")[1]);
    double[] c = transform(wgLon, wgLat);
    obj.set("lng", String.format("%.6f", c[0]));
    obj.set("lat", String.format("%.6f", c[1]));

    return JSONUtil.toJsonStr(obj);
  }

  /**
   * 火星坐标系转地球坐标系
   */
  @Function
  @Comment("火星坐标系转地球坐标系")
  public String gcj02ToWgs84(@Comment(name = "target", value = "字符串") String coordinate) {
    JSONObject obj = new JSONObject();
    if (coordinate == null || !coordinate.contains(",")) {
      return JSONUtil.toJsonStr(obj);
    }
    double wgLon = Double.parseDouble(coordinate.split(",")[0]);
    double wgLat = Double.parseDouble(coordinate.split(",")[1]);
    Map<String, Double> map = delta(wgLat, wgLon);
    double lat = map.get("lat");
    double lon = map.get("lon");
    obj.set("lng", String.format("%.6f", wgLon - lon));
    obj.set("lat", String.format("%.6f", wgLat - lat));
    return JSONUtil.toJsonStr(obj);
  }

  /**
   * 车辆 十六进制转中文gbk 内部bytebuf中转
   */
  @Function
  @Comment("车辆 十六进制转中文gbk  内部bytebuf中转")
  public String hexToGBK(@Comment(name = "target", value = "字符串") String str) {
    if (StrUtil.isBlank(str)) {
      return null;
    }
    byte[] a = HexUtil.decodeHex(str);
    return new String(a, Charset.forName("gbk")).trim();
  }

  /**
   * 车辆 中文gbk转十六进制 内部bytebuf中转
   */
  @Function
  @Comment("车辆 中文gbk转十六进制  内部bytebuf中转")
  public String GBKToHex(@Comment(name = "target", value = "字符串") String str) {
    if (StrUtil.isBlank(str)) {
      return null;
    }
    byte[] bytes = str.getBytes(Charset.forName("GBK"));
    return HexUtil.encodeHexStr(bytes);
  }

  public static double[] transform(double wgLon, double wgLat) {
    double[] result = new double[2];

    if (outOfChina(wgLon, wgLat)) {
      result[0] = wgLon;
      result[1] = wgLat;
      return result;
    }
    double dLat = transformLat(wgLon - 105.0, wgLat - 35.0);
    double dLon = transformLon(wgLon - 105.0, wgLat - 35.0);
    double radLat = wgLat / 180.0 * PI;
    double magic = Math.sin(radLat);
    magic = 1 - EE * magic * magic;
    double sqrtMagic = Math.sqrt(magic);
    dLat = (dLat * 180.0) / ((a * (1 - EE)) / (magic * sqrtMagic) * PI);
    dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * PI);
    result[0] = wgLon + dLon;
    result[1] = wgLat + dLat;
    return result;
  }

  private static double transformLon(double x, double y) {
    double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
    ret += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0;
    ret += (20.0 * Math.sin(x * PI) + 40.0 * Math.sin(x / 3.0 * PI)) * 2.0 / 3.0;
    ret += (150.0 * Math.sin(x / 12.0 * PI) + 300.0 * Math.sin(x / 30.0 * PI)) * 2.0 / 3.0;
    return ret;
  }

  private static double transformLat(double x, double y) {
    double ret =
        -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
    ret += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0;
    ret += (20.0 * Math.sin(y * PI) + 40.0 * Math.sin(y / 3.0 * PI)) * 2.0 / 3.0;
    ret += (160.0 * Math.sin(y / 12.0 * PI) + 320 * Math.sin(y * PI / 30.0)) * 2.0 / 3.0;
    return ret;
  }

  private static Map delta(double lat, double lon) {
    double dLat = transformLat(lon - 105.0, lat - 35.0);
    double dLon = transformLon(lon - 105.0, lat - 35.0);
    double radLat = lat / 180.0 * PI;
    double magic = Math.sin(radLat);
    magic = 1 - EE * magic * magic;
    double sqrtMagic = Math.sqrt(magic);
    dLat = (dLat * 180.0) / ((a * (1 - EE)) / (magic * sqrtMagic) * PI);
    dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * PI);

    Map<String, Double> map = new HashMap<>();
    map.put("lat", dLat);
    map.put("lon", dLon);
    return map;
  }

  private static boolean outOfChina(double lon, double lat) {
    if ((lon < 72.004 || lon > 137.8347) && (lat < 0.8293 || lat > 55.8271)) {
      return true;
    } else {
      return false;
    }
  }

  public static int getCRC(byte[] bytes) {
    int crc = 0xFFFF;
    int polynomial = 0x1021;
    for (byte b : bytes) {
      for (int i = 0; i < 8; i++) {
        boolean bit = ((b >> (7 - i) & 1) == 1);
        boolean c15 = ((crc >> 15 & 1) == 1);
        crc <<= 1;
        if (c15 ^ bit) {
          crc ^= polynomial;
        }
      }
    }
    crc &= 0xFFFF;
    return crc;
  }

  // =====================
  // CRC 工具：命名统一以 crc_ 前缀
  // =====================

  @Function
  @Comment("CRC-CS校验值（逐字节求和低8位，两位大写HEX）")
  public String crc_cs(@Comment(name = "target", value = "十六进制字符串") String hex) {
    return checkCRC(hex);
  }

  @Function
  @Comment("CRC-CS校验验证（忽略大小写）")
  public Boolean crc_csVerify(
      @Comment(name = "data", value = "十六进制字符串") String hex,
      @Comment(name = "cs", value = "期望CS，两位HEX") String expectedCs) {
    if (StrUtil.isBlank(hex) || StrUtil.isBlank(expectedCs)) {
      return false;
    }
    String calc = checkCRC(hex);
    return StrUtil.equalsIgnoreCase(calc, expectedCs);
  }

  @Function
  @Comment("CRC-CCITT(0xFFFF) 校验值（四位大写HEX）")
  public String crc_ccitt16(@Comment(name = "target", value = "十六进制字符串") String hex) {
    return checkCRCCCITT(hex);
  }

  @Function
  @Comment("CRC-CCITT(0xFFFF) 校验验证（忽略大小写）")
  public Boolean crc_ccitt16Verify(
      @Comment(name = "data", value = "十六进制字符串") String hex,
      @Comment(name = "crc", value = "期望CRC，四位HEX") String expectedCrc) {
    if (StrUtil.isBlank(hex) || StrUtil.isBlank(expectedCrc)) {
      return false;
    }
    String calc = checkCRCCCITT(hex);
    return StrUtil.equalsIgnoreCase(calc, expectedCrc);
  }
}
