/*
 * Modbus RTU helpers for Magic-API
 */
package cn.universal.core.engine.extend;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.universal.core.engine.annotation.Comment;
import cn.universal.core.engine.annotation.Function;
import org.springframework.stereotype.Component;

@Component
public class ModbusFunctions implements IdeMagicFunction {

  /** Modbus从站地址处理（兼容字符串/数字输入，自动转为2位十六进制 */
  @Function
  @Comment("Modbus从站地址处理：兼容字符串/数字输入，自动转为2位十六进制（符合YX523R报警器格式）")
  public String modbus_addr(
      @Comment(name = "inputAddr", value = "从站地址（支持String：如'9'/'FF'；Number：如9/255）")
          Object inputAddr) {
    // 1. 处理空输入：默认使用说明书规定的默认485地址01 🔶1-27
    if (inputAddr == null) {
      return "01";
    }
    int addrDecimal; // 地址的十进制数值
    // 2. 兼容String类型输入（支持十进制字符串、十六进制字符串，如"9"、"0x09"、"FF"）
    if (inputAddr instanceof String) {
      String addrStr = (String) inputAddr;
      // 2.1 清理字符串：去除非十六进制字符（如"0x09"→"009"，" 1A "→"1A"）
      String cleanedAddr = addrStr.replaceAll("[^0-9A-Fa-f]", "").toUpperCase();
      // 2.2 校验清理后字符串有效性：避免空串或无法解析的情况
      if (StrUtil.isBlank(cleanedAddr)) {
        throw new IllegalArgumentException("从站地址字符串格式非法，无法解析（需含0-9、A-F字符）");
      }
      // 2.3 将清理后的十六进制字符串转为十进制数值（如"09"→9，"FF"→255）
      try {
        addrDecimal = Integer.parseInt(cleanedAddr, 16);
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("从站地址字符串解析失败，非法格式：" + addrStr);
      }
    }
    // 3. 兼容Number类型输入（如Integer、Long等，直接转为十进制数值）
    else if (inputAddr instanceof Number) {
      addrDecimal = ((Number) inputAddr).intValue();
    }
    // 4. 不支持的输入类型：抛出异常
    else {
      throw new IllegalArgumentException("从站地址输入类型非法，仅支持String或Number（如'9'、9）");
    }
    // 5. 校验地址范围：符合说明书"地址码十进制0-255"的要求 🔶1-49
    if (addrDecimal < 0 || addrDecimal > 255) {
      throw new IllegalArgumentException("从站地址超出有效范围（0-255），当前地址：" + addrDecimal);
    }
    // 6. 转为2位十六进制字符串：不足2位左补0（如9→"09"，15→"0F"，255→"FF"）
    return StrUtil.padPre(Integer.toHexString(addrDecimal).toUpperCase(), 2, '0');
  }

  // =====================
  // CRC16 (Modbus) 多项式 0xA001 低字节在前
  // =====================

  @Function
  @Comment("Modbus CRC16 计算（返回低字节在前的四位HEX，如A1B2->B2A1）")
  public String modbus_crc16(@Comment(name = "hex", value = "十六进制字符串") String hex) {
    if (StrUtil.isBlank(hex) || hex.length() % 2 != 0) {
      return null;
    }
    byte[] bytes = HexUtil.decodeHex(hex);
    int crc = 0xFFFF;
    for (byte b : bytes) {
      crc ^= (b & 0xFF);
      for (int i = 0; i < 8; i++) {
        if ((crc & 0x0001) != 0) {
          crc = (crc >>> 1) ^ 0xA001;
        } else {
          crc = (crc >>> 1);
        }
      }
    }
    int lo = crc & 0xFF;
    int hi = (crc >>> 8) & 0xFF;
    return String.format("%02X%02X", lo, hi);
  }

  @Function
  @Comment("Modbus CRC16 校验（忽略大小写）")
  public Boolean modbus_crc16Verify(
      @Comment(name = "data", value = "十六进制字符串") String hex,
      @Comment(name = "crc", value = "期望CRC 低字节在前") String expected) {
    if (StrUtil.isBlank(hex) || StrUtil.isBlank(expected)) {
      return false;
    }
    String calc = modbus_crc16(hex);
    return StrUtil.equalsIgnoreCase(calc, expected);
  }

  // =====================
  // 常用帧构建（读保持寄存器、写单寄存器等）
  // =====================

  @Function
  @Comment("构建Modbus读保持寄存器（功能码0x03）：返回完整帧(不含CRC)与CRC")
  public String modbus_buildReadHolding(
      @Comment(name = "slave", value = "从站地址 0-247") Integer slave,
      @Comment(name = "addr", value = "寄存器地址 0-65535") Integer addr,
      @Comment(name = "qty", value = "寄存器数量 1-125") Integer qty) {
    JSONObject obj = new JSONObject();
    if (slave == null || addr == null || qty == null) {
      return JSONUtil.toJsonStr(obj);
    }
    if (slave < 0 || slave > 247 || addr < 0 || addr > 0xFFFF || qty < 1 || qty > 125) {
      return JSONUtil.toJsonStr(obj);
    }
    String pdu = String.format("%02X03%04X%04X", slave, addr, qty);
    String crc = modbus_crc16(pdu);
    obj.set("pdu", pdu);
    obj.set("crc", crc);
    obj.set("frame", pdu + crc);
    return JSONUtil.toJsonStr(obj);
  }

  @Function
  @Comment("构建Modbus写单寄存器（功能码0x06）：返回完整帧(不含CRC)与CRC")
  public String modbus_buildWriteSingle(
      @Comment(name = "slave", value = "从站地址 0-247") Integer slave,
      @Comment(name = "addr", value = "寄存器地址 0-65535") Integer addr,
      @Comment(name = "value", value = "写入值 0-65535") Integer value) {
    JSONObject obj = new JSONObject();
    if (slave == null || addr == null || value == null) {
      return JSONUtil.toJsonStr(obj);
    }
    if (slave < 0 || slave > 247 || addr < 0 || addr > 0xFFFF || value < 0 || value > 0xFFFF) {
      return JSONUtil.toJsonStr(obj);
    }
    String pdu = String.format("%02X06%04X%04X", slave, addr, value);
    String crc = modbus_crc16(pdu);
    obj.set("pdu", pdu);
    obj.set("crc", crc);
    obj.set("frame", pdu + crc);
    return JSONUtil.toJsonStr(obj);
  }

  @Function
  @Comment("解析Modbus读保持寄存器响应：返回json{slave,func,byteCount,data[],crc}")
  public String modbus_parseReadHoldingResp(
      @Comment(name = "frame", value = "完整响应帧 十六进制") String frame) {
    JSONObject obj = new JSONObject();
    if (StrUtil.isBlank(frame)) {
      return JSONUtil.toJsonStr(obj);
    }
    String f = frame.toUpperCase();
    try {
      if (f.length() < 10) {
        return JSONUtil.toJsonStr(obj);
      }
      String noCrc = f.substring(0, f.length() - 4);
      String crc = f.substring(f.length() - 4);
      String calc = modbus_crc16(noCrc);
      obj.set("crc", crc);
      obj.set("crcOk", StrUtil.equalsIgnoreCase(calc, crc));
      int idx = 0;
      int slave = Integer.parseInt(noCrc.substring(idx, idx + 2), 16);
      idx += 2;
      String func = noCrc.substring(idx, idx + 2);
      idx += 2;
      int byteCount = Integer.parseInt(noCrc.substring(idx, idx + 2), 16);
      idx += 2;
      int words = byteCount / 2;
      int[] data = new int[words];
      for (int i = 0; i < words; i++) {
        data[i] = Integer.parseInt(noCrc.substring(idx, idx + 4), 16);
        idx += 4;
      }
      obj.set("slave", slave);
      obj.set("func", func);
      obj.set("byteCount", byteCount);
      obj.set("data", data);
      return JSONUtil.toJsonStr(obj);
    } catch (Exception e) {
      return JSONUtil.toJsonStr(obj);
    }
  }

  @Function
  @Comment("判断是否为 Modbus-TCP 报文（基于MBAP头校验）")
  public Boolean isModbusTcp(
      @Comment(name = "hex", value = "完整报文，十六进制字符串，允许含空格/0x") String hex) {
    if (StrUtil.isBlank(hex)) {
      return false;
    }
    String cleaned = hex.replaceAll("[^0-9A-Fa-f]", "").toUpperCase();
    if (cleaned.length() % 2 != 0) {
      return false;
    }
    int totalBytes = cleaned.length() / 2;
    if (totalBytes < 7) {
      return false; // MBAP 头至少 7 字节
    }
    try {
      int pid = Integer.parseInt(cleaned.substring(4, 8), 16);
      int lenField = Integer.parseInt(cleaned.substring(8, 12), 16);
      // LEN 表示后续字节数（UnitId + PDU），应等于 totalBytes - 6
      return pid == 0x0000 && lenField == (totalBytes - 6);
    } catch (Exception e) {
      return false;
    }
  }

  @Function
  @Comment("判断是否为 Modbus-RTU 报文（基于尾部CRC16校验，低字节在前）")
  public Boolean isModbusRtu(
      @Comment(name = "hex", value = "完整报文，十六进制字符串，允许含空格/0x") String hex) {
    if (StrUtil.isBlank(hex)) {
      return false;
    }
    String cleaned = hex.replaceAll("[^0-9A-Fa-f]", "").toUpperCase();
    if (cleaned.length() % 2 != 0 || cleaned.length() < 8) { // 最少 addr(1)+func(1)+crc(2)
      return false;
    }
    try {
      String dataNoCrc = cleaned.substring(0, cleaned.length() - 4);
      String crcTail = cleaned.substring(cleaned.length() - 4);
      String calc = modbus_crc16(dataNoCrc);
      return StrUtil.equalsIgnoreCase(calc, crcTail);
    } catch (Exception e) {
      return false;
    }
  }
}
