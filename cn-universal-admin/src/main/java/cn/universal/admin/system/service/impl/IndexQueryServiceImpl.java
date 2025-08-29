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

package cn.universal.admin.system.service.impl;

import cn.universal.admin.system.service.IndexQueryService;
import cn.universal.persistence.dto.IndexQueryDTO;
import cn.universal.persistence.entity.IoTDevice;
import cn.universal.persistence.entity.IoTProduct;
import cn.universal.persistence.entity.IoTUserApplication;
import cn.universal.persistence.entity.RuleModel;
import cn.universal.persistence.mapper.IoTDeviceMapper;
import cn.universal.persistence.mapper.IoTProductMapper;
import cn.universal.persistence.mapper.IoTUserApplicationMapper;
import cn.universal.persistence.mapper.RuleModelMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/** 首页统计 */
@Service
public class IndexQueryServiceImpl implements IndexQueryService {

  @Resource private IoTDeviceMapper ioTDeviceMapper;

  @Resource private RuleModelMapper ruleModelMapper;

  @Resource private IoTProductMapper ioTProductMapper;

  @Resource private IoTUserApplicationMapper iotUserApplicationMapper;

  @Override
  public IndexQueryDTO queryIndexQty(String creator) {
    IndexQueryDTO qty = new IndexQueryDTO();
    IoTProduct product = new IoTProduct();
    product.setCreatorId(creator);
    qty.setProduct(ioTProductMapper.selectCount(product));

    RuleModel ruleModel = new RuleModel();
    ruleModel.setCreatorId(creator);
    qty.setRule(ruleModelMapper.selectCount(ruleModel));

    IoTUserApplication userApplication = new IoTUserApplication();
    userApplication.setUnionId(creator);
    userApplication.setDeleted(0);
    qty.setApps(iotUserApplicationMapper.selectCount(userApplication));

    IoTDevice ioTDevice = new IoTDevice();
    ioTDevice.setCreatorId(creator);
    qty.setDevice(ioTDeviceMapper.selectCount(ioTDevice));
    ioTDevice.setState(true);
    qty.setOnline(ioTDeviceMapper.selectCount(ioTDevice));
    qty.setOffline(qty.getDevice() - qty.getOnline());

    return qty;
  }
}
