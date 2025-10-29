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

package cn.universal.persistence.entity.admin.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/** 路由配置信息 @Author ruoyi */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RouterVo {

  /** 路由名字 */
  private String name;

  /** 路由地址 */
  private String path;

  /** 是否隐藏路由，当设置 true 的时候该路由不会再侧边栏出现 */
  private boolean hidden;

  /** 重定向地址，当设置 noRedirect 的时候该路由在面包屑导航中不可被点击 */
  private String redirect;

  /** 组件地址 */
  private String component;

  /** 当你一个路由下面的 children 声明的路由大于1个时，自动会变成嵌套的模式--如组件页面 */
  private Boolean alwaysShow;

  /** 其他元素 */
  private MetaVo meta;

  /** 子路由 */
  private List<RouterVo> children;
}
