package cn.universal.admin.system.notice;

import cn.universal.admin.system.service.ISysNoticeService;
import cn.universal.persistence.entity.IoTUser;
import cn.universal.persistence.entity.admin.SysNotice;
import cn.universal.persistence.page.TableDataInfo;
import cn.universal.security.BaseController;
import cn.universal.security.utils.SecurityUtils;
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
        IoTUser iotUser = loginIoTUnionUser(SecurityUtils.getUnionId());
        String currentUser = SecurityUtils.getUnionId();
        
        // 如果不是管理员，只显示该用户创建的通知
        if (!iotUser.isAdmin()) {
            notice.setCreateBy(currentUser);
        }
        
        startPage();
        List<SysNotice> list = noticeService.selectNoticeList(notice);
        return getDataTable(list);
    }

    /**
     * 获取系统通知详细信息
     */
    @GetMapping(value = "/{noticeId}")
    public SysNotice getInfo(@PathVariable("noticeId") Long noticeId) {
        SysNotice notice = noticeService.selectNoticeById(noticeId);
        if (notice == null) {
            return null;
        }
        
        String currentUser = SecurityUtils.getUnionId();
        IoTUser iotUser = loginIoTUnionUser(SecurityUtils.getUnionId());
        
        // 如果不是管理员且不是创建者，无权限访问
        if (!iotUser.isAdmin() && !currentUser.equals(notice.getCreateBy())) {
            return null;
        }
        
        return notice;
    }

    /**
     * 新增系统通知
     */
    @PostMapping
    public void add(@RequestBody SysNotice notice) {
        // 设置创建者
        notice.setCreateBy(SecurityUtils.getUnionId());
        noticeService.insertNotice(notice);
    }

    /**
     * 修改系统通知
     */
    @PutMapping
    public void edit(@RequestBody SysNotice notice) {
        String currentUser = SecurityUtils.getUnionId();
        IoTUser iotUser = loginIoTUnionUser(SecurityUtils.getUnionId());
        
        // 检查权限
        SysNotice existingNotice = noticeService.selectNoticeById(notice.getNoticeId());
        if (existingNotice == null) {
            throw new RuntimeException("通知不存在");
        }
        
        // 如果不是管理员且不是创建者，无权限修改
        if (!iotUser.isAdmin() && !currentUser.equals(existingNotice.getCreateBy())) {
            throw new RuntimeException("无权限修改该通知");
        }
        
        // 设置更新者
        notice.setUpdateBy(currentUser);
        noticeService.updateNotice(notice);
    }

    /**
     * 删除系统通知
     */
    @DeleteMapping("/{noticeIds}")
    public void remove(@PathVariable Long[] noticeIds) {
        String currentUser = SecurityUtils.getUnionId();
        IoTUser iotUser = loginIoTUnionUser(SecurityUtils.getUnionId());
        
        // 如果不是管理员，检查是否有权限删除
        if (!iotUser.isAdmin()) {
            for (Long noticeId : noticeIds) {
                SysNotice notice = noticeService.selectNoticeById(noticeId);
                if (notice != null && !currentUser.equals(notice.getCreateBy())) {
                    throw new RuntimeException("无权限删除该通知");
                }
            }
        }
        
        noticeService.deleteNoticeByIds(noticeIds);
    }
}
