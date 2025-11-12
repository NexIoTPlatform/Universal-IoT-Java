param()

$ErrorActionPreference = "Stop"

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$RepoRoot  = (Resolve-Path (Join-Path $ScriptDir "..")).Path

Write-Host "🚀 启动 IoT 平台 Docker 服务..."

try { docker info | Out-Null } catch { Write-Host "❌ Docker 未运行，请先启动 Docker"; exit 1 }

$useDockerCompose = $false
try { docker compose version | Out-Null; $useDockerCompose = $true } catch { try { docker-compose version | Out-Null } catch { Write-Host "❌ Docker Compose 不可用，请检查安装"; exit 1 } }

New-Item -ItemType Directory -Force -Path (Join-Path $ScriptDir "logs") | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $ScriptDir "mysql/conf") | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $ScriptDir "mysql/init") | Out-Null

if (-not (Test-Path (Join-Path $ScriptDir ".env"))) { Copy-Item (Join-Path $ScriptDir "env.example") (Join-Path $ScriptDir ".env") -Force }

Write-Host "🛠 检查并构建前后端..."

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
  Write-Host "🔨 后端未构建，开始执行 Maven Reactor 构建..."
  Push-Location $RepoRoot
  & mvn -q -T 1C -DskipTests -pl cn-universal-web -am install
  & mvn -q -DskipTests -pl cn-universal-web package
  Pop-Location
}

if (-not (Test-Path (Join-Path $RepoRoot "cn-universal-web-ui/dist"))) {
  Write-Host "🔨 前端未构建，开始执行 NPM 构建..."
  Push-Location (Join-Path $RepoRoot "cn-universal-web-ui")
  & npm ci
  & npm run build
  Pop-Location
}

Write-Host "🔧 构建镜像并启动服务..."

$skipFrontend = $false
try { docker pull nginx:alpine | Out-Null } catch { Write-Host "⚠️ 无法拉取 nginx:alpine，将仅启动后端与基础服务"; $skipFrontend = $true }

Push-Location $ScriptDir
if ($useDockerCompose) {
  if ($skipFrontend) { docker compose up -d --build backend mysql redis emqx adminer } else { docker compose up -d --build }
} else {
  if ($skipFrontend) { docker-compose up -d --build backend mysql redis emqx adminer } else { docker-compose up -d --build }
}
Pop-Location

Start-Sleep -Seconds 10

Write-Host "📊 服务状态："
if ($useDockerCompose) { Push-Location $ScriptDir; docker compose ps; Pop-Location } else { Push-Location $ScriptDir; docker-compose ps; Pop-Location }

Write-Host ""
Write-Host "🌐 服务访问地址："
Write-Host "   前端界面: http://localhost:80"
Write-Host "   后端 API: http://localhost:9092"
Write-Host "   EMQX 管理: http://localhost:18083 (admin/public)"
Write-Host "   数据库管理: http://localhost:8081"
if ($skipFrontend) { Write-Host "⚠️ 已跳过前端。后端接口: http://localhost:9092" }
Write-Host ""
Write-Host "📋 常用命令："
Write-Host "   查看日志: (cd docker && docker compose logs -f) 或 docker-compose logs -f"
Write-Host "   停止服务: (cd docker && docker compose down) 或 docker-compose down"
Write-Host "   重启服务: (cd docker && docker compose restart) 或 docker-compose restart"
Write-Host "   查看状态: (cd docker && docker compose ps) 或 docker-compose ps"
Write-Host ""
Write-Host "✅ 服务启动完成！"
