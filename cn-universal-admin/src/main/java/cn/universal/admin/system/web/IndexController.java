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

package cn.universal.admin.system.web;

import cn.universal.admin.common.utils.SecurityUtils;
import cn.universal.admin.system.service.IndexQueryService;
import cn.universal.persistence.dto.IndexQueryDTO;
import cn.universal.persistence.query.AjaxResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 首页数据接口
 *
 * @since 2023/8/12 16:51
 */
@RestController
@Tag(name = "首页overview", description = "首页overview")
@RequestMapping("admin/index")
@Slf4j
public class IndexController extends BaseController {

  @Resource private IndexQueryService indexQueryService;

  @Operation(summary = "overview统计")
  @GetMapping("/overview")
  public AjaxResult<IndexQueryDTO> index() {
    boolean admin = loginIoTUnionUser(SecurityUtils.getUnionId()).isAdmin();
    IndexQueryDTO qtyDTO =
        indexQueryService.queryIndexQty(admin ? null : SecurityUtils.getUnionId());
    return AjaxResult.success(qtyDTO);
  }
}
