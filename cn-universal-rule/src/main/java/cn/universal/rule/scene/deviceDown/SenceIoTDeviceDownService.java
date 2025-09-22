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

package cn.universal.rule.scene.deviceDown;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.universal.common.domain.R;
import cn.universal.core.service.IoTDownlFactory;
import cn.universal.persistence.entity.IoTDevice;
import cn.universal.persistence.entity.IoTDeviceTags;
import cn.universal.persistence.entity.IoTProduct;
import cn.universal.persistence.entity.SceneLinkage;
import cn.universal.persistence.entity.bo.TriggerBO;
import cn.universal.persistence.entity.bo.TriggerBO.ExecData;
import cn.universal.persistence.mapper.IoTDeviceMapper;
import cn.universal.persistence.mapper.IoTProductMapper;
import cn.universal.persistence.query.IoTDeviceQuery;
import cn.universal.rule.enums.RunStatus;
import cn.universal.rule.model.ExeRunContext;
import cn.universal.rule.model.ExeRunContext.ExeRunContextBuilder;
import cn.universal.rule.model.bo.DownResult;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SenceIoTDeviceDownService {

  @Resource private IoTDeviceMapper ioTDeviceMapper;
  @Resource private IoTProductMapper ioTProductMapper;

  public RunStatus matchSuccess(List<ExeRunContext> exeRunContexts) {
    if (CollectionUtil.isEmpty(exeRunContexts)) {
      return RunStatus.error;
    }
    boolean allSuccess = exeRunContexts.stream().allMatch(ExeRunContext::isSuccess);
    boolean allError = exeRunContexts.stream().noneMatch(ExeRunContext::isSuccess);
    if (allSuccess) {
      return RunStatus.success;
    } else if (allError) {
      return RunStatus.error;
    } else {
      return RunStatus.exist_success;
    }
  }

  public List<ExeRunContext> deviceDown(JSONObject message, SceneLinkage sceneLinkage) {
    // 解析execAction
    List<TriggerBO> execs =
        JSONUtil.parseArray(sceneLinkage.getExecAction()).stream()
            .map(o -> BeanUtil.toBean(o, TriggerBO.class))
            .collect(Collectors.toList());
    String unionId = sceneLinkage.getUpdateBy();
    List<ExeRunContext> runContexts = new ArrayList<>();
    for (TriggerBO exec : execs) {
      ExeRunContextBuilder exeRunContextBuilder =
          ExeRunContext.builder().trigger(exec.getTrigger());
      if (TriggerBO.ExecTriggerType.device.name().equals(exec.getTrigger())) {
        exeRunContextBuilder.target(exec.getDeviceId());
        exeRunContextBuilder.param(exec.getExecData());
        String downRequest = getDownRequest(exec, unionId);
        if (StrUtil.isEmpty(downRequest)) {
          exeRunContextBuilder.result("设备不存在");
          runContexts.add(exeRunContextBuilder.build());
          // 设备不存在
          continue;
        }
        try {
          DownResult downResult = functionDown(downRequest);
          log.debug("device场景联动结果={}", JSONUtil.toJsonStr(downResult));
          if (!downResult.getSuccess()) {
            log.warn("场景联动指令下发{},设备编号:{},场景编号:{}", "异常", exec.getDeviceId(), sceneLinkage.getId());
            exeRunContextBuilder.success(false);
          } else {
            exeRunContextBuilder.success(true);
          }
          exeRunContextBuilder.result(
              downResult.getDownResult() == null ? "" : downResult.getDownResult());
        } catch (Exception e) {
          exeRunContextBuilder.success(false);
          exeRunContextBuilder.result("返回为空，失败");
          log.error("场景联动指令下发失败，设备编号:{}", exec.getDeviceId());
        }
        runContexts.add(exeRunContextBuilder.build());
      }
    }
    return runContexts;
  }

  // 拼装功能下行指令
  private String getDownRequest(TriggerBO o, String unionId) {
    JSONObject downRequest = new JSONObject();
    IoTDevice ioTDevice =
        ioTDeviceMapper.getOneByDeviceId(
            IoTDeviceQuery.builder().deviceId(o.getDeviceId()).build());
    if (ioTDevice == null) {
      // 因设备被删除，场景联动的配置未修改
      return null;
    }
    String productKey = ioTDevice.getProductKey();
    downRequest.set("appUnionId", unionId);
    downRequest.set("productKey", productKey);
    downRequest.set("deviceId", o.getDeviceId());
    downRequest.set("cmd", "DEV_FUNCTION");

    JSONObject function = new JSONObject();
    function.set("messageType", "FUNCTIONS");
    function.set("function", o.getModelId());

    JSONObject data = new JSONObject();
    for (ExecData execData : o.getExecData()) {
      data.set(execData.getId(), execData.getParams());
    }
    function.set("data", data);
    downRequest.set("function", function);

    return JSONUtil.toJsonStr(downRequest);
  }

  // 指令下发
  private DownResult functionDown(String downRequest) {
    JSONObject jsonObject = JSONUtil.parseObj(downRequest);
    String deviceId = jsonObject.getStr("deviceId");
    String productKey = jsonObject.getStr("productKey");
    String appUnionId = jsonObject.getStr("appUnionId");
    IoTDevice ioTDeviceBo =
        ioTDeviceMapper.getOneByDeviceId(
            IoTDeviceQuery.builder().productKey(productKey).deviceId(deviceId).build());
    if (!ioTDeviceBo.getCreatorId().equals(appUnionId)) {
      log.error("用户{}尝试访问不属于自己的设备，设备ID：{}", appUnionId, deviceId);
      return DownResult.builder().success(false).downResult("您没有权限操作此设备。").build();
    }
    IoTProduct ioTProduct = ioTProductMapper.getProductByProductKey(productKey);
    IoTDeviceTags devTag = new IoTDeviceTags();
    devTag.setKey("geoPoint");
    devTag.setIotId(ioTDeviceBo.getIotId());
    R result = IoTDownlFactory.getIDown(ioTProduct.getThirdPlatform()).doAction(downRequest);
    log.info("场景联动,deviceId={} function={} 返回={}", deviceId, downRequest, result);
    JSONObject jsonObject1 = JSONUtil.parseObj(result);
    jsonObject1.set("down", downRequest);
    if (0 == jsonObject1.getInt("code")) {
      return DownResult.builder().success(true).downResult(result.getData()).build();
    } else {
      log.warn("场景联动指令下发失败，错误信息：{}", jsonObject1.getStr("msg"));
      return DownResult.builder().success(false).downResult(jsonObject1.getStr("msg")).build();
    }
  }
}
