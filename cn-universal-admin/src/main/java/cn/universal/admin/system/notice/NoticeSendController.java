package cn.universal.admin.system.notice;

import cn.universal.manager.notice.model.NoticeSendRequest;
import cn.universal.manager.notice.service.NoticeService;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 通知发送 */
@RestController
@RequestMapping("/admin/v1/notice")
public class NoticeSendController {

  private final NoticeService noticeService;

  @Autowired
  public NoticeSendController(NoticeService noticeService) {
    this.noticeService = noticeService;
  }

  @PostMapping("/send")
  public Map<String, Object> send(@RequestBody Map<String, Object> request) {
    Long templateId = Long.valueOf(request.get("templateId").toString());
    String receivers = (String) request.get("receivers");
    Object params = request.get("params");

    if (templateId == null) {
      throw new IllegalArgumentException("模板ID不能为空");
    }

    NoticeSendRequest req = new NoticeSendRequest();
    req.setTemplateId(templateId);
    req.setReceivers(receivers);
    if (params instanceof Map) {
      req.setParams((Map<String, Object>) params);
    }
    noticeService.send(req);

    return Map.of("code", 0, "msg", "发送成功");
  }
}
