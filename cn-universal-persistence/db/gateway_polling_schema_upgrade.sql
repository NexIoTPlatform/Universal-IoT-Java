-- ============================================
-- 网关云端轮询功能 - 数据库升级脚本
-- 用途: 将单一 deviceId 改为 productKey + deviceId 组合键
-- 执行时间: 2025-10-26
-- 注意: 如果是全新安装，请直接执行 gateway_polling_schema.sql
-- ============================================

-- Step 1: 修改 iot_gateway_polling_config 表的唯一索引
-- ============================================

-- 1.1 删除旧的唯一索引
ALTER TABLE `iot_gateway_polling_config` DROP INDEX IF EXISTS `uk_device`;

-- 1.2 添加新的组合唯一索引
ALTER TABLE `iot_gateway_polling_config`
    ADD UNIQUE KEY `uk_device` (`product_key`, `device_id`);

-- Step 2: 修改 iot_gateway_polling_command 表
-- ============================================

-- 2.1 添加 gateway_product_key 字段
ALTER TABLE `iot_gateway_polling_command`
    ADD COLUMN `gateway_product_key` VARCHAR(128) NULL COMMENT '网关产品KEY' AFTER `id`;

-- 2.2 更新现有数据 (如果有)
-- 通过 gateway_device_id 关联到 iot_device 表，获取 product_key
UPDATE `iot_gateway_polling_command` c
    INNER JOIN `iot_device` d
ON c.`gateway_device_id` = d.`device_id`
    SET c.`gateway_product_key` = d.`product_key`
WHERE c.`gateway_product_key` IS NULL OR c.`gateway_product_key` = '';

-- 2.3 将字段设置为 NOT NULL (确保所有数据已更新)
ALTER TABLE `iot_gateway_polling_command`
    MODIFY COLUMN `gateway_product_key` VARCHAR (128) NOT NULL COMMENT '网关产品KEY';

-- 2.4 删除旧索引
ALTER TABLE `iot_gateway_polling_command` DROP INDEX IF EXISTS `idx_gateway`;

-- 2.5 添加新索引
ALTER TABLE `iot_gateway_polling_command`
    ADD INDEX `idx_gateway` (`gateway_product_key`, `gateway_device_id`, `execution_order`);

-- ============================================
-- 验证脚本
-- ============================================

-- 验证 iot_gateway_polling_config 表的索引
SHOW
INDEX FROM `iot_gateway_polling_config` WHERE `Key_name` = 'uk_device';

-- 验证 iot_gateway_polling_command 表的字段
DESC `iot_gateway_polling_command`;

-- 验证 iot_gateway_polling_command 表的索引
SHOW
INDEX FROM `iot_gateway_polling_command` WHERE `Key_name` = 'idx_gateway';

-- 验证数据完整性 (应该返回 0)
SELECT COUNT(*) AS missing_product_key_count
FROM `iot_gateway_polling_command`
WHERE `gateway_product_key` IS NULL
   OR `gateway_product_key` = '';

-- ============================================
-- 回滚脚本 (如果需要)
-- ============================================

/*
-- 回滚 Step 2
ALTER TABLE `iot_gateway_polling_command` DROP INDEX IF EXISTS `idx_gateway`;
ALTER TABLE `iot_gateway_polling_command` ADD INDEX `idx_gateway` (`gateway_device_id`, `execution_order`);
ALTER TABLE `iot_gateway_polling_command` DROP COLUMN `gateway_product_key`;

-- 回滚 Step 1
ALTER TABLE `iot_gateway_polling_config` DROP INDEX IF EXISTS `uk_device`;
ALTER TABLE `iot_gateway_polling_config` ADD UNIQUE KEY `uk_device` (`device_id`);
*/
