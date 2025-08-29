#!/bin/bash

# 启动应用服务脚本 (不包含依赖服务)
set -e

echo "🚀 启动 IoT 平台应用服务..."

# 检查 Docker 是否运行
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker 未运行，请先启动 Docker"
    exit 1
fi

# 检查 Docker Compose 是否可用
if ! docker-compose version > /dev/null 2>&1; then
    echo "❌ Docker Compose 不可用，请检查安装"
    exit 1
fi

# 创建必要的目录
echo "📁 创建必要的目录..."
mkdir -p logs

# 检查镜像是否存在
echo "🔍 检查镜像..."
if ! docker images | grep -q "cn-universal-backend.*latest"; then
    echo "❌ 后端镜像不存在，请先构建镜像"
    echo "   执行: ./build-docker.sh default local"
    exit 1
fi

if ! docker images | grep -q "cn-universal-frontend.*latest"; then
    echo "❌ 前端镜像不存在，请先构建镜像"
    echo "   执行: ./build-docker.sh default local"
    exit 1
fi

# 启动应用服务
echo "🔧 启动应用服务..."
docker-compose -f docker-compose.apps.yml up -d

# 等待服务启动
echo "⏳ 等待服务启动..."
sleep 10

# 检查服务状态
echo "📊 服务状态："
docker-compose -f docker-compose.apps.yml ps

# 显示访问信息
echo ""
echo "🌐 应用服务访问地址："
echo "   前端界面: http://localhost:80"
echo "   后端 API: http://localhost:9092"
echo ""
echo "⚠️  注意：依赖服务 (MySQL/Redis/EMQX) 未启动"
echo "   如需完整环境，请配置外部服务或解决网络问题后使用完整版本"
echo ""
echo "📋 常用命令："
echo "   查看日志: docker-compose -f docker-compose.apps.yml logs -f"
echo "   停止服务: docker-compose -f docker-compose.apps.yml down"
echo "   重启服务: docker-compose -f docker-compose.apps.yml restart"
echo "   查看状态: docker-compose -f docker-compose.apps.yml ps"
echo ""
echo "✅ 应用服务启动完成！"
