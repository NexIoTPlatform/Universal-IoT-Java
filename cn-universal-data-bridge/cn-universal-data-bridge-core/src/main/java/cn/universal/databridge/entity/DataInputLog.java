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

package cn.universal.databridge.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据输入日志实体
 *
 * @author gitee.com/NexIoT
 * @version 1.0
 * @since 2025/1/15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "iot_data_input_log")
public class DataInputLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 配置ID */
    @Column(name = "config_id")
    private Long configId;

    /** 配置名称 */
    @Column(name = "config_name")
    private String configName;

    /** 源系统 */
    @Column(name = "source_system")
    private String sourceSystem;

    /** 消息数量 */
    @Column(name = "message_count")
    private Integer messageCount;

    /** 成功数量 */
    @Column(name = "success_count")
    private Integer successCount;

    /** 失败数量 */
    @Column(name = "failed_count")
    private Integer failedCount;

    /** 错误信息 */
    @Column(name = "error_message")
    private String errorMessage;

    /** 执行时间(毫秒) */
    @Column(name = "execution_time")
    private Long executionTime;

    /** 状态 */
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private Status status;

    /** 创建时间 */
    @Column(name = "create_time")
    private LocalDateTime createTime;

    /** 创建者 */
    @Column(name = "create_by")
    private String createBy;

    /**
     * 状态枚举
     */
    public enum Status {
        SUCCESS,    // 成功
        FAILED,     // 失败
        RUNNING     // 运行中
    }
}
