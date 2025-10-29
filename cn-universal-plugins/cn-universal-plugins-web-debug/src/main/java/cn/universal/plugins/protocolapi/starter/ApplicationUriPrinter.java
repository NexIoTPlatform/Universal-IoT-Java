package cn.universal.plugins.protocolapi.starter;

import cn.universal.plugins.protocolapi.core.config.MagicAPIProperties;
import cn.universal.plugins.protocolapi.utils.PathUtils;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 输出服务访问地址
 *
 * @author 冰点
 * @date 2021-6-3 12:08:59
 * @since 1.2.1
 */
@Component
@ConditionalOnProperty(name = "magic-api.show-url", havingValue = "true", matchIfMissing = true)
@Order
@Slf4j
public class ApplicationUriPrinter implements CommandLineRunner {

  @Value("${server.port:8080}")
  private int port;

  @Value("${server.servlet.context-path:}")
  private String contextPath;

  @Autowired private MagicAPIProperties properties;

  @Override
  public void run(String... args) throws Exception {
    log.info(
        "********************************************当前服务相关地址********************************************");
    String ip = "IP";
    try {
      ip = InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException e) {
      System.out.println("当前服务地址获取失败");
    }
    String magicWebPath = properties.getWeb();
    String schema = "http://";
    String localUrl =
        schema
            + PathUtils.replaceSlash(
                String.format(
                    "localhost:%s/%s/%s/",
                    port, contextPath, Objects.toString(properties.getPrefix(), "")));
    String externUrl =
        schema
            + PathUtils.replaceSlash(
                String.format(
                    "%s:%s/%s/%s/",
                    ip, port, contextPath, Objects.toString(properties.getPrefix(), "")));
    log.info(
        "服务启动成功，magic-api已内置启动! Access URLs:" + "\n\t接口本地地址: \t\t%s" + "\n\t接口外部地址: \t\t%s\n",
        localUrl, externUrl);
    if (!StringUtils.isEmpty(magicWebPath)) {
      String webPath =
          schema
              + PathUtils.replaceSlash(
                  String.format("%s:%s/%s/%s/index.html", ip, port, contextPath, magicWebPath));
      log.info("\t协议调试器地址: \t\t" + webPath);
    }
    log.info("\t可通过配置关闭输出: \tmagic-api.show-url=false");
    log.info(
        "********************************************当前服务相关地址********************************************");
  }
}
