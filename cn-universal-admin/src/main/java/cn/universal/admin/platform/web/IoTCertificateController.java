package cn.universal.admin.platform.web;

import static cn.universal.common.constant.IoTConstant.CERT_DEFAULT_KEY;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.universal.admin.common.utils.SecurityUtils;
import cn.universal.admin.platform.service.ICertificateService;
import cn.universal.admin.system.web.BaseController;
import cn.universal.persistence.dto.IoTCertificateQueryDTO;
import cn.universal.persistence.entity.IoTCertificate;
import cn.universal.persistence.page.TableDataInfo;
import cn.universal.persistence.query.AjaxResult;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/admin/v1/certificate")
public class IoTCertificateController extends BaseController {

  @Autowired private ICertificateService certificateService;

  /** 证书多条件查询，参数用DTO封装 */
  @GetMapping("/list")
  public TableDataInfo list(IoTCertificateQueryDTO query) {
    String userId = SecurityUtils.getUnionId();
    boolean isAdmin =
        SecurityUtils.getIoTUnionUser() != null && SecurityUtils.getIoTUnionUser().isAdmin();
    startPage();
    List<IoTCertificate> list =
        certificateService.searchCertificates(
            query.getName(),
            userId,
            query.getSslKey(),
            query.getExpireStart(),
            query.getExpireEnd(),
            isAdmin);
    if (CollUtil.isNotEmpty(list)) {
      list.forEach(
          item -> {
            if (StrUtil.isNotBlank(item.getCertPassword())) {
              item.setCertPassword(null);
            }
            if (StrUtil.isNotBlank(item.getKeyPassword())) {
              item.setKeyPassword(null);
            }
          });
    }
    return getDataTable(list);
  }

  @GetMapping("/get")
  public IoTCertificate get(@RequestParam String sslKey) {
    IoTCertificate cert = certificateService.getBySslKey(sslKey);
    String userId = SecurityUtils.getUnionId();
    boolean isAdmin =
        SecurityUtils.getIoTUnionUser() != null && SecurityUtils.getIoTUnionUser().isAdmin();
    if (cert == null || (!isAdmin && !userId.equals(cert.getCreateUser()))) {
      return null; // 或抛出无权限异常
    }
    return cert;
  }

  @PostMapping("/add")
  public AjaxResult add(@RequestBody IoTCertificate cert) {
    // 仅管理员可添加默认证书
    if (CERT_DEFAULT_KEY.equals(cert.getSslKey())
        && (SecurityUtils.getIoTUnionUser() == null
            || !SecurityUtils.getIoTUnionUser().isAdmin())) {
      return AjaxResult.error("无权限：只有管理员可操作默认证书");
    }
    if (cert != null && StrUtil.isBlank(cert.getSslKey())) {
      cert.setSslKey(IdUtil.objectId());
    }
    cert.setCreateUser(SecurityUtils.getUnionId());
    certificateService.addCertificate(cert);
    return AjaxResult.success("添加成功");
  }

  @PostMapping("/update")
  public AjaxResult update(@RequestBody IoTCertificate cert) {
    // 仅管理员可更新默认证书
    if (CERT_DEFAULT_KEY.equals(cert.getSslKey())
        && (SecurityUtils.getIoTUnionUser() == null
            || !SecurityUtils.getIoTUnionUser().isAdmin())) {
      return AjaxResult.error("无权限：只有管理员可操作默认证书");
    }
    String userId = SecurityUtils.getUnionId();
    IoTCertificate old = certificateService.getBySslKey(cert.getSslKey());
    if (old == null
        || (SecurityUtils.getIoTUnionUser() != null
            && !SecurityUtils.getIoTUnionUser().isAdmin()
            && !userId.equals(old.getCreateUser()))) {
      return AjaxResult.error("无权限");
    }
    certificateService.updateCertificate(cert);
    return AjaxResult.success("更新成功");
  }

  @PostMapping("/delete")
  public AjaxResult delete(@RequestParam Long id) {
    IoTCertificate cert =
        certificateService.listAll().stream()
            .filter(c -> c.getId().equals(id))
            .findFirst()
            .orElse(null);
    // 仅管理员可删除默认证书
    if (cert != null && CERT_DEFAULT_KEY.equals(cert.getSslKey())) {
      return AjaxResult.error("默认证书，只能编辑不能删除");
    }
    String userId = SecurityUtils.getUnionId();
    if (cert == null
        || (SecurityUtils.getIoTUnionUser() != null
            && !SecurityUtils.getIoTUnionUser().isAdmin()
            && !userId.equals(cert.getCreateUser()))) {
      return AjaxResult.error("无权限");
    }
    certificateService.deleteCertificate(id);
    return AjaxResult.success("删除成功");
  }

  /** 证书内容直接上传接口 */
  @PostMapping("/uploadContent")
  public AjaxResult uploadContent(
      @RequestParam("sslKey") String sslKey,
      @RequestParam("certContent") String certContent,
      @RequestParam("keyContent") String keyContent,
      @RequestParam(value = "name", required = false) String name) {
    // 仅管理员可上传默认证书
    if (CERT_DEFAULT_KEY.equals(sslKey)
        && (SecurityUtils.getIoTUnionUser() == null
            || !SecurityUtils.getIoTUnionUser().isAdmin())) {
      return AjaxResult.error("无权限：只有管理员可操作默认证书");
    }
    IoTCertificate cert =
        IoTCertificate.builder()
            .sslKey(sslKey)
            .certContent(certContent)
            .keyContent(keyContent)
            .name(name)
            .createUser(SecurityUtils.getUnionId())
            .build();
    certificateService.addCertificate(cert);
    return AjaxResult.success("上传成功");
  }

  /** 证书文件上传接口，支持pem/key文件，自动保存内容到数据库 */
  @PostMapping("/uploadFile")
  public AjaxResult uploadFile(
      @RequestParam("sslKey") String sslKey,
      @RequestParam("file") List<MultipartFile> files,
      @RequestParam(value = "name", required = false) String name) {
    // 仅管理员可上传默认证书
    if (CERT_DEFAULT_KEY.equals(sslKey)
        && (SecurityUtils.getIoTUnionUser() == null
            || !SecurityUtils.getIoTUnionUser().isAdmin())) {
      return AjaxResult.error("无权限：只有管理员可操作默认证书");
    }
    String certContent = null;
    String keyContent = null;
    try {
      for (MultipartFile file : files) {
        String filename = file.getOriginalFilename();
        String content = new String(file.getBytes());
        if (filename != null && filename.endsWith(".pem")) {
          certContent = content;
        } else if (filename != null
            && (filename.endsWith(".key") || filename.contains("private"))) {
          keyContent = content;
        }
      }
      if (certContent == null || keyContent == null) {
        return AjaxResult.error("请同时上传pem和key文件");
      }
      IoTCertificate cert =
          IoTCertificate.builder()
              .sslKey(sslKey)
              .certContent(certContent)
              .keyContent(keyContent)
              .name(name)
              .createUser(SecurityUtils.getUnionId())
              .build();
      certificateService.addCertificate(cert);
      return AjaxResult.success("上传并保存成功", cert);
    } catch (Exception e) {
      return AjaxResult.error("上传失败: " + e.getMessage());
    }
  }
}
