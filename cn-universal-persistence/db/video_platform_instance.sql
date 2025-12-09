-- 视频平台实例表
CREATE TABLE IF NOT EXISTS `video_platform_instance` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `platform_type` VARCHAR(50) NOT NULL COMMENT '平台类型：wvp/ics/icc',
  `instance_key` VARCHAR(100) NOT NULL COMMENT '实例唯一标识',
  `name` VARCHAR(200) NOT NULL COMMENT '实例名称',
  `endpoint` VARCHAR(500) NOT NULL COMMENT '平台API地址或域',
  `auth` TEXT COMMENT '鉴权配置（JSON）',
  `version` VARCHAR(50) COMMENT '平台版本',
  `options` TEXT COMMENT '其他选项配置（JSON）',
  `auto_create_products` TINYINT DEFAULT 0 COMMENT 'WVP是否自动创建GB/级联产品：0-否，1-是',
  `enabled` TINYINT DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
  `creator_id` VARCHAR(100) COMMENT '创建者ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_instance_key` (`instance_key`),
  KEY `idx_platform_type` (`platform_type`),
  KEY `idx_enabled` (`enabled`),
  KEY `idx_creator_id` (`creator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频平台实例表';

-- 设备缓存表（中间表：设备列表与通道聚合信息）
CREATE TABLE IF NOT EXISTS `video_platform_device_cache` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `instance_key` VARCHAR(100) NOT NULL COMMENT '平台实例标识',
  `device_id` VARCHAR(100) NOT NULL COMMENT '设备ID/通道ID',
  `device_name` VARCHAR(200) DEFAULT NULL COMMENT '设备名称',
  `status` VARCHAR(50) DEFAULT NULL COMMENT '设备状态：online/offline等',
  `model` VARCHAR(100) DEFAULT NULL COMMENT '设备型号',
  `configuration` TEXT COMMENT '设备配置（JSON），包含channelList等',
  `org_id` VARCHAR(100) DEFAULT NULL COMMENT '组织ID',
  `create_id` VARCHAR(100) DEFAULT NULL COMMENT '创建者ID',
  `update_id` VARCHAR(100) DEFAULT NULL COMMENT '更新者ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_instance_device` (`instance_key`, `device_id`),
  KEY `idx_instance_key` (`instance_key`),
  KEY `idx_org_id` (`org_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频平台设备缓存表';

-- 组织缓存表（中间表：组织树结构）
CREATE TABLE IF NOT EXISTS `video_platform_org_cache` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `instance_key` VARCHAR(100) NOT NULL COMMENT '平台实例标识',
  `org_id` VARCHAR(100) NOT NULL COMMENT '组织ID',
  `parent_org_id` VARCHAR(100) DEFAULT NULL COMMENT '父组织ID',
  `org_name` VARCHAR(200) NOT NULL COMMENT '组织名称',
  `path` VARCHAR(500) DEFAULT NULL COMMENT '组织路径（可选）',
  `create_id` VARCHAR(100) DEFAULT NULL COMMENT '创建者ID',
  `update_id` VARCHAR(100) DEFAULT NULL COMMENT '更新者ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_instance_org` (`instance_key`, `org_id`),
  KEY `idx_instance_key` (`instance_key`),
  KEY `idx_parent_org_id` (`parent_org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频平台组织缓存表';
