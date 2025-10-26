/// *
// *
// * Copyright (c) 2025, IoT-Universal. All Rights Reserved.
// *
// * @Description: 本文件由 gitee.com/NexIoT 开发并拥有版权，未经授权严禁擅自商用、复制或传播。
// * @Author: gitee.com/NexIoT
//
// * @Wechat: outlookFil
// *
// *
// */
//
// package cn.universal.web.config;
//
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.fasterxml.jackson.databind.SerializationFeature;
// import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
// import java.util.List;
// import org.apache.rocketmq.spring.support.RocketMQMessageConverter;
// import org.springframework.beans.factory.config.BeanDefinition;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.context.annotation.Role;
// import org.springframework.messaging.converter.CompositeMessageConverter;
// import org.springframework.messaging.converter.MappingJackson2MessageConverter;
// import org.springframework.messaging.converter.MessageConverter;
//
// @Configuration
// public class RocketMQEnhanceConfig {
//
//  /** */
//  @Bean
//  @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
//  public static RocketMQMessageConverter enhanceRocketMQMessageConverter() {
//    RocketMQMessageConverter converter = new RocketMQMessageConverter();
//    CompositeMessageConverter compositeMessageConverter =
//        (CompositeMessageConverter) converter.getMessageConverter();
//    List<MessageConverter> messageConverterList = compositeMessageConverter.getConverters();
//    for (MessageConverter messageConverter : messageConverterList) {
//      if (messageConverter instanceof MappingJackson2MessageConverter) {
//        MappingJackson2MessageConverter jackson2MessageConverter =
//            (MappingJackson2MessageConverter) messageConverter;
//        ObjectMapper objectMapper = jackson2MessageConverter.getObjectMapper();
//        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
//        objectMapper.registerModules(new JavaTimeModule());
//      }
//    }
//    return converter;
//  }
// }
