package cn.universal.mqtt.protocol.processor.down;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.universal.common.constant.IoTConstant.DeviceStatus;
import cn.universal.common.constant.IoTConstant.DownCmd;
import cn.universal.common.constant.IoTConstant.ERROR_CODE;
import cn.universal.common.domain.R;
import cn.universal.dm.device.service.impl.IoTProductDeviceService;
import cn.universal.mqtt.protocol.entity.MQTTDownRequest;
import cn.universal.mqtt.protocol.processor.MQTTDownMessageProcessor;
import cn.universal.persistence.base.IoTDeviceLifeCycle;
import cn.universal.persistence.base.IoTDownAdapter;
import cn.universal.persistence.entity.IoTDevice;
import cn.universal.persistence.entity.IoTProduct;
import cn.universal.persistence.entity.SupportMapAreas;
import cn.universal.persistence.mapper.IoTDeviceMapper;
import cn.universal.persistence.mapper.SupportMapAreasMapper;
import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * mqtt 设备增加
 *
 * @version 1.0 @Author gitee.com/NexIoT
 * @since 2025/7/10 10:40
 */
@Component
@Slf4j(topic = "mqtt")
public class MQTTDownAdd extends IoTDownAdapter<MQTTDownRequest>
    implements MQTTDownMessageProcessor {

  private String CUSTOM_FIELD = "customField";

  @Resource(name = "ioTDeviceActionAfterService")
  private IoTDeviceLifeCycle ioTDeviceLifeCycle;

  @Resource private SupportMapAreasMapper supportMapAreasMapper;
  @Resource private IoTDeviceMapper ioTDeviceMapper;
  @Autowired private IoTProductDeviceService iotProductDeviceService;

  @Override
  public R<?> process(MQTTDownRequest downRequest) {
    /** 当编解码请求结果为空 或者 编解码请求结果不为空且状态码等于0时，添加设备到本地 */
    log.info(
        "添加mqtt设备,deviceId={},productKey={}",
        downRequest.getDeviceId(),
        downRequest.getProductKey());
    R preR =
        beforeDownAction(downRequest.getIoTProduct(), downRequest.getDownCommonData(), downRequest);
    if (Objects.nonNull(preR)) {
      return preR;
    }
    IoTDevice ioTDevice =
        IoTDevice.builder()
            .productKey(downRequest.getProductKey())
            .deviceId(downRequest.getDeviceId())
            .build();
    int size = ioTDeviceMapper.selectCount(ioTDevice);
    if (size > 0) {
      // 设备已经存在
      return R.error(
          ERROR_CODE.DEV_ADD_DEVICE_ID_EXIST.getCode(),
          ERROR_CODE.DEV_ADD_DEVICE_ID_EXIST.getName());
    }
    // 操作数据库
    R<?> rs = saveIoTDevice(downRequest);
    return rs;
  }

  private R<?> saveIoTDevice(MQTTDownRequest downRequest) {
    IoTDevice ioTDevice =
        IoTDevice.builder()
            .deviceId(downRequest.getDeviceId())
            .createTime(System.currentTimeMillis() / 1000)
            .deviceName(downRequest.getDownCommonData().getDeviceName())
            .state(DeviceStatus.offline.getCode())
            .iotId(downRequest.getProductKey()+downRequest.getDeviceId())
            .application(downRequest.getApplicationId())
            .creatorId(downRequest.getAppUnionId())
            .productName(downRequest.getIoTProduct().getName())
            .detail(downRequest.getDetail())
            .gwProductKey(downRequest.getGwProductKey())
            .extDeviceId(downRequest.getGwDeviceId())
            .productKey(downRequest.getProductKey())
            .build();

    if (StrUtil.isNotBlank(downRequest.getDownCommonData().getExtDeviceId())) {
      ioTDevice.setExtDeviceId(downRequest.getDownCommonData().getExtDeviceId());
    }

    if (StrUtil.isNotBlank(downRequest.getDownCommonData().getLatitude())
        && StrUtil.isNotBlank(downRequest.getDownCommonData().getLongitude())) {
      ioTDevice.setCoordinate(
          StrUtil.join(
              ",",
              downRequest.getDownCommonData().getLongitude(),
              downRequest.getDownCommonData().getLatitude()));

      SupportMapAreas supportMapAreas =
          supportMapAreasMapper.selectMapAreas(
              downRequest.getDownCommonData().getLongitude(),
              downRequest.getDownCommonData().getLatitude());
      if (supportMapAreas == null) {
        log.info(
            "查询区域id为空,lot={},lat={}",
            downRequest.getDownCommonData().getLongitude(),
            downRequest.getDownCommonData().getLatitude());
      } else {
        ioTDevice.setAreasId(supportMapAreas.getId());
      }
    }
    Map<String, Object> config = new HashMap<>();
    finalDown(config, downRequest.getIoTProduct(), downRequest.getCmd(), downRequest.getData());
    ioTDevice.setConfiguration(JSONUtil.toJsonStr(config));
    int success = ioTDeviceMapper.insertUseGeneratedKeys(ioTDevice);
    if (success > 0) {
      // 推送设备创建消息
      ioTDeviceLifeCycle.create(
          downRequest.getProductKey(), downRequest.getDeviceId(), downRequest);
      // 组件返回字段
      Map<String, Object> result = new HashMap<>();
      result.put("iotId", ioTDevice.getIotId());
      result.put("areasId", ioTDevice.getAreasId() == null ? "" : ioTDevice.getAreasId());
      if (StrUtil.isNotBlank(downRequest.getIoTProduct().getMetadata())) {
        result.put("metadata", JSONUtil.parseObj(downRequest.getIoTProduct().getMetadata()));
      }
      result.put("productKey", downRequest.getProductKey());
      result.put("deviceNode", downRequest.getIoTProduct().getDeviceNode());
      return R.ok(result);
    } else {
      return R.error("设备增加失败");
    }
  }

  /**
   * 设备后置处理
   *
   * <p>用于[添加设备]，添加data额外属性值到数据库configuration字段
   */
  protected void finalDown(
      Map<String, Object> config, IoTProduct product, DownCmd downCmd, Object data) {
    try {
      if (product != null
          && StrUtil.isNotBlank(product.getThirdConfiguration())
          && DownCmd.DEV_ADD.equals(downCmd)) {
        JSONObject jsonObject = JSONUtil.parseObj(product.getThirdConfiguration());
        JSONArray customFields = jsonObject.getJSONArray(CUSTOM_FIELD);
        if (jsonObject == null || customFields == null || customFields.size() <= 0) {
          return;
        }
        for (Object obj : customFields) {
          JSONObject object = (JSONObject) obj;
          Object fieldValue = BeanUtil.getFieldValue(data, object.getStr("id"));
          config.put(object.getStr("id"), fieldValue);
        }
      }
    } catch (Exception e) {
      log.warn("={}", e);
    }
  }

  @Override
  public boolean supports(MQTTDownRequest request) {
    if (!DownCmd.DEV_ADD.equals(request.getCmd())) {
      return false;
    }
    return true;
  }

  @Override
  public String getName() {
    return "";
  }

  @Override
  public int getOrder() {
    return 0;
  }
}
