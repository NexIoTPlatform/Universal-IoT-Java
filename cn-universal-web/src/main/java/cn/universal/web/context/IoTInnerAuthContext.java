/*
 *
 * Copyright (c) 2025, IoT-Universal. All Rights Reserved.
 *
 * @Description: 本文件由 gitee.com/NexIoT 开发并拥有版权，未经授权严禁擅自商用、复制或传播。
 * @Author: gitee.com/NexIoT

 * @Wechat: outlookFil
 *
 *
 */

package cn.universal.web.context;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.universal.admin.system.service.impl.UserApplicationService;
import cn.universal.common.constant.IoTConstant;
import cn.universal.common.exception.IoTException;
import cn.universal.common.utils.AESOperator;
import cn.universal.common.utils.AuthUtil;
import cn.universal.persistence.entity.IoTUserApplication;
import cn.universal.persistence.mapper.IoTUserMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @version 1.0 @Author gitee.com/NexIoT
 * @since 2025/8/30
 */
@Slf4j
@Component
public class IoTInnerAuthContext {

  @Resource private UserApplicationService userApplicationService;

  @Resource private IoTUserMapper iotUserMapper;

  public JSONObject checkAndDecryptMsg(String encrypt, HttpServletRequest request) {

    String appId = request.getHeader(IoTConstant.HTTP_HEADER_APP);
    String time = request.getHeader(IoTConstant.HTTP_HEADER_TIME);
    String auth = request.getHeader(IoTConstant.HTTP_HEADER_AUTH);
    if (StrUtil.isBlank(appId)) {
      throw new IoTException("请求头[" + IoTConstant.HTTP_HEADER_APP + "] 不能为空", -1);
    }
    if (StrUtil.isBlank(time)) {
      throw new IoTException("请求头[" + IoTConstant.HTTP_HEADER_TIME + "] 不能为空", -1);
    }
    if (StrUtil.isBlank(time)) {
      throw new IoTException("请求头[" + IoTConstant.HTTP_HEADER_AUTH + "] 不能为空", -1);
    }
    IoTUserApplication iotUserApplication = userApplicationService.getUserAppByAppid(appId);

    ZonedDateTime temporalAccessor = ZonedDateTime.from(AuthUtil.parseZ8DateTime(time));
    ZonedDateTime now = ZonedDateTime.now().withZoneSameInstant(ZoneId.of(IoTConstant.HTTP_UTC_8));
    if (Duration.between(now, temporalAccessor).abs().getSeconds()
        > IoTConstant.HTTP_AUTH_TIMEOUT) {
      log.info(
          "app=[{}] IP=[{}] 验证已过期", iotUserApplication.getUnionId(), IPUtils.getIpAddr(request));
      throw new IoTException("auth expire", 10000);
    }

    String curAuth = AuthUtil.hashingMsg(time, iotUserApplication.getAppId(), encrypt);
    if (!Objects.equals(auth, curAuth)) {
      log.info(
          "app【{}】from {},auth：{},curAuth：{} 验证失败",
          iotUserApplication.getUnionId(),
          IPUtils.getIpAddr(request),
          auth,
          curAuth);
      throw new IoTException("auth failed", 10000);
    }

    if (!encrypt.startsWith("{") || !encrypt.endsWith("}")) {
      encrypt =
          AESOperator.getInstance()
              .decrypt(
                  encrypt.trim(), iotUserApplication.getAppId(), iotUserApplication.getAppSecret());
      if (encrypt == null) {
        log.info("app【{}】from {} 解密错误", iotUserApplication.getAppId(), IPUtils.getIpAddr(request));
        throw new IoTException("decrypt msg failed", 10000);
      }
    }
    JSONObject decode = JSONUtil.parseObj(encrypt);
    // 设置appUnionId
    decode.set("appUnionId", iotUserApplication.getUnionId());
    decode.set("applicationId", iotUserApplication.getAppUniqueId());
    return decode;
  }
}
