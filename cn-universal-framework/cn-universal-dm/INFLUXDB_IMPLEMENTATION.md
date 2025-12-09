# InfluxDB 设备日志存储策略实现文档

## 概述

`InfluxDBDeviceLogService` 是 NexIoT 平台中 InfluxDB 时序数据库存储策略的实现，用于高效存储和查询物联网设备日志数据。

## 主要特性

1. **时序数据优化**: 专为时间序列数据设计，支持高效的时间范围查询
2. **自动数据清理**: 通过 InfluxDB 保留策略（Retention Policy）自动清理过期数据
3. **灵活的查询**: 使用 Flux 查询语言，支持复杂的数据分析
4. **分离存储**: 主日志、属性元数据、事件元数据分别存储，提高查询效率

## 数据模型

### Measurement（测量值）

- `device_log`: 设备主日志
- `property_metadata`: 属性元数据日志  
- `event_metadata`: 事件元数据日志

### Tags（索引字段）

| Tag 名称 | 说明 | 示例 |
|---------|------|------|
| productKey | 产品标识 | product_001 |
| deviceId | 设备ID | device_123 |
| iotId | 设备唯一ID | product_001device_123 |
| messageType | 消息类型 | PROPERTIES/EVENT/FUNCTIONS |
| event | 事件标识 | alarm_event |
| property | 属性标识 | temperature |

### Fields（数据字段）

| Field 名称 | 类型 | 说明 |
|-----------|------|------|
| content | String | 内容数据（JSON格式） |
| deviceName | String | 设备名称 |
| commandId | String | 命令ID |
| commandStatus | Integer | 命令状态 |
| point | String | 地理位置坐标 |
| ext1 | String | 扩展字段1（属性名/事件数据） |
| ext2 | String | 扩展字段2（格式化值） |
| ext3 | String | 扩展字段3（单位符号） |

## 配置说明

### Maven 依赖

已在 `cn-universal-dm/pom.xml` 中添加：

```xml
<!-- InfluxDB 客户端依赖 -->
<dependency>
    <groupId>com.influxdb</groupId>
    <artifactId>influxdb-client-java</artifactId>
    <version>6.10.0</version>
</dependency>
```

### 应用配置

在 `application.yml` 中添加：

```yaml
influxdb:
  enable: true  # 启用 InfluxDB 存储策略
  url: http://127.0.0.1:8086
  token: your-influxdb-token
  org: nexiot
  bucket: device_logs
```

### InfluxDB 服务配置

1. **安装 InfluxDB 2.x**
   ```bash
   # Docker 方式
   docker run -d -p 8086:8086 \
     --name influxdb \
     -v influxdb-data:/var/lib/influxdb2 \
     influxdb:2.7
   ```

2. **创建 Token**
   - 访问 http://localhost:8086
   - 登录后进入 Data > Tokens
   - 生成新的 All Access Token

3. **配置保留策略（Retention Policy）**
   ```bash
   # 设置 30 天保留策略（自动删除 30 天前的数据）
   influx bucket update \
     --name device_logs \
     --retention 720h  # 30天 = 30*24 = 720小时
   ```

## 关键实现

### 1. 数据写入

```java
// 主日志写入
Point point = Point.measurement("device_log")
    .addTag("productKey", productKey)
    .addTag("deviceId", deviceId)
    .addTag("iotId", iotId)
    .addTag("messageType", messageType)
    .addField("content", content)
    .addField("deviceName", deviceName)
    .time(Instant.now(), WritePrecision.MS);

writeApi.writePoint(point);
```

### 2. 数据查询

使用 Flux 查询语言：

```java
String flux = """
    from(bucket: "device_logs")
      |> range(start: -30d)
      |> filter(fn: (r) => r._measurement == "device_log")
      |> filter(fn: (r) => r.iotId == "device_001")
      |> sort(columns: ["_time"], desc: true)
      |> limit(n: 10)
    """;

List<FluxTable> tables = queryApi.query(flux);
```

### 3. 分页实现

```java
// Flux 分页
int offset = (pageNum - 1) * pageSize;
flux.append("  |> limit(n: ").append(pageSize)
    .append(", offset: ").append(offset).append(")\n");
```

## InfluxDB 不兼容的地方及处理方案

### 1. 没有自增 ID

**问题**: InfluxDB 不支持传统的自增 ID
**方案**: 使用时间戳（毫秒）作为唯一标识

```java
vo.setId(record.getTime().toEpochMilli());  // 时间戳作为ID
```

### 2. Tags 只能是字符串类型

**问题**: Tags 不支持数值类型
**方案**: 数值字段统一存储在 Fields 中

```java
.addTag("productKey", productKey)      // Tag: 字符串
.addField("commandStatus", status)     // Field: 整型
```

### 3. 不支持 UPDATE/DELETE

**问题**: InfluxDB 不支持传统的更新和删除操作
**方案**: 
- 使用 Retention Policy 自动清理过期数据
- 不实现 MySQL 中的 `deleteTopPropertiesRecord` 逻辑
- 建议在 InfluxDB 中配置合适的保留策略

### 4. 查询结果格式特殊

**问题**: InfluxDB 查询返回的是 field-value 形式，每个字段一个 table
**方案**: 按时间戳分组重组数据

```java
Map<Instant, IoTDeviceLogVO> recordMap = new HashMap<>();
for (FluxTable table : tables) {
    for (FluxRecord record : table.getRecords()) {
        Instant time = record.getTime();
        IoTDeviceLogVO vo = recordMap.computeIfAbsent(time, ...);
        // 根据 _field 填充对应字段
        String field = record.getField();
        switch (field) {
            case "content": vo.setContent(...); break;
            case "deviceName": vo.setDeviceName(...); break;
        }
    }
}
```

### 5. 不支持复杂 JOIN 查询

**问题**: InfluxDB 不支持多表 JOIN
**方案**: 
- 数据预聚合
- 应用层多次查询合并结果
- 合理设计 measurement 和 tags 避免 JOIN 需求

## 性能优化建议

### 1. 合理使用 Tags

- **Tags 用于索引**: 高频查询字段设置为 Tag（productKey, deviceId 等）
- **限制 Tag 基数**: Tag 值不要太多（避免高基数问题）
- **Fields 存储数据**: 低频查询字段或数值字段设置为 Field

### 2. 时间范围查询

```java
// ✅ 推荐：限制时间范围
flux.append("  |> range(start: -30d)\n");

// ❌ 避免：无限制时间范围
flux.append("  |> range(start: 0)\n");
```

### 3. 使用保留策略

```bash
# 设置不同的保留策略
influx bucket create --name device_logs_30d --retention 720h
influx bucket create --name device_logs_365d --retention 8760h
```

### 4. 批量写入

```java
// 使用批量写入API提高性能
WriteApi writeApi = influxDBClient.getWriteApi();
writeApi.writePoints(points);  // 批量写入
```

## 与其他存储策略的对比

| 特性 | MySQL | IoTDB | ClickHouse | **InfluxDB** |
|------|-------|-------|------------|-------------|
| 数据模型 | 关系型 | 时序 | 列式 | **时序** |
| 查询语言 | SQL | SQL | SQL | **Flux** |
| 时间序列优化 | ❌ | ✅ | ✅ | **✅** |
| 自增ID | ✅ | ❌ | ✅ | **❌** |
| 自动数据清理 | ❌ | ✅ | ✅ | **✅** (Retention Policy) |
| 复杂查询 | ✅ | ⚠️ | ✅ | **⚠️** |
| 写入性能 | ⚠️ | ✅ | ✅ | **✅** |
| 查询性能 | ⚠️ | ✅ | ✅ | **✅** |

## 使用示例

### 1. 启用 InfluxDB 存储策略

在产品配置中选择 `influxdb` 存储策略：

```java
ioTProduct.setStorePolicy("influxdb");
```

### 2. 查询设备日志

```java
LogQuery query = new LogQuery();
query.setIotId("product_001device_123");
query.setMessageType("PROPERTIES");
query.setPageNum(1);
query.setPageSize(10);

PageBean<IoTDeviceLogVO> result = influxDBDeviceLogService.pageList(query);
```

### 3. 查询事件统计

```java
PageBean<IoTDeviceEvents> events = 
    influxDBDeviceLogService.queryEventTotal("product_001", "iotId_123");
```

## 故障排查

### 1. 依赖未加载

**现象**: 编译错误 `com.influxdb cannot be resolved`

**解决**:
```bash
# 刷新 Maven 依赖
mvn clean install -U

# 或在 IDE 中手动刷新
# IntelliJ IDEA: Maven > Reload All Maven Projects
# Eclipse: 右键项目 > Maven > Update Project
```

### 2. 连接失败

**现象**: `InfluxDB连接失败`

**检查**:
- InfluxDB 服务是否启动
- URL 配置是否正确
- Token 是否有效
- 网络是否可达

### 3. 查询无数据

**现象**: 查询返回空结果

**排查**:
- 检查时间范围（默认查询最近30天）
- 检查过滤条件是否正确
- 检查 bucket 名称是否匹配
- 使用 InfluxDB UI 直接查询验证数据是否存在

## 注意事项

1. **依赖安装**: 首次使用需要执行 `mvn clean install` 安装 InfluxDB 依赖
2. **配置开关**: 通过 `influxdb.enable=true` 启用，避免未配置时启动报错
3. **时间戳精度**: 使用毫秒精度（WritePrecision.MS）保持与 MySQL 一致
4. **数据清理**: 依赖 InfluxDB Retention Policy，不在应用层实现删除逻辑
5. **查询优化**: 始终限制时间范围，避免全量扫描

## 未来优化方向

1. **连续查询（Continuous Queries）**: 预聚合常用统计数据
2. **下采样（Downsampling）**: 长期数据降低精度存储
3. **多 Bucket 策略**: 不同数据类型使用不同的 Bucket 和保留策略
4. **集群部署**: InfluxDB Enterprise 支持集群部署，提高可用性

## 参考资料

- [InfluxDB 官方文档](https://docs.influxdata.com/influxdb/v2.7/)
- [Flux 查询语言](https://docs.influxdata.com/flux/v0.x/)
- [InfluxDB Java Client](https://github.com/influxdata/influxdb-client-java)
