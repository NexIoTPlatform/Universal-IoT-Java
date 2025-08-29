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

package cn.universal.dm.device.service.log;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import cn.hutool.db.GlobalDbConfig;
import cn.hutool.db.Page;
import cn.hutool.db.PageResult;
import cn.hutool.db.ds.DSFactory;
import cn.hutool.db.ds.GlobalDSFactory;
import cn.hutool.db.sql.SqlBuilder;
import cn.hutool.json.JSONObject;
import cn.hutool.setting.Setting;
import cn.universal.common.constant.IoTConstant.MessageType;
import cn.universal.core.message.UPRequest;
import cn.universal.core.metadata.AbstractPropertyMetadata;
import cn.universal.dm.device.entity.IoTDevicePropertiesBO;
import cn.universal.dm.device.service.impl.IoTDeviceService;
import cn.universal.dm.device.service.impl.IoTProductDeviceService;
import cn.universal.persistence.base.BaseUPRequest;
import cn.universal.persistence.dto.IoTDeviceDTO;
import cn.universal.persistence.dto.IoTDeviceMetadataBO;
import cn.universal.persistence.dto.LogStorePolicyDTO;
import cn.universal.persistence.entity.IoTDeviceEvents;
import cn.universal.persistence.entity.IoTDeviceLog;
import cn.universal.persistence.entity.IoTDeviceLogMetadata.IoTDeviceLogMetadataBuilder;
import cn.universal.persistence.entity.IoTProduct;
import cn.universal.persistence.entity.vo.IoTDeviceLogMetadataVO;
import cn.universal.persistence.entity.vo.IoTDeviceLogVO;
import cn.universal.persistence.query.LogQuery;
import cn.universal.persistence.query.PageBean;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import java.sql.SQLException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * clickhouse 存储日志
 *
 * <p>通过配置文件动态加载
 *
 * @version 1.0 @Author Aleo
 * @since 2025/9/22 16:10
 */
@Component
@Slf4j
@ConditionalOnProperty(prefix = "clickhouse", name = "enable", havingValue = "true")
public class ClickHouseIoTDeviceLogService extends AbstractIoTDeviceLogService {

  private String storePolicy = "clickhouse";

  @Value("${clickhouse.table.iot_device_log}")
  private String devLogTableName;

  @Value("${clickhouse.table.iot_device_log_metadata}")
  private String devLogMetaTableName;

  @Resource private IoTProductDeviceService iotProductDeviceService;

  @Value(value = "${clickhouse.address}")
  private String address;

  @Value(value = "${clickhouse.username}")
  private String username;

  @Value(value = "${clickhouse.password}")
  private String password;

  @Resource private IoTDeviceService iotDeviceService;

  static {
    // clickhouse不支持主键自增
    GlobalDbConfig.setReturnGeneratedKey(false);
  }

  @PostConstruct
  public void initDb() {

    Setting setting = new Setting();
    setting.set("url", address);
    setting.set("user", username);
    setting.set("pass", password);
    setting.set("showSql", "true");
    setting.set("formatSql", "true");
    setting.set("sqlLevel", "debug");
    GlobalDSFactory.set(DSFactory.create(setting));
    log.info("初始化clickhouse成功");
  }

  @Override
  @Async("taskExecutor")
  public void saveDeviceLog(BaseUPRequest upRequest, IoTDeviceDTO noUse, IoTProduct ioTProduct) {
    /** 产品数据存储策略，不为空则保存日志 */
    if (StrUtil.isNotBlank(ioTProduct.getStorePolicy())) {
      IoTDeviceDTO ioTDeviceDTO = iotDeviceService.selectDevInstanceBO(upRequest.getIotId());
      try {
        IoTDeviceLog ioTDeviceLog = build(upRequest, ioTDeviceDTO);
        /** isToUnderlineCase true 驼峰转下划线 */
        Entity entity = Entity.create(devLogTableName).parseBean(ioTDeviceLog, true, false);
        int count = Db.use().insert(entity);
        log.info("clickhouse插入成功={}", count);
      } catch (Exception e) {
        log.error("保存设备日志报错={}", e);
      }
      String storePolicyConfiguration = ioTProduct.getStorePolicyConfiguration();
      try {
        if (StrUtil.isNotBlank(storePolicyConfiguration)) {
          LogStorePolicyDTO productLogStorePolicy =
              iotProductDeviceService.getProductLogStorePolicy(ioTProduct.getProductKey());
          saveLogStorePolicy(productLogStorePolicy, upRequest, ioTProduct);
        }
      } catch (Exception e) {
        log.error("保存设备属性扩展日志报错={}", e);
      }
    }
  }

  @Override
  public void saveDeviceLog(
      IoTDeviceLog ioTDeviceLog, IoTDeviceDTO ioTDeviceDTO, IoTProduct ioTProduct) {
    /** 产品数据存储策略，不为空则保存日志 */
    if (StrUtil.isNotBlank(ioTProduct.getStorePolicy())) {
      IoTDeviceMetadataBO metadataBO =
          iotDeviceService.selectDevMetadataBo(ioTDeviceDTO.getIotId());
      try {
        /** isToUnderlineCase true 驼峰转下划线 */
        Entity entity = Entity.create(devLogTableName).parseBean(ioTDeviceLog, true, false);
        int count = Db.use().insert(entity);
        log.info("clickhouse插入成功={}", count);
      } catch (Exception e) {
        log.error("保存设备日志报错={}", e);
      }
    }
  }

  private void saveLogStorePolicy(
      LogStorePolicyDTO logStorePolicyDTO, UPRequest up, IoTProduct ioTProduct) {
    if (MessageType.PROPERTIES.equals(up.getMessageType()) && up.getProperties() != null) {
      up.getProperties()
          .forEach(
              (key, value) -> {
                if (logStorePolicyDTO.getProperties().containsKey(key)) {
                  AbstractPropertyMetadata propertyOrNull =
                      getDeviceMetadata(ioTProduct.getMetadata()).getPropertyOrNull(key);
                  IoTDevicePropertiesBO ioTDevicePropertiesBO = new IoTDevicePropertiesBO();
                  ioTDevicePropertiesBO.withValue(propertyOrNull, value);
                  // TODO event
                  IoTDeviceLogMetadataBuilder IoTDeviceLogMetadataBuilder = builder(up);
                  IoTDeviceLogMetadataBuilder.id(IdUtil.getSnowflake().nextId());
                  IoTDeviceLogMetadataBuilder.property(key);
                  IoTDeviceLogMetadataBuilder.content(
                      StrUtil.str(value, CharsetUtil.charset("UTF-8")));
                  IoTDeviceLogMetadataBuilder.ext1(ioTDevicePropertiesBO.getPropertyName());
                  IoTDeviceLogMetadataBuilder.ext2(ioTDevicePropertiesBO.getFormatValue());
                  IoTDeviceLogMetadataBuilder.ext3(ioTDevicePropertiesBO.getSymbol());
                  /** isToUnderlineCase true 驼峰转下划线 */
                  Entity entity =
                      Entity.create(devLogMetaTableName)
                          .parseBean(IoTDeviceLogMetadataBuilder.build(), true, false);
                  try {
                    Db.use().insert(entity);
                  } catch (SQLException throwables) {
                    throwables.printStackTrace();
                  }
                }
              });
    }
    if (MessageType.EVENT.equals(up.getMessageType())
        && logStorePolicyDTO.getEvent().containsKey(up.getEvent())) {
      IoTDeviceLogMetadataBuilder IoTDeviceLogMetadataBuilder = builder(up);
      IoTDeviceLogMetadataBuilder.id(IdUtil.getSnowflake().nextId());
      IoTDeviceLogMetadataBuilder.event(up.getEvent());
      IoTDeviceLogMetadataBuilder.content(up.getEventName());
      Entity entity =
          Entity.create(devLogMetaTableName)
              .parseBean(IoTDeviceLogMetadataBuilder.build(), true, false);
      try {
        Db.use().insert(entity);
      } catch (SQLException e) {
        log.error("插入clickhouse异常，tablename={},e={}", devLogMetaTableName, e);
      }
    }
  }

  @Override
  public PageBean<IoTDeviceLogVO> pageList(LogQuery logQuery) {
    SqlBuilder builder = SqlBuilder.create().select().from(devLogTableName);
    StringBuilder builder1 = new StringBuilder(builder.build());
    builder1.append(" WHERE 1=1");
    if (StrUtil.isNotBlank(logQuery.getIotId())) {
      builder1.append(" AND  iot_id ='" + logQuery.getIotId() + "'");
    }
    if (StrUtil.isNotBlank(logQuery.getDeviceId())) {
      builder1.append(" AND device_id='" + logQuery.getDeviceId() + "'");
    }
    if (StrUtil.isNotBlank(logQuery.getMessageType())) {
      builder1.append(" AND message_type='" + logQuery.getMessageType() + "'");
    }
    if (StrUtil.isNotBlank(logQuery.getDeviceName())) {
      builder1.append(" AND device_name='" + logQuery.getDeviceName() + "'");
    }
    if (StrUtil.isNotBlank(logQuery.getEvent())) {
      builder1.append(" AND event='" + logQuery.getEvent() + "'");
    }
    if (MapUtil.isNotEmpty(logQuery.getParams())
        && ObjectUtil.isNotNull(logQuery.getParams().get("properties"))) {
      builder1.append(
          " AND JSONHas(content,'properties','" + logQuery.getParams().get("properties") + "')=1");
    }
    if (MapUtil.isNotEmpty(logQuery.getParams())
        && ObjectUtil.isNotNull(logQuery.getParams().get("event"))) {
      builder1.append(" AND event='" + logQuery.getParams().get("event") + "'");
    }
    if (MapUtil.isNotEmpty(logQuery.getParams())
        && ObjectUtil.isNotNull(logQuery.getParams().get("beginCreateTime"))) {
      builder1.append(" AND create_time >=" + logQuery.getParams().get("beginCreateTime"));
    }
    if (MapUtil.isNotEmpty(logQuery.getParams())
        && ObjectUtil.isNotNull(logQuery.getParams().get("endCreateTime"))) {
      builder1.append(" AND create_time <= " + logQuery.getParams().get("beginCreateTime"));
    }
    builder1.append(" ORDER BY create_time DESC ");
    try {
      // logQuery.getPageNum() - 1 偏移量计算有点问题
      PageResult<Entity> page =
          Db.use()
              .page(
                  builder1.toString(), Page.of(logQuery.getPageNum() - 1, logQuery.getPageSize()));
      List<IoTDeviceLogVO> ioTDeviceLogVOS = BeanUtil.copyToList(page, IoTDeviceLogVO.class);
      return new PageBean(
          ioTDeviceLogVOS,
          Long.parseLong(page.getTotal() + ""),
          logQuery.getPageSize(),
          logQuery.getPageNum());

    } catch (SQLException e) {
      log.warn("clickhouse query log error = {}", e);
      return null;
    }
  }

  @Override
  public IoTDeviceLogVO queryById(LogQuery logQuery) {
    try {
      Entity vo = Db.use().get(Entity.create(devLogTableName).set("id", logQuery.getId()));
      IoTDeviceLogVO vos = new IoTDeviceLogVO();
      BeanUtil.copyProperties(vo, vos);
      return vos;
    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }
    return null;
  }

  @Override
  public PageBean<IoTDeviceEvents> queryEventTotal(String productKey, String iotId) {
    List<IoTDeviceEvents> list = selectDevEvents(productKey);
    for (IoTDeviceEvents devEvent : list) {
      SqlBuilder builder =
          SqlBuilder.create()
              .select("COUNT(1) as qty,max(create_time) as create_time")
              .from(devLogTableName);
      StringBuilder builder1 = new StringBuilder(builder.build());
      builder1.append(" WHERE event='" + devEvent.getId() + "'");
      builder1.append(" AND message_type = 'EVENT'");
      builder1.append(" AND iot_id ='" + iotId + "'");
      builder1.append(" ORDER BY `create_time` DESC");
      try {
        Entity event = Db.use().queryOne(builder1.toString());
        devEvent.setQty(event.getInt("qty") >= 100 ? "99+" : event.getInt("qty") + "");
        devEvent.setTime(event.getStr("create_time"));
        //        PageResult<Entity> events = Db.use().page(builder1.toString(), Page.of(0, 100));
        //        int size = events.size();
        //        if (size > 0) {
        //          devEvent.setTime(events.get(0).getStr("create_time"));
        //          devEvent.setQty(size >= 100 ? "99+" : String.valueOf(size));
        //        }
      } catch (SQLException throwables) {
        throwables.printStackTrace();
      }
    }
    return new PageBean(list, 100L, 1, 100);
  }

  @Override
  public PageBean<IoTDeviceLogMetadataVO> queryLogMeta(LogQuery logQuery) {
    SqlBuilder builder = SqlBuilder.create().select().from(devLogMetaTableName);
    StringBuilder builder1 = new StringBuilder(builder.build());
    builder1.append(" WHERE 1=1");
    if (StrUtil.isNotBlank(logQuery.getIotId())) {
      builder1.append(" AND  iot_id ='" + logQuery.getIotId() + "'");
    }
    if (StrUtil.isNotBlank(logQuery.getDeviceId())) {
      builder1.append(" AND device_id='" + logQuery.getDeviceId() + "'");
    }
    if (StrUtil.isNotBlank(logQuery.getProperty())) {
      builder1.append(" AND property='" + logQuery.getProperty() + "'");
    }
    if (StrUtil.isNotBlank(logQuery.getEvent())) {
      builder1.append(" AND event='" + logQuery.getEvent() + "'");
    }
    builder1.append(" ORDER BY create_time DESC ");
    try {
      // logQuery.getPageNum() - 1 偏移量计算有点问题
      PageResult<Entity> page =
          Db.use()
              .page(
                  builder1.toString(), Page.of(logQuery.getPageNum() - 1, logQuery.getPageSize()));
      List<IoTDeviceLogMetadataVO> devLogVos =
          BeanUtil.copyToList(page, IoTDeviceLogMetadataVO.class);
      return new PageBean(
          devLogVos,
          Long.parseLong(page.getTotal() + ""),
          logQuery.getPageSize(),
          logQuery.getPageNum());

    } catch (SQLException e) {
      log.warn("clichouse query log error = {}", e);
      return null;
    }
  }

  @Override
  public String getPolicy() {
    return storePolicy;
  }

  @Override
  public JSONObject configMetadata() {
    return null;
  }
}
