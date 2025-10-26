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

package cn.universal.dm.device.service.wrapper;

import cn.hutool.core.util.StrUtil;
import cn.universal.common.constant.IoTConstant.DeviceNode;
import cn.universal.common.constant.IoTConstant.DownCmd;
import cn.universal.common.domain.R;
import cn.universal.core.message.DownRequest;
import cn.universal.dm.device.service.impl.IoTProductDeviceService;
import cn.universal.persistence.base.IoTDownWrapper;
import cn.universal.persistence.entity.IoTProduct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** 子设备逻辑校验 */
@Service("ioTSubDeviceAddIntercept")
@Slf4j
public class IoTSubDeviceAddIntercept implements IoTDownWrapper {

  @Resource private IoTProductDeviceService iotProductDeviceService;

  @Override
  public R beforeDownAction(IoTProduct product, Object data, DownRequest downRequest) {
    /*返回的R不为空，会影响后续流程，此处只做参数的补充*/
    if (product == null) {
      return null;
    }
    if (downRequest.getCmd() == null) {
      return null;
    }
    if (DownCmd.DEV_ADD.equals(downRequest.getCmd())
        || DownCmd.DEV_ADDS.equals(downRequest.getCmd())) {
      return doGwSubDeviceAdd(product, downRequest);
    }

    return null;
  }

  private R doGwSubDeviceAdd(IoTProduct product, DownRequest downRequest) {
    if (!DeviceNode.GATEWAY_SUB_DEVICE.name().equals(product.getDeviceNode())) {
      return null;
    }
    // 如果是网关子设备，则判断是否绑定了网关
    if (StrUtil.isNotBlank(product.getGwProductKey())) {
      downRequest.setGwProductKey(product.getGwProductKey());
    } else {
      return R.error("添加网关子设备,请先绑定网关");
    }
    return null;
  }

  private void doAddAction() {}
}
