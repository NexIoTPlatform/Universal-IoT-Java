package cn.universal.plugins.protocolapi.core.event;

import cn.universal.plugins.protocolapi.core.config.Constants;
import cn.universal.plugins.protocolapi.core.model.MagicNotify;

public class NotifyEvent extends MagicEvent {

  private String id;

  public NotifyEvent(MagicNotify notify) {
    super(notify.getType(), notify.getAction(), Constants.EVENT_SOURCE_NOTIFY);
    this.id = notify.getId();
  }

  public String getId() {
    return id;
  }
}
