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

package cn.universal.persistence.mapper;

import cn.universal.persistence.common.BaseMapper;
import cn.universal.persistence.consistent.DeviceMetaShardingRouter;
import cn.universal.persistence.consistent.TableShard;
import cn.universal.persistence.entity.IoTDeviceLogMetadata;
import cn.universal.persistence.entity.vo.IoTDeviceLogMetadataVO;
import cn.universal.persistence.query.LogQuery;
import java.util.List;
import org.apache.ibatis.annotations.Param;

@TableShard(
    tableNamePrefix = "iot_device_log_metadata",
    value = "iotId",
    fieldFlag = true,
    shardStrategy = DeviceMetaShardingRouter.class)
public interface IoTDeviceLogMetadataShardMapper extends BaseMapper<IoTDeviceLogMetadata> {

  /**
   * 获取属性最大的条数
   *
   * @param iotId
   * @param maxStorage
   * @param property
   * @return
   */
  Integer getTopPropertiesRecord(
      @Param("iotId") String iotId,
      @Param("maxStorage") int maxStorage,
      @Param("property") String property);

  /**
   * 删除属性最大的条数
   *
   * @param iotId iotId
   * @param topId 待删除的起始id
   * @return 影响条数
   */
  int deleteTopPropertiesRecord(
      @Param("iotId") String iotId,
      @Param("topId") Integer topId,
      @Param("property") String property);

  /**
   * 获取事件最大的条数
   *
   * @param iotId
   * @param maxStorage
   * @param event
   * @return
   */
  Integer getTopEventRecord(
      @Param("iotId") String iotId,
      @Param("maxStorage") int maxStorage,
      @Param("event") String event);

  /**
   * 删除属性最大的条数
   *
   * @param iotId iotId
   * @param topId 待删除的起始id
   * @return 影响条数
   */
  int deleteTopEventRecord(
      @Param("iotId") String iotId, @Param("topId") Integer topId, @Param("event") String event);

  List<IoTDeviceLogMetadataVO> selectLogMetaList(LogQuery logQuery);

  /**
   * 获取设备事件的统计信息
   *
   * @param event 事件id
   * @param iotId 设备iotId
   */
  List<String> queryEventTotalByEventAndId(
      @Param("event") String event, @Param("iotId") String iotId);
}
