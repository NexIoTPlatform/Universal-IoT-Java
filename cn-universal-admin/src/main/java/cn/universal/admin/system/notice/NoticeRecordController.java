package cn.universal.admin.system.notice;

import cn.universal.manager.notice.model.NoticeSendRecord;
import cn.universal.manager.notice.service.NoticeRecordService;
import cn.universal.persistence.entity.IoTUser;
import cn.universal.persistence.page.TableDataInfo;
import cn.universal.security.BaseController;
import cn.universal.security.utils.SecurityUtils;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 通知记录 */
@RestController
@RequestMapping("/admin/v1/notice/record")
public class NoticeRecordController extends BaseController {

  private final NoticeRecordService recordService;

  @Autowired
  public NoticeRecordController(NoticeRecordService recordService) {
    this.recordService = recordService;
  }

  @GetMapping("/page")
  public TableDataInfo<NoticeSendRecord> page(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String type,
      @RequestParam(required = false) String status) {
    IoTUser iotUser = loginIoTUnionUser(SecurityUtils.getUnionId());
    String currentUser = SecurityUtils.getUnionId();
    
    // 如果不是管理员，只显示该用户创建的记录
    String creator = iotUser.isAdmin() ? null : currentUser;
    
    startPage();
    List<NoticeSendRecord> list = recordService.search(keyword, type, status, creator);
    
    return getDataTable(list);
  }
}
