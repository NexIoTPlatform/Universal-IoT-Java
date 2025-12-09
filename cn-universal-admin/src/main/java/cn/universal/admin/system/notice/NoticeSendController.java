package cn.universal.admin.system.notice;

import cn.universal.manager.notice.model.NoticeSendRequest;
import cn.universal.manager.notice.model.NoticeTemplate;
import cn.universal.manager.notice.service.NoticeService;
import cn.universal.manager.notice.service.NoticeTemplateService;
import cn.universal.persistence.entity.IoTUser;
import cn.universal.security.BaseController;
import cn.universal.security.utils.SecurityUtils;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 通知发送 */
@RestController
@RequestMapping("/admin/v1/notice")
public class NoticeSendController extends BaseController {

  private final NoticeService noticeService;
  private final NoticeTemplateService templateService;

  @Autowired
  public NoticeSendController(
      NoticeService noticeService, NoticeTemplateService templateService) {
    this.noticeService = noticeService;
    this.templateService = templateService;
  }

  @PostMapping("/send")
  public Map<String, Object> send(@RequestBody Map<String, Object> request) {
    Long templateId = Long.valueOf(request.get("templateId").toString());
    String receivers = (String) request.get("receivers");
    Object params = request.get("params");

    if (templateId == null) {
      throw new IllegalArgumentException("模板ID不能为空");
    }

    String currentUser = SecurityUtils.getUnionId();
    IoTUser iotUser = loginIoTUnionUser(SecurityUtils.getUnionId());

    // 检查模板是否存在及权限
    NoticeTemplate template = templateService.getById(templateId);
    if (template == null) {
      throw new IllegalArgumentException("模板不存在");
    }

    // 如果不是管理员且不是创建者，无权限发送
    if (!iotUser.isAdmin() && !currentUser.equals(template.getCreator())) {
      throw new IllegalArgumentException("无权限使用该模板发送通知");
    }

    NoticeSendRequest req = new NoticeSendRequest();
    req.setTemplateId(templateId);
    req.setReceivers(receivers);
    req.setCreator(currentUser); // 设置创建者为当前用户
    if (params instanceof Map) {
      req.setParams((Map<String, Object>) params);
    }
    noticeService.send(req);

    return Map.of("code", 0, "msg", "发送成功");
  }
}
