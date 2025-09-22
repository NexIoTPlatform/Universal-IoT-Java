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

package cn.universal.web.context;

import com.alibaba.ttl.TransmittableThreadLocal;

public class TtlAuthContextHolder {

  private TransmittableThreadLocal threadLocal = new TransmittableThreadLocal();
  private static final TtlAuthContextHolder instance = new TtlAuthContextHolder();

  private TtlAuthContextHolder() {}

  public static TtlAuthContextHolder getInstance() {
    return instance;
  }

  public void setContext(Object t) {
    this.threadLocal.set(t);
  }

  public String getContext() {
    return (String) this.threadLocal.get();
  }

  public void clear() {
    this.threadLocal.remove();
  }
}
