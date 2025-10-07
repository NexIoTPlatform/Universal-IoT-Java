package cn.universal.admin.system.notice;

import cn.hutool.json.JSONObject;
import cn.universal.security.utils.SecurityUtils;
import cn.universal.security.BaseController;
import cn.universal.manager.notice.dto.NoticeChannelDTO;
import cn.universal.manager.notice.model.NoticeChannel;
import cn.universal.manager.notice.service.NoticeChannelService;
import cn.universal.manager.notice.util.JsonDesensitizationUtil;
import cn.universal.persistence.page.TableDataInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
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
@RequestMapping("/admin/v1/notice/channel")
public class NoticeChannelController extends BaseController {

  private final NoticeChannelService noticeChannelService;
  private final ObjectMapper objectMapper;

  @Autowired
  public NoticeChannelController(
      NoticeChannelService noticeChannelService, ObjectMapper objectMapper) {
    this.noticeChannelService = noticeChannelService;
    this.objectMapper = objectMapper;
  }

  @GetMapping("/list")
  public TableDataInfo<NoticeChannelDTO> list(
      @RequestParam(defaultValue = "1") Integer pageNum,
      @RequestParam(defaultValue = "10") Integer pageSize,
      @RequestParam(required = false) String name,
      @RequestParam(required = false) String channelType) {
    startPage();
    List<NoticeChannel> list = noticeChannelService.search(name, channelType, null);

    List<NoticeChannelDTO> dtoList =
        list.stream()
            .map(
                channel -> {
                  NoticeChannelDTO dto = new NoticeChannelDTO();
                  dto.setId(channel.getId());
                  dto.setName(channel.getName());
                  dto.setChannelType(channel.getChannelType());
                  dto.setStatus(channel.getStatus());
                  dto.setRemark(channel.getRemark());
                  dto.setCreator(channel.getCreator());
                  dto.setCreateTime(channel.getCreateTime());
                  dto.setUpdateTime(channel.getUpdateTime());

                  // 将config字符串转换为对象并进行脱敏处理
                  if (channel.getConfig() != null && !channel.getConfig().isEmpty()) {
                    try {
                      JSONObject desensitizedConfig =
                          JsonDesensitizationUtil.desensitize(channel.getConfig());
                      dto.setConfig(desensitizedConfig);
                    } catch (Exception e) {
                      dto.setConfig(null);
                    }
                  }

                  return dto;
                })
            .toList();

    return getDataTable(dtoList);
  }

  @GetMapping("/get")
  public NoticeChannelDTO get(@RequestParam Long id) {
    try {
      NoticeChannel channel = noticeChannelService.getById(id);
      if (channel == null) {
        return null;
      }
      NoticeChannelDTO dto = new NoticeChannelDTO();
      dto.setId(channel.getId());
      dto.setName(channel.getName());
      dto.setChannelType(channel.getChannelType());
      dto.setStatus(channel.getStatus());
      dto.setRemark(channel.getRemark());
      dto.setCreator(channel.getCreator());
      dto.setCreateTime(channel.getCreateTime());
      dto.setUpdateTime(channel.getUpdateTime());

      // 将config字符串转换为对象并进行脱敏处理
      if (channel.getConfig() != null && !channel.getConfig().isEmpty()) {
        JSONObject desensitizedConfig = JsonDesensitizationUtil.desensitize(channel.getConfig());
        dto.setConfig(desensitizedConfig);
      }

      return dto;
    } catch (Exception e) {
      return null;
    }
  }

  @PostMapping("/save")
  public Map<String, Object> save(@RequestBody NoticeChannelDTO channelDTO) {
    try {
      NoticeChannel channel = new NoticeChannel();
      channel.setId(channelDTO.getId());
      channel.setName(channelDTO.getName());
      channel.setChannelType(channelDTO.getChannelType());
      channel.setStatus(channelDTO.getStatus());
      channel.setRemark(channelDTO.getRemark());
      channel.setCreator(SecurityUtils.getUnionId());
      channel.setCreateTime(channelDTO.getCreateTime());
      channel.setUpdateTime(channelDTO.getUpdateTime());
      // 将config对象转换为JSON字符串
      if (channelDTO.getConfig() != null) {
        channel.setConfig(objectMapper.writeValueAsString(channelDTO.getConfig()));
      }

      noticeChannelService.save(channel);
      return Map.of("code", 0, "msg", "操作成功");
    } catch (Exception e) {
      return Map.of("code", 1, "msg", "操作失败: " + e.getMessage());
    }
  }

  @PostMapping("/delete")
  public Map<String, Object> delete(@RequestBody List<Long> ids) {
    try {
      if (ids != null && !ids.isEmpty()) {
        noticeChannelService.deleteBatch(ids);
        return Map.of("code", 0, "msg", "删除成功");
      } else {
        return Map.of("code", 1, "msg", "未提供有效的ID");
      }
    } catch (Exception e) {
      return Map.of("code", 1, "msg", "删除失败: " + e.getMessage());
    }
  }
}
