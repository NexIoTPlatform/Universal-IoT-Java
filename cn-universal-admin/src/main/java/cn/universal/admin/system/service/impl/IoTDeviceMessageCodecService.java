/*
 *
 * Copyright (c) 2025, NexIoT. All Rights Reserved.
 *
 * @Description: 本文件由 gitee.com/NexIoT 开发并拥有版权，未经授权严禁擅自商用、复制或传播。
 * @Author: gitee.com/NexIoT
 * @Email: wo8335224@gmail.com
 * @Wechat: outlookFil
 *
 *
 */

package cn.universal.admin.system.service.impl;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.universal.common.exception.CodecException;
import cn.universal.core.protocol.jar.ProtocolCodecJar;
import cn.universal.core.protocol.jscrtipt.ProtocolCodecJscript;
import cn.universal.core.protocol.magic.ProtocolCodecMagic;
import cn.universal.core.protocol.support.ProtocolCodecSupport;
import cn.universal.core.protocol.support.ProtocolSupportDefinition;
import cn.universal.core.service.ICodecService;
import cn.universal.dm.device.service.impl.IoTProductDeviceService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** 在线编解码转换测试 */
@Service("messageCodecService")
@Slf4j
public class IoTDeviceMessageCodecService {

  @Resource private IoTProductDeviceService iotProductDeviceService;

  @Autowired private ICodecService codecService;

  // 预解析
  public Object preDecode(String productKey, Object payload) {
    if (productKey == null || payload == null) {
      return null;
    }
    try {
      Object result = codecService.preDecode(productKey, JSONUtil.toJsonStr(payload));
      log.info("产品编号={} 原始报文={} , preDecode解码={}", productKey, payload, JSONUtil.toJsonStr(result));
      return result;
    } catch (Exception e) {
      log.info(
          "产品编号={} 原始报文={} , preDecode解码异常", productKey, payload, ExceptionUtil.getRootCause(e));
      return payload;
    }
  }

  // 上行解码
  public Object decode(String productKey, String payload) throws CodecException {
    if (StrUtil.isBlank(payload)) {
      return null;
    }
    try {
      Object result = codecService.decode(productKey, payload);
      log.info("产品编号={} 原始报文={} , 解码={}", productKey, payload, JSONUtil.toJsonStr(result));
      return result;
    } catch (Exception e) {
      log.error("产品编号={} 原始报文={} , 解码异常", productKey, payload, e);
      return null;
    }
  }

  // 下行编码
  public String encode(String productKey, String payload) throws CodecException {
    try {
      String result = codecService.encode(productKey, payload);
      log.info("产品编号={} 原始报文={} , 编码={}", productKey, payload, JSONUtil.toJsonStr(result));
      return result;
    } catch (Exception e) {
      log.error("产品编号={} 原始报文={} , 编码异常", productKey, payload, e);
      return null;
    }
  }

  /**
   * 查询产品的解码
   *
   * @param productKey
   * @return
   */
  protected ProtocolSupportDefinition selectDevProtocolDef(String productKey) {
    return iotProductDeviceService.selectProtocolDef(productKey);
  }

  /**
   * 获取编解码插件，工程模式
   *
   * @param supportType
   * @return
   */
  protected ProtocolCodecSupport getProtocolCodecProvider(String supportType) {
    if (supportType == null) {
      return null;
    }
    if (supportType.equalsIgnoreCase("jar")) {
      return ProtocolCodecJar.getInstance();
    } else if (supportType.equalsIgnoreCase("jscript")) {
      return ProtocolCodecJscript.getInstance();
    } else if (supportType.equalsIgnoreCase("magic")) {
      return ProtocolCodecMagic.getInstance();
    }
    return null;
  }
}
