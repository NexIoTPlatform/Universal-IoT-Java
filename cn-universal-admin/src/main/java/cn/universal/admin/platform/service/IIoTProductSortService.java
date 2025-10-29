/*
 *
 * Copyright (c) 2025, NexIoT. All Rights Reserved.
 *
 * @Description: 本文件由 gitee.com/NexIoT 开发并拥有版权，未经授权严禁擅自商用、复制或传播。
 * @Author: gitee.com/NexIoT
 * @Email: wo8335224@gmail.com
 * @Wechat: outlookFil
 *
 *
 */

package cn.universal.admin.platform.service;

import cn.universal.persistence.entity.IoTProductSort;
import cn.universal.persistence.entity.bo.RuleModelBO.IoTProductSortBO;
import java.util.List;

/**
 * 产品分类Service接口 @Author gitee.com/NexIoT
 *
 * @since 2025-12-29
 */
public interface IIoTProductSortService {

  /**
   * 查询产品分类
   *
   * @param id 产品分类主键
   * @return 产品分类
   */
  public IoTProductSort selectDevProductSortById(String id);

  /**
   * 查询产品分类列表
   *
   * @param ioTProductSort 产品分类
   * @return 产品分类集合
   */
  public List<IoTProductSort> selectDevProductSortList(IoTProductSort ioTProductSort);

  /**
   * 新增产品分类
   *
   * @param ioTProductSort 产品分类
   * @return 结果
   */
  public int insertDevProductSort(IoTProductSort ioTProductSort);

  /**
   * 修改产品分类
   *
   * @param ioTProductSort 产品分类
   * @return 结果
   */
  public int updateDevProductSort(IoTProductSort ioTProductSort);

  /**
   * 批量删除产品分类
   *
   * @param ids 需要删除的产品分类主键集合
   * @return 结果
   */
  public int deleteDevProductSortByIds(String[] ids);

  /**
   * 删除产品分类信息
   *
   * @param id 产品分类主键
   * @return 结果
   */
  public int deleteDevProductSortById(String id);

  /**
   * 构建前端所需要树结构
   *
   * @param ioTProductSortList 菜单列表
   * @return 树结构列表
   */
  public List<IoTProductSort> buildProductSortTree(List<IoTProductSort> ioTProductSortList);

  // ==================== 新增的BO方法 ====================

  /**
   * 新增产品分类（使用BO）
   *
   * @param bo 产品分类业务对象
   * @return 结果
   */
  public int insertDevProductSort(IoTProductSortBO bo);

  /**
   * 修改产品分类（使用BO）
   *
   * @param bo 产品分类业务对象
   * @return 结果
   */
  public int updateDevProductSort(IoTProductSortBO bo);

  /**
   * 获取产品分类树结构
   *
   * @return 树结构列表
   */
  public List<IoTProductSort> getProductSortTree();

  /**
   * 构建前端所需要下拉树结构
   *
   * @param devProductSortList 菜单列表
   * @return 下拉树结构列表
   */
  // public List<TreeSelect> buildProductSortTreeSelect(List<IoTProductSort> devProductSortList);

}
