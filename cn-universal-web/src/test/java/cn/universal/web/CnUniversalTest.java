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

package cn.universal.web;

import cn.universal.CnUniversalIoTApplication;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CnUniversalIoTApplication.class)
@Slf4j
public class CnUniversalTest {

  @Resource private LoggingSystem loggingSystem;

  @Before
  public void before() {
    loggingSystem.setLogLevel("com.tk.mapper", LogLevel.DEBUG);
    loggingSystem.setLogLevel("cn.universal", LogLevel.DEBUG);
    loggingSystem.setLogLevel("cn.hutool", LogLevel.DEBUG);
  }

  @Test
  public void testCount() throws Exception {
    Assert.assertTrue(true);
  }
}
