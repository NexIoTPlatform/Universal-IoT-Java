package cn.universal.core.protocol.jscrtipt;

import cn.hutool.core.util.StrUtil;
import java.util.HashSet;
import java.util.Set;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

/** 提取 JS 脚本中的方法名 */
public class JsMethodExtractor {
  /**
   * 从 JS 脚本内容中提取所有方法名
   *
   * @param scriptContent JS 脚本字符串
   * @return 方法名集合（仅包含函数类型的标识符）
   */
  public static Set<String> extractMethodNames(String scriptContent) {
    if (StrUtil.isEmpty(scriptContent)) {
      return new HashSet<>();
    }
    Set<String> methodNames = new HashSet<>();
    // 使用 GraalVM 上下文执行脚本并获取全局对象
    try (Context context = Context.create("js")) {
      // 执行脚本（加载所有定义）
      context.eval("js", scriptContent);
      // 获取 JS 全局对象（包含所有顶级定义）
      Value global = context.getBindings("js");
      // 遍历全局对象的所有成员
      for (String key : global.getMemberKeys()) {
        Value member = global.getMember(key);
        // 判断是否为可执行函数（排除变量、常量等非函数类型）
        if (isFunction(member)) {
          methodNames.add(key);
        }
      }
    }
    return methodNames;
  }

  /** 判断 GraalVM Value 是否为 JS 函数 */
  private static boolean isFunction(Value value) {
    // 函数必须满足：可执行 + 不是 null + 不是 undefined + 类型名称为 "function
    return value.canExecute()
        && !value.isNull()
        && "function".equalsIgnoreCase(value.getMetaObject().getMetaSimpleName());
  }
}
