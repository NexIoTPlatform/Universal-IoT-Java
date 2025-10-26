package cn.universal.plugins.protocolapi.function.service;

import cn.universal.plugins.protocolapi.core.service.AbstractPathMagicResourceStorage;
import cn.universal.plugins.protocolapi.function.model.FunctionInfo;

public class FunctionInfoMagicResourceStorage
    extends AbstractPathMagicResourceStorage<FunctionInfo> {

  @Override
  public String folder() {
    return "function";
  }

  @Override
  public Class<FunctionInfo> magicClass() {
    return FunctionInfo.class;
  }

  @Override
  public String buildMappingKey(FunctionInfo info) {
    return buildMappingKey(info, magicResourceService.getGroupPath(info.getGroupId()));
  }

  @Override
  public void validate(FunctionInfo entity) {
    notBlank(entity.getPath(), FUNCTION_PATH_REQUIRED);
  }
}
