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

import cn.universal.admin.platform.service.IGatewayPollingService;
import cn.universal.common.domain.R;
import cn.universal.persistence.dto.GatewayPollingConfigDTO;
import cn.universal.security.BaseController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 网关轮询配置控制器
 * 
 * @author Aleo
 * @date 2025-10-26
 */
@RestController
@RequestMapping("/admin/v1/gateway/polling")
@Tag(name = "网关轮询管理", description = "网关云端轮询配置管理接口")
public class GatewayPollingController extends BaseController {

  @Autowired
  private IGatewayPollingService gatewayPollingService;

  /**
   * 保存网关轮询配置
   */
  @Operation(summary = "保存轮询配置", description = "保存或更新网关轮询配置及指令")
  @PostMapping("/save")
  @PreAuthorize("@ss.hasPermi('platform:device:edit')")
  public R saveConfig(@RequestBody GatewayPollingConfigDTO dto) {
    return gatewayPollingService.savePollingConfig(dto);
  }

  /**
   * 获取网关轮询配置
   */
  @Operation(summary = "获取轮询配置", description = "根据产品Key和设备ID获取轮询配置")
  @GetMapping("/get/{productKey}/{deviceId}")
  @PreAuthorize("@ss.hasPermi('platform:device:query')")
  public R getConfig(
      @Parameter(description = "产品Key", required = true)
      @PathVariable String productKey,
      @Parameter(description = "设备ID", required = true)
      @PathVariable String deviceId) {
    return gatewayPollingService.getPollingConfig(productKey, deviceId);
  }

  /**
   * 删除网关轮询配置
   */
  @Operation(summary = "删除轮询配置", description = "删除指定设备的轮询配置")
  @DeleteMapping("/delete/{productKey}/{deviceId}")
  @PreAuthorize("@ss.hasPermi('platform:device:remove')")
  public R deleteConfig(
      @Parameter(description = "产品Key", required = true)
      @PathVariable String productKey,
      @Parameter(description = "设备ID", required = true)
      @PathVariable String deviceId) {
    return gatewayPollingService.deletePollingConfig(productKey, deviceId);
  }

  /**
   * 测试轮询指令
   */
  @Operation(summary = "测试轮询指令", description = "立即执行一次轮询测试")
  @PostMapping("/test/{productKey}/{deviceId}")
  @PreAuthorize("@ss.hasPermi('platform:device:edit')")
  public R testPolling(
      @Parameter(description = "产品Key", required = true)
      @PathVariable String productKey,
      @Parameter(description = "设备ID", required = true)
      @PathVariable String deviceId) {
    return gatewayPollingService.testPolling(productKey, deviceId);
  }
}
