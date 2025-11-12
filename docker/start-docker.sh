#!/bin/bash

# Docker 快速启动脚本
set -e

echo "🚀 启动 IoT 平台 Docker 服务..."

# 统一脚本路径与仓库根路径，保证任意位置执行都正确
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

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
mkdir -p "${SCRIPT_DIR}/logs"
mkdir -p "${SCRIPT_DIR}/mysql/conf" "${SCRIPT_DIR}/mysql/init"

# 检查配置文件
if [ ! -f "${SCRIPT_DIR}/.env" ]; then
    echo "📝 创建 .env 文件..."
    cp "${SCRIPT_DIR}/env.example" "${SCRIPT_DIR}/.env"
    echo "✅ .env 文件已创建，请根据需要修改配置"
fi

# 自动构建（如未构建）
echo "🛠 检查并构建前后端..."

# 构建后端（如未构建产物）
if [ ! -d "${REPO_ROOT}/cn-universal-web/target/cn-universal-web" ]; then
  echo "🔨 后端未构建，开始执行 Maven Reactor 构建..."
  (
    cd "${REPO_ROOT}"
    echo "📦 构建并安装依赖模块 (Reactor -am)..."
    mvn -q -T 1C -DskipTests -pl cn-universal-web -am install
    echo "📦 打包 cn-universal-web..."
    mvn -q -DskipTests -pl cn-universal-web package
  )
fi

# 构建前端（如未构建产物）
if [ ! -d "${REPO_ROOT}/cn-universal-web-ui/dist" ]; then
  echo "🔨 前端未构建，开始执行 NPM 构建..."
  (cd "${REPO_ROOT}/cn-universal-web-ui" && npm ci && npm run build)
fi

# 构建镜像并启动服务
echo "🔧 构建镜像并启动服务..."
SKIP_FRONTEND=0
if ! docker pull --quiet nginx:alpine >/dev/null 2>&1; then
  echo "⚠️ 无法从 Docker Hub 拉取 nginx:alpine，前端镜像可能无法构建。将仅启动后端与基础服务。"
  SKIP_FRONTEND=1
fi

if [ "$SKIP_FRONTEND" -eq 1 ]; then
  (cd "${SCRIPT_DIR}" && docker-compose up -d --build backend mysql redis emqx adminer)
else
  (cd "${SCRIPT_DIR}" && docker-compose up -d --build)
fi

# 等待服务启动
echo "⏳ 等待服务启动..."
sleep 10

# 检查服务状态
echo "📊 服务状态："
(cd "${SCRIPT_DIR}" && docker-compose ps)

# 显示访问信息
echo ""
echo "🌐 服务访问地址："
echo "   前端界面: http://localhost:80"
echo "   后端 API: http://localhost:9092"
echo "   EMQX 管理: http://localhost:18083 (admin/public)"
echo "   数据库管理: http://localhost:8081"
echo ""
echo "📋 常用命令："
echo "   查看日志: (cd docker && docker-compose logs -f)"
echo "   停止服务: (cd docker && docker-compose down)"
echo "   重启服务: (cd docker && docker-compose restart)"
echo "   查看状态: (cd docker && docker-compose ps)"
if [ "$SKIP_FRONTEND" -eq 1 ]; then
  echo "⚠️ 已跳过前端。原因：无法访问 Docker Hub。后端接口: http://localhost:9092"
fi
echo ""
echo "✅ 服务启动完成！"
