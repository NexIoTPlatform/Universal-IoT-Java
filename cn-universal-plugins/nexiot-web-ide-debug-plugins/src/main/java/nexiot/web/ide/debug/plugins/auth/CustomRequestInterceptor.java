package nexiot.web.ide.debug.plugins.auth;

import cn.universal.core.engine.MagicScriptContext;
import nexiot.web.ide.debug.plugins.core.config.Constants;
import nexiot.web.ide.debug.plugins.core.interceptor.RequestInterceptor;
import nexiot.web.ide.debug.plugins.core.model.ApiInfo;
import nexiot.web.ide.debug.plugins.core.model.JsonBean;
import nexiot.web.ide.debug.plugins.core.servlet.MagicHttpServletRequest;
import nexiot.web.ide.debug.plugins.core.servlet.MagicHttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** 自定义未授权访问 */
@Component
@Slf4j
public class CustomRequestInterceptor implements RequestInterceptor {

  @Override
  public Object preHandle(
      ApiInfo info,
      MagicScriptContext context,
      MagicHttpServletRequest request,
      MagicHttpServletResponse response)
      throws Exception {
    String token = request.getHeader(Constants.MAGIC_TOKEN_HEADER);

    if (token == null || token.isEmpty()) {
      return new JsonBean<>(403, "未授权访问");
    }
    String username = SymmetricEncryptionUtil.desDecryptWithBuiltinKey(token);
    if (username == null || username.isEmpty()) {
      return new JsonBean<>(403, "用户无权限");
    }
    if (!username.equals(info.getCreateBy())) {
      return new JsonBean<>(403, "你没有调用权限");
    }
    log.info("info={},context={}", info, context);
    return null;
  }
}
