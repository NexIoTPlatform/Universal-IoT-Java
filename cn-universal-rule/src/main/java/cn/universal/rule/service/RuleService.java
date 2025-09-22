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

package cn.universal.rule.service;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.universal.common.exception.IoTException;
import cn.universal.core.message.UPRequest;
import cn.universal.persistence.dto.IoTDeviceDTO;
import cn.universal.persistence.entity.RuleModel;
import cn.universal.persistence.entity.RuleModelInstance;
import cn.universal.persistence.entity.bo.RuleModelBO;
import cn.universal.persistence.entity.vo.RuleModelVO;
import cn.universal.persistence.mapper.RuleModelInstanceMapper;
import cn.universal.persistence.mapper.RuleModelMapper;
import cn.universal.rule.engine.RuleEngine;
import cn.universal.rule.enums.RuleDataLevel;
import cn.universal.rule.model.RuleConfig;
import cn.universal.rule.model.bo.RuleBo;
import cn.universal.rule.model.bo.RuleTargetTestBO;
import cn.universal.rule.transmit.RuleTransmitTemplate;
import jakarta.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

/**
 * 规则引擎服务 @Author Aleo
 *
 * @since 2023/1/13 14:34
 */
@Service
@Slf4j
public class RuleService {

  @Resource private RuleModelMapper ruleModelMapper;

  @Resource private RuleModelInstanceMapper ruleModelInstanceMapper;

  @Resource private RuleTransmitTemplate ruleTransmitTemplate;

  @Resource private RuleEngine ruleEngine;

  @Async("taskExecutor")
  public void rule(UPRequest param, IoTDeviceDTO instance) {
    doExecuteRule(JSONUtil.parseObj(param), instance);
  }

  public void doExecuteRule(JSONObject param, IoTDeviceDTO instance) {
    RuleModelBO ruleModelBo = new RuleModelBO();
    ruleModelBo.setProductKey(instance.getProductKey());
    ruleModelBo.setCreatorId(instance.getUserUnionId());
    ruleModelBo.setGroupIds(instance.getDevGroupId());
    ruleModelBo.setIotId(instance.getIotId());
    List<RuleModel> ruleModels = ruleModelMapper.selectRuleByBo(ruleModelBo);
    if (CollectionUtils.isEmpty(ruleModels)) {
      return;
    }
    ruleModels.forEach(
        ruleModel -> {
          try {
            RuleConfig ruleConfig = JSONUtil.toBean(ruleModel.getConfig(), RuleConfig.class);

            JSONObject result =
                ruleEngine.executeRule(param, ruleConfig.getSql(), instance.getAppId());

            if (Objects.nonNull(result)) {
              ruleConfig
                  .getTargets()
                  .forEach(
                      target -> {
                        try {
                          log.info(
                              "匹配到规则引擎，调用转发,ruleId:{},deviceId={}",
                              ruleModel.getId(),
                              instance.getDeviceId());
                          ruleTransmitTemplate.transmit(result, target);
                        } catch (Exception e) {
                          log.error(
                              "执行规则错误,调用转发目标失败,deviceId:{},iotId:{},ruleId:{},data:{},target:{}",
                              instance.getDeviceId(),
                              instance.getIotId(),
                              ruleModel.getId(),
                              result,
                              target);
                          log.error("执行规则错误,调用转发目标失败", e);
                        }
                      });
            }
          } catch (Exception e) {
            log.error(
                "执行规则错误,deviceId:{},iotId:{},ruleId:{}, ruleName:{}",
                instance.getDeviceId(),
                instance.getIotId(),
                ruleModel.getId(),
                ruleModel.getRuleName(),
                e);
          }
        });
  }

  public JSONObject testExecuteRule(RuleBo sqlBo) {
    JSONObject jsonObject;
    try {
      jsonObject = JSONUtil.parseObj(sqlBo.getPayload());
    } catch (Exception e) {
      throw new IoTException("payload必须为json格式");
    }
    return ruleEngine.executeRule(
        jsonObject, sqlBo.getConfig().getSql(), sqlBo.getConfig().getAppId());
  }

  /**
   * 查询规则模型
   *
   * @param ruleModelBo
   * @return
   */
  public List<RuleModelVO> queryRuleListByBo(RuleModelBO ruleModelBo) {
    return ruleModelMapper.selectRuleListByBo(ruleModelBo);
  }

  public RuleModel queryByIdAndCreator(Long id, String creator) {
    RuleModel ruleModel =
        ruleModelMapper.selectOne(RuleModel.builder().id(id).creatorId(creator).build());
    if (Objects.isNull(ruleModel)) {
      throw new IoTException("未能匹配到规则");
    }
    return ruleModel;
  }

  /**
   * 查询规则Vo
   *
   * @param id
   * @param creator
   * @return
   */
  public RuleModelVO queryVoByIdAndCreator(Long id, String creator) {
    RuleModel ruleModel = queryByIdAndCreator(id, creator);
    List<RuleModelInstance> instances =
        ruleModelInstanceMapper.select(RuleModelInstance.builder().modelId(id).build());

    return new RuleModelVO(ruleModel, instances);
  }

  /**
   * 更新规则状态
   *
   * @param ruleModelBo
   */
  public void changeStatus(RuleModelBO ruleModelBo) {
    RuleModel ruleModel = queryByIdAndCreator(ruleModelBo.getId(), ruleModelBo.getCreatorId());
    ruleModel.setStatus(ruleModelBo.getStatus());
    ruleModel.setUpdateTime(new Date());
    ruleModelMapper.updateByPrimaryKeySelective(ruleModel);
  }

  /**
   * 新增规则
   *
   * @param ruleBo
   */
  public Long addRule(RuleBo ruleBo) {
    RuleModel ruleModel = new RuleModel();
    ruleModel.setStatus("stop");
    ruleModel.setRuleName(ruleBo.getRuleName());
    ruleModel.setCreatorId(ruleBo.getCreatorId());
    ruleModel.setConfig(JSONUtil.toJsonStr(ruleBo.getConfig()));
    ruleModel.setCreateTime(new Date());

    ruleModel.setDataLevel(ruleBo.getDataLevel());
    ruleModel.setProductKey(ruleBo.getProductKey());
    ruleModelMapper.insert(ruleModel);

    saveRuleInstance(ruleBo, ruleModel);

    return ruleModel.getId();
  }

  public void deletedRule(Long id, String creator) {
    Example example = new Example(RuleModel.class);
    example.createCriteria().andEqualTo("id", id).andEqualTo("creatorId", creator);
    ruleModelMapper.deleteByExample(example);
  }

  /**
   * 保存规则实例
   *
   * @param ruleBo
   * @param ruleModel
   */
  private void saveRuleInstance(RuleBo ruleBo, RuleModel ruleModel) {
    if (RuleDataLevel.product.name().equals(ruleBo.getDataLevel())) {
      if (StringUtils.isEmpty(ruleBo.getProductKey())) {
        throw new IoTException("ProductKey不能为空");
      }
      RuleModelInstance instance = new RuleModelInstance();
      instance.setModelId(ruleModel.getId());
      instance.setRelationType(ruleModel.getDataLevel());
      instance.setRelationId(ruleModel.getProductKey());
      ruleModelInstanceMapper.insertSelective(instance);
    } else if (RuleDataLevel.group.name().equals(ruleBo.getDataLevel())) {
      if (StringUtils.isEmpty(ruleBo.getGroupId())) {
        throw new IoTException("关联设备不能为空");
      }
      RuleModelInstance instance = new RuleModelInstance();
      instance.setModelId(ruleModel.getId());
      instance.setRelationType(ruleModel.getDataLevel());
      instance.setRelationId(ruleBo.getGroupId());
      ruleModelInstanceMapper.insertSelective(instance);
    } else {
      if (CollectionUtils.isEmpty(ruleBo.getRelationIds())) {
        throw new IoTException("关联设备不能为空");
      }
      ruleBo
          .getRelationIds()
          .forEach(
              id -> {
                RuleModelInstance instance = new RuleModelInstance();
                instance.setModelId(ruleModel.getId());
                instance.setRelationType(ruleModel.getDataLevel());
                instance.setRelationId(id);
                ruleModelInstanceMapper.insertSelective(instance);
              });
    }
  }

  /**
   * 更新规则
   *
   * @param ruleBo
   */
  @Transactional(rollbackFor = Exception.class)
  public void updateRule(RuleBo ruleBo) {
    RuleModel ruleModel = queryByIdAndCreator(ruleBo.getId(), ruleBo.getCreatorId());
    ruleModel.setRuleName(ruleBo.getRuleName());
    ruleModel.setProductKey(ruleBo.getProductKey());
    ruleModel.setDataLevel(ruleBo.getDataLevel());
    ruleModel.setCreatorId(ruleBo.getCreatorId());
    ruleModel.setConfig(JSONUtil.toJsonStr(ruleBo.getConfig()));
    ruleModel.setUpdateTime(new Date());
    ruleModel.setDescription(ruleBo.getDescription());
    ruleModelMapper.updateByPrimaryKey(ruleModel);

    ruleModelInstanceMapper.delete(RuleModelInstance.builder().modelId(ruleBo.getId()).build());

    saveRuleInstance(ruleBo, ruleModel);
  }

  /**
   * 测试转发调用目标
   *
   * @param testBo
   */
  public String testRuleTarget(RuleTargetTestBO testBo) {
    JSONObject jsonObject;
    try {
      jsonObject = JSONUtil.parseObj(testBo.getParam());
    } catch (Exception e) {
      throw new IoTException("请求参数必须为json格式");
    }
    return ruleTransmitTemplate.testTransmit(jsonObject, testBo.ruleTarget);
  }

  /**
   * 更新规则转发目标
   *
   * @param ruleBo
   */
  public void updateRuleTargets(RuleBo ruleBo) {
    RuleModel ruleModel = queryByIdAndCreator(ruleBo.getId(), ruleBo.getCreatorId());

    RuleConfig ruleConfig = JSONUtil.toBean(ruleModel.getConfig(), RuleConfig.class);
    ruleConfig.setTargets(ruleBo.getConfig().getTargets());
    ruleModel.setConfig(JSONUtil.toJsonStr(ruleConfig));
    ruleModel.setUpdateTime(new Date());

    ruleModelMapper.updateByPrimaryKeySelective(ruleModel);
  }
}
