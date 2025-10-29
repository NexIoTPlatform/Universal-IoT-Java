package cn.universal.web.service.impl;

import cn.universal.persistence.entity.IoTProduct;
import cn.universal.persistence.entity.IoTUser;
import cn.universal.persistence.entity.IoTUserApplication;
import cn.universal.persistence.mapper.IoTProductMapper;
import cn.universal.persistence.mapper.IoTUserApplicationMapper;
import cn.universal.persistence.mapper.IoTUserMapper;
import cn.universal.web.service.EmqxAclQueryService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

/**
 * EMQX ACL 查询服务实现类 复用现有的认证查询逻辑
 *
 * @author gitee.com/NexIoT
 * @version 1.0
 * @since 2025/1/20
 */
@Slf4j
@Service
public class EmqxAclQueryServiceImpl implements EmqxAclQueryService {

  private final IoTProductMapper ioTProductMapper;
  private final IoTUserApplicationMapper ioTUserApplicationMapper;
  private final IoTUserMapper ioTUserMapper;

  public EmqxAclQueryServiceImpl(
      IoTProductMapper ioTProductMapper,
      IoTUserApplicationMapper ioTUserApplicationMapper,
      IoTUserMapper ioTUserMapper) {
    this.ioTProductMapper = ioTProductMapper;
    this.ioTUserApplicationMapper = ioTUserApplicationMapper;
    this.ioTUserMapper = ioTUserMapper;
  }

  @Override
  public IoTProduct queryProductByKey(String productKey) {
    if (productKey == null) {
      return null;
    }

    try {
      Example example = new Example(IoTProduct.class);
      example.createCriteria().andEqualTo("productKey", productKey).andEqualTo("state", 0);
      return ioTProductMapper.selectOneByExample(example);
    } catch (Exception e) {
      log.error("查询产品信息失败: productKey={}, error={}", productKey, e.getMessage(), e);
      return null;
    }
  }

  @Override
  public IoTUserApplication queryApplicationById(String appId) {
    if (appId == null) {
      return null;
    }

    try {
      Example example = new Example(IoTUserApplication.class);
      example.createCriteria().andEqualTo("appId", appId).andEqualTo("appStatus", 0);

      return ioTUserApplicationMapper.selectOneByExample(example);
    } catch (Exception e) {
      log.error("查询应用信息失败: appId={}, error={}", appId, e.getMessage(), e);
      return null;
    }
  }

  @Override
  public IoTUser queryUserByUsername(String username) {
    if (username == null) {
      return null;
    }

    try {
      Example example = new Example(IoTUser.class);
      example.createCriteria().andEqualTo("username", username);

      return ioTUserMapper.selectOneByExample(example);
    } catch (Exception e) {
      log.error("查询用户信息失败: username={}, error={}", username, e.getMessage(), e);
      return null;
    }
  }

  @Override
  public List<IoTUserApplication> queryApplicationsByUnionId(String unionId) {
    if (unionId == null) {
      return null;
    }

    try {
      Example example = new Example(IoTUserApplication.class);
      example.createCriteria().andEqualTo("unionId", unionId).andEqualTo("appStatus", 0);

      return ioTUserApplicationMapper.selectByExample(example);
    } catch (Exception e) {
      log.error("查询用户关联应用失败: unionId={}, error={}", unionId, e.getMessage(), e);
      return null;
    }
  }
}
