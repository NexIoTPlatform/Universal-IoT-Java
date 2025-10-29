package cn.universal.admin.system.notice;

import cn.universal.admin.system.service.ISysNoticeService;
import cn.universal.persistence.entity.admin.SysNotice;
import cn.universal.persistence.page.TableDataInfo;
import cn.universal.security.BaseController;
import java.util.List;
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
 * 系统通知Controller
 * 
 * @author universal
 * @date 2023-01-01
 */
@RestController
@RequestMapping("/system/notice")
public class NoticeController extends BaseController {
    
    @Autowired
    private ISysNoticeService noticeService;

    /**
     * 查询系统通知列表
     */
    @GetMapping("/list")
    public TableDataInfo<SysNotice> list(SysNotice notice) {
        startPage();
        List<SysNotice> list = noticeService.selectNoticeList(notice);
        return getDataTable(list);
    }

    /**
     * 获取系统通知详细信息
     */
    @GetMapping(value = "/{noticeId}")
    public SysNotice getInfo(@PathVariable("noticeId") Long noticeId) {
        return noticeService.selectNoticeById(noticeId);
    }

    /**
     * 新增系统通知
     */
    @PostMapping
    public void add(@RequestBody SysNotice notice) {
        noticeService.insertNotice(notice);
    }

    /**
     * 修改系统通知
     */
    @PutMapping
    public void edit(@RequestBody SysNotice notice) {
        noticeService.updateNotice(notice);
    }

    /**
     * 删除系统通知
     */
    @DeleteMapping("/{noticeIds}")
    public void remove(@PathVariable Long[] noticeIds) {
        noticeService.deleteNoticeByIds(noticeIds);
    }
}
