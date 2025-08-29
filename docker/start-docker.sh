#!/bin/bash

# Docker 快速启动脚本
set -e

echo "🚀 启动 IoT 平台 Docker 服务..."

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
mkdir -p mysql/conf mysql/init

# 检查配置文件
if [ ! -f ".env" ]; then
    echo "📝 创建 .env 文件..."
    cp env.example .env
    echo "✅ .env 文件已创建，请根据需要修改配置"
fi

# 启动服务
echo "🔧 启动服务..."
docker-compose up -d

# 等待服务启动
echo "⏳ 等待服务启动..."
sleep 10

# 检查服务状态
echo "📊 服务状态："
docker-compose ps

# 显示访问信息
echo ""
echo "🌐 服务访问地址："
echo "   前端界面: http://localhost:80"
echo "   后端 API: http://localhost:8080"
echo "   EMQX 管理: http://localhost:18083 (admin/public)"
echo "   数据库管理: http://localhost:8081"
echo ""
echo "📋 常用命令："
echo "   查看日志: docker-compose logs -f"
echo "   停止服务: docker-compose down"
echo "   重启服务: docker-compose restart"
echo "   查看状态: docker-compose ps"
echo ""
echo "✅ 服务启动完成！"
