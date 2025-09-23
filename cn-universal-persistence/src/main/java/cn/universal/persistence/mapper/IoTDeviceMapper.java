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
import cn.universal.persistence.dto.IoTDeviceDTO;
import cn.universal.persistence.dto.IoTDeviceMetadataBO;
import cn.universal.persistence.entity.IoTDevice;
import cn.universal.persistence.entity.IoTProduct;
import cn.universal.persistence.entity.bo.IoTDeviceBO;
import cn.universal.persistence.entity.bo.IoTDeviceHistoryBO;
import cn.universal.persistence.entity.bo.IoTGwDeviceBO;
import cn.universal.persistence.entity.bo.NetworkBO;
import cn.universal.persistence.entity.vo.GatewayDeviceVo;
import cn.universal.persistence.entity.vo.IoTDeviceCompanyVO;
import cn.universal.persistence.entity.vo.IoTDeviceCountVO;
import cn.universal.persistence.entity.vo.IoTDeviceModelVO;
import cn.universal.persistence.entity.vo.IoTDeviceVO;
import cn.universal.persistence.query.IoTAPIQuery;
import cn.universal.persistence.query.IoTDeviceQuery;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.ibatis.annotations.Param;

public interface IoTDeviceMapper extends BaseMapper<IoTDevice> {

  /**
   * deviceId 设备编号,extDeviceId 扩展设备编号,iotId 设备统一编号
   *
   * @param map
   * @return IoTDeviceDTO
   */
  IoTDeviceDTO selectIoTDeviceBO(Map<String, Object> map);

  /**
   * 查询设备信息。分页查询
   *
   * @param map deviceId 设备编号,extDeviceId 扩展设备编号,iotId 设备统一编号
   * @return IoTDeviceDTO
   */
  List<IoTDeviceDTO> selectDevInstanceBOV2List(Map<String, Object> map);

  /** 计算应用设备数量 */
  List<IoTDeviceCountVO> countDevByApplication(String appUniqueId);

  /** 查询设备元数据 */
  IoTDeviceMetadataBO selectDevMetadataBo(String iotId);

  List<IoTDevice> selectList(
      @Param("productKey") String productKey, @Param("start") int start, @Param("limit") int limit);

  List<IoTDevice> selectListByProductKey(@Param("productKey") String productKey);

  IoTDeviceVO apiDeviceInfo(IoTAPIQuery apiQuery);

  List<IoTDeviceVO> apiDeviceList(IoTAPIQuery apiQuery);

  /** 查询设备 */
  public IoTDevice selectDevInstanceById(String id);

  /** 查询设备列表 */
  public List<IoTDevice> selectDevInstanceList(
      @Param("ioTDevice") IoTDevice ioTDevice, @Param("unionId") String unionId);

  /** 按第三方平台过滤的设备列表（新接口专用） */
  List<IoTDevice> selectDevInstanceListByPlatform(
      @Param("ioTDevice") IoTDevice ioTDevice,
      @Param("thirdPlatform") String thirdPlatform,
      @Param("unionId") String unionId);

  public List<IoTDevice> selectDevInstanceUnList(
      @Param("ioTDevice") IoTDevice ioTDevice, @Param("unionId") String unionId);

  List<IoTDevice> selectDevInstanceForExport(
      @Param("ioTDevice") IoTDevice ioTDevice, @Param("unionId") String unionId);

  /** 新增设备 */
  public int insertDevInstance(IoTDevice ioTDevice);

  /** 修改设备 */
  public int updateDevInstance(IoTDevice ioTDevice);

  int bindApp(@Param("id") String[] id, @Param("appUniqueId") String appUniqueId);

  /** 设备绑定应用 */
  int apiBindApp(@Param("appUniqueId") String appUniqueId, @Param("iotId") String iotId);

  /** 设备应用解绑 */
  int apiUnBindApp(@Param("iotId") String iotId);

  /** 删除设备 */
  public int deleteDevInstanceById(String id);

  /** 批量删除设备 */
  public int deleteDevInstanceByIds(String[] ids);

  /** 根据分组id获取设备集合 */
  List<IoTDevice> selectDevListByIds(String groupId);

  int selectDevByAppUniqueId(String[] id);

  /** 查询所以设备型号 */
  List<IoTDeviceCompanyVO> selectAllBySupportChild(
      @Param("supportChild") Boolean supportChild, @Param("apps") String apps);

  IoTDevice getOneByDeviceId(@Param("query") IoTDeviceQuery query);

  IoTDevice selectIoTDevice(
      @Param("productKey") String productKey, @Param("deviceId") String deviceId);

  IoTDevice getOneByIotId(@Param("query") IoTDeviceQuery query);

  /** 根据产品key查型号配置 */
  IoTDeviceModelVO getModelByProductKey(String productKey);

  List<IoTDevice> selectNotBindDevInstanceList(
      @Param("groupId") String groupId,
      @Param("dev") IoTDevice ioTDevice,
      @Param("unionId") String unionId);

  /**
   * 查询满足离线标准的deviceId
   *
   * @param productKey 产品标识
   * @param difference 时间差额
   */
  List<String> selectOfflineThresholdIotIds(
      @Param("productKey") String productKey, @Param("difference") String difference);

  /** 选择离线摄像头 */
  List<IoTDevice> selectOfflineCamera(
      @Param("productKeyList") List<String> productKeyList,
      @Param("discardValue") Integer discardValue,
      @Param("status") Integer status,
      @Param("start") Integer start,
      @Param("limit") Integer limit);

  /** 查询设备历史总数 */
  public int selectDevInstanceHistory(IoTDeviceHistoryBO ioTDeviceHistoryBO);

  public int insertDevInstanceHistory(IoTDeviceHistoryBO ioTDeviceHistoryBO);

  /** 修改设备配置字段 */
  public int updateDevConfiguration(Map<String, String> map);

  List<IoTDeviceVO> selectFenceDevice(IoTDeviceBO ioTDeviceBO);

  List<IoTDevice> hikDeviceList(IoTAPIQuery apiQuery);

  void clearDevBindInfo(IoTDevice instance);

  List<IoTDevice> queryMileSightList(@Param("bo") NetworkBO bo);

  List<GatewayDeviceVo> getGatewayDeviceList(@Param("bo") IoTGwDeviceBO bo);

  List<IoTDevice> selectDevInstanceListWithTags(
      @Param("ioTDevice") IoTDeviceBO ioTDevice, @Param("unionId") String unionId);

  List<IoTDevice> listByIds(String[] ids);

  List<IoTProduct> selectProductListInBatchFunction(
      @Param("applicationId") Long applicationId, @Param("unionId") String unionId);

  int checkIotIdUnique(@Param("iotId") String iotId);

  IoTDevice getOneByExtId(@Param("extId") String extId);

  /** 根据productKey统计设备数量 */
  int countByProductKey(String productKey);

  /** 批量刷新设备日志，更新online_time和state，初始化registry_time（如为空） */
  void batchFlushLog(@Param("iotIds") Set<String> iotIds);

  /** 统计所有设备总数 */
  long countAllDevices();

  /** 统计在线设备总数 */
  long countOnlineDevices();

  /** 根据创建者统计设备总数 */
  long countDevicesByCreator(@Param("creatorId") String creatorId);

  /** 根据创建者统计在线设备总数 */
  long countOnlineDevicesByCreator(@Param("creatorId") String creatorId);
}
