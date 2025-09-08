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

package cn.universal.plugins.protocolapi.extend;

import cn.hutool.core.collection.CollUtil;
import cn.universal.core.engine.reflection.JavaReflection;
import java.util.Map;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class FunctionRegistrar implements ApplicationContextAware {

  @Override
  public void setApplicationContext(ApplicationContext context) {
    Map<String, IdeMagicFunction> beansOfType = context.getBeansOfType(IdeMagicFunction.class);
    if (CollUtil.isNotEmpty(beansOfType)) {
      beansOfType.forEach((key, value) -> JavaReflection.registerFunction(value));
    }
  }
}
