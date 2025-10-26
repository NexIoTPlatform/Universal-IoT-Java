package cn.universal.plugins.protocolapi.core.service.impl;

import cn.universal.plugins.protocolapi.core.model.ApiInfo;
import cn.universal.plugins.protocolapi.core.service.AbstractPathMagicResourceStorage;
import cn.universal.plugins.protocolapi.utils.PathUtils;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

public class ApiInfoMagicResourceStorage extends AbstractPathMagicResourceStorage<ApiInfo> {

  private final String prefix;

  public ApiInfoMagicResourceStorage(String prefix) {
    this.prefix = StringUtils.defaultIfBlank(prefix, "") + "/";
  }

  @Override
  public String folder() {
    return "api";
  }

  @Override
  public Class<ApiInfo> magicClass() {
    return ApiInfo.class;
  }

  @Override
  public String buildMappingKey(ApiInfo info, String path) {
    return info.getMethod().toUpperCase() + ":" + super.buildMappingKey(info, path);
  }

  @Override
  public String buildMappingKey(ApiInfo info) {
    return PathUtils.replaceSlash(
        buildMappingKey(
            info,
            this.prefix
                + Objects.toString(magicResourceService.getGroupPath(info.getGroupId()), "")));
  }

  @Override
  public void validate(ApiInfo entity) {
    notBlank(entity.getMethod(), REQUEST_METHOD_REQUIRED);
    super.validate(entity);
  }
}
