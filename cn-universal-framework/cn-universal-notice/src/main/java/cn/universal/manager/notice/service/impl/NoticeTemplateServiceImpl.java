package cn.universal.manager.notice.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.universal.manager.notice.mapper.NoticeTemplateMapper;
import cn.universal.manager.notice.model.NoticeChannel;
import cn.universal.manager.notice.model.NoticeTemplate;
import cn.universal.manager.notice.service.NoticeChannelService;
import cn.universal.manager.notice.service.NoticeTemplateService;
import cn.universal.manager.notice.service.channel.NoticeChannelFactory;
import cn.universal.manager.notice.service.channel.NoticeSendChannel;
import cn.universal.manager.notice.util.TemplateUtil;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

@Service
public class NoticeTemplateServiceImpl implements NoticeTemplateService {

  private final NoticeTemplateMapper templateMapper;
  private final NoticeChannelService noticeChannelService;
  private final NoticeChannelFactory channelFactory;

  public NoticeTemplateServiceImpl(
      NoticeTemplateMapper templateMapper,
      NoticeChannelService noticeChannelService,
      NoticeChannelFactory channelFactory) {
    this.templateMapper = templateMapper;
    this.noticeChannelService = noticeChannelService;
    this.channelFactory = channelFactory;
  }

  @Override
  public List<NoticeTemplate> list() {
    return templateMapper.selectAll();
  }

  @Override
  public void save(NoticeTemplate template) {
    Date now = new Date();
    if (template.getId() == null) {
      // 新增
      template.setCreateTime(now);
      template.setUpdateTime(now);
      templateMapper.insertSelective(template);
    } else {
      // 更新
      template.setUpdateTime(now);
      templateMapper.updateByPrimaryKeySelective(template);
    }
  }

  @Override
  public void delete(Long id) {
    templateMapper.deleteByPrimaryKey(id);
  }

  @Override
  public NoticeTemplate getById(Long id) {
    return templateMapper.selectByPrimaryKey(id);
  }

  @Override
  public List<NoticeTemplate> search(String name, String channelType, String status) {
    Example example = new Example(NoticeTemplate.class);
    Example.Criteria c = example.createCriteria();
    if (StrUtil.isNotEmpty(name)) {
      c.andLike("name", "%" + name + "%");
    }
    if (StrUtil.isNotEmpty(channelType)) {
      c.andEqualTo("channelType", channelType);
    }
    if (StrUtil.isNotEmpty(status)) {
      c.andEqualTo("status", status);
    }
    example.orderBy("createTime").desc();
    return templateMapper.selectByExample(example);
  }

  @Override
  public void deleteBatch(List<Long> ids) {
    Example example = new Example(NoticeTemplate.class);
    example.createCriteria().andIn("id", ids);
    templateMapper.deleteByExample(example);
  }

  @Override
  public void enableBatch(List<Long> ids) {
    if (ids == null || ids.isEmpty()) {
      return;
    }
    Example example = new Example(NoticeTemplate.class);
    example.createCriteria().andIn("id", ids);
    NoticeTemplate update = new NoticeTemplate();
    update.setStatus("1"); // 启用
    templateMapper.updateByExampleSelective(update, example);
  }

  @Override
  public void disableBatch(List<Long> ids) {
    if (ids == null || ids.isEmpty()) {
      return;
    }
    Example example = new Example(NoticeTemplate.class);
    example.createCriteria().andIn("id", ids);
    NoticeTemplate update = new NoticeTemplate();
    update.setStatus("0"); // 停用
    templateMapper.updateByExampleSelective(update, example);
  }

  @Override
  public void testTemplate(Long templateId, String receivers, Object params) {
    // 1. 获取模板
    NoticeTemplate template = getById(templateId);
    if (template == null) {
      throw new IllegalArgumentException("模板不存在");
    }

    // 2. 获取渠道配置
    NoticeChannel channelConfig = noticeChannelService.getById(template.getChannelId());
    if (channelConfig == null) {
      throw new IllegalArgumentException("渠道配置不存在");
    }

    // 3. 替换参数
    Map<String, Object> paramMap = null;
    if (params instanceof Map) {
      paramMap = (Map<String, Object>) params;
    } else {
      paramMap = TemplateUtil.parseJson(TemplateUtil.toJson(params));
    }

    String content = TemplateUtil.replaceParams(template.getContent(), paramMap);

    // 4. 获取对应的通知渠道
    NoticeSendChannel channel =
        channelFactory
            .getChannel(template.getChannelType())
            .orElseThrow(
                () -> new IllegalArgumentException("不支持的渠道类型: " + template.getChannelType()));

    // 5. 发送测试消息
    try {
      channel.send(content, receivers, channelConfig, paramMap);
      System.out.println(
          "✅ 测试发送成功 - 模板: " + template.getName() + ", 渠道: " + template.getChannelType());
    } catch (Exception e) {
      System.err.println(
          "❌ 测试发送失败 - 模板: " + template.getName() + ", 渠道: " + template.getChannelType());
      System.err.println("错误信息: " + e.getMessage());
      throw new RuntimeException("测试发送失败", e);
    }
  }
}
