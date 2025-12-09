package cn.universal.admin.system.notice;

import cn.universal.manager.notice.dto.NoticeTemplateDTO;
import cn.universal.manager.notice.model.NoticeTemplate;
import cn.universal.manager.notice.service.NoticeTemplateService;
import cn.universal.persistence.entity.IoTUser;
import cn.universal.persistence.page.TableDataInfo;
import cn.universal.persistence.query.AjaxResult;
import cn.universal.security.BaseController;
import cn.universal.security.utils.SecurityUtils;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/v1/notice/template")
public class NoticeTemplateController extends BaseController {

  private final NoticeTemplateService noticeTemplateService;

  @Autowired
  public NoticeTemplateController(NoticeTemplateService noticeTemplateService) {
    this.noticeTemplateService = noticeTemplateService;
  }

  @GetMapping("/list")
  public TableDataInfo<NoticeTemplateDTO> list(
      @RequestParam(defaultValue = "1") Integer pageNum,
      @RequestParam(defaultValue = "10") Integer pageSize,
      @RequestParam(required = false) String name,
      @RequestParam(required = false) String channelType) {
    IoTUser iotUser = loginIoTUnionUser(SecurityUtils.getUnionId());
    String currentUser = SecurityUtils.getUnionId();
    
    startPage();
    List<NoticeTemplate> list = noticeTemplateService.search(name, channelType, null);
    
    // 如果不是管理员，只显示该用户创建的模板
    if (!iotUser.isAdmin()) {
      list = list.stream()
          .filter(template -> currentUser.equals(template.getCreator()))
          .collect(Collectors.toList());
    }

    List<NoticeTemplateDTO> dtoList =
        list.stream()
            .map(
                template -> {
                  NoticeTemplateDTO dto = new NoticeTemplateDTO();
                  dto.setId(template.getId());
                  dto.setName(template.getName());
                  dto.setChannelType(template.getChannelType());
                  dto.setChannelId(template.getChannelId());
                  dto.setContent(template.getContent());
                  dto.setReceivers(template.getReceivers());
                  dto.setStatus(template.getStatus());
                  dto.setRemark(template.getRemark());
                  dto.setCreator(template.getCreator());
                  dto.setCreateTime(template.getCreateTime());
                  dto.setUpdateTime(template.getUpdateTime());

                  return dto;
                })
            .toList();
    return getDataTable(dtoList);
  }

  @PostMapping("/save")
  public AjaxResult save(@RequestBody NoticeTemplateDTO templateDTO) {
    try {
      String currentUser = SecurityUtils.getUnionId();
      IoTUser iotUser = loginIoTUnionUser(SecurityUtils.getUnionId());
      
      // 如果是修改操作，检查权限
      if (templateDTO.getId() != null) {
        NoticeTemplate existingTemplate = noticeTemplateService.getById(templateDTO.getId());
        if (existingTemplate == null) {
          return AjaxResult.error("模板不存在");
        }
        // 如果不是管理员且不是创建者，无权限修改
        if (!iotUser.isAdmin() && !currentUser.equals(existingTemplate.getCreator())) {
          return AjaxResult.error("无权限修改该模板");
        }
      }
      
      noticeTemplateService.saveTemplate(templateDTO, currentUser);
      return AjaxResult.success("操作成功");
    } catch (Exception e) {
      return AjaxResult.error("操作失败: " + e.getMessage());
    }
  }

  @PostMapping("/delete")
  public AjaxResult delete(@RequestBody List<Long> ids) {
    try {
      if (ids != null && !ids.isEmpty()) {
        String currentUser = SecurityUtils.getUnionId();
        IoTUser iotUser = loginIoTUnionUser(SecurityUtils.getUnionId());
        
        // 如果不是管理员，检查是否有权限删除
        if (!iotUser.isAdmin()) {
          for (Long id : ids) {
            NoticeTemplate template = noticeTemplateService.getById(id);
            if (template != null && !currentUser.equals(template.getCreator())) {
              return AjaxResult.error("无权限删除该模板");
            }
          }
        }
        
        noticeTemplateService.deleteBatch(ids);
        return AjaxResult.success("删除成功");
      } else {
        return AjaxResult.success("未提供有效的ID");
      }
    } catch (Exception e) {
      return AjaxResult.success("删除失败: " + e.getMessage());
    }
  }

  /** 批量启用通知模板 */
  @PostMapping("/enable")
  public AjaxResult enable(@RequestBody List<Long> ids) {
    try {
      if (ids != null && !ids.isEmpty()) {
        String currentUser = SecurityUtils.getUnionId();
        IoTUser iotUser = loginIoTUnionUser(SecurityUtils.getUnionId());
        
        // 如果不是管理员，检查是否有权限启用
        if (!iotUser.isAdmin()) {
          for (Long id : ids) {
            NoticeTemplate template = noticeTemplateService.getById(id);
            if (template != null && !currentUser.equals(template.getCreator())) {
              return AjaxResult.error("无权限启用该模板");
            }
          }
        }
        
        noticeTemplateService.enableBatch(ids);
        return AjaxResult.success("启用成功");
      } else {
        return AjaxResult.success("未提供有效的ID");
      }
    } catch (Exception e) {
      return AjaxResult.error("启用失败: " + e.getMessage());
    }
  }

  /** 批量停用通知模板 */
  @PostMapping("/disable")
  public AjaxResult disable(@RequestBody List<Long> ids) {
    try {
      if (ids != null && !ids.isEmpty()) {
        String currentUser = SecurityUtils.getUnionId();
        IoTUser iotUser = loginIoTUnionUser(SecurityUtils.getUnionId());
        
        // 如果不是管理员，检查是否有权限停用
        if (!iotUser.isAdmin()) {
          for (Long id : ids) {
            NoticeTemplate template = noticeTemplateService.getById(id);
            if (template != null && !currentUser.equals(template.getCreator())) {
              return AjaxResult.error("无权限停用该模板");
            }
          }
        }
        
        noticeTemplateService.disableBatch(ids);
        return AjaxResult.success("停用成功");
      } else {
        return AjaxResult.success("未提供有效的ID");
      }
    } catch (Exception e) {
      return AjaxResult.error("停用失败: " + e.getMessage());
    }
  }

  @GetMapping("/get")
  public AjaxResult get(@RequestParam Long id) {
    try {
      NoticeTemplate template = noticeTemplateService.getById(id);
      if (template == null) {
        return AjaxResult.error("模板不存在");
      }
      
      String currentUser = SecurityUtils.getUnionId();
      IoTUser iotUser = loginIoTUnionUser(SecurityUtils.getUnionId());
      
      // 如果不是管理员且不是创建者，无权限访问
      if (!iotUser.isAdmin() && !currentUser.equals(template.getCreator())) {
        return AjaxResult.error("无权限访问该模板");
      }
      
      NoticeTemplateDTO dto = new NoticeTemplateDTO();
      dto.setId(template.getId());
      dto.setName(template.getName());
      dto.setChannelType(template.getChannelType());
      dto.setChannelId(template.getChannelId());
      dto.setContent(template.getContent());
      dto.setReceivers(template.getReceivers());
      dto.setStatus(template.getStatus());
      dto.setRemark(template.getRemark());
      dto.setCreator(template.getCreator());
      dto.setCreateTime(template.getCreateTime());
      dto.setUpdateTime(template.getUpdateTime());
      return AjaxResult.success(dto);
    } catch (Exception e) {
      return AjaxResult.error("查询失败: " + e.getMessage());
    }
  }

  @PostMapping("/test")
  public AjaxResult test(@RequestBody Map<String, Object> request) {
    try {
      Long templateId = Long.valueOf(request.get("templateId").toString());
      String receivers = (String) request.get("receivers");
      Object params = request.get("params");
      
      String currentUser = SecurityUtils.getUnionId();
      IoTUser iotUser = loginIoTUnionUser(SecurityUtils.getUnionId());
      
      // 检查模板是否存在及权限
      NoticeTemplate template = noticeTemplateService.getById(templateId);
      if (template == null) {
        return AjaxResult.error("模板不存在");
      }
      
      // 如果不是管理员且不是创建者，无权限测试
      if (!iotUser.isAdmin() && !currentUser.equals(template.getCreator())) {
        return AjaxResult.error("无权限测试该模板");
      }
      
      noticeTemplateService.testTemplate(templateId, receivers, params);
      return AjaxResult.success("成功");
    } catch (Exception e) {
      return AjaxResult.error("测试失败: " + e.getMessage());
    }
  }
}
