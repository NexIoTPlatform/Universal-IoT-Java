package cn.wvp.protocol.handler;

import cn.universal.common.domain.R;
import cn.wvp.protocol.entity.WvpDownRequest;

public interface WvpCommandHandler {
  String getSupportedCommand();
  R<?> handle(WvpDownRequest request);
}
