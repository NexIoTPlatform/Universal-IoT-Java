package cn.universal.web.service;

/**
 * EMQX ACL 日志服务接口 用于记录 ACL 检查结果
 *
 * @version 1.0 @Author Aleo
 * @since 2025/1/20
 */
public interface EmqxAclLogService {

  /**
   * 记录 ACL 检查结果
   *
   * @param username 用户名
   * @param topic 主题
   * @param action 操作类型
   * @param clientId 客户端ID
   * @param ipAddress IP地址
   * @param aclType ACL类型
   * @param result 检查结果
   */
  void logAclResult(
      String username,
      String topic,
      String action,
      String clientId,
      String ipAddress,
      String aclType,
      String result);
}
