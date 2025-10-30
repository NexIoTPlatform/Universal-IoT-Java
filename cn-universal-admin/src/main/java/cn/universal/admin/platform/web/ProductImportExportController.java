/*
 *
 * Copyright (c) 2025, NexIoT. All Rights Reserved.
 *
 * @Description: 本文件由 gitee.com/NexIoT 开发并拥有版权，未经授权严禁擅自商用、复制或传播。
 * @Author: gitee.com/NexIoT
 * @Email: wo8335224@gmail.com
 * @Wechat: outlookFil
 *
 *
 */

package cn.universal.admin.platform.web;

import cn.hutool.json.JSONUtil;
import cn.universal.admin.platform.service.IIoTProductService;
import cn.universal.common.annotation.Log;
import cn.universal.common.enums.BusinessType;
import cn.universal.common.exception.IoTException;
import cn.universal.persistence.entity.IoTUser;
import cn.universal.persistence.entity.dto.ProductExportPackageDTO;
import cn.universal.persistence.query.AjaxResult;
import cn.universal.persistence.query.IoTProductQuery;
import cn.universal.security.BaseController;
import cn.universal.security.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 产品导入导出控制器 专门处理产品的完整导出导入功能
 *
 * @author NexIoT
 * @since 2025-10-27
 */
@Slf4j
@Tag(name = "产品导入导出管理", description = "产品完整配置的导入导出接口")
@RestController
@RequestMapping("/admin/v1/product/transfer")
public class ProductImportExportController extends BaseController {

  @Autowired private IIoTProductService devProductService;

  /** 导出产品完整包 包含产品基本信息、设备协议、网络配置、物模型等所有配置 导出后可直接在其他环境导入使用，无需额外配置 */
  @PostMapping("/export")
  @Operation(summary = "导出产品完整包", description = "导出产品及其关联的协议、网络配置、物模型等完整信息")
  @Log(title = "产品完整包导出", businessType = BusinessType.EXPORT)
  public void exportProductPackages(HttpServletResponse response, IoTProductQuery query) {
    try {
      // 获取当前用户信息
      IoTUser iotUser = loginIoTUnionUser(SecurityUtils.getUnionId());

      // 权限控制：非管理员只能导出自己创建的产品
      query.setSelf(!iotUser.isAdmin());
      if (query.isSelf()) {
        query.setCreatorId(iotUser.getUnionId());
      }
      if (query.isHasDevice()) {
        query.setCreatorId(iotUser.getUnionId());
      }

      // 执行导出
      List<ProductExportPackageDTO> packageList = devProductService.exportProductPackages(query);

      // 设置响应头
      response.setContentType("application/json");
      response.setCharacterEncoding("utf-8");

      // 生成文件名：产品导出包_时间戳.json
      String fileName = String.format("product_packages_%d.json", System.currentTimeMillis());
      response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

      // 缓存控制
      response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
      response.setHeader("Pragma", "no-cache");
      response.setHeader("Expires", "0");
      response.setHeader("X-Content-Type-Options", "nosniff");
      response.setHeader("X-Download-Options", "noopen");
      response.setHeader("Content-Encoding", "identity");

      // 转换为JSON并写入响应
      String jsonData = JSONUtil.toJsonPrettyStr(packageList);
      byte[] bytes = jsonData.getBytes("UTF-8");
      response.setContentLength(bytes.length);

      try (java.io.OutputStream out = response.getOutputStream()) {
        out.write(bytes);
        out.flush();
      }

      log.info("产品导出成功: 用户={}, 导出数量={}", iotUser.getUnionId(), packageList.size());
    } catch (Exception e) {
      log.error("产品导出失败", e);
      throw new IoTException("导出失败：" + e.getMessage());
    }
  }

  /** 导入产品完整包 支持导入完整的产品配置，包括协议、网络、物模型等 自动处理ProductKey冲突、协议依赖等问题 */
  @PostMapping("/import")
  @Operation(summary = "导入产品完整包", description = "导入产品完整配置包，自动处理所有依赖关系")
  @Log(title = "产品完整包导入", businessType = BusinessType.IMPORT)
  @Transactional(rollbackFor = Exception.class)
  public AjaxResult<Void> importProductPackages(MultipartFile file) {
    try {
      // 获取当前用户ID
      String unionId = loginIoTUnionUser(SecurityUtils.getUnionId()).getUnionId();

      // 读取上传的JSON文件
      String jsonContent = new String(file.getBytes(), "UTF-8");

      // 解析为产品包列表
      List<ProductExportPackageDTO> packages =
          JSONUtil.toList(jsonContent, ProductExportPackageDTO.class);

      if (packages == null || packages.isEmpty()) {
        return AjaxResult.error("导入文件为空或格式不正确");
      }

      // 执行导入
      String result = devProductService.importProductPackages(packages, unionId);

      log.info("产品导入完成: 用户={}, 导入数量={}", unionId, packages.size());
      return AjaxResult.success(result);
    } catch (Exception e) {
      log.error("产品导入失败", e);
      return AjaxResult.error("导入失败：" + e.getMessage());
    }
  }
}
