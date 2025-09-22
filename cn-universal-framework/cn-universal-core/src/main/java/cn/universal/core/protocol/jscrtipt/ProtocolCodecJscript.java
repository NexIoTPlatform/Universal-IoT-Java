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

package cn.universal.core.protocol.jscrtipt;

import cn.hutool.core.codec.Base64Decoder;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.universal.common.exception.CodecException;
import cn.universal.common.exception.IoTException;
import cn.universal.core.protocol.request.ProtocolDecodeRequest;
import cn.universal.core.protocol.request.ProtocolEncodeRequest;
import cn.universal.core.protocol.support.ProtocolCodecLoader;
import cn.universal.core.protocol.support.ProtocolCodecSupport;
import cn.universal.core.protocol.support.ProtocolCodecSupportWrapper;
import cn.universal.core.protocol.support.ProtocolCodecWrapper;
import cn.universal.core.protocol.support.ProtocolSupportDefinition;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleScriptContext;
import lombok.extern.slf4j.Slf4j;

/**
 * jar加解密插件支持，动态加载jar包
 *
 * @version 1.0 @Author Aleo
 * @since 2025/8/9 19:28
 */
@Slf4j
public class ProtocolCodecJscript extends ProtocolCodecSupportWrapper
    implements ProtocolCodecLoader, ProtocolCodecSupport, ProtocolCodecWrapper {

  private final Map<String, Invocable> codecJscriptProvider = new ConcurrentHashMap();

  private final Map<String, Set<String>> methodCache = new ConcurrentHashMap<>();

  private final ScriptEngineManager factory = new ScriptEngineManager();

  private ProtocolCodecJscript() {}

  private static class ProtocolCodecJscriptProviderHoler {

    private static ProtocolCodecJscript INSTANCE = new ProtocolCodecJscript();
  }

  public static ProtocolCodecJscript getInstance() {
    return ProtocolCodecJscriptProviderHoler.INSTANCE;
  }

  @Override
  public String getProviderType() {
    return "jscript";
  }

  @Override
  public void load(ProtocolSupportDefinition definition) throws CodecException {
    try {
      Map<String, Object> config = definition.getConfiguration();
      // JS原始代码
      String location =
          (String)
              Optional.ofNullable(config.get("location"))
                  .map(String::valueOf)
                  .orElseThrow(
                      () -> {
                        return new IoTException(
                            "javascript source code not exist, can not do encode or decode ");
                      });
      // 获取提供者,产品唯一
      String provider =
          (String)
              Optional.ofNullable(config.get("provider"))
                  .map(String::valueOf)
                  .map(String::trim)
                  .orElseThrow(
                      () -> {
                        return new IllegalArgumentException("provider");
                      });
      // 初始化 - 使用 GraalJS 替代 Nashorn
      ScriptEngine engine = factory.getEngineByName("graal.js");
      if (engine == null) {
        // 如果 GraalJS 不可用，尝试使用 JavaScript 引擎
        engine = factory.getEngineByName("JavaScript");
      }
      if (engine == null) {
        throw new CodecException(
            "No JavaScript engine available. Please ensure GraalJS is in classpath.");
      }
      // 加载javascript es5的语法
      if (location.startsWith("http://") || location.startsWith("https://")) {
        engine.eval(HttpUtil.downloadString(location, Charset.defaultCharset()));
      } else {
        engine.eval(location);
      }
      // 处理内部实现了哪些方法
      evalMethodCache(definition, engine);
      Invocable invocable = (Invocable) engine;
      codecJscriptProvider.put(provider, invocable);
    } catch (Exception e) {
      String error = ExceptionUtil.getMessage(e);
      log.error("加载jscript编解码出错={}", error);
      throw new CodecException(error);
    }
  }

  private void evalMethodCache(ProtocolSupportDefinition definition, ScriptEngine engine) {
    SimpleScriptContext scriptContext = (SimpleScriptContext) engine.getContext();
    List<Integer> scopes = scriptContext.getScopes();
    Set<String> methods = new HashSet<>();
    for (Integer ints : scopes) {
      Bindings bindings = scriptContext.getBindings(ints);
      if (MapUtil.isNotEmpty(bindings)) {
        bindings.forEach(
            (k, v) -> {
              methods.add(k);
            });
      }
    }
    methodCache.put(definition.getId(), methods);
  }

  @Override
  public String decode(ProtocolDecodeRequest decodeRequest) throws CodecException {
    if (!codecJscriptProvider.containsKey(decodeRequest.getDefinition().getProvider())) {
      load(decodeRequest.getDefinition());
    }
    // 如果编解码内部不包含decode方法，则直接返回原串
    if (methodCache.get(decodeRequest.getDefinition().getId()) == null
        || !methodCache
            .get(decodeRequest.getDefinition().getId())
            .contains(CodecMethod.decode.name())) {
      return decodeRequest.getPayload();
    }
    Invocable invocable = codecJscriptProvider.get(decodeRequest.getDefinition().getProvider());
    try {
      Boolean needBs4Decode =
          (Boolean)
              decodeRequest.getDefinition().getConfiguration().getOrDefault("needBs4Decode", false);
      String payload = decodeRequest.getPayload();
      // 发现是Bse64，进行解码且做16进制转换
      // 这块的存在是有争议，理论是有编解码实现
      if (needBs4Decode) {
        payload = HexUtil.encodeHexStr(Base64Decoder.decode(payload));
      }
      Object result =
          invocable.invokeFunction(CodecMethod.decode.name(), payload, decodeRequest.getContext());
      return str(result);
    } catch (Exception e) {
      String error = ExceptionUtil.getMessage(e);
      log.info("设备 js 解码 payload = {}", decodeRequest.getPayload());
      log.error(
          "产品型号={} 提供者={} Jscript解码失败={}",
          decodeRequest.getDefinition().getId(),
          decodeRequest.getDefinition().getProvider(),
          e);
      throw new CodecException(error);
    }
  }

  @Override
  public String encode(ProtocolEncodeRequest encodeRequest) throws CodecException {
    if (!codecJscriptProvider.containsKey(encodeRequest.getDefinition().getProvider())) {
      load(encodeRequest.getDefinition());
    }
    // 如果编解码内部不包含 encode 方法，则直接返回原串
    if (methodCache.get(encodeRequest.getDefinition().getId()) == null
        || !methodCache
            .get(encodeRequest.getDefinition().getId())
            .contains(CodecMethod.encode.name())) {
      return encodeRequest.getPayload();
    }
    Invocable invocable = codecJscriptProvider.get(encodeRequest.getDefinition().getProvider());
    try {
      Object result =
          invocable.invokeFunction(
              CodecMethod.encode.name(), encodeRequest.getPayload(), encodeRequest.getContext());
      return str(result);
    } catch (Exception e) {
      String error = ExceptionUtil.getMessage(e);
      log.error(
          "产品型号={} 提供者={} Jscript编码失败={}",
          encodeRequest.getDefinition().getId(),
          encodeRequest.getDefinition().getProvider(),
          e);
      throw new CodecException(error);
    }
  }

  @Override
  public void remove(String provider) {
    if (StrUtil.isNotBlank(provider)) {
      codecJscriptProvider.remove(provider);
    }
  }

  @Override
  public void load(String provider, Object providerImpl) {}

  @Override
  public String preDecode(ProtocolDecodeRequest protocolDecodeRequest) throws CodecException {
    if (!codecJscriptProvider.containsKey(protocolDecodeRequest.getDefinition().getProvider())) {
      load(protocolDecodeRequest.getDefinition());
    }
    // 如果编解码内部不包含 preDecode 方法，则直接返回原串
    if (methodCache.get(protocolDecodeRequest.getDefinition().getId()) == null
        || !methodCache
            .get(protocolDecodeRequest.getDefinition().getId())
            .contains(CodecMethod.preDecode.name())) {
      return protocolDecodeRequest.getPayload();
    }
    Invocable invocable =
        codecJscriptProvider.get(protocolDecodeRequest.getDefinition().getProvider());
    try {
      Object result =
          invocable.invokeFunction(
              CodecMethod.preDecode.name(), protocolDecodeRequest.getPayload());
      return str(result);
    } catch (Exception e) {
      String error = ExceptionUtil.getMessage(e);
      log.error(
          "产品型号={} 提供者={} Jscript预编码失败={}",
          protocolDecodeRequest.getDefinition().getId(),
          protocolDecodeRequest.getDefinition().getProvider(),
          e);
      throw new CodecException(error);
    }
  }

  // 在ProtocolCodecJscript类中实现isLoaded方法
  @Override
  public boolean isLoaded(String provider, CodecMethod codecMethod) {
    // 检查provider是否是否已加载到缓存中
    if (!codecJscriptProvider.containsKey(provider)) {
      return false;
    }
    // 检查是否是否包含目标方法（可选，根据实际需求决定
    Set<String> methods = methodCache.get(provider);
    if (methods == null) {
      return false;
    }
    return methods.contains(codecMethod.name());
  }

  @Override
  public String iotToYour(ProtocolEncodeRequest encodeRequest) throws CodecException {
    if (!codecJscriptProvider.containsKey(encodeRequest.getDefinition().getProvider())) {
      load(encodeRequest.getDefinition());
    }
    // 如果编解码内部不包含 iotToYour 方法，则使用encode方法
    if (methodCache.get(encodeRequest.getDefinition().getId()) == null
        || !methodCache
            .get(encodeRequest.getDefinition().getId())
            .contains(CodecMethod.iotToYour.name())) {
      return encode(encodeRequest);
    }
    Invocable invocable = codecJscriptProvider.get(encodeRequest.getDefinition().getProvider());
    try {
      Object result =
          invocable.invokeFunction(
              CodecMethod.iotToYour.name(), encodeRequest.getPayload(), encodeRequest.getContext());
      return str(result);
    } catch (Exception e) {
      String error = ExceptionUtil.getMessage(e);
      log.error(
          "产品型号={} 提供者={} Jscript iotToYour失败={}",
          encodeRequest.getDefinition().getId(),
          encodeRequest.getDefinition().getProvider(),
          e);
      throw new CodecException(error);
    }
  }

  @Override
  public String yourToIot(ProtocolDecodeRequest decodeRequest) throws CodecException {
    if (!codecJscriptProvider.containsKey(decodeRequest.getDefinition().getProvider())) {
      load(decodeRequest.getDefinition());
    }
    // 如果编解码内部不包含 yourToIot 方法，则使用decode方法
    if (methodCache.get(decodeRequest.getDefinition().getId()) == null
        || !methodCache
            .get(decodeRequest.getDefinition().getId())
            .contains(CodecMethod.yourToIot.name())) {
      return decode(decodeRequest);
    }
    Invocable invocable = codecJscriptProvider.get(decodeRequest.getDefinition().getProvider());
    try {
      Object result =
          invocable.invokeFunction(
              CodecMethod.yourToIot.name(), decodeRequest.getPayload(), decodeRequest.getContext());
      return str(result);
    } catch (Exception e) {
      String error = ExceptionUtil.getMessage(e);
      log.error(
          "产品型号={} 提供者={} Jscript yourToIot失败={}",
          decodeRequest.getDefinition().getId(),
          decodeRequest.getDefinition().getProvider(),
          e);
      throw new CodecException(error);
    }
  }
}
