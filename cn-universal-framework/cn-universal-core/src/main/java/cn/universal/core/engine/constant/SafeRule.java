

package cn.universal.core.engine.constant;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Author Aleo
 *
 * @version 1.0
 * @since 2023/5/19
 */

/**
 * @version 1.0 @Author Aleo
 * @since 2023/5/19
 */
public class SafeRule {

  public static Set<String> packages =
      Stream.of(
              "cn.hutool.json.JSONUtil",
              "cn.hutool.service.util.HexUtil",
              "cn.hutool.service.util.RandomUtil",
              "cn.hutool.service.util.StrUtil")
          .collect(Collectors.toSet());

  public static boolean filter(String packageName) {
    return packageName != null
        && !"".equalsIgnoreCase(packageName)
        && !packages.contains(packageName);
  }
}
