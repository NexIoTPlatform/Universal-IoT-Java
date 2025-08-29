# Docker 部署指南

## 概述

本项目支持 Docker 容器化部署，可以保护源码不被泄露，同时提供灵活的配置管理。

## 架构说明

- **后端服务**: Spring Boot 3.5.0 应用，运行在 JDK 21 环境中
- **前端服务**: Nginx 静态文件服务，支持 SPA 路由
- **数据库**: MySQL 5.8 (兼容 MySQL 5.7，忽略表名大小写)
- **缓存**: Redis 7
- **消息队列**: EMQX 5.3 MQTT Broker
- **管理工具**: Adminer (数据库管理)

## 前置要求

- Docker 20.10+
- Docker Compose 2.0+
- JDK 21 (用于构建项目)
- 已构建的后端 JAR 包
- 已构建的前端 dist 目录

## 技术栈版本

- **Java**: JDK 21
- **Spring Boot**: 3.5.0
- **MySQL**: 5.8 (兼容 5.7)
- **Redis**: 7.x
- **EMQX**: 5.3
- **Nginx**: Alpine 版本

## 快速开始

### 1. 构建项目

```bash
# 后端构建
cd cn-universal-web
mvn clean package -DskipTests
cd ..

# 前端构建
cd cn-universal-web-ui
npm run build
cd ..
```

### 2. 构建 Docker 镜像

```bash
# 使用构建脚本
chmod +x build-docker.sh
./build-docker.sh

# 或手动构建
docker build -t cn-universal-backend:latest ./cn-universal-web
docker build -t cn-universal-frontend:latest ./cn-universal-web-ui
```

### 3. 配置环境变量

```bash
# 复制环境变量模板
cp env.example .env

# 编辑 .env 文件，根据需要修改配置
vim .env
```

### 4. 启动服务

```bash
# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

## 配置说明

### 环境变量配置

主要配置项通过环境变量管理：

```bash
# 数据库配置
MYSQL_HOST=mysql
MYSQL_PORT=3306
MYSQL_DATABASE=univ
MYSQL_USERNAME=root
MYSQL_PASSWORD=your_password

# Redis 配置
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=your_redis_password
REDIS_DATABASE=11

# MQTT 配置
MQTT_HOST=tcp://emqx:1883
MQTT_USERNAME=universal_iot
MQTT_PASSWORD=your_mqtt_password
MQTT_CLIENT_ID_PREFIX=univ_iot_docker
```

### 端口映射

- **前端**: http://localhost:80
- **后端 API**: http://localhost:8080
- **MySQL**: localhost:3306
- **Redis**: localhost:6379
- **MQTT**: localhost:1883
- **EMQX 管理界面**: http://localhost:18083
- **数据库管理**: http://localhost:8081

## 服务管理

### 常用命令

```bash
# 启动服务
docker-compose up -d

# 停止服务
docker-compose down

# 重启服务
docker-compose restart

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f [service_name]

# 进入容器
docker-compose exec [service_name] bash

# 更新镜像并重启
docker-compose pull
docker-compose up -d
```

### 数据持久化

- **MySQL 数据**: `mysql_data` 卷
- **Redis 数据**: `redis_data` 卷
- **EMQX 数据**: `emqx_data` 和 `emqx_log` 卷
- **应用日志**: `./logs` 目录

## 安全配置

### 生产环境建议

1. **修改默认密码**: 更新 `.env` 文件中的默认密码
2. **限制端口访问**: 只暴露必要的端口
3. **网络隔离**: 使用 Docker 网络隔离服务
4. **日志管理**: 配置日志轮转和监控

### 防火墙配置

```bash
# 只允许必要端口
sudo ufw allow 80/tcp    # 前端
sudo ufw allow 8080/tcp  # 后端
sudo ufw allow 3306/tcp  # MySQL (如需要外部访问)
sudo ufw allow 1883/tcp  # MQTT (如需要外部访问)
```

## 监控和维护

### 健康检查

所有服务都配置了健康检查：

```bash
# 检查服务健康状态
docker-compose ps

# 查看健康检查详情
docker inspect cn-universal-backend | grep Health -A 10
```

### 日志管理

```bash
# 查看实时日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f backend
docker-compose logs -f frontend

# 查看应用日志
tail -f logs/application.log
```

### 备份和恢复

```bash
# 备份 MySQL 数据
docker-compose exec mysql mysqldump -u root -p univ > backup.sql

# 恢复 MySQL 数据
docker-compose exec -T mysql mysql -u root -p univ < backup.sql

# 备份 Redis 数据
docker cp cn-universal-redis:/data/dump.rdb ./redis_backup.rdb
```

## 故障排除

### 常见问题

1. **端口冲突**: 检查端口是否被占用
2. **内存不足**: 增加 Docker 内存限制
3. **权限问题**: 检查文件权限和用户
4. **网络问题**: 检查 Docker 网络配置

### 调试命令

```bash
# 查看容器详细信息
docker inspect [container_name]

# 查看容器资源使用
docker stats

# 查看网络配置
docker network ls
docker network inspect iot-network
```

## 扩展部署

### 集群部署

对于生产环境，建议：

1. 使用 Docker Swarm 或 Kubernetes
2. 配置负载均衡器
3. 使用外部数据库和缓存服务
4. 配置监控和告警系统

### 微服务拆分

可以将服务进一步拆分：

- 用户服务
- 设备管理服务
- 数据处理服务
- 通知服务

## 联系支持

如有问题，请查看：

1. Docker 日志
2. 应用日志
3. 配置文件
4. 网络连接状态
