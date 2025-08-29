package cn.universal.core.protocol.magic;

import cn.universal.core.engine.MagicScript;
import cn.universal.core.engine.runtime.MagicScriptRuntime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

/** 基于 magic-script 提取JS脚本中的方法名工具类 */
@Slf4j
public class MagicScriptMethodExtractor {

  /**
   * 从magic-script脚本中提取所有变量/方法名（兼容原逻辑）
   *
   * @param scriptContent 脚本内容字符串
   * @return 脚本中定义的所有变量/方法名集合
   * @throws Exception 脚本编译失败时抛出异常
   */
  public static Set<String> extractVarNames(String scriptContent) {
    try {
      // 创建并编译脚本
      MagicScript script = MagicScript.create(scriptContent, null);
      MagicScriptRuntime compiled = script.compile();

      // 获取所有变量/方法名并转为Set
      String[] varNames = compiled.getVarNames();
      return varNames != null ? new HashSet<>(Arrays.asList(varNames)) : new HashSet<>();
    } catch (Exception e) {
      log.info("MagicScriptMethodExtractor.extractVarNames error", e);
    }
    return new HashSet<>();
  }

  /**
   * 从脚本中提取与目标方法集合匹配的支持方法
   *
   * @param scriptContent 脚本内容
   * @param targetMethods 目标方法集合（如{decode, encode}）
   * @return 脚本中存在的目标方法交集
   * @throws Exception 脚本编译失败时抛出异常
   */
  public static Set<String> extractSupportMethods(String scriptContent, Set<String> targetMethods) {
    // 提取脚本中的所有方法名
    Set<String> allVarNames = extractVarNames(scriptContent);

    // 计算与目标方法的交集
    Set<String> supportMethods = new HashSet<>();
    for (String varName : allVarNames) {
      if (targetMethods.contains(varName)) {
        supportMethods.add(varName);
      }
    }
    return supportMethods;
  }
}
