package cn.universal.manager.notice.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.universal.manager.notice.mapper.NoticeSendRecordMapper;
import cn.universal.manager.notice.model.NoticeSendRecord;
import cn.universal.manager.notice.service.NoticeRecordService;
import java.util.List;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

@Service
public class NoticeRecordServiceImpl implements NoticeRecordService {

  private final NoticeSendRecordMapper recordMapper;

  public NoticeRecordServiceImpl(NoticeSendRecordMapper recordMapper) {
    this.recordMapper = recordMapper;
  }

  @Override
  public void save(NoticeSendRecord record) {
    record.setSendTime(new java.util.Date());
    recordMapper.insertSelective(record);
  }

  @Override
  public List<NoticeSendRecord> list() {
    return recordMapper.selectAll();
  }

  @Override
  public List<NoticeSendRecord> search(String keyword, String type, String status) {
    return search(keyword, type, status, null);
  }

  @Override
  public List<NoticeSendRecord> search(String keyword, String type, String status, String creator) {
    Example example = new Example(NoticeSendRecord.class);
    Example.Criteria c = example.createCriteria();
    if (StrUtil.isNotEmpty(keyword)) {
      c.andLike("receivers", "%" + keyword + "%");
    }
    if (StrUtil.isNotEmpty(type)) {
      c.andEqualTo("status", type);
    }
    if (StrUtil.isNotEmpty(status)) {
      c.andEqualTo("status", status);
    }
    if (StrUtil.isNotEmpty(creator)) {
      c.andEqualTo("creator", creator);
    }
    example.orderBy("sendTime").desc();
    return recordMapper.selectByExample(example);
  }
}
