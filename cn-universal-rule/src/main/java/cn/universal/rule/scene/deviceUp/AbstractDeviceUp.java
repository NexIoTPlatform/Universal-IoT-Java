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

package cn.universal.rule.scene.deviceUp;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.universal.persistence.dto.IoTDeviceDTO;
import cn.universal.persistence.entity.IoTDeviceRuleLog;
import cn.universal.persistence.entity.SceneLinkage;
import cn.universal.persistence.entity.bo.TriggerBO;
import cn.universal.persistence.mapper.IoTDeviceRuleLogMapper;
import cn.universal.persistence.mapper.SceneLinkageMapper;
import cn.universal.rule.enums.RunStatus;
import cn.universal.rule.express.ExpressTemplate;
import cn.universal.rule.model.ExeRunContext;
import cn.universal.rule.scene.deviceDown.SenceIoTDeviceDownService;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.CollectionUtils;

@Slf4j
public abstract class AbstractDeviceUp implements DeviceUp {

  /** 场景联动类型 */
  public byte ruleLogType = 1;

  @Resource private SceneLinkageMapper sceneLinkageMapper;
  @Resource private StringRedisTemplate stringRedisTemplate;
  @Resource protected ExpressTemplate expressTemplate;
  @Resource private SenceIoTDeviceDownService downService;

  @Resource private IoTDeviceRuleLogMapper ioTDeviceRuleLogMapper;

  @Override
  @Async
  public void consumer(JSONObject object, IoTDeviceDTO ioTDeviceDTO) {
    doTestTrigger(object, ioTDeviceDTO);
  }

  /** 判断触发条件是否满足 */
  public void doTestTrigger(JSONObject param, IoTDeviceDTO instance) {
    // 查询启用的规则
    List<SceneLinkage> sceneLinkageList =
        sceneLinkageMapper.selectSceneLinkageList(
            SceneLinkage.builder().devId(instance.getDeviceId()).status(0).build());
    // 是否存在该设备的场景联动
    if (CollectionUtils.isEmpty(sceneLinkageList)) {
      log.info("场景联动结束，未能匹配到设备,设备id:{}", instance.getDeviceId());
      return;
    }
    List<IoTDeviceRuleLog> logRules = new ArrayList<>();
    sceneLinkageList.forEach(
        sceneLinkage -> {
          // 创建日志
          IoTDeviceRuleLog logRule =
              IoTDeviceRuleLog.builder()
                  .cId(sceneLinkage.getId() + "")
                  .conditions("系统自动")
                  .cName(sceneLinkage.getSceneName())
                  .cType(ruleLogType)
                  .createTime(new Date())
                  .build();
          try {
            // 沉默周期判断
            String sleepKey =
                String.format(
                    "scene-trigger-sleep:%s:%s", sceneLinkage.getId(), instance.getDeviceId());
            if (StringUtils.isNotEmpty(stringRedisTemplate.opsForValue().get(sleepKey))) {
              log.info(
                  "场景联动结束，该场景联动处于沉默周期内,场景联动id:{},设备id:{}",
                  sceneLinkage.getId(),
                  instance.getDeviceId());
              logRule.setCStatus(RunStatus.error.code);
              logRule.setContent("处于沉默周期中");
              //          logRules.add(logRule);
              return;
            }
            // 是否满足触发条件
            boolean isTouch = testDeviceTrigger(sceneLinkage, param);
            // 进入沉默周期
            if (isTouch && !sceneLinkage.getSleepCycle().equals(0)) {
              stringRedisTemplate
                  .opsForValue()
                  .set(
                      sleepKey,
                      sceneLinkage.getSleepCycle().toString(),
                      sceneLinkage.getSleepCycle(),
                      TimeUnit.SECONDS);
            }
            if (!isTouch) {
              log.info(
                  "场景联动结束，不满足触发条件,场景联动id:{},设备id:{}", sceneLinkage.getId(), instance.getDeviceId());
              return;
            }
            // 执行动作，返回结果
            List<ExeRunContext> runContexts = downService.deviceDown(param, sceneLinkage);
            logRule.setCStatus(downService.matchSuccess(runContexts).code);
            logRule.setContent(JSONUtil.toJsonStr(runContexts));
            logRules.add(logRule);
            // 记录日志
          } catch (Exception e) {
            e.printStackTrace();
            log.error(
                "执行场景联动触发条件判断错误，sceneId:{},deviceId:{}",
                sceneLinkage.getId(),
                instance.getDeviceId(),
                e.getCause());
            logRule.setCStatus(RunStatus.error.code);
            logRule.setContent("执行场景联动触发条件判断错误");
            logRules.add(logRule);
          }
        });
    if (CollectionUtil.isNotEmpty(logRules)) {
      ioTDeviceRuleLogMapper.insertList(logRules);
    }
  }

  /** 判断是否存在设备触发的条件 */
  public Boolean testDeviceTrigger(SceneLinkage sceneLinkage, JSONObject param) {
    JSONArray jsonArray = JSONUtil.parseArray(sceneLinkage.getTriggerCondition());
    if (CollectionUtils.isEmpty(jsonArray)) {
      log.debug("触发条件为空deviceId={}", param);
      return false;
    }

    List<TriggerBO> triggers =
        jsonArray.stream()
            .map(o -> BeanUtil.toBean(o, TriggerBO.class))
            .filter(
                o ->
                    "device".equals(o.getTrigger())
                        && param.getStr("messageType").equalsIgnoreCase(o.getType()))
            .collect(Collectors.toList());

    if (CollectionUtils.isEmpty(triggers)) {
      log.warn("触发行为为空");
      return false;
    }
    String separator = "one".equalsIgnoreCase(sceneLinkage.getTouch()) ? "||" : "&&";
    return testAlarm(triggers, separator, param);
  }

  /** 具体判断事件或属性 */
  public boolean testAlarm(List<TriggerBO> triggers, String separator, JSONObject param) {
    return false;
  }
}
