package cn.universal.plugins.protocolapi.core.service;

import cn.universal.plugins.protocolapi.core.model.MagicEntity;
import java.util.Collections;
import java.util.List;

public interface MagicDynamicRegistry<T extends MagicEntity> {

  /** 注册 */
  boolean register(T entity);

  /** 取消注册 */
  boolean unregister(T entity);

  T getMapping(String mappingKey);

  /** 资源存储器 */
  MagicResourceStorage<T> getMagicResourceStorage();

  default List<T> defaultMappings() {
    return Collections.emptyList();
  }
}
