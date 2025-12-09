#!/bin/bash

# 构建 MySQL 镜像
echo "🔨 Building custom MySQL image with init data..."

# 设置镜像名称和版本
IMAGE_NAME="nexiot/mysql"
IMAGE_TAG="8.0-init"

# 构建镜像
docker build -t ${IMAGE_NAME}:${IMAGE_TAG} -f docker/mysql/Dockerfile docker/mysql/

# 检查构建结果
if [ $? -eq 0 ]; then
    echo "✅ MySQL image built successfully: ${IMAGE_NAME}:${IMAGE_TAG}"
    echo ""
    echo "📤 Push to registry:"
    echo "   docker push ${IMAGE_NAME}:${IMAGE_TAG}"
else
    echo "❌ Build failed!"
    exit 1
fi
