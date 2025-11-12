param()

$ErrorActionPreference = "Stop"

Write-Host "🚀 启动独立前端 Docker 服务..."

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$WebUiRoot = (Resolve-Path (Join-Path $ScriptDir "..")).Path

try { docker info | Out-Null } catch { Write-Host "❌ Docker 未运行，请先启动 Docker"; exit 1 }

$useDockerCompose = $false
try { docker compose version | Out-Null; $useDockerCompose = $true } catch { try { docker-compose version | Out-Null } catch { Write-Host "❌ Docker Compose 不可用，请检查安装"; exit 1 } }

if (-not (Test-Path (Join-Path $ScriptDir ".env"))) { if (Test-Path (Join-Path $ScriptDir "env.example")) { Copy-Item (Join-Path $ScriptDir "env.example") (Join-Path $ScriptDir ".env") -Force } }

if (-not (Test-Path (Join-Path $WebUiRoot "dist"))) {
  Write-Host "🔨 前端未构建，开始执行 NPM 构建..."
  Push-Location $WebUiRoot
  & npm ci
  & npm run build
  Pop-Location
}

Write-Host "🔧 构建镜像并启动服务..."
Push-Location $ScriptDir
if ($useDockerCompose) { docker compose up -d --build } else { docker-compose up -d --build }
Pop-Location

$port = if ($env:FRONTEND_PORT) { $env:FRONTEND_PORT } else { 80 }
Write-Host "✅ 前端已启动： http://localhost:$port"
Write-Host "📊 查看状态： (cd cn-universal-web-ui/docker && docker compose ps) 或 docker-compose ps"
Write-Host "🔍 查看日志： (cd cn-universal-web-ui/docker && docker compose logs -f) 或 docker-compose logs -f"
