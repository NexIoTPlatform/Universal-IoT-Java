# cn-universal-web-ui 前端独立 Docker 目录

本目录用于前端独立运行的 Docker 化编排（不依赖后端与其他服务）。

## 目录结构
- `docker-compose.yml`：前端单服务的编排文件，使用上级目录的 Dockerfile 构建
- `start-frontend.sh`：一键构建并启动前端镜像与容器（自动构建 dist）
- `env.example`：默认环境变量示例（端口、Dockerfile 名称）

## 使用方法
1. 构建与启动
   ```bash
   cd cn-universal-web-ui/docker
   ./start-frontend.sh
   ```
   - 脚本会在未构建 `dist/` 时自动执行 `npm ci && npm run build`
   - 默认暴露端口为 `80`，可通过 `.env` 修改 `FRONTEND_PORT`

2. 直接使用 docker-compose
   ```bash
   cd cn-universal-web-ui/docker
   cp env.example .env  # 如需修改端口
   docker-compose up -d --build
   ```

3. 访问
   - 前端地址：`http://localhost:80`（或你在 `.env` 指定的端口）

## 备注
- 构建上下文指向上级目录 `..`，使用现有 `Dockerfile`（默认 `nginx:alpine`），只复制 `dist/` 静态资源与 `nginx.conf`
- 如需切换到其它 Dockerfile（例如 `Dockerfile.ubuntu`），可在 `.env` 中设置 `DOCKERFILE_NAME`
- 前端已有 `.dockerignore`，镜像构建不会包含源代码或 `.git` 等无关内容