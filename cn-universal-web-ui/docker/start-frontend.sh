#!/bin/bash

set -e

echo "🚀 启动独立前端 Docker 服务..."

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
WEBUI_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

# 检查 Docker 与 Compose
if ! docker info > /dev/null 2>&1; then
  echo "❌ Docker 未运行，请先启动 Docker"
  exit 1
fi
if ! docker-compose version > /dev/null 2>&1; then
  echo "❌ Docker Compose 不可用，请检查安装"
  exit 1
fi

# 生成 .env（如不存在）
if [ ! -f "${SCRIPT_DIR}/.env" ] && [ -f "${SCRIPT_DIR}/env.example" ]; then
  echo "📝 创建 .env 文件..."
  cp "${SCRIPT_DIR}/env.example" "${SCRIPT_DIR}/.env"
fi

# 前端构建（如未构建 dist）
if [ ! -d "${WEBUI_ROOT}/dist" ]; then
  echo "🔨 前端未构建，开始执行 NPM 构建..."
  (cd "${WEBUI_ROOT}" && npm ci && npm run build)
fi

# 构建并启动
echo "🔧 构建镜像并启动服务..."
(cd "${SCRIPT_DIR}" && docker-compose up -d --build)

PORT=${FRONTEND_PORT:-80}
echo "✅ 前端已启动： http://localhost:${PORT}"
echo "📊 查看状态： (cd cn-universal-web-ui/docker && docker-compose ps)"
echo "🔍 查看日志： (cd cn-universal-web-ui/docker && docker-compose logs -f)"