/*
 *
 * Copyright (c) 2025, NexIoT. All Rights Reserved.
 *
 * @Description: 本文件由 gitee.com/NexIoT 开发并拥有版权，未经授权严禁擅自商用、复制或传播。
 *
 * @Author: gitee.com/NexIoT
 *
 * @Email: wo8335224@gmail.com
 *
 * @Wechat: outlookFil
 *
 *
 */

package cn.universal.admin.platform.web;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.universal.admin.common.utils.ExcelUtil;
import cn.universal.admin.system.service.IIoTUserApplicationService;
import cn.universal.admin.system.service.ISysRoleService;
import cn.universal.common.annotation.Log;
import cn.universal.common.constant.IoTConstant;
import cn.universal.common.constant.IoTUserConstants;
import cn.universal.common.enums.BusinessType;
import cn.universal.common.exception.IoTException;
import cn.universal.common.utils.StringUtils;
import cn.universal.persistence.entity.IoTUser;
import cn.universal.persistence.entity.admin.SysRole;
import cn.universal.persistence.entity.bo.IoTUserBO;
import cn.universal.persistence.entity.vo.IoTUserVO;
import cn.universal.persistence.page.TableDataInfo;
import cn.universal.persistence.query.AjaxResult;
import cn.universal.security.BaseController;
import cn.universal.security.service.IoTUserService;
import cn.universal.security.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * IoT用户管理控制器
 *
 * @author gitee.com/NexIoT
 * @version 1.0
 * @since 2025/8/12 19:11
 */
@RestController
@RequestMapping("admin/system/user")
@Slf4j
public class IoTUserController extends BaseController {

  @Resource private IoTUserService ioTUserService;
  @Resource private ISysRoleService iSysRoleService;

  @Resource private IIoTUserApplicationService iotUserApplicationService;

  /** 分页查询用户列表 */
  @GetMapping("/list")
  public TableDataInfo<IoTUser> pageList(IoTUser user) {
    log.info("当前用户={}", SecurityUtils.getUnionId());
    if (ioTUserService.selectUserByUnionId(SecurityUtils.getUnionId()).isAdmin()) {
      user.setCreateBy(null);
    } else {
      user.setCreateBy(SecurityUtils.getUnionId());
    }
    startPage();
    List<IoTUser> userList = ioTUserService.selectUserList(user);
    return getDataTable(userList);
  }

  @Operation(summary = "导出用户应用信息列表")
  @PostMapping("/export")
  @Log(title = "导出用户应用信息列表", businessType = BusinessType.EXPORT)
  public void export(HttpServletResponse response, IoTUser iotUser) {
    log.info("导出用户,操作人={},参数={}", SecurityUtils.getUnionId(), JSONUtil.toJsonStr(iotUser));
    if (ioTUserService.selectUserByUnionId(SecurityUtils.getUnionId()).isAdmin()) {
      iotUser.setCreateBy(null);
    } else {
      iotUser.setCreateBy(SecurityUtils.getUnionId());
    }
    List<IoTUser> list = ioTUserService.selectUserList(iotUser);
    ExcelUtil<IoTUser> util = new ExcelUtil<IoTUser>(IoTUser.class);
    util.exportExcel(response, list, "用户信息数据");
  }

  /** 根据用户编号获取详细信息 */
  @GetMapping(value = {"/", "/{id}"})
  public AjaxResult<JSONObject> getInfo(@PathVariable(value = "id", required = false) Long id) {

    JSONObject jsonObject = new JSONObject();
    List<SysRole> roles = iSysRoleService.selectRoleAll();
    IoTUser currentUser = ioTUserService.selectUserByUnionId(SecurityUtils.getUnionId());
    jsonObject.set(
        "roles",
        currentUser.isAdmin()
            ? roles
            : roles.stream()
                .filter(r -> SecurityUtils.getUnionId().equals(r.getCreateBy()))
                .collect(Collectors.toList()));
    if (StringUtils.isNotNull(id)) {
      IoTUser iotUser = ioTUserService.selectUserById(id);
      if (!SecurityUtils.getUnionId().equalsIgnoreCase(iotUser.getParentUnionId())
          && !currentUser.isAdmin()) {
        throw new IoTException("你无权查询用户");
      }
      IoTUserVO iotUserVO = BeanUtil.toBean(iotUser, IoTUserVO.class);
      iotUserVO.setRoleIds(iSysRoleService.selectRoleListByUnionId(iotUser.getUnionId()));
      jsonObject.set("data", iotUserVO);
    }

    return AjaxResult.success(jsonObject);
  }

  /** 新增用户 */
  @Operation(summary = "新增用户")
  @PostMapping
  @Log(title = "新增用户", businessType = BusinessType.INSERT)
  public AjaxResult<Void> add(@Validated @RequestBody IoTUserBO userbo) {
    log.info("新增用户,操作人={},参数={}", SecurityUtils.getUnionId(), JSONUtil.toJsonStr(userbo));
    String unionId = SecurityUtils.getUnionId();
    IoTUser parentUser = ioTUserService.selectUserByUnionId(unionId);
    if (!parentUser.isAdmin()) {
      throw new IoTException("你无权操作");
    }
    IoTUser user = BeanUtil.toBean(userbo, IoTUser.class);
    if (Objects.isNull(user.getMobile())) {
      return AjaxResult.error("新增用户'" + user.getUsername() + "'失败，手机号码不得为空");
    }
    if (IoTUserConstants.NOT_UNIQUE.equals(ioTUserService.checkUserNameUnique(user))) {
      return AjaxResult.error("新增用户'" + user.getUsername() + "'失败，用户名已存在");
    } else if (Validator.isNotEmpty(user.getMobile())
        && IoTUserConstants.NOT_UNIQUE.equals(ioTUserService.checkPhoneUnique(user))) {
      return AjaxResult.error("新增用户'" + user.getUsername() + "'失败，手机号码已存在");
    }
    if (StrUtil.isNotEmpty(user.getPassword())) {
      Matcher matcher = IoTConstant.pattern.matcher(user.getPassword());
      if (!matcher.matches()) {
        throw new IoTException("密码中必须包含字母、数字、特殊字符，至少8个字符，最多30个字符");
      }
    }

    userbo.setCreateBy(unionId);
    if (!parentUser.isAdmin()) {
      userbo.setParentUnionId(unionId);
    }
    userbo.setIdentity(parentUser.getIdentity() + 1);
    userbo.setCreateDate(new Date());
    userbo.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
    // userbo.setPassword(user.getPassword());
    userbo.setUnionId(userbo.getUsername());
    userbo.setDeleted(0);
    
    return toAjax(ioTUserService.insertUser(userbo));
  }

  /** 修改用户 */
  @Operation(summary = "修改用户")
  @PutMapping
  @Log(title = "修改用户", businessType = BusinessType.UPDATE)
  public AjaxResult<Void> edit(@Validated @RequestBody IoTUserBO userbo) {
    log.info("修改用户,操作人={},参数={}", SecurityUtils.getUnionId(), JSONUtil.toJsonStr(userbo));
    IoTUser user = BeanUtil.toBean(userbo, IoTUser.class);
    ioTUserService.checkUserAllowed(user);
    IoTUser updateUser = ioTUserService.selectUserById(userbo.getId());
    IoTUser iotUser = ioTUserService.selectUserByUnionId(SecurityUtils.getUnionId());
    // 非超管身份只能操作自己和子账户
    if (!SecurityUtils.getUnionId().equalsIgnoreCase(updateUser.getParentUnionId())
        && (iotUser == null || !user.getId().equals(iotUser.getId()))
        && !iotUser.isAdmin()) {
      return error("你无权操作");
    }
    if (Validator.isEmpty(user.getMobile())) {
      return error("修改用户'" + user.getUsername() + "'失败，手机号码不能为空");
    }
    if (IoTUserConstants.NOT_UNIQUE.equals(ioTUserService.checkPhoneUnique(user))) {
      return error("修改用户'" + user.getUsername() + "'失败，手机号码已存在");
    }

    if ("".equals(userbo.getPassword())) {
      userbo.setPassword(null);
    } else if (userbo.getPassword() != null) {
      // Pattern pattern = Pattern.compile(
      // "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[@#$%^&+=])[0-9a-zA-Z@#$%^&+=]{8,30}$");
      Matcher matcher = IoTConstant.pattern.matcher(user.getPassword());
      if (!matcher.matches()) {
        throw new IoTException("密码中必须包含字母、数字、特殊字符，至少8个字符，最多30个字符");
      }
      userbo.setPassword(SecurityUtils.encryptPassword(userbo.getPassword()));
    }
    userbo.setUpdateBy(SecurityUtils.getUnionId());
    userbo.setUpdateDate(new Date());
    int count = ioTUserService.updateUser(userbo);
    if (count > 0) {
      iotUserApplicationService.EnDisableIoTUser(
          userbo.getUnionId(), IoTConstant.NORMAL.toString().equals(iotUser.getStatus()));
    }
    return toAjax(count);
  }

  /** 删除用户 */
  @Operation(summary = "删除用户")
  @DeleteMapping("/{userIds}")
  @Log(title = "删除用户", businessType = BusinessType.DELETE)
  public AjaxResult<Void> remove(@PathVariable Long[] userIds) {
    if (userIds != null && userIds.length > 1) {
      return error("不允许批量删除");
    }
    boolean isAdmin = ioTUserService.selectUserByUnionId(SecurityUtils.getUnionId()).isAdmin();
    IoTUser deleteUser = ioTUserService.selectUserById(userIds[0]);
    if (!SecurityUtils.getUnionId().equalsIgnoreCase(deleteUser.getParentUnionId()) && !isAdmin) {
      return error("你无权删除用户");
    }
    return toAjax(ioTUserService.deleteUserByIds(userIds));
  }

  /** 根据用户编号获取授权角色 */
  @GetMapping("/authRole/{id}")
  public AjaxResult authRole(@PathVariable("id") Long id) {
    log.info("授权用户,操作人={},参数={}", SecurityUtils.getUnionId(), id);

    JSONObject object = new JSONObject();
    IoTUser currentUser = ioTUserService.selectUserByUnionId(SecurityUtils.getUnionId());
    IoTUser user = ioTUserService.selectUserById(id);
    if (!SecurityUtils.getUnionId().equalsIgnoreCase(user.getParentUnionId())
        && !currentUser.isAdmin()) {
      return error("你无权操作用户");
    }
    List<SysRole> roles = iSysRoleService.selectRolesByUnionId(user.getUnionId());
    object.set("user", user);
    object.set(
        "roles",
        user.isAdmin()
            ? roles
            : roles.stream().filter(r -> !r.isAdmin()).collect(Collectors.toList()));
    return AjaxResult.success(object);
  }

  /** 用户授权角色 */
  @PutMapping("/authRole")
  public AjaxResult<Void> insertAuthRole(String unionId, Long[] roleIds) {
    IoTUser currentUser = ioTUserService.selectUserByUnionId(SecurityUtils.getUnionId());
    IoTUser user = ioTUserService.selectUserByUnionId(unionId);
    if (!SecurityUtils.getUnionId().equalsIgnoreCase(user.getParentUnionId())
        && !currentUser.isAdmin()) {
      return error("你无权操作用户");
    }
    ioTUserService.insertUserAuth(unionId, roleIds);
    return success();
  }
}
