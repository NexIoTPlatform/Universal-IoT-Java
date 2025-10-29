package cn.universal.plugins.protocolapi.core.handler;

import cn.universal.plugins.protocolapi.core.annotation.Message;
import cn.universal.plugins.protocolapi.core.config.Constants;
import cn.universal.plugins.protocolapi.core.config.MessageType;
import cn.universal.plugins.protocolapi.core.config.WebSocketSessionManager;
import cn.universal.plugins.protocolapi.core.context.MagicConsoleSession;

public class MagicCoordinationHandler {

  @Message(MessageType.SET_FILE_ID)
  public void setFileId(MagicConsoleSession session, String fileId) {
    session.setAttribute(Constants.WEBSOCKET_ATTRIBUTE_FILE_ID, fileId);
    WebSocketSessionManager.sendToOther(
        session.getClientId(),
        MessageType.INTO_FILE_ID,
        session.getAttribute(Constants.WEBSOCKET_ATTRIBUTE_CLIENT_ID),
        fileId);
  }
}
