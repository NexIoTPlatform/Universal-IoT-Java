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

package cn.universal.web.context; // package cn.universal.context;
//
// import cn.universal.core.third.IDown;
// import cn.universal.core.third.IUP;
// import java.util.HashMap;
// import java.util.Map;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.beans.BeansException;
// import org.springframework.context.ApplicationContext;
// import org.springframework.context.ApplicationContextAware;
// import org.springframework.stereotype.Component;
//
/// **
// * 多实现路由
// *
// * @Author Aleo
// * @version 1.0
// * @since 2025/8/12 19:11
// */
// @Component
// @Slf4j
// public class IoTDownlFactory implements ApplicationContextAware {
//
//  private static Map<String, IDown> iDownMap = new HashMap<>();
//  private static Map<String, IUP> iupMap = new HashMap<>();
//
//  @Override
//  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//    //处理下行
//    Map<String, IDown> downMap = applicationContext.getBeansOfType(IDown.class);
//    downMap.forEach((key, value) -> iDownMap.put(value.name(), value));
//    log.info("init IDown third ,[{}]", iDownMap);
//    //处理上行
//    Map<String, IUP> upMap = applicationContext.getBeansOfType(IUP.class);
//    upMap.forEach((key, value) -> iupMap.put(value.name(), value));
//    log.info("init IUP third ,[{}]", iupMap);
//
//  }
//
//  public static <T extends IDown> T getIDown(String code) {
//    return (T) iDownMap.get(code);
//  }
//
//  public static <T extends IDown> T getIUP(String code) {
//    return (T) iupMap.get(code);
//  }
// }
