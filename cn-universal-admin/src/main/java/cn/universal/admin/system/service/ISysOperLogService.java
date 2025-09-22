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

package cn.universal.admin.system.service;

import cn.universal.persistence.entity.SysOperLog;
import java.util.List;

/** 操作日志 服务层 @Author ruoyi */
public interface ISysOperLogService {

  List<SysOperLog> selectPageOperLogList(SysOperLog operLog);

  /**
   * 新增操作日志
   *
   * @param operLog 操作日志对象
   */
  public void insertOperlog(SysOperLog operLog);

  /**
   * 查询系统操作日志集合
   *
   * @param operLog 操作日志对象
   * @return 操作日志集合
   */
  public List<SysOperLog> selectOperLogList(SysOperLog operLog);

  /**
   * 批量删除系统操作日志
   *
   * @param operIds 需要删除的操作日志ID
   * @return 结果
   */
  public int deleteOperLogByIds(Long[] operIds);

  /**
   * 查询操作日志详细
   *
   * @param operId 操作ID
   * @return 操作日志对象
   */
  public SysOperLog selectOperLogById(Long operId);

  /** 清空操作日志 */
  public void cleanOperLog();
}
