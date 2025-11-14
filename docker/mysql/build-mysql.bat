@echo off
chcp 65001 >nul

REM 构建 MySQL 镜像
echo 🔨 Building custom MySQL image with init data...

REM 设置镜像名称和版本
set IMAGE_NAME=nexiot/mysql
set IMAGE_TAG=8.0-init

REM 构建镜像
docker build -t %IMAGE_NAME%:%IMAGE_TAG% -f docker/mysql/Dockerfile docker/mysql/

REM 检查构建结果
if %errorlevel% equ 0 (
    echo ✅ MySQL image built successfully: %IMAGE_NAME%:%IMAGE_TAG%
    echo.
    echo 📤 Push to registry:
    echo    docker push %IMAGE_NAME%:%IMAGE_TAG%
) else (
    echo ❌ Build failed!
    exit /b 1
)

pause
