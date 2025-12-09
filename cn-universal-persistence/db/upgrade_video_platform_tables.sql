-- =====================================================
-- 视频平台数据库表结构升级脚本
-- 功能: 创建新的设备表结构(主表+扩展表)
-- 作者: NexIoT
-- 日期: 2025-11-08
-- 注意: video_platform_instance表结构无需变更,保持原样
-- =====================================================

-- 使用数据库
USE `univ`;

-- =====================================================
-- 步骤1: 创建视频平台设备主表(公共字段)
-- =====================================================
CREATE TABLE IF NOT EXISTS `video_platform_device` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `instance_key` VARCHAR(100) NOT NULL COMMENT '平台实例唯一标识',
  `device_id` VARCHAR(100) NOT NULL COMMENT '设备ID（平台侧唯一标识）',
  `device_name` VARCHAR(200) DEFAULT NULL COMMENT '设备名称',
  `device_status` VARCHAR(50) DEFAULT NULL COMMENT '设备状态: online/offline/1/0等',
  `device_model` VARCHAR(100) DEFAULT NULL COMMENT '设备型号',
  `device_ip` VARCHAR(50) DEFAULT NULL COMMENT '设备IP地址',
  `device_port` INT DEFAULT NULL COMMENT '设备端口',
  `manufacturer` VARCHAR(100) DEFAULT NULL COMMENT '设备厂商',
  `org_id` VARCHAR(100) DEFAULT NULL COMMENT '所属组织ID',
  `org_name` VARCHAR(200) DEFAULT NULL COMMENT '所属组织名称',
  `gps_x` VARCHAR(50) DEFAULT NULL COMMENT '经度（longitude）',
  `gps_y` VARCHAR(50) DEFAULT NULL COMMENT '纬度（latitude）',
  `gps_z` VARCHAR(50) DEFAULT NULL COMMENT 'Z轴高度（altitude）',
  `configuration` TEXT COMMENT '设备配置（JSON）包含channelList、能力集等公共扩展信息',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注说明',
  `enabled` TINYINT DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_instance_device` (`instance_key`, `device_id`),
  KEY `idx_instance_key` (`instance_key`),
  KEY `idx_device_status` (`device_status`),
  KEY `idx_org_id` (`org_id`),
  KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频平台设备主表';

-- =====================================================
-- 步骤2: 创建视频平台设备扩展表(设备级平台特有字段)
-- =====================================================
CREATE TABLE IF NOT EXISTS `video_platform_device_ext` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `device_id` BIGINT NOT NULL COMMENT '关联主表设备ID',
  `instance_key` VARCHAR(100) NOT NULL COMMENT '平台实例唯一标识',
  `platform_device_id` VARCHAR(100) NOT NULL COMMENT '平台侧设备ID',
  
  -- WVP GB28181 设备级特有字段
  `charset` VARCHAR(20) DEFAULT NULL COMMENT 'WVP字符集: GB2312/UTF-8',
  `transport` VARCHAR(20) DEFAULT NULL COMMENT 'WVP传输协议: UDP/TCP',
  `stream_mode` VARCHAR(50) DEFAULT NULL COMMENT 'WVP流模式: TCP-ACTIVE/TCP-PASSIVE/UDP',
  `host_address` VARCHAR(100) DEFAULT NULL COMMENT 'WVP主机地址',
  `expires` INT DEFAULT NULL COMMENT 'WVP注册有效期(秒)',
  `keepalive_time` DATETIME DEFAULT NULL COMMENT 'WVP最后心跳时间',
  `register_time` DATETIME DEFAULT NULL COMMENT 'WVP注册时间',
  `media_server_id` VARCHAR(100) DEFAULT NULL COMMENT 'WVP流媒体服务器ID',
  
  -- 海康ISC 设备级特有字段
  `encode_dev_index_code` VARCHAR(100) DEFAULT NULL COMMENT '海康编码设备索引码',
  `device_capability_set` VARCHAR(100) DEFAULT NULL COMMENT '海康设备能力集编码',
  
  -- 大华ICC 设备级特有字段
  `device_sn` VARCHAR(100) DEFAULT NULL COMMENT '大华设备序列号',
  `device_category` INT DEFAULT NULL COMMENT '大华设备类别',
  `device_type` VARCHAR(50) DEFAULT NULL COMMENT '大华设备类型',
  `owner_code` VARCHAR(100) DEFAULT NULL COMMENT '大华所属者编码',
  `is_online` VARCHAR(10) DEFAULT NULL COMMENT '大华在线状态: 0/1',
  `sleep_stat` INT DEFAULT NULL COMMENT '大华休眠状态: 0-非休眠/1-休眠',
  `third_proxy_port` INT DEFAULT NULL COMMENT '大华第三方代理端口',
  `third_proxy_server_code` VARCHAR(100) DEFAULT NULL COMMENT '大华第三方代理服务器编码',
  `license_limit` INT DEFAULT NULL COMMENT '大华license限制',
  `offline_reason` VARCHAR(200) DEFAULT NULL COMMENT '大华离线原因',
  `sub_system` VARCHAR(100) DEFAULT NULL COMMENT '大华子系统标识',
  `units_info` TEXT COMMENT '大华单元信息（JSON）包含unitType等',
  
  -- 扩展字段（预留）
  `ext_field1` VARCHAR(500) DEFAULT NULL COMMENT '扩展字段1',
  `ext_field2` VARCHAR(500) DEFAULT NULL COMMENT '扩展字段2',
  `ext_field3` TEXT DEFAULT NULL COMMENT '扩展字段3（JSON）',
  
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_device_instance` (`device_id`, `instance_key`),
  UNIQUE KEY `uk_instance_platform_device` (`instance_key`, `platform_device_id`),
  KEY `idx_instance_key` (`instance_key`),
  KEY `idx_device_sn` (`device_sn`),
  KEY `idx_encode_dev_index_code` (`encode_dev_index_code`),
  KEY `idx_media_server_id` (`media_server_id`),
  CONSTRAINT `fk_video_device_ext` FOREIGN KEY (`device_id`) REFERENCES `video_platform_device` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频平台设备扩展表(设备级特有字段)';

-- =====================================================
-- 步骤3: 创建视频平台通道表(通道级数据)
-- =====================================================
CREATE TABLE IF NOT EXISTS `video_platform_channel` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `device_id` BIGINT NOT NULL COMMENT '关联设备主表ID',
  `instance_key` VARCHAR(100) NOT NULL COMMENT '平台实例唯一标识',
  `platform_device_id` VARCHAR(100) NOT NULL COMMENT '平台侧设备ID',
  `channel_id` VARCHAR(100) NOT NULL COMMENT '通道ID（平台侧唯一标识）',
  
  -- 通道公共字段
  `channel_name` VARCHAR(200) DEFAULT NULL COMMENT '通道名称',
  `channel_status` VARCHAR(50) DEFAULT NULL COMMENT '通道状态: online/offline',
  `channel_type` VARCHAR(50) DEFAULT NULL COMMENT '通道类型: analog/digital/virtual',
  `parent_id` VARCHAR(100) DEFAULT NULL COMMENT '父通道ID（级联场景）',
  `manufacturer` VARCHAR(100) DEFAULT NULL COMMENT '通道厂商',
  `model` VARCHAR(100) DEFAULT NULL COMMENT '通道型号',
  `owner` VARCHAR(100) DEFAULT NULL COMMENT '通道所有者',
  `civil_code` VARCHAR(100) DEFAULT NULL COMMENT '行政区划',
  `address` VARCHAR(200) DEFAULT NULL COMMENT '安装地址',
  `parental` INT DEFAULT 0 COMMENT '是否有子设备: 0-否 1-是',
  `safety_way` INT DEFAULT NULL COMMENT '信令安全模式',
  `register_way` INT DEFAULT NULL COMMENT '注册方式',
  `secrecy` INT DEFAULT NULL COMMENT '保密属性',
  `ip_address` VARCHAR(50) DEFAULT NULL COMMENT '通道IP地址',
  `port` INT DEFAULT NULL COMMENT '通道端口',
  `longitude` VARCHAR(50) DEFAULT NULL COMMENT '经度',
  `latitude` VARCHAR(50) DEFAULT NULL COMMENT '纬度',
  `ptz_type` INT DEFAULT 0 COMMENT '云台类型: 0-不支持 1-球机 2-半球 3-固定枪机 4-遥控枪机',
  `position_type` INT DEFAULT NULL COMMENT '位置类型',
  
  -- WVP GB28181 通道级特有字段
  `stream_id` VARCHAR(100) DEFAULT NULL COMMENT 'WVP流ID',
  `gb_stream_id` VARCHAR(100) DEFAULT NULL COMMENT 'WVP国标流ID',
  `has_audio` TINYINT DEFAULT 0 COMMENT 'WVP是否有音频',
  
  -- 海康ISC 通道级特有字段
  `camera_index_code` VARCHAR(100) DEFAULT NULL COMMENT '海康摄像机唯一标识码(用于抓图/预览)',
  `channel_no` VARCHAR(20) DEFAULT NULL COMMENT '海康通道号',
  `camera_type` INT DEFAULT NULL COMMENT '海康摄像机类型: 0-枪机/1-球机/2-半球',
  `ptz` TINYINT DEFAULT NULL COMMENT '海康是否支持云台: 0-否/1-是',
  `capability_set` VARCHAR(100) DEFAULT NULL COMMENT '海康通道能力集编码',
  `install_location` VARCHAR(200) DEFAULT NULL COMMENT '海康安装位置',
  
  -- 大华ICC 通道级特有字段
  `channel_code` VARCHAR(100) DEFAULT NULL COMMENT '大华通道编码',
  `channel_seq` INT DEFAULT NULL COMMENT '大华通道序号',
  `encode_format` VARCHAR(50) DEFAULT NULL COMMENT '大华编码格式',
  `resolution` VARCHAR(50) DEFAULT NULL COMMENT '大华分辨率',
  
  -- 通道能力与配置
  `capabilities` TEXT COMMENT '通道能力集（JSON）: 录像/抓图/对讲/报警等',
  `stream_config` TEXT COMMENT '流配置（JSON）: 主码流/子码流参数',
  
  -- 扩展字段
  `ext_field1` VARCHAR(500) DEFAULT NULL COMMENT '扩展字段1',
  `ext_field2` VARCHAR(500) DEFAULT NULL COMMENT '扩展字段2',
  `ext_field3` TEXT DEFAULT NULL COMMENT '扩展字段3（JSON）',
  
  `enabled` TINYINT DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_device_channel` (`device_id`, `channel_id`),
  UNIQUE KEY `uk_instance_channel` (`instance_key`, `channel_id`),
  KEY `idx_instance_key` (`instance_key`),
  KEY `idx_platform_device_id` (`platform_device_id`),
  KEY `idx_channel_status` (`channel_status`),
  KEY `idx_camera_index_code` (`camera_index_code`),
  KEY `idx_channel_code` (`channel_code`),
  KEY `idx_ptz_type` (`ptz_type`),
  CONSTRAINT `fk_video_channel_device` FOREIGN KEY (`device_id`) REFERENCES `video_platform_device` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频平台通道表';

-- =====================================================
-- 步骤4: 验证表结构创建
-- =====================================================
SELECT 
    'video_platform_device' AS table_name,
    COUNT(*) AS column_count
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = 'univ' 
  AND TABLE_NAME = 'video_platform_device'

UNION ALL

SELECT 
    'video_platform_device_ext' AS table_name,
    COUNT(*) AS column_count
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = 'univ' 
  AND TABLE_NAME = 'video_platform_device_ext'

UNION ALL

SELECT 
    'video_platform_channel' AS table_name,
    COUNT(*) AS column_count
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = 'univ' 
  AND TABLE_NAME = 'video_platform_channel'

UNION ALL

SELECT 
    'video_platform_instance' AS table_name,
    COUNT(*) AS column_count
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = 'univ' 
  AND TABLE_NAME = 'video_platform_instance';

-- =====================================================
-- 步骤5: 检查video_platform_instance表现有数据
-- =====================================================
SELECT 
    platform_type,
    COUNT(*) AS instance_count,
    GROUP_CONCAT(instance_key) AS instance_keys
FROM video_platform_instance
GROUP BY platform_type;

-- =====================================================
-- 步骤6: 检查是否存在旧的缓存表数据需要迁移
-- =====================================================
SELECT 
    CASE 
        WHEN EXISTS (
            SELECT 1 FROM information_schema.TABLES 
            WHERE TABLE_SCHEMA = 'univ' 
              AND TABLE_NAME = 'video_platform_device_cache'
        ) THEN '存在旧缓存表,需要执行数据迁移'
        ELSE '无旧缓存表,无需迁移'
    END AS migration_status,
    IFNULL((
        SELECT COUNT(*) 
        FROM video_platform_device_cache
    ), 0) AS cache_device_count;

-- =====================================================
-- 可选步骤: 从旧缓存表迁移数据(如果存在)
-- =====================================================
-- 注意: 只有在video_platform_device_cache表存在时才执行
-- 
-- INSERT INTO video_platform_device (
--     instance_key,
--     device_id,
--     device_name,
--     device_status,
--     device_model,
--     org_id,
--     configuration,
--     enabled,
--     create_time,
--     update_time
-- )
-- SELECT 
--     cache.instance_key,
--     cache.device_id,
--     cache.device_name,
--     cache.status AS device_status,
--     cache.model AS device_model,
--     cache.org_id,
--     cache.configuration,
--     1 AS enabled,
--     cache.create_time,
--     cache.update_time
-- FROM video_platform_device_cache cache
-- ON DUPLICATE KEY UPDATE
--     device_name = VALUES(device_name),
--     device_status = VALUES(device_status),
--     device_model = VALUES(device_model),
--     org_id = VALUES(org_id),
--     configuration = VALUES(configuration),
--     update_time = VALUES(update_time);

-- =====================================================
-- 使用说明
-- =====================================================
-- 1. video_platform_instance表无需变更,保持原结构和数据
-- 2. 新创建video_platform_device主表(设备公共字段)
-- 3. 新创建video_platform_device_ext扩展表(设备级特有字段)
-- 4. 新创建video_platform_channel通道表(1设备:N通道)
-- 5. 如果存在video_platform_device_cache旧表,需要单独执行迁移
-- 6. 迁移后建议对每个平台实例执行一次强制同步以获取完整数据
-- 7. 组织树缓存表video_platform_org_cache保持不变
-- 8. 通道数据将从 configuration JSON中提取并存入video_platform_channel表
