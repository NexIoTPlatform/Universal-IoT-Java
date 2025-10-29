package cn.universal.plugins.protocolapi.core.handler;

import cn.universal.plugins.protocolapi.core.annotation.Message;
import cn.universal.plugins.protocolapi.core.config.Constants;
import cn.universal.plugins.protocolapi.core.config.MessageType;
import cn.universal.plugins.protocolapi.core.config.WebSocketSessionManager;
import cn.universal.plugins.protocolapi.core.context.MagicConsoleSession;
import cn.universal.plugins.protocolapi.core.context.MagicUser;
import cn.universal.plugins.protocolapi.core.interceptor.AuthorizationInterceptor;
import cn.universal.plugins.protocolapi.utils.IpUtils;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;

/**
 * UI上其它操作处理
 *
 * @author mxd
 */
public class MagicWorkbenchHandler {

  private final AuthorizationInterceptor authorizationInterceptor;

  private static final MagicUser guest = new MagicUser("guest", "游客", "unauthorization");

  private static final Logger logger = LoggerFactory.getLogger(MagicWorkbenchHandler.class);

  public MagicWorkbenchHandler(AuthorizationInterceptor authorizationInterceptor) {
    this.authorizationInterceptor = authorizationInterceptor;
  }

  @Message(MessageType.LOGIN)
  public void onLogin(MagicConsoleSession session, String token, String clientId) {
    session.setClientId(clientId);
    MagicUser user = null;
    try {
      user = authorizationInterceptor.getUserByToken(token);
    } catch (Exception e) {
      if (!authorizationInterceptor.requireLogin()) {
        user = guest;
      }
    }
    if (user != null) {
      String ip =
          Optional.ofNullable(session.getWebSocketSession().getRemoteAddress())
              .map(it -> it.getAddress().getHostAddress())
              .orElse("unknown");
      HttpHeaders headers = session.getWebSocketSession().getHandshakeHeaders();
      ip = IpUtils.getRealIP(ip, headers::getFirst, null);
      if (user.getTimeout() > 0) {
        session.setUser(user);
        session.setTimeout(user.getTimeout() * 1000 + System.currentTimeMillis());
      }
      session.setAttribute(Constants.WEBSOCKET_ATTRIBUTE_USER_ID, user.getId());
      session.setAttribute(
          Constants.WEBSOCKET_ATTRIBUTE_USER_IP, StringUtils.defaultIfBlank(ip, "unknown"));
      session.setAttribute(Constants.WEBSOCKET_ATTRIBUTE_USER_NAME, user.getUsername());
      session.setActivateTime(System.currentTimeMillis());
      synchronized (MagicWorkbenchHandler.class) {
        if (WebSocketSessionManager.getConsoleSession(clientId) != null) {
          WebSocketSessionManager.sendBySession(
              session, WebSocketSessionManager.buildMessage(MessageType.LOGIN_RESPONSE, "-1"));
          return;
        }
        WebSocketSessionManager.add(session);
      }
      WebSocketSessionManager.sendBySession(
          session,
          WebSocketSessionManager.buildMessage(
              MessageType.LOGIN_RESPONSE, "1", session.getAttributes()));
      List<Map<String, Object>> messages = getOnlineUsers();
      if (!messages.isEmpty()) {
        WebSocketSessionManager.sendByClientId(
            session.getClientId(),
            WebSocketSessionManager.buildMessage(MessageType.ONLINE_USERS, messages));
      }
      WebSocketSessionManager.sendToMachine(MessageType.SEND_ONLINE, session.getClientId());
      WebSocketSessionManager.sendToOther(
          session.getClientId(), MessageType.USER_LOGIN, session.getAttributes());
    } else {
      WebSocketSessionManager.sendBySession(
          session, WebSocketSessionManager.buildMessage(MessageType.LOGIN_RESPONSE, "0"));
    }
  }

  @Message(MessageType.SEND_ONLINE)
  public void sendOnline(String clientId) {
    List<Map<String, Object>> messages = getOnlineUsers();
    if (!messages.isEmpty()) {
      WebSocketSessionManager.sendToMachineByClientId(
          clientId, WebSocketSessionManager.buildMessage(MessageType.ONLINE_USERS, messages));
    }
  }

  @Message(MessageType.PONG)
  public String pong(MagicConsoleSession session) {
    session.setActivateTime(System.currentTimeMillis());
    MagicUser user = session.getUser();
    if (user != null && session.getTimeout() - System.currentTimeMillis() < 60 * 1000) {
      String oldToken = user.getToken();
      authorizationInterceptor.refreshToken(user);
      String newToken = user.getToken();
      session.setTimeout(System.currentTimeMillis() + user.getTimeout() * 1000);
      if (!Objects.equals(newToken, oldToken)) {
        WebSocketSessionManager.sendBySession(
            session, WebSocketSessionManager.buildMessage(MessageType.REFRESH_TOKEN, newToken));
      }
    }
    return null;
  }

  private List<Map<String, Object>> getOnlineUsers() {
    return WebSocketSessionManager.getSessions().stream()
        .map(MagicConsoleSession::getAttributes)
        .collect(Collectors.toList());
  }
}
