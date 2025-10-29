# Docker 问题排查指南

## 网络连接问题

### 1. 镜像拉取失败

**问题描述**: 拉取 Docker 镜像时出现网络超时或认证失败

**解决方案**:

#### 方案 A: 配置 Docker 镜像加速器

```bash
# 创建或编辑 Docker 配置文件
sudo mkdir -p /etc/docker
sudo tee /etc/docker/daemon.json <<-'EOF'
{
  "registry-mirrors": [
    "https://docker.mirrors.ustc.edu.cn",
    "https://hub-mirror.c.163.com",
    "https://mirror.baidubce.com"
  ]
}
EOF

# 重启 Docker 服务
sudo systemctl daemon-reload
sudo systemctl restart docker
```

#### 方案 B: 使用国内镜像源

```bash
# 阿里云镜像加速器
sudo tee /etc/docker/daemon.json <<-'EOF'
{
  "registry-mirrors": ["https://your-alibaba-mirror.mirror.aliyuncs.com"]
}
EOF
```

#### 方案 C: 配置代理

```bash
# 设置 Docker 代理
export HTTP_PROXY="http://proxy.example.com:8080"
export HTTPS_PROXY="http://proxy.example.com:8080"
export NO_PROXY="localhost,127.0.0.1"

# 重启 Docker 服务
sudo systemctl restart docker
```

### 2. 镜像标签不存在

**问题描述**: 指定的镜像标签不存在或已被弃用

**解决方案**:

#### 检查可用标签

```bash
# 查看 OpenJDK 可用标签
docker search openjdk:21

# 拉取测试
docker pull openjdk:21
docker pull openjdk:21-alpine
docker pull amazoncorretto:21
```

#### 使用替代镜像

```bash
# 方案 1: 使用官方 OpenJDK
FROM openjdk:21

# 方案 2: 使用 Alpine 版本 (更小)
FROM openjdk:21-alpine

# 方案 3: 使用 Amazon Corretto (企业级)
FROM amazoncorretto:21

# 方案 4: 使用 Eclipse Temurin
FROM eclipse-temurin:21-jre
```

## 构建问题

### 1. 构建上下文过大

**问题描述**: 构建时复制了不必要的文件，导致构建缓慢

**解决方案**:

#### 优化 .dockerignore

```dockerfile
# 排除源代码
src/
pom.xml
target/classes/
target/test-classes/

# 排除 IDE 文件
.idea/
.vscode/
*.iml

# 排除版本控制
.git/
.gitignore
```

#### 使用多阶段构建

```dockerfile
# 构建阶段
FROM maven:3.9-amazoncorretto-21 AS builder
WORKDIR /app
COPY pom.xml .
COPY src/ ./src/
RUN mvn clean package -DskipTests

# 运行阶段
FROM openjdk:21-jre-slim
COPY --from=builder /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 2. 权限问题

**问题描述**: 容器内文件权限不正确

**解决方案**:

#### 设置正确的用户权限

```dockerfile
# 创建应用用户
RUN groupadd -r appuser && useradd -r -g appuser appuser

# 复制文件并设置权限
COPY target/app/ ./
RUN chown -R appuser:appuser /app

# 切换到应用用户
USER appuser
```

## 运行时问题

### 1. 内存不足

**问题描述**: 容器内存不足导致应用崩溃

**解决方案**:

#### 配置 JVM 参数

```dockerfile
# 在 Dockerfile 中设置 JVM 参数
ENV JAVA_OPTS="-Xms512m -Xmx1g -XX:+UseG1GC"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar cn-universal-web.jar"]
```

#### 限制容器资源

```yaml
# docker-compose.yml
services:
  backend:
    deploy:
      resources:
        limits:
          memory: 2G
        reservations:
          memory: 1G
```

### 2. 健康检查失败

**问题描述**: 健康检查端点不可用

**解决方案**:

#### 配置 Spring Boot 健康检查

```properties
# application.properties
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=always
management.health.diskspace.enabled=true
```

#### 调整健康检查参数

```dockerfile
# 增加启动等待时间
HEALTHCHECK --interval=30s --timeout=3s --start-period=120s --retries=5 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1
```

## 性能优化

### 1. 镜像大小优化

**使用 Alpine 基础镜像**

```dockerfile
FROM openjdk:21-alpine
RUN apk add --no-cache curl
```

**使用 JRE 而不是 JDK**

```dockerfile
FROM openjdk:21-jre-slim
```

**多阶段构建**

```dockerfile
FROM maven:3.9-amazoncorretto-21 AS builder
# ... 构建步骤

FROM openjdk:21-jre-slim AS runtime
# ... 运行步骤
```

### 2. 构建缓存优化

**优化 Dockerfile 顺序**

```dockerfile
# 先复制依赖文件
COPY pom.xml .
COPY src/ ./src/

# 再复制源代码
COPY . .

# 最后执行构建
RUN mvn clean package
```

## 常用命令

### 镜像管理

```bash
# 查看本地镜像
docker images

# 删除镜像
docker rmi <image_id>

# 清理悬空镜像
docker image prune
```

### 容器管理

```bash
# 查看运行中的容器
docker ps

# 查看所有容器
docker ps -a

# 进入容器
docker exec -it <container_id> bash

# 查看容器日志
docker logs -f <container_id>
```

### 网络管理

```bash
# 查看网络
docker network ls

# 检查网络连接
docker network inspect <network_name>

# 创建自定义网络
docker network create my-network
```

## 故障排查流程

1. **检查 Docker 服务状态**
2. **查看容器日志**
3. **检查网络连接**
4. **验证配置文件**
5. **检查资源使用情况**
6. **查看健康检查状态**

## 联系支持

如果问题仍然存在，请提供以下信息：

1. Docker 版本 (`docker --version`)
2. Docker Compose 版本 (`docker-compose --version`)
3. 操作系统信息
4. 错误日志
5. 相关配置文件
