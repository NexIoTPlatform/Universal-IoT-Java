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

package cn.universal.persistence.consistent;

import cn.hutool.core.util.ObjectUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.PostConstruct;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** 根据设备iotId的hash值分表 @Author gitee.com/NexIoT */
@Component
public class DeviceMetaShardingRouter implements ITableShardingStrategy {

  private static Integer maxTableShard;
  private static Integer virtualNode;

  /** 日志分表是否开启 */
  @Value("${shard.logMeta.enable}")
  private Boolean enable;

  /** 日志分表数量 */
  @Value("${shard.logMeta.table.number}")
  public void setMaxTableShard(Integer maxTableShard) {
    DeviceMetaShardingRouter.maxTableShard = maxTableShard;
  }

  /** 每个实际节点对应节点数量 */
  @Value("${shard.logMeta.virtual.number}")
  public void setVirtualNode(Integer virtualNode) {
    DeviceMetaShardingRouter.virtualNode = virtualNode;
  }

  /** key表示日志表序号的hash值，value表示日志表序号 */
  private static SortedMap<Integer, Integer> sortedMap = new TreeMap<>();

  private static Cache<String, Integer> tableNoMap =
      Caffeine.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).maximumSize(2048).build();

  //  static {
  //    for (int i = 0; i < maxTableShard; i++) {
  //      for (int j = 0; j <= virtualNode; j++) {
  //        int hash = getHash("ww048gf52544ea28frece10a1a#" + i + "_" + j);
  //        sortedMap.put(hash, i);
  //      }
  //    }
  //  }
  @PostConstruct
  private void initMap() {
    for (int i = 0; i < maxTableShard; i++) {
      for (int j = 0; j <= virtualNode; j++) {
        int hash = getHash("ww048gf52544ea28frece10a1a#" + i + "_" + j);
        sortedMap.put(hash, i);
      }
    }
  }

  @Override
  public String generateTableName(String tableNamePrefix, Object value) {
    if (!enable) {
      return tableNamePrefix;
    }
    verificationTableNamePrefix(tableNamePrefix);
    if (ObjectUtil.isEmpty(value)) {
      throw new RuntimeException("日志事件表分表异常，设备iotId为空");
    }
    String iotId = value.toString();
    // 获取分表序号
    Integer tableNo = tableNoMap.getIfPresent(iotId);
    if (tableNo == null) {
      tableNo = convertToHash(iotId);
    }
    return tableNamePrefix + "_" + tableNo;
  }

  /** 根据设备iotId转换hash获取分表序号 */
  private int convertToHash(String iotId) {
    int hash = getHash(iotId);
    SortedMap<Integer, Integer> subMap = sortedMap.tailMap(hash);
    if (subMap.isEmpty()) {
      // 如果没有比该key的hash值大的，则从第一个node开始
      Integer i = sortedMap.firstKey();
      // 返回对应的日志表序号
      Integer tableNo = sortedMap.get(i);
      tableNoMap.put(iotId, tableNo);
      return tableNo;
    } else {
      // 第一个Key就是顺时针过去离node最近的那个结点
      Integer i = subMap.firstKey();
      // 返回对应的日志表序号
      Integer tableNo = subMap.get(i);
      tableNoMap.put(iotId, tableNo);
      return tableNo;
    }
    //    int hash = mod(getHash(iotId), HASH_RING);
    //    hash = hash % MAX_TABLE_SHARD;

    //    return hash;
  }

  /** 计算Hash值, 使用FNV1_32_HASH算法 */
  public static int getHash(String str) {
    final int p = 16777619;
    int hash = (int) 2166136261L;
    for (int i = 0; i < str.length(); i++) {
      hash = (hash ^ str.charAt(i)) * p;
    }
    hash += hash << 13;
    hash ^= hash >> 7;
    hash += hash << 3;
    hash ^= hash >> 17;
    hash += hash << 5;

    if (hash < 0) {
      hash = Math.abs(hash);
    }
    return hash;
  }
}
