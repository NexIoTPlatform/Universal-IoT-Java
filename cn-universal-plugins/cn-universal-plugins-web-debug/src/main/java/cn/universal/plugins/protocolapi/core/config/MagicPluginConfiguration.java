package cn.universal.plugins.protocolapi.core.config;

import cn.universal.plugins.protocolapi.core.model.Plugin;
import cn.universal.plugins.protocolapi.core.web.MagicControllerRegister;

public interface MagicPluginConfiguration {

  Plugin plugin();

  /** 注册Controller */
  default MagicControllerRegister controllerRegister() {
    return (mapping, configuration) -> {};
  }
}
