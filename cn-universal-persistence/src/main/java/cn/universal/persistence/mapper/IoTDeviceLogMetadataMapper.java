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

package cn.universal.persistence.mapper;

import cn.universal.persistence.common.BaseMapper;
import cn.universal.persistence.entity.IoTDeviceLogMetadata;
import cn.universal.persistence.entity.vo.IoTDeviceLogMetadataVO;
import cn.universal.persistence.query.LogQuery;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface IoTDeviceLogMetadataMapper extends BaseMapper<IoTDeviceLogMetadata> {

  /**
   * 删除属性最大的条数
   *
   * @param iotId iotId
   * @param maxStorage 最大存储条数
   * @return 影响条数
   */
  int deleteTopPropertiesRecord(
      @Param("iotId") String iotId,
      @Param("maxStorage") int maxStorage,
      @Param("property") String property);

  /**
   * 删除属性最大的条数
   *
   * @param iotId iotId
   * @param maxStorage 最大存储条数
   * @return 影响条数
   */
  int deleteTopEventRecord(
      @Param("iotId") String iotId,
      @Param("maxStorage") int maxStorage,
      @Param("event") String event);

  List<IoTDeviceLogMetadataVO> selectLogMetaList(LogQuery logQuery);

  /**
   * 获取设备事件的统计信息
   *
   * @param event 事件id
   * @param iotId 设备iotId
   */
  List<String> queryEventTotalByEventAndId(
      @Param("event") String event, @Param("iotId") String iotId);

  Long queryLogMetaIdByTime(@Param("tablesIndex") int tablesIndex, @Param("time") Long time);

  int deleteLogMetaById(@Param("tablesIndex") int tablesIndex, @Param("id") Long id);

  int deleteLogMetaByTask(
      @Param("tablesIndex") int tablesIndex,
      @Param("productKey") String productKey,
      @Param("id") Long id);

  Long queryLogMetaIdByProductKeyAndTime(
      @Param("tablesIndex") int tablesIndex,
      @Param("productKey") String productKey,
      @Param("time") Long time);
}
