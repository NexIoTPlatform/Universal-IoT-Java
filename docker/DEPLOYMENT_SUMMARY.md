# IoT 平台 Docker 部署总结

## 🎯 部署状态

### ✅ 已完成
- [x] 后端 Docker 镜像构建成功 (`cn-universal-backend:latest`)
- [x] 前端 Docker 镜像构建成功 (`cn-universal-frontend:latest`)
- [x] 应用服务容器启动成功
- [x] 前端服务健康运行 (端口 80)
- [x] 后端服务容器运行中 (端口 8080)

### ⚠️ 当前问题
- [ ] 后端服务启动失败 - 缺少数据库连接
- [ ] 依赖服务 (MySQL/Redis/EMQX) 未启动 - 网络问题

## 🐳 Docker 镜像信息

### 后端镜像
```bash
# 镜像名称
cn-universal-backend:latest

# 基础镜像
openjdk:21 (Oracle Linux 8.8)

# 镜像大小
2.38GB

# 端口
8080

# 状态
容器运行中，但应用启动失败
```

### 前端镜像
```bash
# 镜像名称
cn-universal-frontend:latest

# 基础镜像
openjdk:21 + nginx (本地构建)

# 镜像大小
1.47GB

# 端口
80

# 状态
健康运行 ✅
```

## 🚀 部署方式

### 1. 应用服务部署 (当前状态)
```bash
# 启动应用服务
./start-apps.sh

# 访问地址
前端: http://localhost:80
后端: http://localhost:8080 (启动失败)
```

### 2. 完整服务部署 (需要解决网络问题)
```bash
# 启动完整服务
./start-docker.sh

# 包含服务
- MySQL 5.8
- Redis 7.x
- EMQX 5.3
- 后端应用
- 前端应用
```

## 🔧 构建选项

### 后端构建版本
```bash
./build-docker.sh [backend] [frontend]

# 后端版本
- default: OpenJDK 21 (Oracle Linux)
- alpine: OpenJDK 21 Alpine (更小)
- corretto: Amazon Corretto 21 (企业级)

# 示例
./build-docker.sh                    # 默认版本
./build-docker.sh alpine             # 后端 Alpine + 前端默认
./build-docker.sh corretto ubuntu    # 后端 Corretto + 前端 Ubuntu
```

### 前端构建版本
```bash
# 前端版本
- default: Alpine + nginx (网络问题)
- ubuntu: Ubuntu + nginx
- centos: CentOS + nginx
- simple: Python HTTP 服务器
- local: 基于本地镜像 + nginx ✅

# 推荐使用
./build-docker.sh default local
```

## 🌐 网络问题解决方案

### 方案 1: 配置 Docker 镜像加速器
```bash
# 创建 Docker 配置文件
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

### 方案 2: 使用本地已有镜像
```bash
# 当前已成功使用
FROM openjdk:21  # 已下载到本地
```

### 方案 3: 配置代理
```bash
# 设置环境变量
export HTTP_PROXY="http://proxy.example.com:8080"
export HTTPS_PROXY="http://proxy.example.com:8080"
export NO_PROXY="localhost,127.0.0.1"
```

## 📊 服务状态检查

### 查看容器状态
```bash
# 应用服务
docker-compose -f docker-compose.apps.yml ps

# 完整服务
docker-compose ps
```

### 查看日志
```bash
# 后端日志
docker-compose -f docker-compose.apps.yml logs backend

# 前端日志
docker-compose -f docker-compose.apps.yml logs frontend

# 实时日志
docker-compose -f docker-compose.apps.yml logs -f
```

### 健康检查
```bash
# 前端健康检查
curl http://localhost:80/health

# 后端健康检查 (需要先解决数据库问题)
curl http://localhost:8080/actuator/health
```

## 🔍 问题诊断

### 后端启动失败原因
1. **数据源配置缺失**: 缺少 MySQL 连接配置
2. **依赖服务未启动**: MySQL/Redis/EMQX 容器未运行
3. **配置文件问题**: `application-docker.properties` 中的数据库配置无法连接

### 解决方案优先级
1. **高优先级**: 解决网络问题，启动完整服务
2. **中优先级**: 配置外部数据库服务
3. **低优先级**: 使用 H2 内存数据库进行测试

## 📝 下一步行动

### 立即行动
1. 配置 Docker 镜像加速器
2. 重新尝试启动完整服务
3. 验证所有服务状态

### 备选方案
1. 配置外部 MySQL/Redis/EMQX 服务
2. 修改应用配置使用外部服务
3. 创建测试环境配置

### 长期优化
1. 优化 Docker 镜像大小
2. 完善监控和日志系统
3. 建立 CI/CD 流水线

## 📞 技术支持

如果遇到问题，请提供以下信息：
1. Docker 版本 (`docker --version`)
2. Docker Compose 版本 (`docker-compose --version`)
3. 操作系统信息
4. 错误日志
5. 网络环境信息

## 📚 相关文档

- [DOCKER_DEPLOYMENT.md](./DOCKER_DEPLOYMENT.md) - Docker 部署详细说明
- [DOCKER_TROUBLESHOOTING.md](./DOCKER_TROUBLESHOOTING.md) - 问题排查指南
- [DEPENDENCY_ANALYSIS.md](./DEPENDENCY_ANALYSIS.md) - 依赖兼容性分析
