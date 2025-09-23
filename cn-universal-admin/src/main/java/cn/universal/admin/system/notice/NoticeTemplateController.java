package cn.universal.admin.system.notice;

import cn.universal.admin.common.utils.SecurityUtils;
import cn.universal.admin.system.web.BaseController;
import cn.universal.manager.notice.dto.NoticeTemplateDTO;
import cn.universal.manager.notice.model.NoticeTemplate;
import cn.universal.manager.notice.service.NoticeTemplateService;
import cn.universal.persistence.page.TableDataInfo;
import cn.universal.persistence.query.AjaxResult;
import java.util.List;
import java.util.Map;
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
    startPage();
    List<NoticeTemplate> list = noticeTemplateService.search(name, channelType, null);

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
      NoticeTemplate template = new NoticeTemplate();
      template.setId(templateDTO.getId());
      template.setName(templateDTO.getName());
      template.setChannelType(templateDTO.getChannelType());
      template.setChannelId(templateDTO.getChannelId());
      template.setContent(templateDTO.getContent());
      template.setReceivers(templateDTO.getReceivers());
      template.setStatus(templateDTO.getStatus());
      template.setRemark(templateDTO.getRemark());
      template.setCreator(SecurityUtils.getUnionId());
      template.setCreateTime(templateDTO.getCreateTime());
      template.setUpdateTime(templateDTO.getUpdateTime());

      noticeTemplateService.save(template);
      return AjaxResult.success("操作成功");
    } catch (Exception e) {
      return AjaxResult.error("操作失败: " + e.getMessage());
    }
  }

  @PostMapping("/delete")
  public AjaxResult delete(@RequestBody List<Long> ids) {
    try {
      if (ids != null && !ids.isEmpty()) {
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
  public NoticeTemplateDTO get(@RequestParam Long id) {
    try {
      NoticeTemplate template = noticeTemplateService.getById(id);
      if (template == null) {
        return null;
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

      return dto;
    } catch (Exception e) {
      return null;
    }
  }

  @PostMapping("/test")
  public AjaxResult test(@RequestBody Map<String, Object> request) {
    Long templateId = Long.valueOf(request.get("templateId").toString());
    String receivers = (String) request.get("receivers");
    Object params = request.get("params");
    noticeTemplateService.testTemplate(templateId, receivers, params);
    return AjaxResult.success("成功");
  }
}
