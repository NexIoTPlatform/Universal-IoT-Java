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

package cn.universal.http.protocol.protocol;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.universal.common.exception.CodecException;
import cn.universal.core.engine.MagicScript;
import cn.universal.core.engine.MagicScriptContext;
import cn.universal.core.engine.runtime.MagicScriptRuntime;
import cn.universal.core.protocol.request.ProtocolDecodeRequest;
import cn.universal.core.protocol.request.ProtocolEncodeRequest;
import cn.universal.core.protocol.support.ProtocolCodecSupport;
import cn.universal.core.protocol.support.ProtocolCodecSupportWrapper;
import cn.universal.core.protocol.support.ProtocolSupportDefinition;
import cn.universal.dm.device.service.impl.IoTProductDeviceService;
import cn.universal.http.protocol.enums.HTTPCodecMethod;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HTTPProtocolUniversalCodec extends ProtocolCodecSupportWrapper
    implements HTTPProtocolCodecLoader, ProtocolCodecSupport {

  private final Map<String, MagicScript> addProvider = new ConcurrentHashMap<>();
  private final Map<String, MagicScript> updateProvider = new ConcurrentHashMap<>();
  private final Map<String, MagicScript> deleteProvider = new ConcurrentHashMap<>();
  private final Map<String, MagicScript> queryProvider = new ConcurrentHashMap<>();
  private final Map<String, MagicScript> iotToYourProvider = new ConcurrentHashMap<>();
  private final Map<String, MagicScript> yourToIotProvider = new ConcurrentHashMap<>();
  private final Map<String, MagicScript> encodeProvider = new ConcurrentHashMap<>();
  private final Map<String, MagicScript> decodeProvider = new ConcurrentHashMap<>();

  private final Map<String, Set<String>> methodCache = new ConcurrentHashMap<>();

  private HTTPProtocolUniversalCodec() {}

  private static class HTTPProtocolUniversalCodecHolder {
    private static final HTTPProtocolUniversalCodec INSTANCE = new HTTPProtocolUniversalCodec();
  }

  public static HTTPProtocolUniversalCodec getInstance() {
    return HTTPProtocolUniversalCodecHolder.INSTANCE;
  }

  @Override
  public void load(HTTPProtocolSupportDefinition definition, HTTPCodecMethod codecMethod)
      throws CodecException {
    try {
      String scriptCode = definition.getScript();
      String productKey = definition.getProductKey();
      if (StrUtil.isBlank(scriptCode) || StrUtil.isBlank(productKey)) {
        throw new CodecException("script ||productKey 不能为空");
      }

      // 根据不同的编解码方法添加相应的脚本
      switch (codecMethod) {
        case codecAdd:
          scriptCode = scriptCode + "  \n return add(payload)";
          break;
        case codecUpdate:
          scriptCode = scriptCode + "  \n return update(payload)";
          break;
        case codecDelete:
          scriptCode = scriptCode + "  \n return delete(payload)";
          break;
        case codecQuery:
          scriptCode = scriptCode + "  \n return query(payload)";
          break;
        case iotToYour:
          scriptCode = scriptCode + "  \n return function(payload)";
          break;
        case encode:
          scriptCode = scriptCode + "  \n return encode(payload)";
          break;
        case decode:
          scriptCode = scriptCode + "  \n return decode(payload)";
          break;
        case preDecode:
          scriptCode = scriptCode + "  \n return decode(payload)"; // HTTP没有preDecode，使用decode
          break;
        case yourToIot:
          scriptCode = scriptCode + "  \n return yourToIot(payload)";
          break;
        case codecFunction:
          scriptCode = scriptCode + "  \n return codecFunction(payload)";
          break;
        case codecOther:
          scriptCode = scriptCode + "  \n return codecOther(payload)";
          break;
        default:
          scriptCode = scriptCode + "  \n return other(payload)";
          break;
      }

      // 初始化
      long t1 = System.currentTimeMillis();
      MagicScript script = MagicScript.create(scriptCode, null);
      MagicScriptRuntime compile = script.compile();
      evalMethodCache(productKey, compile);
      long t2 = System.currentTimeMillis();
      log.info("编译耗时：{}", (t2 - t1) + "ms");

      // 根据方法类型存储到对应的provider
      switch (codecMethod) {
        case codecAdd:
          addProvider.put(productKey, script);
          break;
        case codecUpdate:
          updateProvider.put(productKey, script);
          break;
        case codecDelete:
          deleteProvider.put(productKey, script);
          break;
        case codecQuery:
          queryProvider.put(productKey, script);
          break;
        case iotToYour:
          iotToYourProvider.put(productKey, script);
          break;
        case decode:
          decodeProvider.put(productKey, script);
          break;
        case encode:
          encodeProvider.put(productKey, script);
          break;
        case preDecode:
          decodeProvider.put(productKey, script); // HTTP没有preDecode，使用decode的provider
          break;
        case yourToIot:
          yourToIotProvider.put(productKey, script);
          break;
        case codecFunction:
          iotToYourProvider.put(productKey, script); // 使用iotToYour的provider
          break;
        case codecOther:
          yourToIotProvider.put(productKey, script); // 使用yourToIot的provider
          break;
        default:
          addProvider.put(productKey, script);
          break;
      }
    } catch (Exception e) {
      log.error("http load universal magic error", e);
      throw new CodecException(e.getMessage());
    }
  }

  private void evalMethodCache(String productKey, MagicScriptRuntime compile) {
    String[] varNames = compile.getVarNames();
    if (varNames != null && varNames.length > 0) {
      Set<String> methods = new HashSet<>();
      Set<String> collect =
          Stream.of(HTTPCodecMethod.values()).map(Enum::name).collect(Collectors.toSet());
      for (String method : varNames) {
        if (collect.contains(method)) {
          methods.add(method);
        }
      }
      methodCache.put(productKey, methods);
    }
  }

  // 实现ProtocolCodecSupport接口的方法
  @Override
  public String decode(ProtocolDecodeRequest decodeRequest) throws CodecException {
    return decode(decodeRequest.getDefinition().getId(), decodeRequest.getPayload());
  }

  @Override
  public String encode(ProtocolEncodeRequest encodeRequest) throws CodecException {
    return encode(encodeRequest.getDefinition().getId(), encodeRequest.getPayload());
  }

  @Override
  public boolean isLoaded(String provider, CodecMethod codecMethod) {
    return isLoaded(provider);
  }

  // 实现IProtocolCodecLoader接口的方法
  @Override
  public void load(String productKey, CodecMethod codecMethod) throws CodecException {
    // 检查是否已经加载
    if (isLoaded(productKey, codecMethod)) {
      return;
    }

    HTTPCodecMethod httpCodecMethod = null;
    switch (codecMethod) {
      case decode:
        httpCodecMethod = HTTPCodecMethod.decode;
        break;
      case encode:
        httpCodecMethod = HTTPCodecMethod.encode;
        break;
      case preDecode:
        httpCodecMethod = HTTPCodecMethod.preDecode;
        break;
      case codecAdd:
        httpCodecMethod = HTTPCodecMethod.codecAdd;
        break;
      case codecDelete:
        httpCodecMethod = HTTPCodecMethod.codecDelete;
        break;
      case codecUpdate:
        httpCodecMethod = HTTPCodecMethod.codecUpdate;
        break;
      case codecQuery:
        httpCodecMethod = HTTPCodecMethod.codecQuery;
        break;
      case iotToYour:
        httpCodecMethod = HTTPCodecMethod.iotToYour;
        break;
      case yourToIot:
        httpCodecMethod = HTTPCodecMethod.yourToIot;
        break;
      case codecFunction:
        httpCodecMethod = HTTPCodecMethod.codecFunction;
        break;
      case codecOther:
        httpCodecMethod = HTTPCodecMethod.codecOther;
        break;
    }
    if (httpCodecMethod != null) {
      // 从数据库加载协议配置
      try {
        IoTProductDeviceService bean = SpringUtil.getBean(IoTProductDeviceService.class);
        ProtocolSupportDefinition definition = bean.selectProtocolDef(productKey);
        Map<String, Object> config = definition.getConfiguration();
        String scriptCode =
            (String)
                Optional.ofNullable(config.get("location"))
                    .map(String::valueOf)
                    .orElseThrow(() -> new CodecException("magic engine source code not exist"));

        HTTPProtocolSupportDefinition httpDefinition =
            HTTPProtocolSupportDefinition.builder()
                .productKey(definition.getId())
                .script(scriptCode)
                .build();

        load(httpDefinition, httpCodecMethod);
      } catch (Exception e) {
        log.error("Failed to load protocol for productKey: {}", productKey, e);
        throw new CodecException(e.getMessage());
      }
    }
  }

  @Override
  public String execute(String productKey, String payload, CodecMethod codecMethod)
      throws CodecException {
    switch (codecMethod) {
      case decode:
        return decode(productKey, payload);
      case encode:
        return encode(productKey, payload);
      case preDecode:
        return decode(productKey, payload); // HTTP没有preDecode，使用decode
      case codecAdd:
        return add(productKey, payload);
      case codecDelete:
        return delete(productKey, payload);
      case codecUpdate:
        return update(productKey, payload);
      case codecQuery:
        return query(productKey, payload);
      case iotToYour:
        return iotToYour(productKey, payload);
      case yourToIot:
        return yourToIot(productKey, payload);
      case codecFunction:
        return iotToYour(productKey, payload); // 映射到iotToYour
      case codecOther:
        return yourToIot(productKey, payload); // 映射到yourToIot
      default:
        throw new CodecException("不支持的编解码方法: " + codecMethod);
    }
  }

  // 实现HTTP特有的方法
  public String add(String productKey, String payload) throws CodecException {
    try {
      // 检查是否已经加载
      if (!isLoaded(productKey, CodecMethod.codecAdd)) {
        load(productKey, CodecMethod.codecAdd);
      }
      if (methodCache.get(productKey) == null
          || !methodCache.get(productKey).contains(HTTPCodecMethod.codecAdd.name())) {
        return payload;
      }
      MagicScript magicScript = addProvider.get(productKey);
      MagicScriptContext context = new MagicScriptContext();
      context.set("payload", payload);
      Object result = magicScript.execute(context);
      return str(result);
    } catch (Exception e) {
      String error = ExceptionUtil.getRootCauseMessage(e);
      log.error("ProductKey={} universal add解码失败", productKey, e);
      throw new CodecException(error);
    }
  }

  public String update(String productKey, String payload) throws CodecException {
    try {
      // 检查是否已经加载
      if (!isLoaded(productKey, CodecMethod.codecUpdate)) {
        load(productKey, CodecMethod.codecUpdate);
      }
      if (methodCache.get(productKey) == null
          || !methodCache.get(productKey).contains(HTTPCodecMethod.codecUpdate.name())) {
        return payload;
      }
      MagicScript magicScript = updateProvider.get(productKey);
      MagicScriptContext context = new MagicScriptContext();
      context.set("payload", payload);
      Object result = magicScript.execute(context);
      return str(result);
    } catch (Exception e) {
      String error = ExceptionUtil.getRootCauseMessage(e);
      log.error("ProductKey={} universal update解码失败", productKey, e);
      throw new CodecException(error);
    }
  }

  public String delete(String productKey, String payload) throws CodecException {
    try {
      // 检查是否已经加载
      if (!isLoaded(productKey, CodecMethod.codecDelete)) {
        load(productKey, CodecMethod.codecDelete);
      }
      if (methodCache.get(productKey) == null
          || !methodCache.get(productKey).contains(HTTPCodecMethod.codecDelete.name())) {
        return payload;
      }
      MagicScript magicScript = deleteProvider.get(productKey);
      MagicScriptContext context = new MagicScriptContext();
      context.set("payload", payload);
      Object result = magicScript.execute(context);
      return str(result);
    } catch (Exception e) {
      String error = ExceptionUtil.getRootCauseMessage(e);
      log.error("ProductKey={} universal delete解码失败", productKey, e);
      throw new CodecException(error);
    }
  }

  public String query(String productKey, String payload) throws CodecException {
    try {
      // 检查是否已经加载
      if (!isLoaded(productKey, CodecMethod.codecQuery)) {
        load(productKey, CodecMethod.codecQuery);
      }
      if (methodCache.get(productKey) == null
          || !methodCache.get(productKey).contains(HTTPCodecMethod.codecQuery.name())) {
        return payload;
      }
      MagicScript magicScript = queryProvider.get(productKey);
      MagicScriptContext context = new MagicScriptContext();
      context.set("payload", payload);
      Object result = magicScript.execute(context);
      return str(result);
    } catch (Exception e) {
      String error = ExceptionUtil.getRootCauseMessage(e);
      log.error("ProductKey={} universal query解码失败", productKey, e);
      throw new CodecException(error);
    }
  }

  public String iotToYour(String productKey, String payload) throws CodecException {
    try {
      // 检查是否已经加载
      if (!isLoaded(productKey, CodecMethod.iotToYour)) {
        load(productKey, CodecMethod.iotToYour);
      }
      if (methodCache.get(productKey) == null
          || !methodCache.get(productKey).contains(HTTPCodecMethod.iotToYour.name())) {
        return payload;
      }
      MagicScript magicScript = iotToYourProvider.get(productKey);
      MagicScriptContext context = new MagicScriptContext();
      context.set("payload", payload);
      Object result = magicScript.execute(context);
      return str(result);
    } catch (Exception e) {
      String error = ExceptionUtil.getRootCauseMessage(e);
      log.error("ProductKey={} universal iotToYour解码失败", productKey, e);
      throw new CodecException(error);
    }
  }

  public String yourToIot(String productKey, String payload) throws CodecException {
    try {
      // 检查是否已经加载
      if (!isLoaded(productKey, CodecMethod.yourToIot)) {
        load(productKey, CodecMethod.yourToIot);
      }
      if (methodCache.get(productKey) == null
          || !methodCache.get(productKey).contains(HTTPCodecMethod.yourToIot.name())) {
        return payload;
      }
      MagicScript magicScript = yourToIotProvider.get(productKey);
      MagicScriptContext context = new MagicScriptContext();
      context.set("payload", payload);
      Object result = magicScript.execute(context);
      return str(result);
    } catch (Exception e) {
      String error = ExceptionUtil.getRootCauseMessage(e);
      log.error("ProductKey={} universal yourToIot解码失败", productKey, e);
      throw new CodecException(error);
    }
  }

  public String decode(String productKey, String payload) throws CodecException {
    try {
      // 检查是否已经加载
      if (!isLoaded(productKey, CodecMethod.decode)) {
        load(productKey, CodecMethod.decode);
      }
      if (methodCache.get(productKey) == null
          || !methodCache.get(productKey).contains(HTTPCodecMethod.decode.name())) {
        return payload;
      }
      MagicScript magicScript = decodeProvider.get(productKey);
      MagicScriptContext context = new MagicScriptContext();
      context.set("payload", payload);
      Object result = magicScript.execute(context);
      return str(result);
    } catch (Exception e) {
      String error = ExceptionUtil.getRootCauseMessage(e);
      log.error("ProductKey={} universal decode解码失败", productKey, e);
      throw new CodecException(error);
    }
  }

  public String encode(String productKey, String payload) throws CodecException {
    try {
      // 检查是否已经加载
      if (!isLoaded(productKey, CodecMethod.encode)) {
        load(productKey, CodecMethod.encode);
      }
      if (methodCache.get(productKey) == null
          || !methodCache.get(productKey).contains(HTTPCodecMethod.encode.name())) {
        return payload;
      }
      MagicScript magicScript = encodeProvider.get(productKey);
      MagicScriptContext context = new MagicScriptContext();
      context.set("payload", payload);
      Object result = magicScript.execute(context);
      return str(result);
    } catch (Exception e) {
      String error = ExceptionUtil.getRootCauseMessage(e);
      log.error("ProductKey={} universal encode编码失败", productKey, e);
      throw new CodecException(error);
    }
  }

  @Override
  public void remove(String productKey) {
    if (StrUtil.isNotBlank(productKey)) {
      addProvider.remove(productKey);
      updateProvider.remove(productKey);
      queryProvider.remove(productKey);
      deleteProvider.remove(productKey);
      iotToYourProvider.remove(productKey);
      yourToIotProvider.remove(productKey);
      encodeProvider.remove(productKey);
      decodeProvider.remove(productKey);
      methodCache.remove(productKey);
    }
  }

  @Override
  public boolean isLoaded(String productKey) {
    if (StrUtil.isBlank(productKey)) {
      return false;
    }
    return methodCache.containsKey(productKey);
  }

  @Override
  public String iotToYour(ProtocolEncodeRequest encodeRequest) throws CodecException {
    return iotToYour(encodeRequest.getDefinition().getId(), encodeRequest.getPayload());
  }

  @Override
  public String yourToIot(ProtocolDecodeRequest decodeRequest) throws CodecException {
    return yourToIot(decodeRequest.getDefinition().getId(), decodeRequest.getPayload());
  }
}
