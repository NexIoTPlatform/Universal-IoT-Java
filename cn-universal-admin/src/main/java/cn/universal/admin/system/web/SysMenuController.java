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

import cn.hutool.core.lang.Validator;
import cn.universal.common.annotation.Log;
import cn.universal.common.enums.BusinessType;
import cn.universal.admin.system.service.ISysMenuService;
import cn.universal.common.constant.IoTUserConstants;
import cn.universal.common.exception.IoTException;
import cn.universal.persistence.entity.admin.SysMenu;
import cn.universal.persistence.query.AjaxResult;
import cn.universal.security.BaseController;
import cn.universal.security.utils.SecurityUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 菜单信息 @Author ruoyi */
@RestController
@RequestMapping("admin/system/menu")
public class SysMenuController extends BaseController {

  @Autowired private ISysMenuService menuService;

  /** 获取菜单列表 */
  @GetMapping("/list")
  public AjaxResult list(SysMenu menu) {
    List<SysMenu> menus = menuService.selectMenuList(menu, SecurityUtils.getUnionId());
    return AjaxResult.success(menus);
  }

  /** 根据菜单编号获取详细信息 */
  @GetMapping(value = "/{menuId}")
  public AjaxResult getInfo(@PathVariable Long menuId) {
    if (!isAdmin()) {
      throw new IoTException("你无权操作");
    }
    return AjaxResult.success(menuService.selectMenuById(menuId));
  }

  /** 获取菜单下拉树列表 */
  @GetMapping("/treeselect")
  public AjaxResult treeselect(SysMenu menu) {
    List<SysMenu> menus = menuService.selectMenuList(menu, SecurityUtils.getUnionId());
    return AjaxResult.success(menuService.buildMenuTreeSelect(menus));
  }

  /** 加载对应角色菜单列表树 */
  @GetMapping(value = "/roleMenuTreeselect/{roleId}")
  public AjaxResult roleMenuTreeselect(@PathVariable("roleId") Long roleId) {
    List<SysMenu> menus = menuService.selectMenuList(SecurityUtils.getUnionId());
    Map<String, Object> ajax = new HashMap<>();
    ajax.put("checkedKeys", menuService.selectMenuListByRoleId(roleId));
    ajax.put("menus", menuService.buildMenuTreeSelect(menus));
    return AjaxResult.success(ajax);
  }

  /** 新增菜单 */

  //  @Log(title = "菜单管理", businessType = BusinessType.INSERT)
  @PostMapping
  @Log(title = "新增菜单", businessType = BusinessType.INSERT)
  public AjaxResult add(@Validated @RequestBody SysMenu menu) {
    if (!isAdmin()) {
      throw new IoTException("你无权操作");
    }
    if (IoTUserConstants.NOT_UNIQUE.equals(menuService.checkMenuNameUnique(menu))) {
      return error("新增菜单'" + menu.getMenuName() + "'失败，菜单名称已存在");
    } else if (IoTUserConstants.YES_FRAME.equals(menu.getIsFrame())
        && !Validator.isUrl(menu.getPath())) {
      return error("新增菜单'" + menu.getMenuName() + "'失败，地址必须以http(s)://开头");
    }
    menu.setCreateBy(SecurityUtils.getUnionId());
    return toAjax(menuService.insertMenu(menu));
  }

  /** 修改菜单 */

  //  @Log(title = "菜单管理", businessType = BusinessType.UPDATE)
  @PutMapping
  @Log(title = "修改菜单", businessType = BusinessType.UPDATE)
  public AjaxResult edit(@Validated @RequestBody SysMenu menu) {
    if (IoTUserConstants.NOT_UNIQUE.equals(menuService.checkMenuNameUnique(menu))) {
      return error("修改菜单'" + menu.getMenuName() + "'失败，菜单名称已存在");
    } else if (IoTUserConstants.YES_FRAME.equals(menu.getIsFrame())
        && !Validator.isUrl(menu.getPath())) {
      return error("修改菜单'" + menu.getMenuName() + "'失败，地址必须以http(s)://开头");
    } else if (menu.getMenuId().equals(menu.getParentId())) {
      return error("修改菜单'" + menu.getMenuName() + "'失败，上级菜单不能选择自己");
    }
    menu.setUpdateBy(SecurityUtils.getUnionId());
    return toAjax(menuService.updateMenu(menu));
  }

  /** 删除菜单 */
  @PreAuthorize("@ss.hasPermi('system:menu:remove')")
  //  @Log(title = "菜单管理", businessType = BusinessType.DELETE)
  @DeleteMapping("/{menuId}")
  @Log(title = "删除菜单", businessType = BusinessType.DELETE)
  public AjaxResult remove(@PathVariable("menuId") Long menuId) {
    if (menuService.hasChildByMenuId(menuId)) {
      return error("存在子菜单,不允许删除");
    }
    if (menuService.checkMenuExistRole(menuId)) {
      return error("菜单已分配,不允许删除");
    }
    return toAjax(menuService.deleteMenuById(menuId));
  }
}
