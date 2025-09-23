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

package cn.universal.persistence.entity.admin.vo;

import cn.universal.persistence.entity.admin.SysDictData;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 字典类型group分组 @Author ruoyi */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysDictDataVos implements Serializable {

  private static final long serialVersionUID = 1L;

  private String id;
  private String name;
  private List<SysDictData> array;
}
