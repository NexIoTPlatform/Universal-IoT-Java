#!/bin/bash

#======================================================================
# 项目启动shell脚本
# bin目录: 脚本目录
# config目录: 配置文件目录
# logs目录: 项目运行日志目录
# logs/startup.log: 记录启动日志
# nohup后台运行
#
#======================================================================

# 项目名称
APPLICATION="cn-universal-web"

# 项目启动jar包名称
APPLICATION_JAR="${APPLICATION}.jar"

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

# 外部配置文件绝对目录
CONFIG_DIR="${BASE_PATH}/config/"

# 项目日志输出绝对路径
LOG_DIR="$(dirname "${BASE_PATH}")/logs"
LOG_FILE="${APPLICATION}.log"
LOG_PATH="${LOG_DIR}/${LOG_FILE}"

# 项目启动日志输出绝对路径
LOG_STARTUP_PATH="${LOG_DIR}/startup.log"

# 构建JAR_CLASS_PATH
JAR_CLASS_PATH="${BASE_PATH}/${APPLICATION_JAR}:${BASE_PATH}/lib/*"

# 当前时间
NOW=$(date '+%Y-%m-%d %H:%M:%S')

# 启动日志
STARTUP_LOG="================================================ ${NOW} ================================================\n"

# 如果logs文件夹不存在,则创建文件夹
if [ ! -d "${LOG_DIR}" ]; then
  mkdir -p "${LOG_DIR}"
fi

# 如果项目运行日志存在,则清空
if [ -f "${LOG_PATH}" ]; then
  > "${LOG_PATH}"
fi

# 如果项目启动日志不存在,则创建,否则追加
echo -e "${STARTUP_LOG}" >> "${LOG_STARTUP_PATH}"

#==========================================================================================
# JVM Configuration
# -Xmx4096m:设置JVM最大可用内存为4096m
# -Xms4096m:设置JVM初始内存
# -Xmn2048m:设置年轻代大小为2048m
# -XX:MetaspaceSize=2048m:存储class的内存大小
# -XX:MaxMetaspaceSize=2048m:限制Metaspace增长的上限
# -XX:-OmitStackTraceInFastThrow:解决重复异常不打印堆栈信息问题
#==========================================================================================
JAVA_OPT="-Duser.timezone=Asia/Shanghai -server -Xms4096m -Xmx4096m -Xmn2048m -XX:MetaspaceSize=2048m -XX:MaxMetaspaceSize=2048m"

JAVA_OPT="${JAVA_OPT} -XX:-OmitStackTraceInFastThrow"

# ================= 监控与诊断增强 =================
JAVA_OPT="${JAVA_OPT} -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"  # 新增远程调试
#JAVA_OPT="${JAVA_OPT} -Xlog:gc*,gc+heap=debug,gc+age=trace,safepoint:file=${LOG_DIR}/gc.log:time,uptime,level,tags:filecount=5,filesize=100M"
#JAVA_OPT="${JAVA_OPT} -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${LOG_DIR}/"
# 统一日志（替代旧版PrintGC参数）
#JAVA_OPT="${JAVA_OPT} -Xlog:gc*,gc+heap=debug,gc+age=trace,safepoint:file=${LOG_DIR}/gc.log:time,uptime,level,tags:filecount=5,filesize=100M"                                                                                                                                               JAVA_OPT="${JAVA_OPT} -Xlog:gc*,gc+heap=debug,gc+age=trace,safepoint:file=${LOG_DIR}/gc.log:time,uptime,level,tags:filecount=5,filesize=100M" # 内存溢出时自动转储
# 内存溢出时自动转储
JAVA_OPT="${JAVA_OPT} -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${LOG_DIR}/"
JAVA_OPT="${JAVA_OPT} -Xlog:async"
# 添加监控参数，帮助排查假死问题
JAVA_OPT="${JAVA_OPT} -XX:+PrintConcurrentLocks"
JAVA_OPT="${JAVA_OPT} -XX:+UnlockDiagnosticVMOptions"
JAVA_OPT="${JAVA_OPT} -XX:+LogVMOutput -XX:+LogCompilation"
JAVA_OPT="${JAVA_OPT} -Djava.rmi.server.hostname=localhost"
JAVA_OPT="${JAVA_OPT} -Dcom.sun.management.jmxremote"
JAVA_OPT="${JAVA_OPT} -Dcom.sun.management.jmxremote.port=9999"
JAVA_OPT="${JAVA_OPT} -Dcom.sun.management.jmxremote.authenticate=false"
JAVA_OPT="${JAVA_OPT} -Dcom.sun.management.jmxremote.ssl=false"



#=======================================================
# 将命令启动相关日志追加到日志文件
#=======================================================

# 输出项目名称
STARTUP_LOG="${STARTUP_LOG}application name: ${APPLICATION}\n"
# 输出jar包名称
STARTUP_LOG="${STARTUP_LOG}application jar name: ${APPLICATION_JAR}\n"
# 输出主类名
STARTUP_LOG="${STARTUP_LOG}main class: ${MAIN_CLASS}\n"
# 输出项目bin路径
STARTUP_LOG="${STARTUP_LOG}application bin  path: ${BIN_PATH}\n"
# 输出项目根目录
STARTUP_LOG="${STARTUP_LOG}application root path: ${BASE_PATH}\n"
# 打印日志路径
STARTUP_LOG="${STARTUP_LOG}application log  path: ${LOG_PATH}\n"
# 打印JVM配置
STARTUP_LOG="${STARTUP_LOG}application JAVA_OPT : ${JAVA_OPT}\n"

# 检查jar包是否存在
if [ ! -f "${BASE_PATH}/${APPLICATION_JAR}" ]; then
    echo "Error: JAR file ${BASE_PATH}/${APPLICATION_JAR} not found!"
    exit 1
fi

# 检查lib目录是否存在
if [ ! -d "${BASE_PATH}/lib" ]; then
    echo "Error: lib directory ${BASE_PATH}/lib not found!"
    exit 1
fi

# 检查配置文件目录是否存在
if [ ! -d "${CONFIG_DIR}" ]; then
    echo "Warning: Config directory ${CONFIG_DIR} not found!"
fi

# 调试：打印关键变量
echo "BASE_PATH: ${BASE_PATH}" >> "${LOG_STARTUP_PATH}"
echo "APPLICATION_JAR: ${APPLICATION_JAR}" >> "${LOG_STARTUP_PATH}"
echo "LIB_DIR: ${BASE_PATH}/lib" >> "${LOG_STARTUP_PATH}"
# 打印JAR_CLASS_PATH用于调试
echo "JAR_CLASS_PATH: ${JAR_CLASS_PATH}" >> "${LOG_STARTUP_PATH}"

# 打印启动命令
STARTUP_LOG="${STARTUP_LOG}application background startup command: nohup java ${JAVA_OPT} -cp \"${JAR_CLASS_PATH}\" ${MAIN_CLASS} --spring.config.location=${CONFIG_DIR} --logging.config=${CONFIG_DIR}logback-spring.xml > \"${LOG_PATH}\" 2>&1 &\n"

#======================================================================
# 执行启动命令：后台启动项目
#======================================================================

echo "Starting ${APPLICATION}..."

echo -e "${STARTUP_LOG}"

# 启动应用并重定向日志到应用日志文件[1,2](@ref)
nohup java ${JAVA_OPT} -cp "${JAR_CLASS_PATH}" ${MAIN_CLASS} --spring.config.location="${CONFIG_DIR}" --logging.config="${CONFIG_DIR}logback-spring.xml" > /dev/null 2>&1 &

# 等待进程启动（带超时检测）[3](@ref)
MAX_WAIT=15
PID=""
while [ $MAX_WAIT -gt 0 ] && [ -z "$PID" ]; do
    PID=$(pgrep -f "${MAIN_CLASS}")
    sleep 1
    ((MAX_WAIT--))
done

if [ -n "$PID" ]; then
    STARTUP_LOG="${STARTUP_LOG}application pid: ${PID}\n"
    STARTUP_LOG="${STARTUP_LOG}application started successfully!\n"
    echo "Application started successfully! PID: ${PID}"
else
    STARTUP_LOG="${STARTUP_LOG}application failed to start!\n"
    echo "Error: Application failed to start! Check log: ${LOG_PATH}"
    exit 1
fi

# 启动日志追加到启动日志文件中
echo -e "${STARTUP_LOG}" >> "${LOG_STARTUP_PATH}"

echo "Log file: ${LOG_PATH}"
echo "Startup log: ${LOG_STARTUP_PATH}"