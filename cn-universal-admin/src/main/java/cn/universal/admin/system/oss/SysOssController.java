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

package cn.universal.admin.system.oss;

import cn.hutool.core.convert.Convert;
import cn.hutool.http.HttpUtil;
import cn.universal.common.exception.IoTException;
import cn.universal.common.utils.FileUtils;
import cn.universal.ossm.entity.SysOss;
import cn.universal.ossm.entity.bo.SysOssBo;
import cn.universal.ossm.service.ISysOssService;
import cn.universal.persistence.page.TableDataInfo;
import cn.universal.persistence.query.AjaxResult;
import cn.universal.security.BaseController;
import cn.universal.security.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** 文件上传 控制层 @Author Lion Li */
@Validated
@Tag(name = "OSS云存储控制器", description = "OSS云存储管理")
@RestController
@RequestMapping("/admin/system/oss")
public class SysOssController extends BaseController {

  @Resource private ISysOssService iSysOssService;

  @Value("${codec.path:cn-universal}")
  private String prePath;

  /** 查询OSS云存储列表 */
  @Operation(summary = "查询OSS云存储列表")
  @GetMapping("/list")
  public TableDataInfo list(@Validated SysOssBo bo) {
    startPage();
    return getDataTable(iSysOssService.queryPageList(bo));
  }

  /** 上传OSS云存储 */
  @Operation(summary = "上传OSS云存储")
  @PostMapping("/upload")
  public AjaxResult<Map<String, String>> upload(
      @Parameter(name = "file", description = "文件", required = true) @RequestPart("file")
          MultipartFile file) {
    if (file.isEmpty()) {
      throw new IoTException("上传文件不能为空");
    }
    List<SysOss> sysOsses =
        iSysOssService.queryPageList(
            SysOssBo.builder().originalName(prePath + file.getOriginalFilename()).build());
    String unionId = loginIoTUnionUser(SecurityUtils.getUnionId()).getUnionId();
    if (!CollectionUtils.isEmpty(sysOsses)) {
      // 过滤出不是自己创建的同名文件
      List<SysOss> existOss =
          sysOsses.stream()
              .filter(sysOss -> !unionId.equals(sysOss.getCreateBy()))
              .collect(Collectors.toList());
      if (!CollectionUtils.isEmpty(existOss)) {
        throw new IoTException("已存在同名文件！请修改后重试！");
      }
    }
    //    String unionId = SecurityUtils.getUnionId();
    SysOss oss = iSysOssService.upload(file, unionId);
    Map<String, String> map = new HashMap<>(2);
    map.put("url", oss.getUrl());
    String name = oss.getOriginalName();
    map.put("fileName", name.substring(name.lastIndexOf('/') + 1, name.lastIndexOf(".")));
    return AjaxResult.success(map);
  }

  @Operation(summary = "下载OSS云存储")
  @GetMapping("/download/{ossId}")
  public void download(@PathVariable Long ossId, HttpServletResponse response) throws IOException {
    SysOss sysOss = iSysOssService.getById(ossId);
    if (sysOss == null) {
      throw new IoTException("文件数据不存在!");
    }
    response.reset();
    response.addHeader("Access-Control-Allow-Origin", "*");
    response.addHeader("Access-Control-Expose-Headers", "Content-Disposition");
    FileUtils.setAttachmentResponseHeader(
        response, URLEncoder.encode(sysOss.getOriginalName(), StandardCharsets.UTF_8.toString()));
    response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE + "; charset=UTF-8");
    long data = HttpUtil.download(sysOss.getUrl(), response.getOutputStream(), false);
    response.setContentLength(Convert.toInt(data));
  }

  /** 删除OSS云存储 */
  @Operation(summary = "删除OSS云存储")
  @DeleteMapping("/{ossIds}")
  public AjaxResult<Void> remove(@NotEmpty(message = "主键不能为空") @PathVariable Long[] ossIds) {
    return toAjax(iSysOssService.deleteWithValidByIds(Arrays.asList(ossIds), true) ? 1 : 0);
  }
}
