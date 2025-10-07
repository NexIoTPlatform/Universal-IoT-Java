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

package cn.universal.admin.system.web;

import cn.hutool.json.JSONObject;
import cn.universal.common.annotation.Log;
import cn.universal.common.enums.BusinessType;
import cn.universal.security.utils.SecurityUtils;
import cn.universal.security.service.IoTUserService;
import cn.universal.persistence.entity.bo.RuleModelBO;
import cn.universal.persistence.entity.vo.RuleModelVO;
import cn.universal.persistence.page.TableDataInfo;
import cn.universal.persistence.query.AjaxResult;
import cn.universal.rule.model.bo.RuleBo;
import cn.universal.rule.model.bo.RuleTargetTestBO;
import cn.universal.rule.service.RuleService;
import cn.universal.security.BaseController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 规则管理控制器
 *
 * @since 2023/1/14 9:10
 */
@RestController
@Tag(name = "规则引擎", description = "规则引擎")
@RequestMapping("/admin/v1/")
public class RuleController extends BaseController {

  @Resource private RuleService ruleService;
  @Resource private IoTUserService ioTUserService;

  @Operation(summary = "查询规则列表")
  @GetMapping("rule/list")
  public TableDataInfo<RuleModelVO> pageList(RuleModelBO ruleModelBo) {
    ruleModelBo.setCreatorId(loginIoTUnionUser(SecurityUtils.getUnionId()).getUnionId());
    startPage();
    List<RuleModelVO> userList = ruleService.queryRuleListByBo(ruleModelBo);
    return getDataTable(userList);
  }

  @Operation(summary = "查询规则")
  @GetMapping("rule")
  public AjaxResult<RuleModelVO> detail(@RequestParam @Parameter(description = "规则id") Long id) {
    RuleModelVO ruleModel =
        ruleService.queryVoByIdAndCreator(
            id, loginIoTUnionUser(SecurityUtils.getUnionId()).getUnionId());
    return AjaxResult.success(ruleModel);
  }

  @Operation(summary = "新增规则")
  @PostMapping("/rule")
  @Log(title = "新增规则", businessType = BusinessType.INSERT)
  public AjaxResult<Long> addRule(@RequestBody RuleBo ruleModelBo) {
    ruleModelBo.setCreatorId(loginIoTUnionUser(SecurityUtils.getUnionId()).getUnionId());
    Long ruleId = ruleService.addRule(ruleModelBo);
    return AjaxResult.success(ruleId);
  }

  @Operation(summary = "删除规则")
  @DeleteMapping("/rule/{id}")
  @Log(title = "删除规则", businessType = BusinessType.DELETE)
  public AjaxResult<Void> deleteRule(@PathVariable @Parameter(description = "规则id") Long id) {

    ruleService.deletedRule(id, loginIoTUnionUser(SecurityUtils.getUnionId()).getUnionId());
    return AjaxResult.success();
  }

  @Operation(summary = "更新规则")
  @PutMapping("/rule")
  @Log(title = "更新规则", businessType = BusinessType.UPDATE)
  public AjaxResult<Void> updateRule(@RequestBody RuleBo ruleModelBo) {
    ruleModelBo.setCreatorId(loginIoTUnionUser(SecurityUtils.getUnionId()).getUnionId());
    ruleService.updateRule(ruleModelBo);
    return AjaxResult.success();
  }

  @Operation(summary = "更新规则调用目标")
  @PutMapping("/rule/targets")
  @Log(title = "更新规则调用目标", businessType = BusinessType.UPDATE)
  public AjaxResult<Void> updateRuleTargets(@RequestBody RuleBo ruleModelBo) {
    ruleModelBo.setCreatorId(loginIoTUnionUser(SecurityUtils.getUnionId()).getUnionId());
    ruleService.updateRuleTargets(ruleModelBo);
    return AjaxResult.success();
  }

  @Operation(summary = "更新规则状态")
  @PostMapping("rule/status")
  @Log(title = "更新规则状态", businessType = BusinessType.UPDATE)
  public AjaxResult<Void> changeStatus(@RequestBody RuleModelBO ruleModelBo) {
    ruleModelBo.setCreatorId(loginIoTUnionUser(SecurityUtils.getUnionId()).getUnionId());
    ruleService.changeStatus(ruleModelBo);
    return AjaxResult.success();
  }

  @Operation(summary = "sql测试")
  @PostMapping("/rule/sql/test")
  @Log(title = "sql测试", businessType = BusinessType.OTHER)
  public AjaxResult<JSONObject> sqlTest(@RequestBody RuleBo sqlBo) {
    return AjaxResult.success(ruleService.testExecuteRule(sqlBo));
  }

  @Operation(summary = "调用目标测试")
  @PostMapping("/rule/target/test")
  @Log(title = "调用目标测试", businessType = BusinessType.OTHER)
  public AjaxResult<Object> targetTest(@RequestBody RuleTargetTestBO testBo) {
    Object result = ruleService.testRuleTarget(testBo);
    return AjaxResult.success(result);
  }
}
