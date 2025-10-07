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

package cn.universal.admin.platform.web;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.universal.common.annotation.Log;
import cn.universal.common.enums.BusinessType;
import cn.universal.admin.common.utils.ExcelUtil;
import cn.universal.security.utils.SecurityUtils;
import cn.universal.admin.system.service.IoTDeviceProtocolService;
import cn.universal.admin.system.service.impl.IoTDeviceMessageCodecService;
import cn.universal.security.BaseController;
import cn.universal.common.exception.CodecException;
import cn.universal.common.exception.IoTException;
import cn.universal.ossm.service.ISysOssService;
import cn.universal.persistence.entity.IoTDeviceProtocol;
import cn.universal.persistence.entity.IoTUser;
import cn.universal.persistence.entity.bo.IoTDeviceProtocolBO;
import cn.universal.persistence.entity.vo.IoTProtocolVO;
import cn.universal.persistence.page.TableDataInfo;
import cn.universal.persistence.query.AjaxResult;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 设备协议Controller @Author ruoyi
 *
 * @since 2023-01-06
 */
@RestController
@RequestMapping("/admin/v1/protocol")
public class IoTDeviceProtocolController extends BaseController {

  @Autowired private IoTDeviceProtocolService devProtocolService;
  @Resource private IoTDeviceMessageCodecService ioTDeviceMessageCodecService;
  @Resource private ISysOssService ossService;

  /** 查询设备协议列表 */
  @GetMapping("/list")
  public TableDataInfo list(IoTDeviceProtocol ioTDeviceProtocol) {
    IoTUser iotUser = loginIoTUnionUser(SecurityUtils.getUnionId());
    startPage();
    List<IoTDeviceProtocol> list =
        devProtocolService.selectDevProtocolList(ioTDeviceProtocol, iotUser);
    return getDataTable(list);
  }

  /** 查询设备协议列表 */
  @GetMapping("/list/ids")
  public AjaxResult<List<String>> idList(IoTDeviceProtocol ioTDeviceProtocol) {
    IoTUser iotUser = loginIoTUnionUser(SecurityUtils.getUnionId());
    List<IoTDeviceProtocol> list =
        devProtocolService.selectDevProtocolList(ioTDeviceProtocol, iotUser);
    List<String> list1 = list.stream().map(IoTDeviceProtocol::getId).collect(Collectors.toList());
    return AjaxResult.success(list1);
  }

  /** 导出设备协议列表 */
  @PostMapping("/export")
  @Log(title = "导出设备协议列表", businessType = BusinessType.EXPORT)
  public void export(HttpServletResponse response, IoTDeviceProtocol ioTDeviceProtocol) {
    IoTUser iotUser = loginIoTUnionUser(SecurityUtils.getUnionId());
    List<IoTDeviceProtocol> list =
        devProtocolService.selectDevProtocolList(ioTDeviceProtocol, iotUser);
    ExcelUtil<IoTDeviceProtocol> util = new ExcelUtil<IoTDeviceProtocol>(IoTDeviceProtocol.class);
    util.exportExcel(response, list, "设备协议数据");
  }

  /** 获取设备协议详细信息 */
  @GetMapping(value = "/{id}")
  public AjaxResult getInfo(@PathVariable("id") String id) {
    if (StrUtil.isBlank(id)) {
      return AjaxResult.error("id is null");
    }
    IoTUser iotUser = loginIoTUnionUser(SecurityUtils.getUnionId());
    IoTDeviceProtocol ioTDeviceProtocol =
        devProtocolService.selectDevProtocolById(
            id, iotUser.isAdmin() ? null : iotUser.getUnionId());
    IoTProtocolVO devProductVO = BeanUtil.toBean(ioTDeviceProtocol, IoTProtocolVO.class);
    JSONObject config = JSONUtil.parseObj(ioTDeviceProtocol.getConfiguration());
    if ("jar".equals(ioTDeviceProtocol.getType())) {
      devProductVO.setFileName(config.getStr("provider"));
      devProductVO.setUrl(config.getStr("location"));
    } else {
      devProductVO.setJscript(config.getStr("location"));
      devProductVO.setNeedBs4Decode(config.getStr("needBs4Decode"));
    }

    return AjaxResult.success(devProductVO);
  }

  /** 新增设备协议 */
  @PostMapping
  @Log(title = "新增设备协议", businessType = BusinessType.INSERT)
  public AjaxResult add(@RequestBody IoTDeviceProtocolBO ioTDeviceProtocolBO) {
    if (devProtocolService.selectDevProtocolById(ioTDeviceProtocolBO.getId(), null) != null) {
      throw new IoTException("该产品已存在对应协议！");
    }
    if (devProtocolService.countByProvider(ioTDeviceProtocolBO.getFileName()) > 0) {
      throw new IoTException("该包名已存在，请修改后重试！");
    }
    IoTDeviceProtocol ioTDeviceProtocol =
        BeanUtil.toBean(ioTDeviceProtocolBO, IoTDeviceProtocol.class);
    JSONObject object = new JSONObject();
    if (!"jar".equals(ioTDeviceProtocolBO.getType())) {
      object.set("needBs4Decode", false);
      object.set("location", ioTDeviceProtocolBO.getJscript());
      object.set("provider", ioTDeviceProtocolBO.getId());
    } else {
      ioTDeviceProtocol.setState((byte) 0);
      object.set("provider", ioTDeviceProtocolBO.getFileName());
      object.set("location", ioTDeviceProtocolBO.getUrl());
    }

    ioTDeviceProtocol.setConfiguration(object.toString());
    return toAjax(devProtocolService.insertDevProtocol(ioTDeviceProtocol));
  }

  /** 修改设备协议 */
  @PutMapping
  @Log(title = "修改设备协议", businessType = BusinessType.UPDATE)
  public AjaxResult edit(@RequestBody IoTDeviceProtocolBO ioTDeviceProtocolBO) {
    IoTUser iotUser = loginIoTUnionUser(SecurityUtils.getUnionId());
    return toAjax(devProtocolService.updateDevProtocol(ioTDeviceProtocolBO, iotUser));
  }

  /** 删除设备协议 */
  @DeleteMapping("/{ids}")
  @Log(title = "删除设备协议", businessType = BusinessType.DELETE)
  public AjaxResult remove(@PathVariable String[] ids) {
    IoTDeviceProtocol ioTDeviceProtocol =
        devProtocolService.selectDevProtocolById(
            ids[0], loginIoTUnionUser(SecurityUtils.getUnionId()).getUnionId());
    if (ObjectUtil.isNull(ioTDeviceProtocol)) {
      throw new IoTException("该协议没有操作权限！");
    }
    return toAjax(devProtocolService.deleteDevProtocolByIds(ids));
  }

  /** 编解码测试 */
  @PostMapping("/codec")
  public AjaxResult codec(@RequestBody IoTDeviceProtocolBO ioTDeviceProtocolBO) {

    try {
      if (ioTDeviceProtocolBO.getDecode()) {
        return AjaxResult.success(
            ioTDeviceMessageCodecService.decode(
                ioTDeviceProtocolBO.getId(), ioTDeviceProtocolBO.getPayload()));
      } else {
        return AjaxResult.success(
            ioTDeviceMessageCodecService.encode(
                ioTDeviceProtocolBO.getId(), ioTDeviceProtocolBO.getPayload()));
      }
    } catch (CodecException e) {
      return AjaxResult.success(e.getMsg());
    }
  }
}
