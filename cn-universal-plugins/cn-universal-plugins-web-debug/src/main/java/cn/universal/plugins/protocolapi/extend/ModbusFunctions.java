/*
 * Modbus RTU helpers for Magic-API
 */
package cn.universal.plugins.protocolapi.extend;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.universal.core.engine.annotation.Comment;
import cn.universal.core.engine.annotation.Function;
import org.springframework.stereotype.Component;

@Component
public class ModbusFunctions implements IdeMagicFunction {
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
}
