package cn.universal.manager.notice.service.impl;

import cn.universal.manager.notice.mapper.NoticeChannelMapper;
import cn.universal.manager.notice.model.NoticeChannel;
import cn.universal.manager.notice.service.NoticeChannelService;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

@Service
public class NoticeChannelServiceImpl implements NoticeChannelService {

  @Autowired private NoticeChannelMapper noticeChannelMapper;

  @Override
  public List<NoticeChannel> list() {
    return noticeChannelMapper.selectAll();
  }

  @Override
  public List<NoticeChannel> search(String name, String channelType, String status) {
    Example example = new Example(NoticeChannel.class);
    Example.Criteria criteria = example.createCriteria();

    if (name != null && !name.trim().isEmpty()) {
      criteria.andLike("name", "%" + name + "%");
    }

    if (channelType != null && !channelType.trim().isEmpty()) {
      criteria.andEqualTo("channelType", channelType);
    }

    if (status != null && !status.trim().isEmpty()) {
      criteria.andEqualTo("status", status);
    }

    example.orderBy("createTime").desc();
    return noticeChannelMapper.selectByExample(example);
  }

  @Override
  public void save(NoticeChannel channel) {
    Date now = new Date();
    if (channel.getId() == null) {
      // 新增
      channel.setCreateTime(now);
      channel.setUpdateTime(now);
      noticeChannelMapper.insert(channel);
    } else {
      // 更新
      channel.setUpdateTime(now);
      noticeChannelMapper.updateByPrimaryKeySelective(channel);
    }
  }

  @Override
  public void delete(Long id) {
    noticeChannelMapper.deleteByPrimaryKey(id);
  }

  @Override
  public void deleteBatch(List<Long> ids) {
    Example example = new Example(NoticeChannel.class);
    example.createCriteria().andIn("id", ids);
    noticeChannelMapper.deleteByExample(example);
  }

  @Override
  public NoticeChannel getById(Long id) {
    return noticeChannelMapper.selectByPrimaryKey(id);
  }
}
