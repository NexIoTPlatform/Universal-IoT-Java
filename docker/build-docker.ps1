param(
  [string]$Backend = "default",
  [string]$Frontend = "default"
)

$ErrorActionPreference = "Stop"

Write-Host "🚀 开始构建 Docker 镜像 (JDK 21 + MySQL 5.8 + EMQX)..."
Write-Host ""
Write-Host "📋 支持的构建版本："
Write-Host "   后端版本："
Write-Host "     .\build-docker.ps1 -Backend <default|alpine|corretto> -Frontend <default|ubuntu|centos|simple|local>"
Write-Host ""
Write-Host "   示例："
Write-Host "     .\build-docker.ps1"
Write-Host "     .\build-docker.ps1 -Backend alpine"
Write-Host "     .\build-docker.ps1 -Backend corretto -Frontend ubuntu"
Write-Host "     .\build-docker.ps1 -Frontend simple"
Write-Host ""

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$RepoRoot  = (Resolve-Path (Join-Path $ScriptDir "..")).Path

# .env
if (-not (Test-Path (Join-Path $ScriptDir ".env"))) {
  Write-Host "📝 创建 .env 文件..."
  Copy-Item (Join-Path $ScriptDir "env.example") (Join-Path $ScriptDir ".env") -Force
  Write-Host "✅ .env 文件已创建，请根据需要修改配置"
}

# 构建后端（优先使用 tar.gz 产物）
if (-not (Test-Path (Join-Path $RepoRoot "cn-universal-web/target/cn-universal-web"))) {
  $tarPath = Join-Path $RepoRoot "cn-universal-web/target/cn-universal-web.tar.gz"
  if (Test-Path $tarPath) {
    Write-Host "📦 检测到后端产物 cn-universal-web.tar.gz，正在解压..."
    Push-Location (Join-Path $RepoRoot "cn-universal-web/target")
    & tar -xzf cn-universal-web.tar.gz
    Pop-Location
  }
}

if (-not (Test-Path (Join-Path $RepoRoot "cn-universal-web/target/cn-universal-web"))) {
  Write-Host "🔨 未发现后端构建目录，开始执行 Maven Reactor 构建..."
  Push-Location $RepoRoot
  & mvn -q -T 1C -DskipTests -pl cn-universal-web -am install
  & mvn -q -DskipTests -pl cn-universal-web package
  Pop-Location
}

# 构建前端
if (-not (Test-Path (Join-Path $RepoRoot "cn-universal-web-ui/dist"))) {
  Write-Host "🔨 前端未构建，开始执行 NPM 构建..."
  Push-Location (Join-Path $RepoRoot "cn-universal-web-ui")
  & npm ci
  & npm run build
  Pop-Location
}

# 后端镜像
Write-Host "🔨 构建后端镜像..."
Push-Location (Join-Path $RepoRoot "cn-universal-web")
switch ($Backend) {
  "alpine" {
    Write-Host "📦 使用 Alpine 版本 (镜像更小)"
    & docker build -f Dockerfile.alpine -t cn-universal-backend:alpine .
    & docker tag cn-universal-backend:alpine cn-universal-backend:latest
  }
  "corretto" {
    Write-Host "🏢 使用 Amazon Corretto 版本 (企业级支持)"
    & docker build -f Dockerfile.amazoncorretto -t cn-universal-backend:corretto .
    & docker tag cn-universal-backend:corretto cn-universal-backend:latest
  }
  Default {
    Write-Host "📦 使用默认 OpenJDK 版本"
    & docker build -t cn-universal-backend:latest .
  }
}
Pop-Location

# 前端镜像
Write-Host "🔨 构建前端镜像..."
Push-Location (Join-Path $RepoRoot "cn-universal-web-ui")
switch ($Frontend) {
  "ubuntu" {
    Write-Host "🐧 使用 Ubuntu 版本"
    & docker build -f Dockerfile.ubuntu -t cn-universal-frontend:ubuntu .
    & docker tag cn-universal-frontend:ubuntu cn-universal-frontend:latest
  }
  "centos" {
    Write-Host "🔴 使用 CentOS 版本"
    & docker build -f Dockerfile.centos -t cn-universal-frontend:centos .
    & docker tag cn-universal-frontend:centos cn-universal-frontend:latest
  }
  "simple" {
    Write-Host "🐍 使用 Python 简单版本"
    & docker build -f Dockerfile.simple -t cn-universal-frontend:simple .
    & docker tag cn-universal-frontend:simple cn-universal-frontend:latest
  }
  "local" {
    Write-Host "🏠 使用本地镜像版本"
    & docker build -f Dockerfile.local -t cn-universal-frontend:local .
    & docker tag cn-universal-frontend:local cn-universal-frontend:latest
  }
  Default {
    Write-Host "📦 使用默认 Alpine 版本"
    & docker build -t cn-universal-frontend:latest .
  }
}
Pop-Location

Write-Host "✅ 镜像构建完成！"
Write-Host ""
Write-Host "📋 可用镜像："
Write-Host "   - cn-universal-backend:latest"
Write-Host "   - cn-universal-frontend:latest"
Write-Host ""
Write-Host "🚀 启动服务："
Write-Host "   cd docker && docker-compose up -d"
Write-Host ""
Write-Host "📊 查看服务状态："
Write-Host "   cd docker && docker-compose ps"
Write-Host ""
Write-Host "🔍 查看日志："
Write-Host "   cd docker && docker-compose logs -f"
