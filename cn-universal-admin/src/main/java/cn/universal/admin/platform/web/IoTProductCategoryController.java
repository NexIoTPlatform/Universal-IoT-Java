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

package cn.universal.admin.platform.web;

import cn.hutool.core.util.StrUtil;
import cn.universal.common.annotation.Log;
import cn.universal.common.enums.BusinessType;
import cn.universal.admin.platform.service.IIoTProductSortService;
import cn.universal.security.BaseController;
import cn.universal.common.exception.IoTException;
import cn.universal.persistence.entity.IoTProduct;
import cn.universal.persistence.entity.IoTProductSort;
import cn.universal.persistence.entity.bo.RuleModelBO.IoTProductSortBO;
import cn.universal.persistence.mapper.IoTProductMapper;
import cn.universal.persistence.query.AjaxResult;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 产品分类Controller @Author gitee.com/NexIoT
 *
 * @since 2025-12-29
 */
@RestController
@RequestMapping("/admin/sort")
public class IoTProductCategoryController extends BaseController {

  @Autowired private IIoTProductSortService devProductSortService;
  @Resource private IoTProductMapper ioTProductMapper;

  /** 查询产品分类列表 */
  @GetMapping("/list")
  public AjaxResult list(IoTProductSort ioTProductSort) {
    List<IoTProductSort> list = devProductSortService.selectDevProductSortList(ioTProductSort);
    return AjaxResult.success(list);
  }

  /** 获取产品分类树结构 */
  @GetMapping("/tree")
  public AjaxResult getTree() {
    List<IoTProductSort> tree = devProductSortService.getProductSortTree();
    return AjaxResult.success(tree);
  }

  /** 获取产品分类详细信息 */
  @GetMapping(value = "/{id}")
  public AjaxResult getInfo(@PathVariable("id") String id) {
    return AjaxResult.success(devProductSortService.selectDevProductSortById(id));
  }

  /** 获取产品分类详细信息 */
  @PostMapping(value = "/info")
  public AjaxResult getInfo(@RequestBody IoTProductSort ioTProductSort) {
    if (StrUtil.isBlank(ioTProductSort.getId())) {
      AjaxResult.success();
    }
    return AjaxResult.success(
        devProductSortService.selectDevProductSortById(ioTProductSort.getId()));
  }

  /** 新增产品分类（使用BO） */
  @PostMapping("/add")
  @Log(title = "新增产品分类", businessType = BusinessType.INSERT)
  public AjaxResult addWithBo(@Valid @RequestBody IoTProductSortBO bo) {
    try {
      int result = devProductSortService.insertDevProductSort(bo);
      return toAjax(result);
    } catch (IllegalArgumentException e) {
      return AjaxResult.error(e.getMessage());
    } catch (Exception e) {
      return AjaxResult.error("新增产品分类失败：" + e.getMessage());
    }
  }

  /** 修改产品分类（使用BO） */
  @PutMapping("/edit")
  @Log(title = "修改产品分类", businessType = BusinessType.UPDATE)
  public AjaxResult editWithBo(@Valid @RequestBody IoTProductSortBO bo) {
    try {
      int result = devProductSortService.updateDevProductSort(bo);
      return toAjax(result);
    } catch (IllegalArgumentException e) {
      return AjaxResult.error(e.getMessage());
    } catch (Exception e) {
      return AjaxResult.error("修改产品分类失败：" + e.getMessage());
    }
  }

  /** 新增产品分类（原有方法，保持兼容性） */
  @PostMapping
  @Log(title = "新增产品分类", businessType = BusinessType.INSERT)
  public AjaxResult add(@RequestBody IoTProductSort ioTProductSort) {
    return toAjax(devProductSortService.insertDevProductSort(ioTProductSort));
  }

  /** 修改产品分类（原有方法，保持兼容性） */
  @PutMapping
  @Log(title = "修改产品分类", businessType = BusinessType.UPDATE)
  public AjaxResult edit(@RequestBody IoTProductSort ioTProductSort) {
    return toAjax(devProductSortService.updateDevProductSort(ioTProductSort));
  }

  /** 删除产品分类 */
  @DeleteMapping("/{ids}")
  @Log(title = "删除产品分类", businessType = BusinessType.DELETE)
  public AjaxResult remove(@PathVariable String[] ids) {
    for (String id : ids) {
      IoTProductSort ioTProductSort = devProductSortService.selectDevProductSortById(id);
      if (ioTProductSort.getHasChild() == 1) {
        throw new IoTException("该分类下有子分类");
      }
      IoTProduct ioTProduct = IoTProduct.builder().classifiedId(id).build();
      List<IoTProduct> ioTProductList = ioTProductMapper.select(ioTProduct);
      if (!ioTProductList.isEmpty()) {
        throw new IoTException("该分类下有产品");
      }
    }
    return toAjax(devProductSortService.deleteDevProductSortByIds(ids));
  }

  /** 获取菜单下拉树列表 */
  @GetMapping("/treeselect")
  public AjaxResult treeselect(IoTProductSort productSort) {
    List<IoTProductSort> productSortList =
        devProductSortService.selectDevProductSortList(productSort);
    return AjaxResult.success(devProductSortService.buildProductSortTree(productSortList));
  }
}
