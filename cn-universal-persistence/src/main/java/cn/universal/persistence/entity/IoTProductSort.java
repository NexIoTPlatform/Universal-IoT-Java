/*
 *
 * Copyright (c) 2025, IoT-Universal. All Rights Reserved.
 *
 * @Description: 本文件由 Aleo 开发并拥有版权，未经授权严禁擅自商用、复制或传播。
 * @Author: Aleo
 * @Email: wo8335224@gmail.com
 * @Wechat: outlookFil
 *
 *
 */

package cn.universal.persistence.entity;

import cn.universal.common.annotation.Excel;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 产品分类对象 iot_product_sort @Author Aleo
 *
 * @since 2025-12-29
 */
public class IoTProductSort implements Serializable {

  private static final long serialVersionUID = 1L;

  /** id */
  private String id;

  /** 父id */
  @Excel(name = "父id")
  private String parentId;

  /** 是否有子节点 */
  @Excel(name = "是否有子节点")
  private Integer hasChild;

  /** 标识 */
  @Excel(name = "标识")
  private String identification;

  /** 分类名称 */
  @Excel(name = "分类名称")
  private String classifiedName;

  /** 说明 */
  @Excel(name = "说明")
  private String description;

  /** 创建时间 */
  @Excel(name = "创建时间")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private Date createTime;

  /** 创建者 */
  @Excel(name = "创建者")
  private String createBy;

  /** 子分类 */
  private List<IoTProductSort> children = new ArrayList<IoTProductSort>();

  public List<IoTProductSort> getChildren() {
    return children;
  }

  public void setChildren(List<IoTProductSort> children) {
    this.children = children;
  }

  public static long getSerialVersionUID() {
    return serialVersionUID;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getParentId() {
    return parentId;
  }

  public void setParentId(String parentId) {
    this.parentId = parentId;
  }

  public Integer getHasChild() {
    return hasChild;
  }

  public void setHasChild(Integer hasChild) {
    this.hasChild = hasChild;
  }

  public String getIdentification() {
    return identification;
  }

  public void setIdentification(String identification) {
    this.identification = identification;
  }

  public String getClassifiedName() {
    return classifiedName;
  }

  public void setClassifiedName(String classifiedName) {
    this.classifiedName = classifiedName;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  public String getCreateBy() {
    return createBy;
  }

  public void setCreateBy(String createBy) {
    this.createBy = createBy;
  }
}
