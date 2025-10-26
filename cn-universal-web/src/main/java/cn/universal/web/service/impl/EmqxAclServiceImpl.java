package cn.universal.web.service.impl;

import cn.universal.persistence.entity.IoTProduct;
import cn.universal.persistence.entity.IoTUser;
import cn.universal.persistence.entity.IoTUserApplication;
import cn.universal.web.dto.EmqxAclRequest;
import cn.universal.web.dto.EmqxAclResponse;
import cn.universal.web.service.EmqxAclLogService;
import cn.universal.web.service.EmqxAclQueryService;
import cn.universal.web.service.EmqxAclService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * EMQX ACL 授权服务实现类 复用现有的认证逻辑进行权限检查
 *
 * @version 1.0 @Author gitee.com/NexIoT
 * @since 2025/1/20
 */
@Slf4j
@Component
public class EmqxAclServiceImpl implements EmqxAclService {

  private final EmqxAclQueryService emqxAclQueryService;
  private final EmqxAclLogService emqxAclLogService;
  private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

  @Value("${mqtt.cfg.client.username:admin}")
  private String adminUsername;

  @Value("${mqtt.cfg.client.password:admin123456}")
  private String adminPassword;

  @Value("${mqtt.cfg.enable:true}")
  private boolean adminEnable;

  @Value("${mqtt.cfg.defined.thing:$thing}")
  private String thingPrefix;

  public EmqxAclServiceImpl(
      EmqxAclQueryService emqxAclQueryService, EmqxAclLogService emqxAclLogService) {
    this.emqxAclQueryService = emqxAclQueryService;
    this.emqxAclLogService = emqxAclLogService;
  }

  @Override
  public EmqxAclResponse checkAcl(EmqxAclRequest request) {
    if (request == null
        || !StringUtils.hasText(request.getUsername())
        || !StringUtils.hasText(request.getTopic())
        || !StringUtils.hasText(request.getAction())) {
      log.warn(
          "ACL 请求参数无效: username={}, topic={}, action={}",
          request != null ? request.getUsername() : "null",
          request != null ? request.getTopic() : "null",
          request != null ? request.getAction() : "null");
      return new EmqxAclResponse("deny");
    }

    String username = request.getUsername();
    String topic = request.getTopic();
    String action = request.getAction();
    String clientId = request.getClientid();
    String ipAddress = request.getIp_address();

    log.info(
        "开始处理 EMQX ACL 授权请求: username={}, topic={}, action={}, clientId={}, ip={}",
        username,
        topic,
        action,
        clientId,
        ipAddress);

    String aclType = null;
    boolean hasPermission = false;

    try {
      // 1. 尝试配置账号授权
      hasPermission = checkAdminAcl(username, topic, action);
      if (hasPermission) {
        aclType = "ADMIN";
        log.info("配置账号 ACL 授权成功: username={}, topic={}, action={}", username, topic, action);
      }
    } catch (Exception e) {
      log.warn("配置账号 ACL 授权异常: username={}, error={}", username, e.getMessage(), e);
    }

    // 2. 如果配置账号授权失败，尝试产品授权
    if (!hasPermission) {
      try {
        hasPermission = checkProductAcl(username, topic, action);
        if (hasPermission) {
          aclType = "PRODUCT";
          log.info("产品 ACL 授权成功: username={}, topic={}, action={}", username, topic, action);
        }
      } catch (Exception e) {
        log.warn("产品 ACL 授权异常: username={}, error={}", username, e.getMessage(), e);
      }
    }

    // 3. 如果产品授权失败，尝试应用授权
    if (!hasPermission) {
      try {
        hasPermission = checkApplicationAcl(username, topic, action);
        if (hasPermission) {
          aclType = "APPLICATION";
          log.info("应用 ACL 授权成功: username={}, topic={}, action={}", username, topic, action);
        }
      } catch (Exception e) {
        log.warn("应用 ACL 授权异常: username={}, error={}", username, e.getMessage(), e);
      }
    }

    // 4. 如果应用授权失败，尝试用户授权
    if (!hasPermission) {
      try {
        hasPermission = checkUserAcl(username, topic, action);
        if (hasPermission) {
          aclType = "USER";
          log.info("用户 ACL 授权成功: username={}, topic={}, action={}", username, topic, action);
        }
      } catch (Exception e) {
        log.warn("用户 ACL 授权异常: username={}, error={}", username, e.getMessage(), e);
      }
    }

    // 5. 记录 ACL 检查结果
    String result = hasPermission ? "allow" : "deny";
    emqxAclLogService.logAclResult(username, topic, action, clientId, ipAddress, aclType, result);

    log.info(
        "EMQX ACL 授权完成: username={}, topic={}, action={}, result={}, type={}",
        username,
        topic,
        action,
        result,
        aclType);

    return new EmqxAclResponse(result);
  }

  /** 检查配置账号 ACL 权限 */
  private boolean checkAdminAcl(String username, String topic, String action) {
    if (!adminEnable) {
      return false;
    }
    if (!adminUsername.equals(username)) {
      return false;
    }
    return true;
    // 配置账号权限规则
//    if ("subscribe".equals(action)) {
//      // 允许订阅 $thing/# 和 $ota/#
//      return topic.startsWith(thingPrefix + "/") || topic.startsWith("$ota/");
//    } else if ("publish".equals(action)) {
//      // 允许发布到 $thing/down/#
//      return topic.startsWith(thingPrefix + "/down/");
//    }
//    return false;
  }

  /** 检查产品 ACL 权限 */
  private boolean checkProductAcl(String username, String topic, String action) {
    // 查询产品信息
    IoTProduct product = emqxAclQueryService.queryProductByKey(username);
    if (product == null || product.getState() != 0) {
      return false;
    }

    // 产品只能发布，不能订阅
    if (!"publish".equals(action)) {
      return false;
    }

    String productKey = product.getProductKey();

    // 允许发布到以下主题：
    // 1. $thing/${productKey}/#
    // 2. $thing/down/${productKey}/#
    // 3. $ota/update/${productKey}/#
    return topic.startsWith(thingPrefix + "/" + productKey + "/")
        || topic.startsWith(thingPrefix + "/down/" + productKey + "/")
        || topic.startsWith("$ota/update/" + productKey + "/");
  }

  /** 检查应用 ACL 权限 */
  private boolean checkApplicationAcl(String username, String topic, String action) {
    // 查询应用信息
    IoTUserApplication application = emqxAclQueryService.queryApplicationById(username);
    if (application == null
        || application.getAppStatus() != 0
        || !Boolean.TRUE.equals(application.getMqttEnable())
        || application.getValidEndDate() != null
            && application.getValidEndDate().getTime() < System.currentTimeMillis()) {
      return false;
    }

    // 应用只能订阅，不能发布
    if (!"subscribe".equals(action)) {
      return false;
    }

    String appId = application.getAppId();
    if (appId == null) {
      return false;
    }

    // 允许订阅 $thing/${appId} 主题
    return topic.equals(thingPrefix + "/" + appId);
  }

  /** 检查用户 ACL 权限 */
  private boolean checkUserAcl(String username, String topic, String action) {
    // 查询用户信息
    IoTUser user = emqxAclQueryService.queryUserByUsername(username);
    if (user == null) {
      return false;
    }

    // 用户只能订阅，不能发布
    if (!"subscribe".equals(action)) {
      return false;
    }

    // 查询用户关联的应用
    List<IoTUserApplication> applications =
        emqxAclQueryService.queryApplicationsByUnionId(username);
    if (applications == null || applications.isEmpty()) {
      return false;
    }

    // 检查用户是否有权限订阅该主题
    for (IoTUserApplication app : applications) {
      String appId = app.getAppId();
      if (appId != null && topic.equals(thingPrefix + "/" + appId)) {
        return true;
      }
    }
    return false;
  }
}
