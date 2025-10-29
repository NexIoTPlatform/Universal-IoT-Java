-- =============================================
-- 步骤1：删除所有现有相关旧表（⚠️ 彻底清理，避免残留）
-- =============================================
-- 1.1 删除 iot_device_log_0~23 所有分表
DROP TABLE IF EXISTS
    `iot_device_log_0`, `iot_device_log_1`, `iot_device_log_2`, `iot_device_log_3`,
    `iot_device_log_4`, `iot_device_log_5`, `iot_device_log_6`, `iot_device_log_7`,
    `iot_device_log_8`, `iot_device_log_9`, `iot_device_log_10`, `iot_device_log_11`,
    `iot_device_log_12`, `iot_device_log_13`, `iot_device_log_14`, `iot_device_log_15`,
    `iot_device_log_16`, `iot_device_log_17`, `iot_device_log_18`, `iot_device_log_19`,
    `iot_device_log_20`, `iot_device_log_21`, `iot_device_log_22`, `iot_device_log_23`;

-- 1.2 删除 iot_device_log_metadata_0~23 所有分表
DROP TABLE IF EXISTS
    `iot_device_log_metadata_0`, `iot_device_log_metadata_1`, `iot_device_log_metadata_2`,
    `iot_device_log_metadata_3`, `iot_device_log_metadata_4`, `iot_device_log_metadata_5`,
    `iot_device_log_metadata_6`, `iot_device_log_metadata_7`, `iot_device_log_metadata_8`,
    `iot_device_log_metadata_9`, `iot_device_log_metadata_10`, `iot_device_log_metadata_11`,
    `iot_device_log_metadata_12`, `iot_device_log_metadata_13`, `iot_device_log_metadata_14`,
    `iot_device_log_metadata_15`, `iot_device_log_metadata_16`, `iot_device_log_metadata_17`,
    `iot_device_log_metadata_18`, `iot_device_log_metadata_19`, `iot_device_log_metadata_20`,
    `iot_device_log_metadata_21`, `iot_device_log_metadata_22`, `iot_device_log_metadata_23`;

-- =============================================
-- 步骤2：创建第一种类型分表：iot_device_log_0~23（共24个，每个分表内按时间分区）
-- =============================================
-- 2.1 创建 iot_device_log_0（分表示例，0~23结构完全一致，仅表名不同）
CREATE TABLE `iot_device_log_0`
(
    `id`             bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `iot_id`         varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  DEFAULT NULL COMMENT '设备唯一编码',
    `device_id`      varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  DEFAULT NULL COMMENT '设备自身序号',
    `ext_device_id`  varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  DEFAULT '' COMMENT '第三方设备ID',
    `product_key`    varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  DEFAULT '' COMMENT '产品ID',
    `device_name`    varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  DEFAULT NULL COMMENT '设备名称',
    `message_type`   varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  DEFAULT '' COMMENT '消息类型（EVENT/PROPERTIES/FUNCTIONS）',
    `command_id`     varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  DEFAULT NULL COMMENT '指令ID',
    `command_status` tinyint                                                DEFAULT '0' COMMENT '指令状态（0未执行/1成功/2失败）',
    `event`          varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  DEFAULT '' COMMENT '事件名称（如online/offline）',
    `company_no`     varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  DEFAULT NULL COMMENT '公司编号',
    `protocol`       varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  DEFAULT NULL COMMENT '通信协议（MQTT/TCP）',
    `device_node`    varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  DEFAULT NULL COMMENT '节点类型',
    `classified_id`  varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '设备分类ID',
    `org_id`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  DEFAULT NULL COMMENT '所属机构ID',
    `create_id`      varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  DEFAULT NULL COMMENT '操作人ID',
    `instance`       varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  DEFAULT '0' COMMENT '实例名称',
    `point`          varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '经纬度（经度,纬度）',
    `content`        text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin COMMENT '日志详情（JSON格式）',
    `create_time`    datetime NOT NULL                                      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（无0值）',
    PRIMARY KEY (`id`, `create_time`) USING BTREE,                                  -- 主键含分区键，确保分区生效
    KEY              `idx_iot_id` (`iot_id`) USING BTREE,                           -- 按设备编码查询
    KEY              `idx_product_device` (`product_key`, `device_id`) USING BTREE, -- 按产品+设备ID查询
    KEY              `idx_event_time` (`event`, `create_time`) USING BTREE          -- 按事件+时间查询
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT '设备日志分表0（按iotId哈希，内部分区）'
-- 分表内按时间分区（2025-09起，预留pmax兜底）
PARTITION BY RANGE (TO_DAYS(`create_time`)) (
  PARTITION `p202509` VALUES LESS THAN (TO_DAYS('2025-10-01')), -- 2025年9月数据
  PARTITION `p202510` VALUES LESS THAN (TO_DAYS('2025-11-01')), -- 2025年10月数据
  PARTITION `p202511` VALUES LESS THAN (TO_DAYS('2025-12-01')), -- 2025年11月数据
  PARTITION `pmax` VALUES LESS THAN MAXVALUE -- 兜底分区（未创建月份数据临时存入）
);

-- 2.2 创建 iot_device_log_1~23（复制log_0结构，仅表名不同）
CREATE TABLE `iot_device_log_1` LIKE `iot_device_log_0`;
ALTER TABLE `iot_device_log_1` COMMENT '设备日志分表1（按iotId哈希，内部分区）';

CREATE TABLE `iot_device_log_2` LIKE `iot_device_log_0`;
ALTER TABLE `iot_device_log_2` COMMENT '设备日志分表2（按iotId哈希，内部分区）';

CREATE TABLE `iot_device_log_3` LIKE `iot_device_log_0`;
ALTER TABLE `iot_device_log_3` COMMENT '设备日志分表3（按iotId哈希，内部分区）';

CREATE TABLE `iot_device_log_4` LIKE `iot_device_log_0`;
ALTER TABLE `iot_device_log_4` COMMENT '设备日志分表4（按iotId哈希，内部分区）';

CREATE TABLE `iot_device_log_5` LIKE `iot_device_log_0`;
ALTER TABLE `iot_device_log_5` COMMENT '设备日志分表5（按iotId哈希，内部分区）';

CREATE TABLE `iot_device_log_6` LIKE `iot_device_log_0`;
ALTER TABLE `iot_device_log_6` COMMENT '设备日志分表6（按iotId哈希，内部分区）';

CREATE TABLE `iot_device_log_7` LIKE `iot_device_log_0`;
ALTER TABLE `iot_device_log_7` COMMENT '设备日志分表7（按iotId哈希，内部分区）';

CREATE TABLE `iot_device_log_8` LIKE `iot_device_log_0`;
ALTER TABLE `iot_device_log_8` COMMENT '设备日志分表8（按iotId哈希，内部分区）';

CREATE TABLE `iot_device_log_9` LIKE `iot_device_log_0`;
ALTER TABLE `iot_device_log_9` COMMENT '设备日志分表9（按iotId哈希，内部分区）';

CREATE TABLE `iot_device_log_10` LIKE `iot_device_log_0`;
ALTER TABLE `iot_device_log_10` COMMENT '设备日志分表10（按iotId哈希，内部分区）';

CREATE TABLE `iot_device_log_11` LIKE `iot_device_log_0`;
ALTER TABLE `iot_device_log_11` COMMENT '设备日志分表11（按iotId哈希，内部分区）';

CREATE TABLE `iot_device_log_12` LIKE `iot_device_log_0`;
ALTER TABLE `iot_device_log_12` COMMENT '设备日志分表12（按iotId哈希，内部分区）';

CREATE TABLE `iot_device_log_13` LIKE `iot_device_log_0`;
ALTER TABLE `iot_device_log_13` COMMENT '设备日志分表13（按iotId哈希，内部分区）';

CREATE TABLE `iot_device_log_14` LIKE `iot_device_log_0`;
ALTER TABLE `iot_device_log_14` COMMENT '设备日志分表14（按iotId哈希，内部分区）';

CREATE TABLE `iot_device_log_15` LIKE `iot_device_log_0`;
ALTER TABLE `iot_device_log_15` COMMENT '设备日志分表15（按iotId哈希，内部分区）';

CREATE TABLE `iot_device_log_16` LIKE `iot_device_log_0`;
ALTER TABLE `iot_device_log_16` COMMENT '设备日志分表16（按iotId哈希，内部分区）';

CREATE TABLE `iot_device_log_17` LIKE `iot_device_log_0`;
ALTER TABLE `iot_device_log_17` COMMENT '设备日志分表17（按iotId哈希，内部分区）';

CREATE TABLE `iot_device_log_18` LIKE `iot_device_log_0`;
ALTER TABLE `iot_device_log_18` COMMENT '设备日志分表18（按iotId哈希，内部分区）';

CREATE TABLE `iot_device_log_19` LIKE `iot_device_log_0`;
ALTER TABLE `iot_device_log_19` COMMENT '设备日志分表19（按iotId哈希，内部分区）';

CREATE TABLE `iot_device_log_20` LIKE `iot_device_log_0`;
ALTER TABLE `iot_device_log_20` COMMENT '设备日志分表20（按iotId哈希，内部分区）';

CREATE TABLE `iot_device_log_21` LIKE `iot_device_log_0`;
ALTER TABLE `iot_device_log_21` COMMENT '设备日志分表21（按iotId哈希，内部分区）';

CREATE TABLE `iot_device_log_22` LIKE `iot_device_log_0`;
ALTER TABLE `iot_device_log_22` COMMENT '设备日志分表22（按iotId哈希，内部分区）';

CREATE TABLE `iot_device_log_23` LIKE `iot_device_log_0`;
ALTER TABLE `iot_device_log_23` COMMENT '设备日志分表23（按iotId哈希，内部分区）';

-- =============================================
-- 步骤3：创建第二种类型分表：iot_device_log_metadata_0~23（共24个，内部分区）
-- =============================================
-- 3.1 创建 iot_device_log_metadata_0（分表示例，0~23结构一致）
CREATE TABLE `iot_device_log_metadata_0`
(
    `id`           int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `iot_id`       varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '设备唯一编码',
    `product_key`  varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '产品ID',
    `device_name`  varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin          DEFAULT '' COMMENT '设备名称',
    `device_id`    varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin          DEFAULT NULL COMMENT '设备自身序号',
    `message_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin          DEFAULT '' COMMENT '消息类型',
    `event`        varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin          DEFAULT '' COMMENT '事件名称',
    `property`     varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin          DEFAULT '' COMMENT '设备属性（如battery/csq）',
    `content`      text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin COMMENT '元数据详情',
    `ext1`         varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin         DEFAULT '' COMMENT '扩展字段1',
    `ext2`         varchar(655) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin         DEFAULT NULL COMMENT '扩展字段2（长文本）',
    `ext3`         varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin         DEFAULT '' COMMENT '扩展字段3',
    `create_time`  datetime                                              NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（无0值）',
    PRIMARY KEY (`id`, `create_time`) USING BTREE,                             -- 主键含分区键
    KEY            `idx_iot_id` (`iot_id`) USING BTREE,
    KEY            `idx_product_device` (`product_key`, `device_id`) USING BTREE,
    KEY            `idx_time_property` (`create_time`, `property`) USING BTREE -- 按时间+属性查询
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT '设备日志元数据分表0（按iotId哈希，内部分区）'
-- 分表内按时间分区（与log分表对齐）
PARTITION BY RANGE (TO_DAYS(`create_time`)) (
  PARTITION `p202509` VALUES LESS THAN (TO_DAYS('2025-10-01')),
  PARTITION `p202510` VALUES LESS THAN (TO_DAYS('2025-11-01')),
  PARTITION `p202511` VALUES LESS THAN (TO_DAYS('2025-12-01')),
  PARTITION `pmax` VALUES LESS THAN MAXVALUE
);

-- 3.2 创建 iot_device_log_metadata_1~23（复制metadata_0结构）
CREATE TABLE `iot_device_log_metadata_1` LIKE `iot_device_log_metadata_0`;
ALTER TABLE `iot_device_log_metadata_1` COMMENT '设备日志元数据分表1（按iotId哈希，内部分区）';

CREATE TABLE `iot_device_log_metadata_2` LIKE `iot_device_log_metadata_0`;
ALTER TABLE `iot_device_log_metadata_2` COMMENT '设备日志元数据分表2（按iotId哈希，内部分区）';

CREATE TABLE `iot_device_log_metadata_3` LIKE `iot_device_log_metadata_0`;
ALTER TABLE `iot_device_log_metadata_3` COMMENT '设备日志元数据分表3（按iotId哈希，内部分区）';

CREATE TABLE `iot_device_log_metadata_4` LIKE `iot_device_log_metadata_0`;
ALTER TABLE `iot_device_log_metadata_4` COMMENT '设备日志元数据分表4（按iotId哈希，内部分区）';

CREATE TABLE `iot_device_log_metadata_5` LIKE `iot_device_log_metadata_0`;
ALTER TABLE `iot_device_log_metadata_5` COMMENT '设备日志元数据分表5（按iotId哈希，内部分区）';

CREATE TABLE `iot_device_log_metadata_6` LIKE `iot_device_log_metadata_0`;
ALTER TABLE `iot_device_log_metadata_6` COMMENT '设备日志元数据分表6（按iotId哈希，内部分区）';

CREATE TABLE `iot_device_log_metadata_7` LIKE `iot_device_log_metadata_0`;
ALTER TABLE `iot_device_log_metadata_7` COMMENT '设备日志元数据分表7（按iotId哈希，内部分区）';

CREATE TABLE `iot_device_log_metadata_8` LIKE `iot_device_log_metadata_0`;
ALTER TABLE `iot_device_log_metadata_8` COMMENT '设备日志元数据分表8（按iotId哈希，内部分区）';

CREATE TABLE `iot_device_log_metadata_9` LIKE `iot_device_log_metadata_0`;
ALTER TABLE `iot_device_log_metadata_9` COMMENT '设备日志元数据分表9（按iotId哈希，内部分区）';

CREATE TABLE `iot_device_log_metadata_10` LIKE `iot_device_log_metadata_0`;
ALTER TABLE `iot_device_log_metadata_10` COMMENT '设备日志元数据分表10（按iotId哈希，内部分区）';

CREATE TABLE `iot_device_log_metadata_11` LIKE `iot_device_log_metadata_0`;
ALTER TABLE `iot_device_log_metadata_11` COMMENT '设备日志元数据分表11（按iotId哈希，内部分区）';

CREATE TABLE `iot_device_log_metadata_12` LIKE `iot_device_log_metadata_0`;
ALTER TABLE `iot_device_log_metadata_12` COMMENT '设备日志元数据分表12（按iotId哈希，内部分区）';

CREATE TABLE `iot_device_log_metadata_13` LIKE `iot_device_log_metadata_0`;
ALTER TABLE `iot_device_log_metadata_13` COMMENT '设备日志元数据分表13（按iotId哈希，内部分区）';

CREATE TABLE `iot_device_log_metadata_14` LIKE `iot_device_log_metadata_0`;
ALTER TABLE `iot_device_log_metadata_14` COMMENT '设备日志元数据分表14（按iotId哈希，内部分区）';

CREATE TABLE `iot_device_log_metadata_15` LIKE `iot_device_log_metadata_0`;
ALTER TABLE `iot_device_log_metadata_15` COMMENT '设备日志元数据分表15（按iotId哈希，内部分区）';

CREATE TABLE `iot_device_log_metadata_16` LIKE `iot_device_log_metadata_0`;
ALTER TABLE `iot_device_log_metadata_16` COMMENT '设备日志元数据分表16（按iotId哈希，内部分区）';

CREATE TABLE `iot_device_log_metadata_17` LIKE `iot_device_log_metadata_0`;
ALTER TABLE `iot_device_log_metadata_17` COMMENT '设备日志元数据分表17（按iotId哈希，内部分区）';

CREATE TABLE `iot_device_log_metadata_18` LIKE `iot_device_log_metadata_0`;
ALTER TABLE `iot_device_log_metadata_18` COMMENT '设备日志元数据分表18（按iotId哈希，内部分区）';

CREATE TABLE `iot_device_log_metadata_19` LIKE `iot_device_log_metadata_0`;
ALTER TABLE `iot_device_log_metadata_19` COMMENT '设备日志元数据分表19（按iotId哈希，内部分区）';

CREATE TABLE `iot_device_log_metadata_20` LIKE `iot_device_log_metadata_0`;
ALTER TABLE `iot_device_log_metadata_20` COMMENT '设备日志元数据分表20（按iotId哈希，内部分区）';

CREATE TABLE `iot_device_log_metadata_21` LIKE `iot_device_log_metadata_0`;
ALTER TABLE `iot_device_log_metadata_21` COMMENT '设备日志元数据分表21（按iotId哈希，内部分区）';

CREATE TABLE `iot_device_log_metadata_22` LIKE `iot_device_log_metadata_0`;
ALTER TABLE `iot_device_log_metadata_22` COMMENT '设备日志元数据分表22（按iotId哈希，内部分区）';

CREATE TABLE `iot_device_log_metadata_23` LIKE `iot_device_log_metadata_0`;
ALTER TABLE `iot_device_log_metadata_23` COMMENT '设备日志元数据分表23（按iotId哈希，内部分区）';

-- =============================================
-- 步骤4：创建自动分区存储过程（批量为所有分表新增下月分区）
-- 功能：1次执行，为 log_0~23 和 metadata_0~23 共48个分表同步新增分区
-- =============================================
DELIMITER
//
-- 先删除旧存储过程（避免重复创建报错）
DROP PROCEDURE IF EXISTS `auto_add_partition_for_all_shards`;
//
CREATE PROCEDURE `auto_add_partition_for_all_shards`()
BEGIN
    -- 变量定义：分表索引（0~23）、时间、分区名、表名
    DECLARE
`shard_idx` INT DEFAULT 0;
    DECLARE
`next_month` DATE;
    DECLARE
`next_next_month` DATE;
    DECLARE
`partition_name` VARCHAR(20);
    DECLARE
`log_table_name` VARCHAR(50); -- log分表名（如iot_device_log_0）
    DECLARE
`metadata_table_name` VARCHAR(50); -- metadata分表名（如iot_device_log_metadata_0）

    -- 1. 计算分区时间：下月1号（分区名）、下下月1号（分区边界）
    SET
`next_month` = DATE_FORMAT(CURDATE() + INTERVAL 1 MONTH, '%Y-%m-01');
    SET
`next_next_month` = DATE_FORMAT(CURDATE() + INTERVAL 2 MONTH, '%Y-%m-01');
    SET
`partition_name` = CONCAT('p', DATE_FORMAT(`next_month`, '%Y%m')); -- 分区名：p202512

    -- 2. 循环处理0~23所有分表（同时处理log和metadata分表）
    WHILE
`shard_idx` < 24 DO
        -- 2.1 拼接当前分表名
        SET `log_table_name` = CONCAT('iot_device_log_', `shard_idx`);
        SET
`metadata_table_name` = CONCAT('iot_device_log_metadata_', `shard_idx`);

        -- 2.2 为 log分表 新增分区
        SET
@sql_log = CONCAT(
            'ALTER TABLE ', `log_table_name`, ' ADD PARTITION (',
            'PARTITION ', `partition_name`, ' VALUES LESS THAN (TO_DAYS(\'', `next_next_month`, '
\'))',
            ');'
        );
PREPARE stmt_log FROM @sql_log;
EXECUTE stmt_log;
DEALLOCATE PREPARE stmt_log;

-- 2.3 为 metadata分表 新增分区（与log分表对齐）
SET
@sql_metadata = CONCAT(
            'ALTER TABLE ', `metadata_table_name`, ' ADD PARTITION (',
            'PARTITION ', `partition_name`, ' VALUES LESS THAN (TO_DAYS(\'', `next_next_month`, '
\'))',
            ');'
        );
PREPARE stmt_metadata FROM @sql_metadata;
EXECUTE stmt_metadata;
DEALLOCATE PREPARE stmt_metadata;

-- 2.4 打印执行结果（便于验证）
SELECT CONCAT('✅ 已为分表 [', `log_table_name`, '] 和 [', `metadata_table_name`, '] 新增分区：',
              `partition_name`) AS `result`;

-- 分表索引+1，继续下一张
SET
`shard_idx` = `shard_idx` + 1;
END WHILE;

    -- 3. 最终提示
SELECT CONCAT('🎉 所有分表分区新增完成！本次新增分区：', `partition_name`, '，时间范围：', `next_month`, ' ~ ',
              DATE_SUB(`next_next_month`, INTERVAL 1 DAY)) AS `final_result`;
END
//
DELIMITER ;

-- =============================================
-- 步骤5：验证脚本执行结果（确认分表和存储过程创建成功）
-- =============================================
-- 5.1 确认 log分表 已创建（共24个）
SELECT TABLE_NAME, TABLE_COMMENT
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = 'univ'
  AND TABLE_NAME LIKE 'iot_device_log_%'
  AND TABLE_NAME NOT IN ('iot_device_log');
-- 排除非分表

-- 5.2 确认 metadata分表 已创建（共24个）
SELECT TABLE_NAME, TABLE_COMMENT
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = 'univ'
  AND TABLE_NAME LIKE 'iot_device_log_metadata_%'
  AND TABLE_NAME NOT IN ('iot_device_log_metadata');
-- 排除非分表

-- 5.3 确认自动分区存储过程已创建
SELECT ROUTINE_NAME, ROUTINE_TYPE
FROM INFORMATION_SCHEMA.ROUTINES
WHERE ROUTINE_SCHEMA = 'univ'
  AND ROUTINE_NAME = 'auto_add_partition_for_all_shards';