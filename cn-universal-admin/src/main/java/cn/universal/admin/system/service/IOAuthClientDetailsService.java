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

package cn.universal.admin.system.service;

import cn.universal.persistence.entity.OAuth2ClientDetails;

/** OAuth2 */
public interface IOAuthClientDetailsService {

  int insert(OAuth2ClientDetails OAuth2ClientDetails);

  int updateSecret(OAuth2ClientDetails OAuth2ClientDetails);

  int deleteByClientIds(String[] ids);
}
