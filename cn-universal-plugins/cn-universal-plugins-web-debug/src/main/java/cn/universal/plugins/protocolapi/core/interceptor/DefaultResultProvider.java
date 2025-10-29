package cn.universal.plugins.protocolapi.core.interceptor;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import cn.universal.core.engine.MagicScriptContext;
import cn.universal.plugins.protocolapi.core.context.RequestEntity;
import cn.universal.plugins.protocolapi.utils.ScriptManager;
import java.util.Map;

/** 默认结果封装实现 */
public class DefaultResultProvider implements ResultProvider {

  private final String responseScript;

  public DefaultResultProvider(String responseScript) {
    this.responseScript = responseScript;
  }

  @Override
  public Object buildResult(RequestEntity requestEntity, int code, String message, Object data) {
    long timestamp = System.currentTimeMillis();
    if (this.responseScript != null) {
      MagicScriptContext context = new MagicScriptContext();
      context.setScriptName(requestEntity.getMagicScriptContext().getScriptName());
      //			context.set("code", code);
      //			context.set("message", message);
      String v = (String) data;
      if (JSONUtil.isJson(v)) {
        context.putMapIntoContext(JSONUtil.toBean(v, Map.class));
      } else {
        context.set("data", data);
      }
      //			context.set("apiInfo", requestEntity.getApiInfo());
      //			context.set("request", requestEntity.getRequest());
      //			context.set("response", requestEntity.getResponse());
      //			context.set("timestamp", timestamp);
      //			context.set("requestTime", requestEntity.getRequestTime());
      //			context.set("executeTime", timestamp - requestEntity.getRequestTime());
      return ScriptManager.executeExpression(responseScript, context);
    } else {
      if (code == -1 && ObjectUtil.isNull(data)) {
        return message;
      }
      return data;
      //      return new JsonBean<>(code, message, data, (int) (timestamp -
      // requestEntity.getRequestTime()));
    }
  }
}
