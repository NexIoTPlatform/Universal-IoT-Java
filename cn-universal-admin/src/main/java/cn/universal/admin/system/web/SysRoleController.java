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

import cn.hutool.core.util.ObjectUtil;
import cn.universal.admin.common.annotation.Log;
import cn.universal.admin.common.enums.BusinessType;
import cn.universal.admin.common.utils.ExcelUtil;
import cn.universal.admin.common.utils.SecurityUtils;
import cn.universal.admin.system.service.IIotUserService;
import cn.universal.admin.system.service.ISysRoleService;
import cn.universal.common.constant.IoTUserConstants;
import cn.universal.common.exception.IoTException;
import cn.universal.persistence.entity.IoTUser;
import cn.universal.persistence.entity.admin.SysRole;
import cn.universal.persistence.entity.admin.SysUserRole;
import cn.universal.persistence.entity.bo.IoTUserBO;
import cn.universal.persistence.page.TableDataInfo;
import cn.universal.persistence.query.AjaxResult;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 角色信息 @Author ruoyi */
@RestController
@RequestMapping("admin/system/role")
public class SysRoleController extends BaseController {

  @Autowired private ISysRoleService roleService;

  @Autowired private IIotUserService userService;

  @GetMapping("/list")
  public TableDataInfo list(SysRole role) {
    if (!userService.selectUserByUnionId(SecurityUtils.getUnionId()).isAdmin()) {
      role.setCreateBy(SecurityUtils.getUnionId());
    }
    startPage();
    return getDataTable(roleService.selectPageRoleList(role));
  }

  @PostMapping("/export")
  public void export(HttpServletResponse response, SysRole role) {
    if (!userService.selectUserByUnionId(SecurityUtils.getUnionId()).isAdmin()) {
      role.setCreateBy(SecurityUtils.getUnionId());
    }
    List<SysRole> list = roleService.selectRoleList(role);
    ExcelUtil<SysRole> util = new ExcelUtil<SysRole>(SysRole.class);
    util.exportExcel(response, list, "角色数据");
  }

  /** 根据角色编号获取详细信息 */
  @GetMapping(value = "/{roleId}")
  public AjaxResult getInfo(@PathVariable Long roleId) {
    SysRole sysRole = roleService.selectRoleById(roleId);
    IoTUser iotUser = loginIoTUnionUser(SecurityUtils.getUnionId());
    if (!iotUser.isAdmin()
        && ObjectUtil.isNotEmpty(sysRole)
        && !sysRole.getCreateBy().equals(iotUser.getUnionId())) {
      throw new IoTException("你无权操作");
    }
    return AjaxResult.success(sysRole);
  }

  /** 新增角色 */
  @PostMapping
  @Log(title = "新增角色", businessType = BusinessType.INSERT)
  public AjaxResult add(@Validated @RequestBody SysRole role) {
    String unionId = SecurityUtils.getUnionId();
    IoTUser parentUser = userService.selectUserByUnionId(unionId);
    if (!parentUser.isAdmin()) {
      throw new IoTException("你无权操作");
    }
    if (IoTUserConstants.NOT_UNIQUE.equals(roleService.checkRoleNameUnique(role))) {
      return error("新增角色'" + role.getRoleName() + "'失败，角色名称已存在");
    } else if (IoTUserConstants.NOT_UNIQUE.equals(roleService.checkRoleKeyUnique(role))) {
      return error("新增角色'" + role.getRoleName() + "'失败，角色权限已存在");
    }
    role.setCreateBy(SecurityUtils.getUnionId());
    role.setCreateTime(new Date());
    return toAjax(roleService.insertRole(role));
  }

  /** 修改保存角色 */
  @PutMapping
  @Log(title = "修改保存角色", businessType = BusinessType.UPDATE)
  public AjaxResult edit(@Validated @RequestBody SysRole role) {
    SysRole sysRole = roleService.selectRoleById(role.getRoleId());
    IoTUser iotUser = loginIoTUnionUser(SecurityUtils.getUnionId());
    if (!iotUser.isAdmin()
        && ObjectUtil.isNotEmpty(sysRole)
        && !sysRole.getCreateBy().equals(iotUser.getUnionId())) {
      throw new IoTException("你无权操作");
    }
    roleService.checkRoleAllowed(role);
    if (IoTUserConstants.NOT_UNIQUE.equals(roleService.checkRoleNameUnique(role))) {
      return error("修改角色'" + role.getRoleName() + "'失败，角色名称已存在");
    } else if (IoTUserConstants.NOT_UNIQUE.equals(roleService.checkRoleKeyUnique(role))) {
      return error("修改角色'" + role.getRoleName() + "'失败，角色权限已存在");
    }
    role.setUpdateBy(SecurityUtils.getUnionId());

    if (roleService.updateRole(role) > 0) {
      // 更新缓存用户权限
      //      LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
      //      if (Validator.isNotNull(loginUser.getUser()) && !loginUser.getUser().isAdmin()) {
      //        loginUser.setPermissions(permissionService.getMenuPermission(loginUser.getUser()));
      //
      // loginUser.setUser(userService.selectUserByUserName(loginUser.getUser().getUserName()));
      //        tokenService.setLoginUser(loginUser);
      //      }
      return success();
    }
    return error("修改角色'" + role.getRoleName() + "'失败，请联系管理员");
  }

  /** 修改保存数据权限 */
  //  @PreAuthorize("@ss.hasPermi('system:role:edit')")
  ////  @Log(title = "角色管理", businessType = BusinessType.UPDATE)
  //  @PutMapping("/dataScope")
  //  public AjaxResult dataScope(@RequestBody SysRole role) {
  //    roleService.checkRoleAllowed(role);
  //    return AjaxResult.toAjax(roleService.authDataScope(role));
  //  }

  /** 状态修改 */
  @PutMapping("/changeStatus")
  public AjaxResult changeStatus(@RequestBody SysRole role) {
    SysRole sysRole = roleService.selectRoleById(role.getRoleId());
    IoTUser iotUser = loginIoTUnionUser(SecurityUtils.getUnionId());
    if (!iotUser.isAdmin()
        && ObjectUtil.isNotEmpty(sysRole)
        && !sysRole.getCreateBy().equals(iotUser.getUnionId())) {
      throw new IoTException("你无权操作");
    }
    roleService.checkRoleAllowed(role);
    role.setUpdateBy(SecurityUtils.getUnionId());
    return toAjax(roleService.updateRoleStatus(role));
  }

  /** 删除角色 */
  @DeleteMapping("/{roleIds}")
  @Log(title = "删除角色", businessType = BusinessType.DELETE)
  public AjaxResult remove(@PathVariable Long[] roleIds) {
    IoTUser iotUser = loginIoTUnionUser(SecurityUtils.getUnionId());
    Arrays.stream(roleIds)
        .forEach(
            id -> {
              SysRole sysRole = roleService.selectRoleById(id);
              if (!iotUser.isAdmin()
                  && ObjectUtil.isNotEmpty(sysRole)
                  && !sysRole.getCreateBy().equals(iotUser.getUnionId())) {
                throw new IoTException("你无权操作");
              }
            });

    return toAjax(roleService.deleteRoleByIds(roleIds));
  }

  /** 获取角色选择框列表 */

  //  @GetMapping("/optionselect")
  //  public AjaxResult optionselect() {
  //    return AjaxResult.success(roleService.selectRoleAll());
  //  }

  /** 查询已分配用户角色列表 */
  @GetMapping("/authUser/allocatedList")
  public TableDataInfo allocatedList(IoTUserBO iotUserBo) {
    SysRole sysRole = roleService.selectRoleById(iotUserBo.getRoleId());
    IoTUser iotUser = loginIoTUnionUser(SecurityUtils.getUnionId());
    if (!iotUser.isAdmin()
        && ObjectUtil.isNotEmpty(sysRole)
        && !sysRole.getCreateBy().equals(iotUser.getUnionId())) {
      throw new IoTException("你无权操作");
    }
    startPage();
    return getDataTable(userService.selectAllocatedList(iotUserBo));
  }

  /** 查询未分配用户角色列表 */
  @GetMapping("/authUser/unallocatedList")
  public TableDataInfo unallocatedList(IoTUserBO user) {
    SysRole sysRole = roleService.selectRoleById(user.getRoleId());
    IoTUser iotUser = loginIoTUnionUser(SecurityUtils.getUnionId());
    if (!iotUser.isAdmin()
        && ObjectUtil.isNotEmpty(sysRole)
        && !sysRole.getCreateBy().equals(iotUser.getUnionId())) {
      throw new IoTException("你无权操作");
    }
    startPage();
    user.setCreateBy(SecurityUtils.getUnionId());
    return getDataTable(userService.selectUnallocatedList(user));
  }

  /** 取消授权用户 */
  @PutMapping("/authUser/cancel")
  public AjaxResult cancelAuthUser(@RequestBody SysUserRole userRole) {
    SysRole sysRole = roleService.selectRoleById(userRole.getRoleId());
    IoTUser iotUser = loginIoTUnionUser(SecurityUtils.getUnionId());
    if (!iotUser.isAdmin()
        && ObjectUtil.isNotEmpty(sysRole)
        && !sysRole.getCreateBy().equals(iotUser.getUnionId())) {
      throw new IoTException("你无权操作");
    }
    return toAjax(roleService.deleteAuthUser(userRole));
  }

  /** 批量取消授权用户 */
  @PutMapping("/authUser/cancelAll")
  public AjaxResult cancelAuthUserAll(Long roleId, String[] unionIds) {
    SysRole sysRole = roleService.selectRoleById(roleId);
    IoTUser iotUser = loginIoTUnionUser(SecurityUtils.getUnionId());
    if (!iotUser.isAdmin()
        && ObjectUtil.isNotEmpty(sysRole)
        && !sysRole.getCreateBy().equals(iotUser.getUnionId())) {
      throw new IoTException("你无权操作");
    }
    return toAjax(roleService.deleteAuthUsers(roleId, unionIds));
  }

  /** 批量选择用户授权 */
  @PutMapping("/authUser/selectAll")
  public AjaxResult selectAuthUserAll(Long roleId, String[] unionIds) {
    SysRole sysRole = roleService.selectRoleById(roleId);
    IoTUser iotUser = loginIoTUnionUser(SecurityUtils.getUnionId());
    if (!iotUser.isAdmin()
        && ObjectUtil.isNotEmpty(sysRole)
        && !sysRole.getCreateBy().equals(iotUser.getUnionId())) {
      throw new IoTException("你无权操作");
    }
    return toAjax(roleService.insertAuthUsers(roleId, unionIds));
  }
}
