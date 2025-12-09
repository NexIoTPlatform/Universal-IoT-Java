# 仅凭一个 docker-compose 文件运行（发布说明）

此说明指导你如何“脱离源码”，仅提供一个 `docker-compose.release.yml` 给他人即可运行整套服务。

## 核心思路
- 构建并推送后端、前端、（可选）自定义 MySQL 初始镜像到镜像仓库（Docker Hub/GHCR/私有仓库）。
- 在 `docker-compose.release.yml` 中引用这些镜像，不再使用 `build`，不挂载本地目录。
- 默认环境变量内嵌在 compose 中，拿到文件即可 `docker compose up -d`。

## 镜像准备
1. 后端镜像（示例）
   ```bash
   # 在仓库根或 cn-universal-web 下完成打包
   mvn -q -DskipTests -pl cn-universal-web package
   # 构建镜像
   cd cn-universal-web
   docker build -t nexiot/cn-universal-backend:1.0.0 .
   docker push nexiot/cn-universal-backend:1.0.0
   ```

2. 前端镜像（示例）
   ```bash
   cd cn-universal-web-ui
   npm ci && npm run build
   docker build -t nexiot/cn-universal-frontend:1.0.0 .
   docker push nexiot/cn-universal-frontend:1.0.0
   ```

3. MySQL 初始化镜像（可选）
   - 若需自动初始化数据库（而不靠本地挂载），可构建自定义 MySQL 镜像，将 `docker/mysql/init` 下 SQL 复制到 `/docker-entrypoint-initdb.d`。
   - 示例 Dockerfile：
     ```Dockerfile
     FROM mysql:8.0
     COPY init/ /docker-entrypoint-initdb.d/
     ```
   - 构建与推送：
     ```bash
     cd docker/mysql
     docker build -t nexiot/cn-universal-mysql:8.0-inited .
     docker push nexiot/cn-universal-mysql:8.0-inited
     ```
   - 然后在 `docker-compose.release.yml` 中将 `MYSQL_IMAGE` 设置为该镜像。

## 运行（仅凭一个 compose 文件）
- 将 `docker/docker-compose.release.yml` 发给使用者或发布到文档中。
- 使用者只需执行：
  ```bash
  docker compose -f docker-compose.release.yml up -d
  ```
- 访问：
  - 前端：`http://localhost:80`
  - 后端健康：`http://localhost:9092/actuator/health`
  - Adminer：`http://localhost:8081`

## 安全与源码泄漏
- 镜像中不包含源码：后端 Dockerfile 仅复制 `target/cn-universal-web/` 的打包产物；前端仅复制 `dist/` 与 `nginx.conf`。
- `.dockerignore` 已排除了 `src/`、`.git` 等，构建上下文不会带源码。
- 上传镜像到公开仓库不会泄漏源代码，只要遵循上述构建规范。

## 参数覆盖（可选）
- `docker-compose.release.yml` 使用了可覆盖的镜像和配置项，例如：
  - `BACKEND_IMAGE`、`FRONTEND_IMAGE`、`MYSQL_IMAGE` 等镜像名
  - `SPRING_DATASOURCE_*`、`REDIS_*`、`MQTT_*` 等配置
- 使用者可在运行前通过环境变量覆盖，不需要额外 `.env` 文件。

## 注意事项
- 如未采用自定义 MySQL 初始化镜像，请确认后端能自建库表（例如使用 Flyway/Liquibase）。否则首次启动需要手工导入初始化 SQL。
- 发布前建议在全新机器上用该 `docker-compose.release.yml` 做一次完整起跑验证，确保无本地路径依赖。