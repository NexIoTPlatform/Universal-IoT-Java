#!/bin/bash

# Docker 构建脚本
set -e

echo "🚀 开始构建 Docker 镜像 (JDK 21 + MySQL 5.8 + EMQX)..."
echo ""
echo "📋 支持的构建版本："
echo "   后端版本："
echo "     ./build-docker.sh [backend] [frontend]"
echo "     backend: default/alpine/corretto"
echo "     frontend: default/ubuntu/centos/simple/local"
echo ""
echo "   示例："
echo "     ./build-docker.sh                    # 默认版本"
echo "     ./build-docker.sh alpine             # 后端 Alpine + 前端默认"
echo "     ./build-docker.sh corretto ubuntu    # 后端 Corretto + 前端 Ubuntu"
echo "     ./build-docker.sh default simple     # 后端默认 + 前端 Python 简单版"
echo ""

# 检查必要文件
if [ ! -d "../cn-universal-web/target/cn-universal-web" ]; then
    echo "❌ 后端构建文件不存在，请先执行 Maven 构建"
    echo "   执行: mvn clean package -DskipTests"
    exit 1
fi

if [ ! -d "../cn-universal-web-ui/dist" ]; then
    echo "❌ 前端构建文件不存在，请先执行前端构建"
    echo "   执行: cd ../cn-universal-web-ui && npm run build"
    exit 1
fi

# 创建 .env 文件（如果不存在）
if [ ! -f ".env" ]; then
    echo "📝 创建 .env 文件..."
    cp env.example .env
    echo "✅ .env 文件已创建，请根据需要修改配置"
fi

# 构建后端镜像
echo "🔨 构建后端镜像..."
cd ../cn-universal-web

# 选择 Dockerfile 版本
DOCKERFILE_VERSION=${1:-default}
case $DOCKERFILE_VERSION in
    "alpine")
        echo "📦 使用 Alpine 版本 (镜像更小)"
        docker build -f Dockerfile.alpine -t cn-universal-backend:alpine .
        docker tag cn-universal-backend:alpine cn-universal-backend:latest
        ;;
    "corretto")
        echo "🏢 使用 Amazon Corretto 版本 (企业级支持)"
        docker build -f Dockerfile.amazoncorretto -t cn-universal-backend:corretto .
        docker tag cn-universal-backend:corretto cn-universal-backend:latest
        ;;
    *)
        echo "📦 使用默认 OpenJDK 版本"
        docker build -t cn-universal-backend:latest .
        ;;
esac

cd ../docker

# 构建前端镜像
echo "🔨 构建前端镜像..."
cd ../cn-universal-web-ui

# 选择前端 Dockerfile 版本
FRONTEND_VERSION=${2:-default}
case $FRONTEND_VERSION in
    "ubuntu")
        echo "🐧 使用 Ubuntu 版本"
        docker build -f Dockerfile.ubuntu -t cn-universal-frontend:ubuntu .
        docker tag cn-universal-frontend:ubuntu cn-universal-frontend:latest
        ;;
    "centos")
        echo "🔴 使用 CentOS 版本"
        docker build -f Dockerfile.centos -t cn-universal-frontend:centos .
        docker tag cn-universal-frontend:centos cn-universal-frontend:latest
        ;;
    "simple")
        echo "🐍 使用 Python 简单版本"
        docker build -f Dockerfile.simple -t cn-universal-frontend:simple .
        docker tag cn-universal-frontend:simple cn-universal-frontend:latest
        ;;
    "local")
        echo "🏠 使用本地镜像版本"
        docker build -f Dockerfile.local -t cn-universal-frontend:local .
        docker tag cn-universal-frontend:local cn-universal-frontend:latest
        ;;
    *)
        echo "📦 使用默认 Alpine 版本"
        docker build -t cn-universal-frontend:latest .
        ;;
esac

cd ..

echo "✅ 镜像构建完成！"
echo ""
echo "📋 可用镜像："
echo "   - cn-universal-backend:latest"
echo "   - cn-universal-frontend:latest"
echo ""
echo "🚀 启动服务："
echo "   cd docker && docker-compose up -d"
echo ""
echo "📊 查看服务状态："
echo "   cd docker && docker-compose ps"
echo ""
echo "🔍 查看日志："
echo "   cd docker && docker-compose logs -f"
