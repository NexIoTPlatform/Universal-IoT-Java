/*
 *
 * Copyright (c) 2025, IoT-Universal. All Rights Reserved.
 *
 * @Description: 本文件由 gitee.com/NexIoT 开发并拥有版权，未经授权严禁擅自商用、复制或传播。
 * @Author: gitee.com/NexIoT

 * @Wechat: outlookFil
 *
 *
 */

package cn.universal.rocketmq;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Role;

@Configuration
@Slf4j
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class RocketMQAdminToolConfig {

  @Value("${rocketmq.name-server}")
  private String nameServer; // RocketMQ的Nameserver地址

  //
  //  @Value("{rocketmq.producer.group:iot_group}")
  //  private String defaultMQProduct;
  //
  //  @Bean
  //  public RocketMQTemplate rocketMQTemplate() {
  //    RocketMQTemplate template = new RocketMQTemplate();
  //    DefaultMQProducer producer = new DefaultMQProducer(defaultMQProduct);
  //    producer.setNamesrvAddr(nameServer);
  //    template.setProducer(producer);
  //    return template;
  //  }

  @Bean
  @Lazy
  public DefaultMQAdminExt rocketMQAdmin() throws MQClientException {
    DefaultMQAdminExt admin = new DefaultMQAdminExt();
    admin.setNamesrvAddr(nameServer);
    admin.start();
    return admin;
  }
}
