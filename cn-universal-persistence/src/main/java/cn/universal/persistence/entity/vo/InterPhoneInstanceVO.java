/*
 *
 * Copyright (c) 2025, IoT-Universal. All Rights Reserved.
 *
 * @Description: 本文件由 gitee.com/NexIoT 开发并拥有版权，未经授权严禁擅自商用、复制或传播。
 * @Author: gitee.com/NexIoT

 * @Wechat: outlookFil
 *
 *
 */

package cn.universal.persistence.entity.vo;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InterPhoneInstanceVO {

  private static final long serialVersionUID = 1L;

  /** 设备自身序号 */
  private String deviceId;

  /** 设备实例名称 */
  private String deviceName;

  /** 0-离线，1-在线 */
  private Integer state;

  /** 设备创建时间 */
  private Long createTime;

  /** 设备上线时间 */
  private Long onlineTime;

  /** 经度 */
  private BigDecimal longitude;

  /** 纬度 */
  private BigDecimal latitude;

  /** 经纬度 */
  private String coordinate;

  /** 设备属性值 */
  @Builder.Default private Map<String, Object> metadata = new HashMap<>();

  public void setCoordinate(String coordinate) {
    this.coordinate = coordinate;
    if (StrUtil.isNotEmpty(coordinate)) {
      String[] split = coordinate.split(",");
      longitude = new BigDecimal(split[0]);
      latitude = new BigDecimal(split[1]);
    }
  }

  public void setMetadata(String me) {
    if (StrUtil.isNotEmpty(me)) {
      JSONObject obj = JSONUtil.parseObj(me);
      if (Objects.nonNull(obj) && obj.containsKey("state")) {
        JSONObject state = obj.getJSONObject("state");
        if (Objects.nonNull(state) && state.containsKey("reported")) {
          JSONObject reported = state.getJSONObject("reported");
          metadata = reported;
        }
      }
    }
  }
}
