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

package cn.universal.core.engine.convert;

import cn.universal.core.engine.runtime.Variables;
import cn.universal.core.engine.runtime.function.MagicScriptLambdaFunction;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.function.Function;

/** 脚本内部lambda到Java函数式的转换 */
public class FunctionalImplicitConvert implements ClassImplicitConvert {

  private final ClassLoader classLoader = FunctionalImplicitConvert.class.getClassLoader();

  @Override
  public boolean support(Class<?> from, Class<?> to) {
    return MagicScriptLambdaFunction.class.isAssignableFrom(from)
        && to.getAnnotation(FunctionalInterface.class) != null;
  }

  @Override
  public Object convert(Variables variables, Object source, Class<?> target) {
    MagicScriptLambdaFunction function = (MagicScriptLambdaFunction) source;
    if (target == Function.class) {
      return (Function<Object, Object>)
          args -> {
            Object[] param;
            if (args == null) {
              param = new Object[0];
            } else {
              Class<?> aClass = args.getClass();
              if (aClass.isArray() && aClass.getComponentType() == Object.class) {
                param = (Object[]) args;
              } else {
                param = new Object[] {args};
              }
            }
            return function.apply(variables, param);
          };
    }
    return Proxy.newProxyInstance(
        classLoader,
        new Class[] {target},
        (proxy, method, args) -> {
          if (Modifier.isAbstract(method.getModifiers())) {
            return function.apply(variables, args);
          }
          if ("toString".equalsIgnoreCase(method.getName())) {
            return "Proxy(" + source + "," + target + ")";
          }
          return null;
        });
  }
}
