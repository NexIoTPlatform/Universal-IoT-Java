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

package cn.universal.admin.platform.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.universal.admin.common.utils.SecurityUtils;
import cn.universal.admin.platform.service.IIoTProductSortService;
import cn.universal.persistence.entity.IoTProductSort;
import cn.universal.persistence.entity.bo.RuleModelBO.IoTProductSortBO;
import cn.universal.persistence.mapper.IoTProductSortMapper;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 产品分类Service业务层处理 @Author Aleo
 *
 * @since 2025-12-29
 */
@Slf4j
@Service
public class IoTProductSortServiceImpl implements IIoTProductSortService {

  @Resource private IoTProductSortMapper ioTProductSortMapper;

  /**
   * 查询产品分类
   *
   * @param id 产品分类主键
   * @return 产品分类
   */
  @Override
  public IoTProductSort selectDevProductSortById(String id) {
    if (StrUtil.isBlank(id)) {
      log.warn("查询产品分类时ID为空");
      return null;
    }
    return ioTProductSortMapper.selectDevProductSortById(id);
  }

  /**
   * 查询产品分类列表
   *
   * @param ioTProductSort 产品分类查询条件
   * @return 产品分类列表
   */
  @Override
  public List<IoTProductSort> selectDevProductSortList(IoTProductSort ioTProductSort) {
    return ioTProductSortMapper.selectDevProductSortList(ioTProductSort);
  }

  /**
   * 新增产品分类
   *
   * @param ioTProductSort 产品分类
   * @return 结果
   */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public int insertDevProductSort(IoTProductSort ioTProductSort) {
    // 参数验证
    if (ioTProductSort == null) {
      throw new IllegalArgumentException("产品分类信息不能为空");
    }
    if (StrUtil.isBlank(ioTProductSort.getClassifiedName())) {
      throw new IllegalArgumentException("分类名称不能为空");
    }
    if (StrUtil.isBlank(ioTProductSort.getParentId())) {
      throw new IllegalArgumentException("父分类ID不能为空");
    }

    // 检查分类名称是否重复
    if (isClassNameExists(ioTProductSort.getClassifiedName(), ioTProductSort.getParentId(), null)) {
      throw new IllegalArgumentException("同级分类下已存在相同名称的分类");
    }

    // 生成分类ID
    String newId = generateProductSortId(ioTProductSort.getParentId());
    ioTProductSort.setId(newId);

    // 设置默认值
    ioTProductSort.setHasChild(0);
    ioTProductSort.setCreateTime(new Date());
    ioTProductSort.setCreateBy(SecurityUtils.getUnionId());

    // 插入新分类
    int result = ioTProductSortMapper.insertDevProductSort(ioTProductSort);

    // 更新父分类的hasChild状态
    updateParentHasChildStatus(ioTProductSort.getParentId());

    log.info(
        "新增产品分类成功: id={}, name={}, parentId={}",
        newId,
        ioTProductSort.getClassifiedName(),
        ioTProductSort.getParentId());

    return result;
  }

  /**
   * 修改产品分类
   *
   * @param ioTProductSort 产品分类
   * @return 结果
   */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public int updateDevProductSort(IoTProductSort ioTProductSort) {
    // 参数验证
    if (ioTProductSort == null || StrUtil.isBlank(ioTProductSort.getId())) {
      throw new IllegalArgumentException("产品分类ID不能为空");
    }
    if (StrUtil.isBlank(ioTProductSort.getClassifiedName())) {
      throw new IllegalArgumentException("分类名称不能为空");
    }

    // 检查分类是否存在
    IoTProductSort existingSort =
        ioTProductSortMapper.selectDevProductSortById(ioTProductSort.getId());
    if (existingSort == null) {
      throw new IllegalArgumentException("要修改的产品分类不存在");
    }

    // 检查分类名称是否重复（排除自己）
    if (isClassNameExists(
        ioTProductSort.getClassifiedName(), existingSort.getParentId(), ioTProductSort.getId())) {
      throw new IllegalArgumentException("同级分类下已存在相同名称的分类");
    }

    // 不允许修改父分类（避免复杂的树结构变更）
    if (!existingSort.getParentId().equals(ioTProductSort.getParentId())) {
      throw new IllegalArgumentException("不允许修改分类的父级关系");
    }

    // 更新分类信息
    int result = ioTProductSortMapper.updateDevProductSort(ioTProductSort);

    log.info(
        "修改产品分类成功: id={}, name={}", ioTProductSort.getId(), ioTProductSort.getClassifiedName());

    return result;
  }

  /**
   * 批量删除产品分类
   *
   * @param ids 需要删除的产品分类主键数组
   * @return 结果
   */
  @Override
  @Transactional(rollbackFor = Exception.class)
  @CacheEvict(
      cacheNames = {"iot_all_dev_product_list", "iot_dev_product_list"},
      allEntries = true)
  public int deleteDevProductSortByIds(String[] ids) {
    if (ids == null || ids.length == 0) {
      throw new IllegalArgumentException("删除的产品分类ID不能为空");
    }

    int totalDeleted = 0;
    for (String id : ids) {
      if (StrUtil.isNotBlank(id)) {
        totalDeleted += deleteDevProductSortById(id);
      }
    }

    log.info("批量删除产品分类完成: 删除数量={}", totalDeleted);
    return totalDeleted;
  }

  /**
   * 删除产品分类信息
   *
   * @param id 产品分类主键
   * @return 结果
   */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public int deleteDevProductSortById(String id) {
    if (StrUtil.isBlank(id)) {
      throw new IllegalArgumentException("删除的产品分类ID不能为空");
    }

    // 检查分类是否存在
    IoTProductSort ioTProductSort = ioTProductSortMapper.selectDevProductSortById(id);
    if (ioTProductSort == null) {
      throw new IllegalArgumentException("要删除的产品分类不存在");
    }

    // 检查是否有子分类
    List<IoTProductSort> children = ioTProductSortMapper.getListByParentId(id);
    if (!children.isEmpty()) {
      throw new IllegalArgumentException("该分类下还有子分类，请先删除子分类");
    }

    // 删除分类
    int result = ioTProductSortMapper.deleteDevProductSortById(id);

    // 更新父分类的hasChild状态
    updateParentHasChildStatus(ioTProductSort.getParentId());

    log.info("删除产品分类成功: id={}, name={}", id, ioTProductSort.getClassifiedName());

    return result;
  }

  /**
   * 构建前端所需要树结构
   *
   * @param ioTProductSortList 菜单列表
   * @return 树结构列表
   */
  @Override
  public List<IoTProductSort> buildProductSortTree(List<IoTProductSort> ioTProductSortList) {
    if (ioTProductSortList == null || ioTProductSortList.isEmpty()) {
      return new ArrayList<>();
    }

    // 构建ID映射
    List<String> allIds =
        ioTProductSortList.stream().map(IoTProductSort::getId).collect(Collectors.toList());

    // 找出根节点
    List<IoTProductSort> rootNodes =
        ioTProductSortList.stream()
            .filter(sort -> !allIds.contains(sort.getParentId()) || "0".equals(sort.getParentId()))
            .collect(Collectors.toList());

    // 递归构建树结构
    for (IoTProductSort rootNode : rootNodes) {
      buildTreeRecursively(ioTProductSortList, rootNode);
    }

    return rootNodes.isEmpty() ? ioTProductSortList : rootNodes;
  }

  /**
   * 生成产品分类ID
   *
   * @param parentId 父分类ID
   * @return 新的分类ID
   */
  private String generateProductSortId(String parentId) {
    List<IoTProductSort> siblings = ioTProductSortMapper.getListByParentId(parentId);
    if ("0".equals(parentId)) {
      parentId = StrUtil.EMPTY;
    }
    if (siblings.isEmpty()) {
      // 如果是第一个子分类，使用父ID + 100
      return parentId + "100";
    } else {
      // 找到最大的ID并加1
      long maxId =
          siblings.stream()
              .mapToLong(
                  sort -> {
                    try {
                      return Long.parseLong(sort.getId());
                    } catch (NumberFormatException e) {
                      return 0L;
                    }
                  })
              .max()
              .orElse(0L);

      return String.valueOf(maxId + 1);
    }
  }

  /**
   * 检查同级分类下是否存在相同名称
   *
   * @param className 分类名称
   * @param parentId 父分类ID
   * @param excludeId 排除的分类ID（用于更新时排除自己）
   * @return 是否存在
   */
  private boolean isClassNameExists(String className, String parentId, String excludeId) {
    List<IoTProductSort> siblings = ioTProductSortMapper.getListByParentId(parentId);

    return siblings.stream()
        .anyMatch(
            sort -> className.equals(sort.getClassifiedName()) && !sort.getId().equals(excludeId));
  }

  /**
   * 更新父分类的hasChild状态
   *
   * @param parentId 父分类ID
   */
  private void updateParentHasChildStatus(String parentId) {
    if ("0".equals(parentId)) {
      return; // 根分类不需要更新
    }

    IoTProductSort parent = ioTProductSortMapper.selectDevProductSortById(parentId);
    if (parent != null) {
      List<IoTProductSort> children = ioTProductSortMapper.getListByParentId(parentId);
      int hasChild = children.isEmpty() ? 0 : 1;

      if (parent.getHasChild() != hasChild) {
        parent.setHasChild(hasChild);
        ioTProductSortMapper.updateDevProductSort(parent);
      }
    }
  }

  /**
   * 递归构建树结构
   *
   * @param allNodes 所有节点
   * @param currentNode 当前节点
   */
  private void buildTreeRecursively(List<IoTProductSort> allNodes, IoTProductSort currentNode) {
    List<IoTProductSort> children =
        allNodes.stream()
            .filter(node -> currentNode.getId().equals(node.getParentId()))
            .collect(Collectors.toList());

    if (!children.isEmpty()) {
      currentNode.setChildren(children);
      // 递归处理子节点
      for (IoTProductSort child : children) {
        buildTreeRecursively(allNodes, child);
      }
    }
  }

  // ==================== 新增的BO方法实现 ====================

  /**
   * 新增产品分类（使用BO）
   *
   * @param bo 产品分类业务对象
   * @return 结果
   */
  @Override
  public int insertDevProductSort(IoTProductSortBO bo) {
    if (bo == null) {
      throw new IllegalArgumentException("产品分类信息不能为空");
    }

    // 处理parentId，前端可能传入数字0，需要转换为字符串"0"
    if ("0".equals(String.valueOf(bo.getParentId()))) {
      bo.setParentId("0");
    }

    IoTProductSort entity = bo.toEntity();
    return insertDevProductSort(entity);
  }

  /**
   * 修改产品分类（使用BO）
   *
   * @param bo 产品分类业务对象
   * @return 结果
   */
  @Override
  public int updateDevProductSort(IoTProductSortBO bo) {
    if (bo == null) {
      throw new IllegalArgumentException("产品分类信息不能为空");
    }

    // 处理parentId，前端可能传入数字0，需要转换为字符串"0"
    if ("0".equals(String.valueOf(bo.getParentId()))) {
      bo.setParentId("0");
    }

    IoTProductSort entity = bo.toEntity();
    return updateDevProductSort(entity);
  }

  /**
   * 获取产品分类树结构
   *
   * @return 树结构列表
   */
  @Override
  public List<IoTProductSort> getProductSortTree() {
    // 查询所有分类
    List<IoTProductSort> allSorts =
        ioTProductSortMapper.selectDevProductSortList(new IoTProductSort());
    // 构建树结构
    return buildProductSortTree(allSorts);
  }
}
