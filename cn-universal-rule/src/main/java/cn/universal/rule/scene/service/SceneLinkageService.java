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

package cn.universal.rule.scene.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.universal.core.message.UPRequest;
import cn.universal.persistence.dto.IoTDeviceDTO;
import cn.universal.persistence.entity.IoTDeviceRuleLog;
import cn.universal.persistence.entity.SceneLinkage;
import cn.universal.persistence.entity.bo.SceneLinkageBO;
import cn.universal.persistence.entity.bo.TriggerBO;
import cn.universal.persistence.mapper.IoTDeviceRuleLogMapper;
import cn.universal.persistence.mapper.SceneLinkageMapper;
import cn.universal.quartz.domain.SysJob;
import cn.universal.quartz.service.ISysJobService;
import cn.universal.rule.model.ExeRunContext;
import cn.universal.rule.scene.deviceDown.SenceIoTDeviceDownService;
import cn.universal.rule.scene.deviceUp.DeviceUp;
import com.github.pagehelper.PageHelper;
import jakarta.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/**
 * 场景联动Service业务层处理 @Author Aleo
 *
 * @since 2023-03-01
 */
@Service("sceneLinkageService")
@Slf4j
public class SceneLinkageService {

  @Resource private SceneLinkageMapper sceneLinkageMapper;
  @Resource private SenceIoTDeviceDownService downService;

  private final Map<String, DeviceUp> deviceUpMap;

  @Resource private ISysJobService jobService;

  @Resource private IoTDeviceRuleLogMapper ioTDeviceRuleLogMapper;

  @Resource private StringRedisTemplate stringRedisTemplate;

  public SceneLinkageService(List<DeviceUp> deviceUps) {
    //    Map<String, DeviceUp> beans = SpringUtil.getBeansOfType(DeviceUp.class);
    deviceUpMap =
        deviceUps.stream().collect(Collectors.toMap(DeviceUp::messageType, Function.identity()));
  }

  // 设备消息上行 进行触发器判断
  @Async("taskExecutor")
  public void rule(UPRequest param, IoTDeviceDTO instance) {
    JSONObject object = JSONUtil.parseObj(param);
    DeviceUp deviceUp = deviceUpMap.get(object.getStr("messageType"));
    if (deviceUp == null) {
      return;
    }
    deviceUp.consumer(object, instance);
  }

  /**
   * 查询场景联动
   *
   * @param id 场景联动ID
   * @return 场景联动
   */
  public SceneLinkage selectSceneLinkageById(Long id) {
    return sceneLinkageMapper.selectSceneLinkageById(id);
  }

  public Boolean checkSelf(Long id, String unionId) {
    return sceneLinkageMapper.checkSelf(id, unionId) > 0;
  }

  /**
   * 查询场景联动列表
   *
   * @param sceneLinkage 场景联动
   * @return 场景联动
   */
  public List<SceneLinkage> selectSceneLinkageList(SceneLinkage sceneLinkage) {
    return sceneLinkageMapper.selectSceneLinkageList(sceneLinkage);
  }

  /**
   * 新增场景联动
   *
   * @param sceneLinkage 场景联动
   * @return 结果
   */
  @Transactional
  public int insertSceneLinkage(SceneLinkageBO sceneLinkage) {
    sceneLinkage.setStatus(0);
    SceneLinkage sceneLinkage1 = BeanUtil.toBean(sceneLinkage, SceneLinkage.class);
    sceneLinkage1.setTriggerCondition(JSONUtil.toJsonStr(sceneLinkage.getTriggerCondition()));
    sceneLinkage1.setExecAction(JSONUtil.toJsonStr(sceneLinkage.getExecAction()));
    int rows = sceneLinkageMapper.insertSceneLinkage(sceneLinkage1);
    //    int result = 0;
    if (rows != 0) {
      SysJob sysJob = new SysJob();
      sysJob.setCreateBy(sceneLinkage.getCreateBy());
      sysJob.setUpdateBy(sceneLinkage.getCreateBy());
      Date date = new Date();
      sysJob.setUpdateTime(date);
      sysJob.setCreateTime(date);
      sceneLinkage.setId(sceneLinkage1.getId());
      // 定时任务结果 -1为没有定时触发  0新增失败 1成功
      addJob(sceneLinkage, sysJob);
    }
    return rows;
  }

  /**
   * 修改场景联动
   *
   * @param sceneLinkage 场景联动
   * @return 结果
   */
  @Transactional
  public int updateSceneLinkage(SceneLinkageBO sceneLinkage) {
    SceneLinkage sceneLinkage1 = BeanUtil.toBean(sceneLinkage, SceneLinkage.class);
    sceneLinkage1.setTriggerCondition(JSONUtil.toJsonStr(sceneLinkage.getTriggerCondition()));
    sceneLinkage1.setExecAction(JSONUtil.toJsonStr(sceneLinkage.getExecAction()));
    int rows = sceneLinkageMapper.updateSceneLinkage(sceneLinkage1);
    if (rows > 0) {
      // 场景联动修改
      if (sceneLinkage.getTriggerCondition() != null) {
        delJob(sceneLinkage.getId());
        SysJob sysJob = new SysJob();
        sysJob.setCreateBy(sceneLinkage.getCreateBy());
        sysJob.setUpdateBy(sceneLinkage.getCreateBy());
        Date date = new Date();
        sysJob.setUpdateTime(date);
        sysJob.setCreateTime(date);
        addJob(sceneLinkage, sysJob);
      }
      // 只更改了场景启用状态
      else {
        // 恢复场景
        if (sceneLinkage.getStatus() == 0) {
          resumeJob(sceneLinkage.getId());
        }
        // 暂停场景
        else {
          pauseJob(sceneLinkage.getId());
        }
      }
    }
    return rows;
  }

  /**
   * 删除场景联动对象
   *
   * @param ids 需要删除的数据ID
   * @return 结果
   */
  @Transactional
  public int deleteSceneLinkageByIds(Long[] ids) {
    int rows = sceneLinkageMapper.deleteSceneLinkageByIds(ids);
    if (rows > 0) {
      delJob(ids[0]);
    }
    return rows;
  }

  /**
   * 删除场景联动信息
   *
   * @param id 场景联动ID
   * @return 结果
   */
  public int deleteSceneLinkageById(Long id) {
    return sceneLinkageMapper.deleteSceneLinkageById(id);
  }

  /** 执行动作 */
  public List<ExeRunContext> functionDown(Long id) {
    SceneLinkage sceneLinkage = selectSceneLinkageById(id);
    List<ExeRunContext> runContexts = downService.deviceDown(new JSONObject(), sceneLinkage);
    // 创建日志
    IoTDeviceRuleLog logRule =
        IoTDeviceRuleLog.builder()
            .cId(sceneLinkage.getId() + "")
            .cName(sceneLinkage.getSceneName())
            .cType((byte) 1)
            .conditions("手动操作")
            .createTime(new Date())
            .content(JSONUtil.toJsonStr(runContexts))
            .cStatus(downService.matchSuccess(runContexts).code)
            .build();
    ioTDeviceRuleLogMapper.insertSelective(logRule);
    return runContexts;
  }

  /** 执行动作 */
  public List<ExeRunContext> quartzFunctionDown(Long id) {
    String key = "quartzFunctionDown:exec:";
    boolean flag =
        stringRedisTemplate.opsForValue().setIfAbsent(key + id, "0", 5, TimeUnit.MINUTES);
    if (!flag) {
      log.info("获得锁失败中止执行,id={}", id);
      return null;
    }
    SceneLinkage sceneLinkage = selectSceneLinkageById(id);
    List<ExeRunContext> runContexts = downService.deviceDown(new JSONObject(), sceneLinkage);
    // 创建日志
    IoTDeviceRuleLog logRule =
        IoTDeviceRuleLog.builder()
            .cId(sceneLinkage.getId() + "")
            .cName(sceneLinkage.getSceneName())
            .cType((byte) 1)
            .conditions("定时自动")
            .createTime(new Date())
            .content(JSONUtil.toJsonStr(runContexts))
            .cStatus(downService.matchSuccess(runContexts).code)
            .build();
    ioTDeviceRuleLogMapper.insertSelective(logRule);
    return runContexts;
  }

  // 新增调度任务
  public int addJob(SceneLinkageBO sceneLinkage, SysJob sysJob) {
    // 不存在定时任务
    int rows = -1;
    List<TriggerBO> timeTrigger =
        sceneLinkage.getTriggerCondition().stream()
            .filter(
                triggerBo -> {
                  return "timer".equals(triggerBo.getTrigger());
                })
            .collect(Collectors.toList());

    // 存在定时触发的条件
    if (!CollectionUtils.isEmpty(timeTrigger)) {
      Date date = new Date();
      sysJob.setJobGroup("场景联动功能下发");
      sysJob.setInvokeTarget(
          "sceneLinkageService.quartzFunctionDown(" + sceneLinkage.getId() + "L)");
      // 状态（0正常 1暂停）
      sysJob.setStatus("0");
      // 是否并发执行（0允许 1禁止）
      sysJob.setConcurrent("1");
      // 计划执行错误策略（1立即执行 2执行一次 3放弃执行）
      sysJob.setMisfirePolicy("3");
      timeTrigger.forEach(
          triggerBo -> {
            sysJob.setJobName("sceneId:" + sceneLinkage.getId());
            sysJob.setCronExpression(triggerBo.getCron());
            try {
              jobService.insertJob(sysJob);
            } catch (SchedulerException e) {
              log.error(
                  "场景联动调度任务新增失败，场景={},名称={}", sceneLinkage.getId(), sceneLinkage.getSceneName());
            }
          });
    }

    return rows;
  }

  /** 根据场景联动id 删除调度任务 */
  public void delJob(Long id) {
    // 根据调度任务名查找对应任务
    List<SysJob> jobs = jobService.selectJobByName("sceneId:" + id);
    if (!CollectionUtils.isEmpty(jobs)) {
      try {
        jobService.deleteJobByIds(jobs.stream().map(SysJob::getJobId).toArray(Long[]::new));
      } catch (Exception e) {
        log.error("场景联动调度任务删除失败,场景id:{}", id);
      }
    }
  }

  /** 根据场景联动id 暂停调度任务 */
  public void pauseJob(Long id) {
    List<SysJob> jobs = jobService.selectJobByName("sceneId:" + id);
    if (!CollectionUtils.isEmpty(jobs)) {
      jobs.forEach(
          sysJob -> {
            try {
              jobService.pauseJob(sysJob);
            } catch (SchedulerException e) {
              log.error("场景联动调度任务暂停失败,场景id:{}", id);
            }
          });
    }
  }

  /** 根据场景联动id 恢复调度任务 */
  public void resumeJob(Long id) {
    List<SysJob> jobs = jobService.selectJobByName("sceneId:" + id);
    if (!CollectionUtils.isEmpty(jobs)) {
      jobs.forEach(
          sysJob -> {
            try {
              jobService.resumeJob(sysJob);
            } catch (SchedulerException e) {
              log.error("场景联动调度任务恢复失败,场景id:{}", id);
            }
          });
    }
  }

  /** 分页查询场景联动执行日志 */
  public List<IoTDeviceRuleLog> getSceneLinkageLogPage(String sceneId, int pageNum, int pageSize) {
    // 分页
    PageHelper.startPage(pageNum, pageSize);
    tk.mybatis.mapper.entity.Example example =
        new tk.mybatis.mapper.entity.Example(IoTDeviceRuleLog.class);
    example.createCriteria().andEqualTo("cId", sceneId).andEqualTo("cType", (byte) 1);
    example.orderBy("createTime").desc();
    List<IoTDeviceRuleLog> logs = ioTDeviceRuleLogMapper.selectByExample(example);
    return logs;
  }
}
