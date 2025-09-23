package cn.universal.admin.system.notice;

import cn.universal.admin.system.web.BaseController;
import cn.universal.manager.notice.model.NoticeSendRecord;
import cn.universal.manager.notice.service.NoticeRecordService;
import cn.universal.persistence.page.TableDataInfo;
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
    startPage();
    List<NoticeSendRecord> list = recordService.search(keyword, type, status);
    return getDataTable(list);
  }
}
