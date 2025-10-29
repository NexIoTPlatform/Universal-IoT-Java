/*
 *
 * Copyright (c) 2025, NexIoT. All Rights Reserved.
 *
 * @Description: 本文件由 gitee.com/NexIoT 开发并拥有版权，未经授权严禁擅自商用、复制或传播。
 * @Author: gitee.com/NexIoT
 * @Email: wo8335224@gmail.com
 * @Wechat: outlookFil
 *
 *
 */

package cn.universal.admin.common.aspectj;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.universal.common.annotation.Log;
import cn.universal.common.enums.BusinessStatus;
import cn.universal.common.enums.HttpMethod;
import cn.universal.common.utils.LocationUtils;
import cn.universal.common.utils.ServletUtils;
import cn.universal.common.utils.SpringUtils;
import cn.universal.persistence.entity.IoTUser;
import cn.universal.persistence.entity.SysOperLog;
import cn.universal.security.service.AdminLogService;
import cn.universal.security.service.IoTUserService;
import cn.universal.security.utils.SecurityUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;

/** 操作日志记录处理 */
@Aspect
@Component
public class LogAspect {

  @Resource private IoTUserService ioTUserService;
  private static final Logger log = LoggerFactory.getLogger(LogAspect.class);

  // 配置织入点
  @Pointcut("@annotation(cn.universal.common.annotation.Log)")
  public void logPointCut() {}

  /**
   * 处理完请求后执行
   *
   * @param joinPoint 切点
   */
  @AfterReturning(pointcut = "logPointCut()", returning = "jsonResult")
  public void doAfterReturning(JoinPoint joinPoint, Object jsonResult) {
    handleLog(joinPoint, null, jsonResult);
  }

  /**
   * 拦截异常操作
   *
   * @param joinPoint 切点
   * @param e 异常
   */
  @AfterThrowing(value = "logPointCut()", throwing = "e")
  public void doAfterThrowing(JoinPoint joinPoint, Exception e) {
    handleLog(joinPoint, e, null);
  }

  protected void handleLog(final JoinPoint joinPoint, final Exception e, Object jsonResult) {
    try {
      // 获得注解
      Log controllerLog = getAnnotationLog(joinPoint);
      if (controllerLog == null) {
        return;
      }

      // 获取当前的用户
      IoTUser iotUser = ioTUserService.selectUserByUnionId(SecurityUtils.getUnionId());

      // *========数据库日志=========*//
      SysOperLog operLog = new SysOperLog();
      operLog.setStatus(BusinessStatus.SUCCESS.ordinal());
      // 请求的地址
      String ip = ServletUtils.getClientIP();
      operLog.setOperIp(ip);
      operLog.setOperLocation(LocationUtils.getLocationByIp(ip));
      // 返回参数
      operLog.setJsonResult(JSONUtil.toJsonStr(jsonResult));

      operLog.setOperUrl(ServletUtils.getRequest().getRequestURI());
      if (iotUser != null) {
        operLog.setOperName(iotUser.getUsername());
      }

      if (e != null) {
        operLog.setStatus(BusinessStatus.FAIL.ordinal());
        operLog.setErrorMsg(StrUtil.sub(e.getMessage(), 0, 2000));
      }
      // 设置方法名称
      String className = joinPoint.getTarget().getClass().getName();
      String methodName = joinPoint.getSignature().getName();
      operLog.setMethod(className + "." + methodName + "()");
      // 设置请求方式
      operLog.setRequestMethod(ServletUtils.getRequest().getMethod());
      // 处理设置注解上的参数
      getControllerMethodDescription(joinPoint, controllerLog, operLog);
      // 保存数据库
      SpringUtils.getBean(AdminLogService.class).recordOper(operLog);
    } catch (Exception exp) {
      // 记录本地异常日志
      log.error("==前置通知异常==");
      log.error("异常信息", exp);
    }
  }

  /**
   * 获取注解中对方法的描述信息 用于Controller层注解
   *
   * @param log 日志
   * @param operLog 操作日志
   */
  public void getControllerMethodDescription(JoinPoint joinPoint, Log log, SysOperLog operLog)
      throws Exception {
    // 设置action动作
    operLog.setBusinessType(log.businessType().ordinal());
    // 设置标题
    operLog.setTitle(log.title());
    // 设置操作人类别
    operLog.setOperatorType(log.operatorType().ordinal());
    // 是否需要保存request，参数和值
    if (log.isSaveRequestData()) {
      // 获取参数的信息，传入到数据库中。
      setRequestValue(joinPoint, operLog);
    }
  }

  /**
   * 获取请求的参数，放到log中
   *
   * @param operLog 操作日志
   * @throws Exception 异常
   */
  private void setRequestValue(JoinPoint joinPoint, SysOperLog operLog) throws Exception {
    String requestMethod = operLog.getRequestMethod();
    if (HttpMethod.PUT.name().equals(requestMethod)
        || HttpMethod.POST.name().equals(requestMethod)) {
      String params = argsArrayToString(joinPoint.getArgs());
      operLog.setOperParam(StrUtil.sub(params, 0, 2000));
    } else {
      Map<?, ?> paramsMap =
          (Map<?, ?>)
              ServletUtils.getRequest()
                  .getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
      operLog.setOperParam(StrUtil.sub(paramsMap.toString(), 0, 2000));
    }
  }

  /** 是否存在注解，如果存在就获取 */
  private Log getAnnotationLog(JoinPoint joinPoint) throws Exception {
    Signature signature = joinPoint.getSignature();
    MethodSignature methodSignature = (MethodSignature) signature;
    Method method = methodSignature.getMethod();

    if (method != null) {
      return method.getAnnotation(Log.class);
    }
    return null;
  }

  /** 参数拼装 */
  private String argsArrayToString(Object[] paramsArray) {
    StringBuilder params = new StringBuilder();
    if (paramsArray != null && paramsArray.length > 0) {
      for (Object o : paramsArray) {
        if (Validator.isNotNull(o) && !isFilterObject(o)) {
          params.append(JSONUtil.toJsonStr(o)).append(" ");
        }
      }
    }
    return params.toString().trim();
  }

  /**
   * 判断是否需要过滤的对象。
   *
   * @param o 对象信息。
   * @return 如果是需要过滤的对象，则返回true；否则返回false。
   */
  @SuppressWarnings("rawtypes")
  public boolean isFilterObject(final Object o) {
    Class<?> clazz = o.getClass();
    if (clazz.isArray()) {
      return clazz.getComponentType().isAssignableFrom(MultipartFile.class);
    } else if (Collection.class.isAssignableFrom(clazz)) {
      Collection collection = (Collection) o;
      for (Object value : collection) {
        return value instanceof MultipartFile;
      }
    } else if (Map.class.isAssignableFrom(clazz)) {
      Map map = (Map) o;
      for (Object value : map.entrySet()) {
        Map.Entry entry = (Map.Entry) value;
        return entry.getValue() instanceof MultipartFile;
      }
    }
    return o instanceof MultipartFile
        || o instanceof HttpServletRequest
        || o instanceof HttpServletResponse
        || o instanceof BindingResult;
  }
}
