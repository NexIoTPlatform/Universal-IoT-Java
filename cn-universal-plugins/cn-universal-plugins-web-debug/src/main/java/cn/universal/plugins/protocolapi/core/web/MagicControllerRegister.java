package cn.universal.plugins.protocolapi.core.web;

import cn.universal.plugins.protocolapi.core.config.MagicConfiguration;
import cn.universal.plugins.protocolapi.utils.Mapping;

public interface MagicControllerRegister {

  void register(Mapping mapping, MagicConfiguration configuration);
}
