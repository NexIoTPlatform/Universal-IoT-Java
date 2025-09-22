package cn.universal.plugins.protocolapi.starter;

import cn.universal.plugins.protocolapi.core.config.MagicAPIProperties;
import cn.universal.plugins.protocolapi.core.service.impl.ApiInfoMagicResourceStorage;
import cn.universal.plugins.protocolapi.core.service.impl.RequestMagicDynamicRegistry;
import cn.universal.plugins.protocolapi.datasource.model.MagicDynamicDataSource;
import cn.universal.plugins.protocolapi.datasource.service.DataSourceInfoMagicResourceStorage;
import cn.universal.plugins.protocolapi.datasource.service.DataSourceMagicDynamicRegistry;
import cn.universal.plugins.protocolapi.function.service.FunctionInfoMagicResourceStorage;
import cn.universal.plugins.protocolapi.function.service.FunctionMagicDynamicRegistry;
import cn.universal.plugins.protocolapi.utils.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Configuration
@AutoConfigureAfter(MagicModuleConfiguration.class)
public class MagicDynamicRegistryConfiguration {

  private final MagicAPIProperties properties;

  @Autowired @Lazy private RequestMappingHandlerMapping requestMappingHandlerMapping;

  public MagicDynamicRegistryConfiguration(MagicAPIProperties properties) {
    this.properties = properties;
  }

  @Bean
  @ConditionalOnMissingBean
  public ApiInfoMagicResourceStorage apiInfoMagicResourceStorage() {
    return new ApiInfoMagicResourceStorage(properties.getPrefix());
  }

  @Bean
  @ConditionalOnMissingBean
  public RequestMagicDynamicRegistry magicRequestMagicDynamicRegistry(
      ApiInfoMagicResourceStorage apiInfoMagicResourceStorage) throws NoSuchMethodException {
    return new RequestMagicDynamicRegistry(
        apiInfoMagicResourceStorage,
        Mapping.create(requestMappingHandlerMapping, properties.getWeb()),
        properties.isAllowOverride(),
        properties.getPrefix());
  }

  @Bean
  @ConditionalOnMissingBean
  public FunctionInfoMagicResourceStorage functionInfoMagicResourceStorage() {
    return new FunctionInfoMagicResourceStorage();
  }

  @Bean
  @ConditionalOnMissingBean
  public FunctionMagicDynamicRegistry functionMagicDynamicRegistry(
      FunctionInfoMagicResourceStorage functionInfoMagicResourceStorage) {
    return new FunctionMagicDynamicRegistry(functionInfoMagicResourceStorage);
  }

  @Bean
  @ConditionalOnMissingBean
  public DataSourceInfoMagicResourceStorage dataSourceInfoMagicResourceStorage() {
    return new DataSourceInfoMagicResourceStorage();
  }

  @Bean
  @ConditionalOnMissingBean
  public DataSourceMagicDynamicRegistry dataSourceMagicDynamicRegistry(
      DataSourceInfoMagicResourceStorage dataSourceInfoMagicResourceStorage,
      MagicDynamicDataSource magicDynamicDataSource) {
    return new DataSourceMagicDynamicRegistry(
        dataSourceInfoMagicResourceStorage, magicDynamicDataSource);
  }
}
