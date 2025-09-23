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

package cn.universal.common.constant;

/**
 * 摄像头操作命令枚举
 * 直接对应物模型函数名
 *
 * @version 1.0
 * @since 2025/1/15
 */
public enum CameraCommand {
    
    // 摄像头相关物模型函数
    CAMERA_LIVE_STREAM("cameraLiveStream"),
    CAMERA_PLAYBACK("cameraPlayback"),
    CAMERA_SNAPSHOT("cameraSnapshot"),
    CAMERA_TURN("cameraTurn"),
    CAMERA_FLIP_SET("cameraFlipSet");

    private final String functionName;

    CameraCommand(String functionName) {
        this.functionName = functionName;
    }

    /**
     * 获取物模型函数名
     *
     * @return 物模型函数名
     */
    public String getFunctionName() {
        return functionName;
    }

    /**
     * 根据物模型函数名获取对应的命令枚举
     *
     * @param functionName 物模型函数名
     * @return 对应的命令枚举，如果未找到返回null
     */
    public static CameraCommand fromFunctionName(String functionName) {
        for (CameraCommand command : values()) {
            if (command.functionName.equals(functionName)) {
                return command;
            }
        }
        return null;
    }
}
