package cn.universal.plugins.protocolapi.core.service;

import cn.universal.plugins.protocolapi.core.config.JsonCodeConstants;
import cn.universal.plugins.protocolapi.core.model.PathMagicEntity;
import cn.universal.plugins.protocolapi.utils.PathUtils;
import java.util.Objects;

public abstract class AbstractPathMagicResourceStorage<T extends PathMagicEntity>
    implements MagicResourceStorage<T>, JsonCodeConstants {

  protected MagicResourceService magicResourceService;

  @Override
  public String suffix() {
    return ".ms";
  }

  @Override
  public boolean requirePath() {
    return true;
  }

  @Override
  public void setMagicResourceService(MagicResourceService magicResourceService) {
    this.magicResourceService = magicResourceService;
  }

  public String buildMappingKey(T entity, String path) {
    return PathUtils.replaceSlash(
        "/" + Objects.toString(path, "") + "/" + Objects.toString(entity.getPath(), ""));
  }

  @Override
  public void validate(T entity) {
    notBlank(entity.getPath(), REQUEST_PATH_REQUIRED);
    notBlank(entity.getScript(), SCRIPT_REQUIRED);
  }
}
