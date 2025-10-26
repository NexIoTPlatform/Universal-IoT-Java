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

package cn.universal.persistence.mapper.admin;

import cn.universal.persistence.common.BaseMapper;
import cn.universal.persistence.entity.admin.SysDictData;
import cn.universal.persistence.entity.admin.vo.SysDictDataVo;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/** 字典表 数据层 @Author ruoyi */
public interface SysDictDataMapper extends BaseMapper<SysDictData> {

  List<SysDictData> selectDictData(SysDictDataVo sysDictData);

  List<SysDictData> selectDictDataByType(@Param("dictType") String dictType);
}
