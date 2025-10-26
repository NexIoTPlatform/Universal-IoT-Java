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

package cn.universal.persistence.mapper;

import cn.universal.persistence.entity.IoTProductSort;
import java.util.List;

/**
 * 产品分类Mapper接口 @Author gitee.com/NexIoT
 *
 * @since 2025-12-29
 */
public interface IoTProductSortMapper {

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
   * 删除产品分类
   *
   * @param id 产品分类主键
   * @return 结果
   */
  public int deleteDevProductSortById(String id);

  /**
   * 批量删除产品分类
   *
   * @param ids 需要删除的数据主键集合
   * @return 结果
   */
  public int deleteDevProductSortByIds(String[] ids);

  /**
   * 根据父id倒序查出列表
   *
   * @param parentId 需要删除的数据主键集合
   * @return 结果
   */
  public List<IoTProductSort> getListByParentId(String parentId);
}
