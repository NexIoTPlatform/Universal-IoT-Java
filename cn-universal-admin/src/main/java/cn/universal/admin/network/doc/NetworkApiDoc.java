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

package cn.universal.admin.network.doc;

/**
 * 网络组件API文档
 *
 * @version 1.0 @Author Aleo
 * @since 2025/1/20
 */
public class NetworkApiDoc {

  /**
   * 网络组件管理API文档
   *
   * <p>基础路径: /admin/network
   *
   * <p>1. 查询网络组件列表 GET /admin/network/list 参数: - page: 页码 (可选，默认1) - size: 每页大小 (可选，默认10) - type:
   * 网络类型 (可选，单个类型，如 TCP_CLIENT) - types: 网络类型列表 (可选，多个类型，如 ["MQTT_CLIENT", "MQTT_SERVER"]) - name:
   * 网络组件名称 (可选，模糊查询) - productKey: 产品Key (可选) - state: 状态 (可选，true/false) - unionId: 唯一标识 (可选)
   *
   * <p>2. 查询网络组件列表（支持多个类型） GET /admin/network/list/multi-type 参数: - page: 页码 (可选，默认1) - size: 每页大小
   * (可选，默认10) - type: 网络类型 (可选，支持逗号分隔多个类型，如 "MQTT_CLIENT,MQTT_SERVER") - name: 网络组件名称 (可选，模糊查询) -
   * productKey: 产品Key (可选) - state: 状态 (可选，true/false) - unionId: 唯一标识 (可选)
   *
   * <p>3. 根据ID查询网络组件 GET /admin/network/{id} 参数: - id: 网络组件ID (路径参数)
   *
   * <p>4. 新增网络组件 POST /admin/network 请求体: { "type": "TCP_SERVER", "unionId": "unique_id",
   * "productKey": "product_key", "name": "网络组件名称", "description": "详细描述", "state": false,
   * "configuration": "{\"host\":\"0.0.0.0\",\"port\":6372,\"ssl\":false}", "createUser": "admin" }
   *
   * <p>5. 修改网络组件 PUT /admin/network 请求体: { "id": 1, "type": "TCP_SERVER", "unionId": "unique_id",
   * "productKey": "product_key", "name": "网络组件名称", "description": "详细描述", "state": false,
   * "configuration": "{\"host\":\"0.0.0.0\",\"port\":6372,\"ssl\":false}" }
   *
   * <p>6. 删除网络组件 DELETE /admin/network/{id} 参数: - id: 网络组件ID (路径参数)
   *
   * <p>7. 批量删除网络组件 DELETE /admin/network/batch/{ids} 参数: - ids: 网络组件ID数组 (路径参数，逗号分隔)
   *
   * <p>8. 启动网络组件 POST /admin/network/start/{id} 参数: - id: 网络组件ID (路径参数)
   *
   * <p>9. 停止网络组件 POST /admin/network/stop/{id} 参数: - id: 网络组件ID (路径参数)
   *
   * <p>10. 重启网络组件 POST /admin/network/restart/{id} 参数: - id: 网络组件ID (路径参数)
   *
   * <p>11. 获取网络类型列表 GET /admin/network/types
   *
   * <p>12. 验证网络组件配置 POST /admin/network/validate 请求体: { "type": "TCP_SERVER", "unionId":
   * "unique_id", "name": "网络组件名称", "configuration":
   * "{\"host\":\"0.0.0.0\",\"port\":6372,\"ssl\":false}" }
   *
   * <p>响应格式: { "code": 200, "msg": "操作成功", "data": { // 具体数据 } }
   *
   * <p>网络类型说明: - TCP_CLIENT: TCP客户端 - TCP_SERVER: TCP服务端 - MQTT_CLIENT: MQTT客户端 - MQTT_SERVER:
   * MQTT服务端
   *
   * <p>配置示例:
   *
   * <p>TCP_SERVER配置: { "allIdleTime": 0, "allowInsert": false, "alwaysPreDecode": false,
   * "decoderType": "STRING", "host": "0.0.0.0", "idleInterval": 0, "onlyCache": false,
   * "parserConfiguration": { "byteOrderLittle": true, "delimited": "]", "delimitedMaxlength": 1024,
   * "failFast": true }, "parserType": "DELIMITED", "port": 6372, "productKey":
   * "630477f1cd68445d90b04653", "readerIdleTime": 360, "readTimeout": 0, "sendTimeout": 0, "ssl":
   * false, "writerIdleTime": 0 }
   *
   * <p>MQTT_SERVER配置: { "autoReconnect": true, "cleanSession": true, "clientIdPrefix": "univ_cli_",
   * "defaultQos": 1, "enabled": true, "host": "tcp://125.210.48.96:1883", "id":
   * "6863896d75386b5c1e7e908c", "keepAliveInterval": 60, "password": "univiot@2025!", "productKey":
   * "local", "ssl": false, "subscribeTopics": "$univ_cli/up/property/+/+", "username": "univ_cli",
   * "connectTimeout": 30 }
   */
}
