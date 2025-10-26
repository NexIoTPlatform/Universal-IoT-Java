#!/bin/bash

#======================================================================
# 项目停服shell脚本
# 通过项目名称查找到PID
# 然后直接kill -9 pid强制终止
#
#======================================================================

# 项目名称
APPLICATION="cn-universal-web"

# 主类名
MAIN_CLASS="cn.universal.CnUniversalIoTApplication"

# bin目录绝对路径
BIN_PATH=$(cd "$(dirname "$0")" && pwd)
# 进入bin目录
cd "$(dirname "$0")"
# 返回到上一级项目根目录路径
cd ..
# 打印项目根目录绝对路径
BASE_PATH=$(pwd)

echo "Stopping ${APPLICATION}..."

# 查找进程ID，使用主类名匹配
PID=$(ps -ef | grep "${MAIN_CLASS}" | grep -v grep | awk '{ print $2 }')

if [[ -z "$PID" ]]; then
    echo "${APPLICATION} is not running"
    exit 0
else
    echo "Found process(es): ${PID}"
    
    # 直接强制终止所有进程
    for pid in $PID; do
        echo "Force killing process ${pid}..."
        kill -9 "$pid" 2>/dev/null || true
    done
    
    # 等待1秒确保进程完全终止
    sleep 1
    
    # 最终检查
    FINAL_PID=$(ps -ef | grep "${MAIN_CLASS}" | grep -v grep | awk '{ print $2 }')
    
    if [[ -z "$FINAL_PID" ]]; then
        echo "${APPLICATION} stopped successfully"
    else
        echo "Warning: Some processes may still be running: ${FINAL_PID}"
        # 即使还有进程在运行也不退出，让重启脚本处理
        exit 0
    fi
fi