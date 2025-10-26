package cn.universal.web.service.impl;

import cn.universal.web.service.EmqxAclLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * EMQX ACL 日志服务实现类 记录 ACL 检查结果到日志
 *
 * @version 1.0 @Author gitee.com/NexIoT
 * @since 2025/1/20
 */
@Slf4j
@Service
public class EmqxAclLogServiceImpl implements EmqxAclLogService {

  @Override
  public void logAclResult(
      String username,
      String topic,
      String action,
      String clientId,
      String ipAddress,
      String aclType,
      String result) {
    log.info(
        "EMQX ACL 检查结果 - 用户名: {}, 主题: {}, 操作: {}, 客户端ID: {}, IP地址: {}, ACL类型: {}, 结果: {}",
        username,
        topic,
        action,
        clientId,
        ipAddress,
        aclType,
        result);

    // TODO: 可以在这里添加数据库日志记录逻辑
    // 例如记录到专门的 ACL 日志表中，用于审计和分析
  }
}
