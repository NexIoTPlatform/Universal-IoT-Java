package cn.universal.manager.notice.service;

import cn.universal.manager.notice.model.NoticeSendRequest;
import cn.universal.manager.notice.service.channel.NoticeSendResult;

public interface NoticeService {

  void send(NoticeSendRequest req);

  default NoticeSendResult sendR(NoticeSendRequest request) {
    return null;
  }
}
