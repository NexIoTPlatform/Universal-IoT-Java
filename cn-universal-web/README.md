# cn-universal-web 模块

## 概述

`cn-universal-web` 是IoT Universal平台的核心Web模块，提供RESTful API接口和Web服务。该模块基于Spring
Boot
3.x构建，集成了安全认证、设备管理、位置服务等核心功能。

## 模块结构

```
cn-universal-web/
├── src/main/java/cn/universal/
│   ├── CnUniversalIoTApplication.java    # 主应用启动类
│   ├── listener/                         # 应用监听器
│   │   └── ApplicationStartupListener.java
│   ├── monitor/                          # 监控相关
│   └── web/                              # Web层
│       ├── controller/                   # 控制器层
│       │   ├── IotControllerV1.java      # IoT设备通信控制器
│       │   ├── InnerController.java      # 内部系统调用接口
│       │   ├── LocationController.java   # 位置和地理围栏控制器
│       │   ├── TestController.java       # 测试接口控制器
│       │   ├── VersionController.java    # 版本信息控制器
│       │   ├── TCLController.java        # TCL设备控制器
│       │   └── OAuth2TokenController.java # OAuth2令牌控制器
│       ├── config/                       # 配置类
│       │   ├── DefaultSecurityConfig.java # 安全配置
│       │   ├── RedisConfig.java          # Redis配置
│       │   ├── SwaggerConfig.java        # API文档配置
│       │   ├── JacksonConfig.java        # JSON序列化配置
│       │   ├── AsyncExecuteConfig.java   # 异步执行配置
│       │   ├── VirtualThreadConfig.java  # 虚拟线程配置
│       │   └── ...                       # 其他配置类
│       ├── context/                      # 上下文
│       ├── oauth/                        # OAuth2相关
│       ├── auth/                         # 认证相关
│       └── utils/                        # 工具类
├── src/main/resources/                   # 配置文件
│   ├── application-dev.properties        # 开发环境配置
│   ├── application-prod.properties       # 生产环境配置
│   └── ...
└── src/bin/                              # 部署脚本
    ├── Dockerfile                        # Docker镜像配置
    ├── restart.sh                        # 重启脚本
    └── ...
```

## 核心功能

### 1. IoT设备通信 (IotControllerV1)

提供IoT设备与平台之间的通信接口：

- **设备上行数据接收**：支持HTTP、TCP等多种协议
- **设备下行指令发送**：支持加密和未加密两种模式
- **第三方平台集成**：乐橙、移动OneNet等平台对接
- **设备影子查询**：获取设备状态信息
- **产品信息查询**：获取产品配置信息

**主要接口：**

- `POST /iot/v1/down/{productKey}` - 设备下行指令（加密）
- `POST /iot/http/up/{productKey}/{deviceId}` - HTTP上行数据
- `GET /iot/device/shadow/{iotId}` - 查询设备影子
- `GET /iot/product/{productKey}` - 查询产品信息

### 2. 内部系统调用 (InnerController)

提供系统内部服务间的调用接口：

- **用户管理**：新增、删除用户
- **应用管理**：创建用户应用和OAuth客户端
- **许可证管理**：查询和充值设备接入额度
- **设备日志查询**：获取设备事件元数据

**主要接口：**

- `POST /inner/user` - 新增用户
- `DELETE /inner/user` - 删除用户
- `POST /inner/application` - 新增用户应用
- `GET /inner/license` - 查询许可证信息

### 3. 位置和地理围栏 (LocationController)

提供设备位置服务和地理围栏功能：

- **地理围栏管理**：创建、修改、删除围栏
- **围栏与设备关联**：管理设备与围栏的绑定关系
- **围栏触发规则**：配置围栏触发条件和时间规则

**主要接口：**

- `POST /iot/location/selectFence` - 查询围栏列表
- `POST /iot/location/setFence` - 创建地理围栏
- `POST /iot/location/updateFence` - 修改地理围栏
- `DELETE /iot/location/{id}` - 删除地理围栏

### 4. 安全配置 (DefaultSecurityConfig)

提供全面的安全保护机制：

- **环境自适应**：开发和生产环境不同的安全策略
- **JWT认证**：基于JWT的API认证
- **IP白名单**：支持IP地址访问控制
- **CORS配置**：跨域资源共享配置
- **端点保护**：Actuator等敏感端点的访问控制

## 技术特性

### 1. 多级缓存支持

- 集成Redis分布式缓存
- 支持本地缓存和分布式缓存
- 提供缓存统计和监控

### 2. 异步处理

- 支持虚拟线程（JDK 21）
- 异步任务执行配置
- 线程池监控和管理

### 3. 监控和可观测性

- Spring Boot Actuator集成
- 自定义健康检查
- 应用启动监控
- 性能指标收集

### 4. API文档

- Knife4j集成
- OpenAPI 3.0规范
- 自动生成API文档

## 配置说明

### 环境配置

**开发环境 (application-dev.properties)：**

```properties
# 服务器配置
server.port=8080
server.servlet.context-path=/api
# 安全配置
security.production.enabled=false
security.actuator.enabled=true
# 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/iot_universal
spring.datasource.username=root
spring.datasource.password=password
# Redis配置
spring.redis.host=localhost
spring.redis.port=6379
```

**生产环境 (application-prod.properties)：**

```properties
# 安全配置
security.production.enabled=true
security.allowed.ips=192.168.1.100,192.168.1.101
security.actuator.enabled=false
# 性能优化
spring.jvm.memory=-Xms2g -Xmx4g
spring.threads.virtual.enabled=true
```

### 安全配置

**IP白名单配置：**

```properties
# 允许访问的IP地址（逗号分隔）
security.allowed.ips=192.168.1.100,192.168.1.101,10.0.0.50
# 允许访问的主机名
security.allowed.hosts=example.com,api.example.com
```

**OAuth2配置：**

```properties
# OAuth2服务器配置
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8080
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:8080/.well-known/jwks.json
```

## 部署指南

### 1. 本地开发

```bash
# 克隆项目
git clone <mapper-url>
cd cn-universal-web

# 编译项目
mvn clean compile

# 运行应用
mvn spring-boot:run
```

### 2. Docker部署

```bash
# 构建镜像
docker build -t platform-universal-web:latest .

# 运行容器
docker run -d \
  --name platform-universal-web \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  platform-universal-web:latest
```

### 3. 生产部署

```bash
# 使用部署脚本
./src/bin/restart.sh

# 或手动部署
mvn clean package -Dmaven.test.skip=true
java -jar target/cn-universal-web-1.4-SNAPSHOT.jar --spring.profiles.active=prod
```

## 监控和维护

### 1. 健康检查

```bash
# 应用健康状态
curl http://localhost:8080/actuator/health

# 详细健康信息
curl http://localhost:8080/actuator/health/details
```

### 2. 性能监控

```bash
# 应用指标
curl http://localhost:8080/actuator/metrics

# JVM指标
curl http://localhost:8080/actuator/metrics/jvm.memory.used
```

### 3. 日志管理

```bash
# 查看应用日志
tail -f logs/platform-universal-web.log

# 查看错误日志
grep ERROR logs/platform-universal-web.log
```

## 扩展开发

### 1. 添加新的控制器

```java
@RestController
@RequestMapping("/api/v1/custom")
@Slf4j
public class CustomController extends BaseController {
    
    @PostMapping("/operation")
    public R<Object> customOperation(@RequestBody CustomRequest request) {
        // 实现业务逻辑
        return R.ok(result);
    }
}
```

### 2. 添加新的配置

```java
@Configuration
@EnableConfigurationProperties(CustomProperties.class)
public class CustomConfig {
    
    @Bean
    public CustomService customService() {
        return new CustomService();
    }
}
```

### 3. 添加新的监听器

```java
@Component
public class CustomEventListener implements ApplicationListener<CustomEvent> {
    
    @Override
    public void onApplicationEvent(CustomEvent event) {
        // 处理自定义事件
    }
}
```

## 故障排除

### 1. 常见问题

**应用启动失败：**

- 检查数据库连接配置
- 验证Redis服务状态
- 查看端口占用情况

**API访问被拒绝：**

- 检查安全配置
- 验证IP白名单设置
- 确认JWT令牌有效性

**性能问题：**

- 检查JVM内存配置
- 监控线程池状态
- 分析缓存命中率

### 2. 调试模式

```bash
# 启用调试日志
java -jar app.jar --debug

# 启用详细日志
java -jar app.jar --logging.level.cn.universal=DEBUG
```

## 版本历史

- **v1.4-SNAPSHOT** - 当前版本
    - 支持JDK 21和虚拟线程
    - 增强安全配置
    - 优化缓存机制
    - 完善监控功能

- **v1.3** - 历史版本
    - 基础IoT功能
    - OAuth2认证
    - 地理围栏支持

## 贡献指南

1. Fork项目
2. 创建功能分支
3. 提交更改
4. 推送到分支
5. 创建Pull Request

## 许可证

Copyright (c) 2025, IoT-Universal. All Rights Reserved.

---

**作者：** Aleo  
**邮箱：** wo8335224@gmail.com  
**微信：** outlookFil 