package cn.universal.web.service;

import cn.universal.persistence.entity.IoTProduct;
import cn.universal.persistence.entity.IoTUser;
import cn.universal.persistence.entity.IoTUserApplication;
import java.util.List;

/**
 * EMQX ACL 查询服务接口 复用现有的认证查询逻辑
 *
 * @author gitee.com/NexIoT
 * @version 1.0
 * @since 2025/1/20
 */
public interface EmqxAclQueryService {

  /**
   * 根据产品 Key 查询产品信息
   *
   * @param productKey 产品 Key
   * @return 产品信息
   */
  IoTProduct queryProductByKey(String productKey);

  /**
   * 根据应用 ID 查询应用信息
   *
   * @param appId 应用 ID
   * @return 应用信息
   */
  IoTUserApplication queryApplicationById(String appId);

  /**
   * 根据用户名查询用户信息
   *
   * @param username 用户名
   * @return 用户信息
   */
  IoTUser queryUserByUsername(String username);

  /**
   * 根据 Union ID 查询关联的应用列表
   *
   * @param unionId Union ID
   * @return 应用列表
   */
  List<IoTUserApplication> queryApplicationsByUnionId(String unionId);
}
