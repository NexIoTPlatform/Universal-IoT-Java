package cn.universal.plugins.protocolapi.function.service;

import cn.universal.core.engine.MagicResourceLoader;
import cn.universal.core.engine.MagicScriptContext;
import cn.universal.core.engine.exception.MagicExitException;
import cn.universal.core.engine.runtime.ExitValue;
import cn.universal.plugins.protocolapi.core.config.MagicConfiguration;
import cn.universal.plugins.protocolapi.core.event.FileEvent;
import cn.universal.plugins.protocolapi.core.event.GroupEvent;
import cn.universal.plugins.protocolapi.core.model.Parameter;
import cn.universal.plugins.protocolapi.core.service.AbstractMagicDynamicRegistry;
import cn.universal.plugins.protocolapi.core.service.MagicResourceStorage;
import cn.universal.plugins.protocolapi.function.model.FunctionInfo;
import cn.universal.plugins.protocolapi.utils.ScriptManager;
import java.util.List;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;

public class FunctionMagicDynamicRegistry extends AbstractMagicDynamicRegistry<FunctionInfo> {

  private static final Logger logger = LoggerFactory.getLogger(FunctionMagicDynamicRegistry.class);

  public FunctionMagicDynamicRegistry(MagicResourceStorage<FunctionInfo> magicResourceStorage) {
    super(magicResourceStorage);
    MagicResourceLoader.addFunctionLoader(this::lookupLambdaFunction);
  }

  private Object lookupLambdaFunction(MagicScriptContext context, String path) {
    FunctionInfo functionInfo = getMapping(path);
    if (functionInfo != null) {
      String scriptName = MagicConfiguration.getMagicResourceService().getScriptName(functionInfo);
      List<Parameter> parameters = functionInfo.getParameters();
      return (Function<Object[], Object>)
          objects -> {
            MagicScriptContext functionContext = new MagicScriptContext(context.getRootVariables());
            functionContext.setScriptName(scriptName);
            if (objects != null) {
              for (int i = 0, len = objects.length, size = parameters.size();
                  i < len && i < size;
                  i++) {
                functionContext.set(parameters.get(i).getName(), objects[i]);
              }
            }
            Object value = ScriptManager.executeScript(functionInfo.getScript(), functionContext);
            if (value instanceof ExitValue) {
              throw new MagicExitException((ExitValue) value);
            }
            return value;
          };
    }
    return null;
  }

  @EventListener(condition = "#event.type == 'function'")
  public void onFileEvent(FileEvent event) {
    processEvent(event);
  }

  @EventListener(condition = "#event.type == 'function'")
  public void onGroupEvent(GroupEvent event) {
    processEvent(event);
  }

  @Override
  protected boolean register(MappingNode<FunctionInfo> mappingNode) {
    logger.debug("注册函数：{}", mappingNode.getMappingKey());
    return true;
  }

  @Override
  protected void unregister(MappingNode<FunctionInfo> mappingNode) {
    logger.debug("取消注册函数：{}", mappingNode.getMappingKey());
  }
}
