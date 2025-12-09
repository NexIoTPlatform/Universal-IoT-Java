-- ============================================
-- 网关云端轮询功能 - 数据库表结构
-- 创建日期: 2025-10-26
-- 说明: 用于实现DTU网关的云端轮询功能，支持多指令配置
-- ============================================

-- ============================================
-- 1. 网关轮询配置表
-- ============================================
CREATE TABLE `iot_gateway_polling_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `device_id` VARCHAR(128) NOT NULL COMMENT '网关设备ID',
  `product_key` VARCHAR(128) NOT NULL COMMENT '产品KEY',
  `iot_id` VARCHAR(128) DEFAULT NULL COMMENT 'IoT ID',
  
  -- 轮询配置
  `enabled` TINYINT(1) DEFAULT 1 COMMENT '是否启用轮询: 0-禁用, 1-启用',
  `interval_seconds` INT NOT NULL DEFAULT 120 COMMENT '轮询间隔(秒): 30/60/120/300/600',
  `timeout_seconds` INT DEFAULT 10 COMMENT '超时时间(秒)',
  `retry_times` INT DEFAULT 3 COMMENT '失败重试次数',
  
  -- 运行状态
  `next_poll_time` DATETIME DEFAULT NULL COMMENT '下次轮询时间',
  `last_poll_time` DATETIME DEFAULT NULL COMMENT '最后轮询时间',
  `last_success_time` DATETIME DEFAULT NULL COMMENT '最后成功时间',
  `continuous_fail_count` INT DEFAULT 0 COMMENT '连续失败次数',
  `polling_status` VARCHAR(20) DEFAULT 'NORMAL' COMMENT '轮询状态: NORMAL-正常, PAUSED-暂停, FAILED-失败',
  
  -- 统计信息
  `total_poll_count` BIGINT DEFAULT 0 COMMENT '总轮询次数',
  `success_count` BIGINT DEFAULT 0 COMMENT '成功次数',
  `fail_count` BIGINT DEFAULT 0 COMMENT '失败次数',
  
  -- 审计字段
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `creator_id` VARCHAR(64) DEFAULT NULL COMMENT '创建人ID',
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_device` (`product_key`, `device_id`),
  KEY `idx_next_poll` (`interval_seconds`, `next_poll_time`, `polling_status`),
  KEY `idx_product_key` (`product_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='网关轮询配置表';

-- ============================================
-- 2. 网关轮询指令表
-- ============================================
CREATE TABLE `iot_gateway_polling_command` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `gateway_product_key` VARCHAR(128) NOT NULL COMMENT '网关产品KEY',
  `gateway_device_id` VARCHAR(128) NOT NULL COMMENT '网关设备ID',
  `slave_device_id` VARCHAR(128) DEFAULT NULL COMMENT '从站设备ID (可选，用于标识)',
  `command_name` VARCHAR(128) NOT NULL COMMENT '指令名称',
  `execution_order` INT DEFAULT 0 COMMENT '执行顺序',
  
  -- 核心字段：完整指令 (前端生成)
  `command_hex` VARCHAR(1024) NOT NULL COMMENT '完整的轮询指令(HEX格式)',
  `command_type` VARCHAR(32) DEFAULT 'MODBUS' COMMENT '指令类型: MODBUS/S7/OPCUA/CUSTOM',
  `protocol_params` TEXT COMMENT '协议参数JSON (可选，用于前端回显编辑)',
  
  -- 可选：用于数据解析
  `property_mapping` TEXT COMMENT '属性映射JSON (寄存器->物模型属性)',
  `data_parser_script` TEXT COMMENT '数据解析脚本 (可选)',
  
  -- 控制参数
  `enabled` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
  `timeout_ms` INT DEFAULT 5000 COMMENT '超时时间(ms)',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
  
  -- 审计字段
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  PRIMARY KEY (`id`),
  KEY `idx_gateway` (`gateway_product_key`, `gateway_device_id`, `execution_order`),
  KEY `idx_slave` (`slave_device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='网关轮询指令表';

-- ============================================
-- 3. 索引优化
-- ============================================
-- 高效查询待轮询设备
CREATE INDEX idx_polling_due ON iot_gateway_polling_config(
  interval_seconds, 
  next_poll_time, 
  polling_status,
  enabled
);

-- ============================================
-- 4. 示例数据 (可选)
-- ============================================
-- 示例: DTU网关轮询配置
-- INSERT INTO iot_gateway_polling_config 
-- (device_id, product_key, enabled, interval_seconds, timeout_seconds, retry_times, next_poll_time)
-- VALUES 
-- ('dtu_gateway_001', 'modbus_dtu_product', 1, 30, 10, 3, NOW());

-- 示例: 从站温度采集指令
-- INSERT INTO iot_gateway_polling_command 
-- (gateway_device_id, slave_device_id, command_name, execution_order, command_hex, command_type, protocol_params)
-- VALUES 
-- ('dtu_gateway_001', 'slave_temp_001', '读取温度', 1, '010300000002C40B', 'MODBUS', 
--  '{"functionCode":3,"slaveAddress":1,"registerAddress":0,"registerCount":2}');
