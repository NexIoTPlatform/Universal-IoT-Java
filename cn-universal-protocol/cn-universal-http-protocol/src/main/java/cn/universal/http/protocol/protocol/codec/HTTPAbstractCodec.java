package cn.universal.http.protocol.protocol.codec;

import cn.hutool.core.util.StrUtil;
import cn.universal.common.exception.CodecException;
import cn.universal.common.exception.IoTException;
import cn.universal.core.protocol.support.ProtocolSupportDefinition;
import cn.universal.dm.device.service.impl.IoTProductDeviceService;
import cn.universal.http.protocol.enums.HTTPCodecMethod;
import cn.universal.http.protocol.protocol.HTTPProtocolSupportDefinition;
import cn.universal.http.protocol.protocol.HTTPProtocolUniversalCodec;
import cn.universal.persistence.mapper.IoTDeviceProtocolMapper;
import jakarta.annotation.Resource;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;

/**
 * HTTP编解码处理器工具类
 *
 * @author gitee.com/NexIoT
 * @version 1.0
 * @since 2025/6/27 23:47
 */
@Slf4j
public abstract class HTTPAbstractCodec {

  @Resource protected IoTProductDeviceService iotProductDeviceService;

  @Resource protected IoTDeviceProtocolMapper ioTDeviceProtocolMapper;

  private Map<String, Boolean> supportCodecMap = new ConcurrentHashMap<>();

  public void removeCodec(String productKey) {
    HTTPProtocolUniversalCodec instance = HTTPProtocolUniversalCodec.getInstance();
    instance.remove(productKey);
  }

  public boolean support(String productKey) {
    if (!supportCodecMap.containsKey(productKey)) {
      getProtocolDefinition(productKey);
    }
    return supportCodecMap.getOrDefault(productKey, false);
  }

  public ProtocolSupportDefinition getProtocolDefinition(String productKey) {
    ProtocolSupportDefinition definition = iotProductDeviceService.selectProtocolDef(productKey);
    if (definition == null) {
      supportCodecMap.put(productKey, false);
      log.warn("protocol not exist, productKey:{}", productKey);
      return null;
    }
    supportCodecMap.put(productKey, true);
    return definition;
  }

  public void load(String productKey, HTTPCodecMethod method) {
    if (StrUtil.isBlank(productKey)) {
      return;
    }
    ProtocolSupportDefinition protocolDefinition = getProtocolDefinition(productKey);
    if (protocolDefinition == null) {
      return;
    }
    Map<String, Object> config = protocolDefinition.getConfiguration();
    // JS原始代码
    String script =
        (String)
            Optional.ofNullable(config.get("location"))
                .map(String::valueOf)
                .orElseThrow(
                    () -> {
                      return new IoTException(
                          "magic engine source code not exist, can not do encode or decode ");
                    });
    try {
      HTTPProtocolSupportDefinition build =
          HTTPProtocolSupportDefinition.builder()
              .productKey(protocolDefinition.getId())
              .script(script)
              .build();
      HTTPProtocolUniversalCodec instance = HTTPProtocolUniversalCodec.getInstance();
      instance.load(build, method);
    } catch (CodecException e) {
      log.error("HTTPAbstractCodec load error", e);
    }
  }

  protected String codec(String productKey, String payload, HTTPCodecMethod codecMethod) {
    String result = "";
    HTTPProtocolUniversalCodec instance = HTTPProtocolUniversalCodec.getInstance();
    if (!instance.isLoaded(productKey)) {
      load(productKey, HTTPCodecMethod.codecAdd);
    }
    // 开始解码操作
    long t1 = System.currentTimeMillis();
    // 根据类型选择解码实现类
    try {
      result =
          switch (codecMethod) {
            case codecAdd -> instance.codecAdd(productKey, payload);
            case codecUpdate -> instance.codecUpdate(productKey, payload);
            case codecDelete -> instance.codecDelete(productKey, payload);
            case codecQuery -> instance.codecQuery(productKey, payload);
            case iotToYour -> instance.iotToYour(productKey, payload);
            case yourToIot -> instance.yourToIot(productKey, payload);
            case decode -> instance.decode(productKey, payload);
            case encode -> instance.encode(productKey, payload);
            case preDecode -> instance.decode(productKey, payload); // HTTP没有preDecode，使用decode
            case codecFunction -> instance.iotToYour(productKey, payload); // 映射到iotToYour
            case codecOther -> instance.yourToIot(productKey, payload); // 映射到yourToIot
          };
    } catch (Exception e) {
      log.error("productKey={} 原始报文={} , 请求报错", productKey, payload, e);
      result = "编解码失败: " + e.getMessage();
    }
    long t2 = System.currentTimeMillis();
    log.info(
        "HTTPAbstractCodec productKey={} 原始报文={} , 解码={} 耗时={}ms",
        productKey,
        payload,
        result,
        (t2 - t1));
    return result;
  }
}
