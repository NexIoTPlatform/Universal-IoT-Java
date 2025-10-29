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

package cn.universal.core.protocol.jar;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import cn.universal.common.exception.CodecException;
import cn.universal.core.protocol.request.ProtocolDecodeRequest;
import cn.universal.core.protocol.request.ProtocolEncodeRequest;
import cn.universal.core.protocol.support.ProtocolCodecLoader;
import cn.universal.core.protocol.support.ProtocolCodecSupport;
import cn.universal.core.protocol.support.ProtocolCodecSupportWrapper;
import cn.universal.core.protocol.support.ProtocolCodecWrapper;
import cn.universal.core.protocol.support.ProtocolSupportDefinition;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;

/**
 * jar加解密插件支持，动态加载jar包
 *
 * @author gitee.com/NexIoT
 * @version 1.0
 * @since 2025/8/9 19:28
 */
@Slf4j
public class ProtocolCodecJar extends ProtocolCodecSupportWrapper
    implements ProtocolCodecLoader, ProtocolCodecSupport, ProtocolCodecWrapper {

  private final Map<String, ProtocolJarClassLoader> protocolLoaders = new ConcurrentHashMap();
  private final Map<String, Object> codecJarProvider = new ConcurrentHashMap();

  private ProtocolCodecJar() {
    // 已移除 beforeSecurity()，兼容 JDK 17+
  }

  private static class ProtocolCodecJarProviderHolder {

    private static ProtocolCodecJar INSTANCE = new ProtocolCodecJar();
  }

  public static ProtocolCodecJar getInstance() {
    return ProtocolCodecJarProviderHolder.INSTANCE;
  }

  @Override
  public String getProviderType() {
    return "jar";
  }

  ProtocolJarClassLoader createClassLoader(URL localtion) {
    return new ProtocolJarClassLoader(new URL[] {localtion}, this.getClass().getClassLoader());
  }

  @Override
  public void load(ProtocolSupportDefinition definition) throws CodecException {
    synchronized (this) {
      try {
        Map<String, Object> config = definition.getConfiguration();
        // 获取位置
        String location =
            (String)
                Optional.ofNullable(config.get("location"))
                    .map(String::valueOf)
                    .orElseThrow(
                        () -> {
                          return new IllegalArgumentException("location");
                        });
        // 获取提供者
        String provider =
            (String)
                Optional.ofNullable(config.get("provider"))
                    .map(String::valueOf)
                    .map(String::trim)
                    .orElseThrow(
                        () -> {
                          return new IllegalArgumentException("provider");
                        });

        if (location.contains("local")) {
          // 本地解码库，直接过滤
          return;
        }
        URL url;
        if (!location.contains("://")) {
          url = (new File(location)).toURI().toURL();
        } else {
          url = new URL("jar:" + location + "!/");
        }

        // 卸载老的加载器
        codecJarProvider.remove(provider);
        protocolLoaders.remove(provider);
        ProtocolJarClassLoader loader = createClassLoader(url);
        protocolLoaders.put(provider, loader);
        log.info("load protocol support from : {}", location);
        if (provider != null) {
          Object newCodecJarProvider = Class.forName(provider, true, loader).newInstance();

          codecJarProvider.put(provider, newCodecJarProvider);
        }
      } catch (Exception e) {
        String error = ExceptionUtil.getRootCauseMessage(e);
        log.error("加载jar编解码出错={}", e);
        throw new CodecException(error);
      }
    }
  }

  @Override
  public String decode(ProtocolDecodeRequest decodeRequest) throws CodecException {
    try {
      if (!codecJarProvider.containsKey(decodeRequest.getDefinition().getProvider())) {
        load(decodeRequest.getDefinition());
      }
      Object clz = codecJarProvider.get(decodeRequest.getDefinition().getProvider());
      Method method = clz.getClass().getDeclaredMethod(CodecMethod.decode.name(), String.class);
      // jar 未实现 decode 方法，直接返回
      if (method == null) {
        return decodeRequest.getPayload();
      }
      Object result = method.invoke(clz, decodeRequest.getPayload());
      return str(result);
    } catch (Exception e) {
      String error = ExceptionUtil.getRootCauseMessage(e);
      log.error(
          "产品型号={} 提供者={} 解码失败={}",
          decodeRequest.getDefinition().getId(),
          decodeRequest.getDefinition().getProvider(),
          e);
      throw new CodecException(error);
    }
  }

  @Override
  public String encode(ProtocolEncodeRequest encodeRequest) throws CodecException {
    if (!codecJarProvider.containsKey(encodeRequest.getDefinition().getProvider())) {
      load(encodeRequest.getDefinition());
    }
    Object clz = codecJarProvider.get(encodeRequest.getDefinition().getProvider());
    try {
      Method method = clz.getClass().getDeclaredMethod(CodecMethod.encode.name(), String.class);
      // jar 未实现 encode 方法，直接返回
      if (method == null) {
        return encodeRequest.getPayload();
      }
      Object result = method.invoke(clz, encodeRequest.getPayload());
      return str(result);
    } catch (Exception e) {
      String error = ExceptionUtil.getRootCauseMessage(e);
      log.error(
          "产品型号={} 提供者={} 编码失败={}",
          encodeRequest.getDefinition().getId(),
          encodeRequest.getDefinition().getProvider(),
          error);
      throw new CodecException(error);
    }
  }

  @Override
  public String preDecode(ProtocolDecodeRequest protocolDecodeRequest) throws CodecException {
    if (!codecJarProvider.containsKey(protocolDecodeRequest.getDefinition().getProvider())) {
      load(protocolDecodeRequest.getDefinition());
    }
    Object clz = codecJarProvider.get(protocolDecodeRequest.getDefinition().getProvider());
    try {
      Method method = clz.getClass().getDeclaredMethod(CodecMethod.preDecode.name(), String.class);
      // jar 未实现 preDecode 方法，直接返回
      if (method == null) {
        return protocolDecodeRequest.getPayload();
      }
      Object result = method.invoke(clz, protocolDecodeRequest.getPayload());
      return str(result);
    } catch (Exception e) {
      String error = ExceptionUtil.getRootCauseMessage(e);
      log.error(
          "产品型号={} 提供者={} 预编码失败={}",
          protocolDecodeRequest.getDefinition().getId(),
          protocolDecodeRequest.getDefinition().getProvider(),
          error);
      throw new CodecException(error);
    }
  }

  @Override
  public void load(String provider, Object providerImpl) {
    if (StrUtil.isNotBlank(provider) && providerImpl != null) {
      codecJarProvider.put(provider, providerImpl);
    }
  }

  @Override
  public void remove(String provider) {
    if (StrUtil.isNotBlank(provider)) {
      codecJarProvider.remove(provider);
      protocolLoaders.remove(provider);
    }
  }

  // 在ProtocolCodecJar类中添加isLoaded方法
  @Override
  public boolean isLoaded(String provider, CodecMethod codecMethod) {
    // 检查provider对应的是否已加载到缓存中
    if (!codecJarProvider.containsKey(provider)) {
      return false;
    }

    // 检查JAR包中是否实现了对应的方法
    try {
      Object clz = codecJarProvider.get(provider);
      Method method = clz.getClass().getDeclaredMethod(codecMethod.name(), String.class);
      return method != null;
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  public String iotToYour(ProtocolEncodeRequest encodeRequest) throws CodecException {
    try {
      if (!codecJarProvider.containsKey(encodeRequest.getDefinition().getProvider())) {
        load(encodeRequest.getDefinition());
      }
      Object clz = codecJarProvider.get(encodeRequest.getDefinition().getProvider());
      Method method = clz.getClass().getDeclaredMethod(CodecMethod.iotToYour.name(), String.class);
      // jar 未实现 iotToYour 方法，使用encode方法
      if (method == null) {
        return encode(encodeRequest);
      }
      Object result = method.invoke(clz, encodeRequest.getPayload());
      return str(result);
    } catch (Exception e) {
      String error = ExceptionUtil.getRootCauseMessage(e);
      log.error(
          "产品型号={} 提供者={} iotToYour失败={}",
          encodeRequest.getDefinition().getId(),
          encodeRequest.getDefinition().getProvider(),
          e);
      throw new CodecException(error);
    }
  }

  @Override
  public String yourToIot(ProtocolDecodeRequest decodeRequest) throws CodecException {
    try {
      if (!codecJarProvider.containsKey(decodeRequest.getDefinition().getProvider())) {
        load(decodeRequest.getDefinition());
      }
      Object clz = codecJarProvider.get(decodeRequest.getDefinition().getProvider());
      Method method = clz.getClass().getDeclaredMethod(CodecMethod.yourToIot.name(), String.class);
      // jar 未实现 yourToIot 方法，使用decode方法
      if (method == null) {
        return decode(decodeRequest);
      }
      Object result = method.invoke(clz, decodeRequest.getPayload());
      return str(result);
    } catch (Exception e) {
      String error = ExceptionUtil.getRootCauseMessage(e);
      log.error(
          "产品型号={} 提供者={} yourToIot失败={}",
          decodeRequest.getDefinition().getId(),
          decodeRequest.getDefinition().getProvider(),
          e);
      throw new CodecException(error);
    }
  }
}
