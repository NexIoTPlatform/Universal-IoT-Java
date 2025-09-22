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

package cn.universal.admin.generator.mapper;

import cn.universal.admin.generator.domain.GenTable;
import java.util.List;

/** 业务 数据层 @Author ruoyi */
public interface GenTableMapper {

  /**
   * 查询业务列表
   *
   * @param genTable 业务信息
   * @return 业务集合
   */
  List<GenTable> selectGenTableList(GenTable genTable);

  /**
   * 查询据库列表
   *
   * @param genTable 业务信息
   * @return 数据库表集合
   */
  List<GenTable> selectDbTableList(GenTable genTable);

  /**
   * 查询据库列表
   *
   * @param tableNames 表名称组
   * @return 数据库表集合
   */
  List<GenTable> selectDbTableListByNames(String[] tableNames);

  /**
   * 查询所有表信息
   *
   * @return 表信息集合
   */
  List<GenTable> selectGenTableAll();

  /**
   * 查询表ID业务信息
   *
   * @param id 业务ID
   * @return 业务信息
   */
  GenTable selectGenTableById(Long id);

  /**
   * 查询表名称业务信息
   *
   * @param tableName 表名称
   * @return 业务信息
   */
  GenTable selectGenTableByName(String tableName);

  /**
   * 新增业务
   *
   * @param genTable 业务信息
   * @return 结果
   */
  int insertGenTable(GenTable genTable);

  /**
   * 修改业务
   *
   * @param genTable 业务信息
   * @return 结果
   */
  int updateGenTable(GenTable genTable);

  /**
   * 批量删除业务
   *
   * @param ids 需要删除的数据ID
   * @return 结果
   */
  int deleteGenTableByIds(Long[] ids);
}
