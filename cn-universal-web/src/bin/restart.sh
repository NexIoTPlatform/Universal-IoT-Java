#!/bin/bash

# 启用严格模式
set -euo pipefail
trap 'echo "Error: Restart failed at line $LINENO." >&2' ERR

#======================================================================
# 项目重启脚本
# 先调用 stop.sh 停服，再调用 start.sh 启动
#======================================================================

APPLICATION="cn-universal-web"
MAIN_CLASS="cn.universal.CnUniversalIoTApplication"
BIN_PATH=$(cd "$(dirname "$0")" && pwd)
LOG_FILE="${BIN_PATH}/restart.log"

# 记录日志到文件，同时输出到控制台
exec > >(tee -a "$LOG_FILE") 2>&1
echo "$(date '+%Y-%m-%d %H:%M:%S') - Restarting ${APPLICATION}"

# 停止服务
echo "Stopping ${APPLICATION}..."
if ! "${BIN_PATH}/stop.sh"; then
    echo "Warning: Stop script failed, but continuing with restart..."
fi

# 等待进程退出（带超时），如果超时则强制终止
MAX_WAIT=10
while pgrep -f "${MAIN_CLASS}" > /dev/null && [ $MAX_WAIT -gt 0 ]; do
    sleep 1
    ((MAX_WAIT--))
done

# 如果进程仍在运行，强制终止
if pgrep -f "${MAIN_CLASS}" > /dev/null; then
    echo "Warning: Process did not stop within 10 seconds, forcing kill..."
    pkill -9 -f "${MAIN_CLASS}" || true
    sleep 2
fi

# 确认进程已停止
if pgrep -f "${MAIN_CLASS}" > /dev/null; then
    echo "Error: Failed to stop process even with kill -9." >&2
    exit 1
else
    echo "Process stopped successfully."
fi

# 启动服务
echo "Starting ${APPLICATION}..."
if ! "${BIN_PATH}/start.sh"; then
    echo "Error: Start failed." >&2
    exit 1
fi

echo "$(date) - ${APPLICATION} restarted successfully!"
echo "Restart completed. Log details: $LOG_FILE"