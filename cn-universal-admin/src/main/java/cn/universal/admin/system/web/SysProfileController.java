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

package cn.universal.admin.system.web;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.PhoneUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.universal.security.service.IoTUserService;
import cn.universal.security.utils.SecurityUtils;
import cn.universal.admin.system.service.ISysRoleService;
import cn.universal.common.constant.IoTConstant;
import cn.universal.common.constant.IoTUserConstants;
import cn.universal.common.exception.IoTException;
import cn.universal.persistence.entity.IoTUser;
import cn.universal.persistence.entity.bo.IoTUserBO;
import cn.universal.persistence.entity.bo.PasswordBO;
import cn.universal.persistence.query.AjaxResult;
import cn.universal.security.BaseController;
import jakarta.annotation.Resource;
import java.util.Date;
import java.util.Objects;
import java.util.regex.Matcher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 个人信息 */
@RestController
@RequestMapping("/admin/system/user/profile")
public class SysProfileController extends BaseController {

  @Resource private IoTUserService ioTUserService;
  @Resource private ISysRoleService iSysRoleService;

  /** 查询个人信息 */
  @GetMapping
  public AjaxResult<JSONObject> profile() {
    JSONObject jsonObject = new JSONObject();
    String unionId = SecurityUtils.getUnionId();

    IoTUser iotUser = ioTUserService.selectUserByUnionId(unionId);
    // 手机号脱敏
    if (StrUtil.isNotEmpty(iotUser.getMobile())) {
      iotUser.setMobile(PhoneUtil.hideBetween(iotUser.getMobile()).toString());
    }
    jsonObject.set("user", iotUser);
    jsonObject.set("roleGroup", iSysRoleService.selectRolesByUnionId(unionId));

    return AjaxResult.success(jsonObject);
  }

  /** 个人信息 */
  @PutMapping
  public AjaxResult<Void> profile(@RequestBody IoTUserBO iotUserBo) {
    String unionId = SecurityUtils.getUnionId();
    if (Validator.isEmpty(iotUserBo.getMobile())) {
      return error("修改个人信息失败，手机号码不能为空");
    }
    IoTUser user = BeanUtil.toBean(iotUserBo, IoTUser.class);
    if (IoTUserConstants.NOT_UNIQUE.equals(ioTUserService.checkPhoneUnique(user))) {
      return error("修改个人信息失败，手机号码已存在");
    }
    IoTUser iotUser = ioTUserService.selectUserByUnionId(unionId);
    iotUser.setAlias(iotUserBo.getAlias());
    iotUser.setEmail(iotUserBo.getEmail());
    iotUser.setMobile(iotUserBo.getMobile());
    iotUser.setUpdateBy(unionId);
    iotUser.setUpdateDate(new Date());
    if (JSONUtil.isTypeJSON(iotUserBo.getCfg())) {
      JSONObject origin = JSONUtil.parseObj(iotUser.getCfg());
      JSONObject latest = JSONUtil.parseObj(iotUserBo.getCfg());
      origin.set(IoTConstant.EXCLUSIVE_FIRST_LOGIN, latest.get(IoTConstant.EXCLUSIVE_FIRST_LOGIN));
      iotUser.setCfg(JSONUtil.toJsonStr(origin));
    }
    ioTUserService.updateUserById(iotUser);
    return AjaxResult.success();
  }

  /** 重置密码（修改密码） */
  @PutMapping("/updatePwd")
  public AjaxResult<Void> updatePwd(@RequestBody PasswordBO bo) {
    String unionId = SecurityUtils.getUnionId();
    IoTUser iotUser = ioTUserService.selectUserByUnionId(unionId);
    Matcher matcher = IoTConstant.pattern.matcher(bo.getNewPassword());
    if (!matcher.matches()) {
      throw new IoTException("密码中必须包含字母、数字、特殊字符，至少8个字符，最多30个字符");
    }
    if (!SecurityUtils.matchesPassword(bo.getOldPassword(), iotUser.getPassword())) {
      throw new IoTException("旧密码错误");
    }
    iotUser.setPassword(SecurityUtils.encryptPassword(bo.getNewPassword()));
    iotUser.setUpdateBy(unionId);
    iotUser.setUpdateDate(new Date());
    ioTUserService.updateUserById(iotUser);
    return AjaxResult.success();
  }

  /** 修改手机号 */
  @RequestMapping("/updateMobile/{mobile}")
  public AjaxResult<Void> updatePwd(@PathVariable("mobile") String mobile) {
    String unionId = SecurityUtils.getUnionId();
    IoTUser iotUser = ioTUserService.selectUserByUnionId(unionId);
    IoTUser user = ioTUserService.selectUserByMobile(mobile);
    if (Objects.nonNull(user)) {
      throw new IoTException("手机号码已存在,请勿重复添加");
    } else {
      iotUser.setMobile(mobile);
      ioTUserService.updateUserById(iotUser);
      return AjaxResult.success();
    }
  }
}
