package cn.universal.manager.notice.service.impl;

import cn.hutool.json.JSONUtil;
import cn.universal.manager.notice.model.NoticeChannel;
import cn.universal.manager.notice.model.NoticeSendRecord;
import cn.universal.manager.notice.model.NoticeSendRequest;
import cn.universal.manager.notice.model.NoticeTemplate;
import cn.universal.manager.notice.service.NoticeChannelService;
import cn.universal.manager.notice.service.NoticeRecordService;
import cn.universal.manager.notice.service.NoticeService;
import cn.universal.manager.notice.service.NoticeTemplateService;
import cn.universal.manager.notice.service.channel.NoticeSendChannel;
import cn.universal.manager.notice.service.channel.NoticeSendResult;
import cn.universal.manager.notice.util.NoticeTemplateUtil;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Slf4j
public class NoticeServiceImpl implements NoticeService {

  private final NoticeTemplateService templateService;
  private final NoticeRecordService recordService;
  private final NoticeChannelService noticeChannelService;
  private final List<NoticeSendChannel> channelList;

  public NoticeServiceImpl(
      NoticeTemplateService templateService,
      NoticeRecordService recordService,
      NoticeChannelService noticeChannelService,
      List<NoticeSendChannel> channelList) {
    this.templateService = templateService;
    this.recordService = recordService;
    this.noticeChannelService = noticeChannelService;
    this.channelList = channelList;
  }

  private String mergeReceivers(
      String templateDefaultReceivers, String requestReceivers, Map<String, Object> params) {
    String tpl = NoticeTemplateUtil.replaceParams(templateDefaultReceivers, params);
    String req = NoticeTemplateUtil.replaceParams(requestReceivers, params);
    if (!StringUtils.hasText(tpl)) {
      return req;
    }
    if (!StringUtils.hasText(req)) {
      return tpl;
    }
    Set<String> set = new LinkedHashSet<>();
    set.addAll(Arrays.asList(tpl.split(",")));
    set.addAll(Arrays.asList(req.split(",")));
    return String.join(",", set);
  }

  private static class SendContext {

    NoticeTemplate template;
    NoticeChannel channelConfig;
    NoticeSendChannel channel;
    String content;
    String mergedReceivers;
  }

  private SendContext validateAndPrepare(NoticeSendRequest request) {
    if (request == null || request.getTemplateId() == null) {
      throw new IllegalArgumentException("模板ID不能为空");
    }
    NoticeTemplate template = templateService.getById(request.getTemplateId());
    if (template == null) {
      throw new IllegalArgumentException("通知模板不存在");
    }
    if (!StringUtils.hasText(template.getContent())) {
      throw new IllegalArgumentException("模板内容不能为空");
    }
    NoticeChannel channelConfig = noticeChannelService.getById(template.getChannelId());
    if (channelConfig == null) {
      throw new IllegalArgumentException("渠道配置不存在");
    }
    NoticeSendChannel channel =
        channelList.stream()
            .filter(c -> c.support(channelConfig.getChannelType()))
            .findFirst()
            .orElseThrow(
                () -> new IllegalArgumentException("不支持的通知类型:" + channelConfig.getChannelType()));
    String content =
        NoticeTemplateUtil.replaceNestedParams(template.getContent(), request.getParams());
    String mergedReceivers =
        mergeReceivers(template.getReceivers(), request.getReceivers(), request.getParams());
    if (!StringUtils.hasText(mergedReceivers)) {
      //      throw new IllegalArgumentException("收件人为空");
      log.info("推送消息渠道={},收件人为空,部分不需要收件人", request.getTemplateId());
    }
    SendContext ctx = new SendContext();
    ctx.template = template;
    ctx.channelConfig = channelConfig;
    ctx.channel = channel;
    ctx.content = content;
    ctx.mergedReceivers = mergedReceivers;
    return ctx;
  }

  private NoticeSendRecord buildRecord(SendContext ctx, NoticeSendRequest request) {
    NoticeSendRecord record = new NoticeSendRecord();
    record.setTemplateId(ctx.template.getId());
    record.setConfigId(ctx.channelConfig.getId());
    record.setReceivers(ctx.mergedReceivers);
    record.setParams(JSONUtil.toJsonStr(request.getParams()));
    record.setSendTime(new Date());
    return record;
  }

  @Override
  public void send(NoticeSendRequest request) {
    SendContext ctx;
    try {
      ctx = validateAndPrepare(request);
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
    NoticeSendRecord record = buildRecord(ctx, request);
    try {
      NoticeSendResult result =
          ctx.channel.send(
              ctx.content, ctx.mergedReceivers, ctx.channelConfig, request.getParams());
      record.setStatus(result.isSuccess() ? "SUCCESS" : "FAIL");
      record.setResult(result.getErrorMessage() == null ? "OK" : result.getErrorMessage());
      if (!result.isSuccess()) {
        throw new RuntimeException("消息推送失败: " + result.getErrorMessage());
      }
    } finally {
      recordService.save(record);
    }
  }

  @Override
  public NoticeSendResult sendR(NoticeSendRequest request) {
    SendContext ctx;
    try {
      ctx = validateAndPrepare(request);
    } catch (Exception e) {
      return NoticeSendResult.builder().success(false).errorMessage(e.getMessage()).build();
    }
    NoticeSendRecord record = buildRecord(ctx, request);
    NoticeSendResult result;
    try {
      result =
          ctx.channel.send(
              ctx.content, ctx.mergedReceivers, ctx.channelConfig, request.getParams());
      record.setStatus(result.isSuccess() ? "SUCCESS" : "FAIL");
      record.setResult(result.getErrorMessage() == null ? "OK" : result.getErrorMessage());
    } catch (Exception e) {
      record.setStatus("FAIL");
      record.setResult(e.getMessage());
      log.error("推送消息失败", e);
      result =
          NoticeSendResult.builder()
              .success(false)
              .errorMessage("消息推送失败:" + e.getMessage())
              .build();
    } finally {
      recordService.save(record);
    }
    return result;
  }
}
