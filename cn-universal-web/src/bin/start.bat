@echo off
rem ======================================================================
rem Windows startup script
rem
rem ======================================================================

rem Open in a browser
rem start "" "http://localhost:8080/example/hello?name=123"

rem 项目名称
set APPLICATION=cn-universal-web

rem 主类名
set MAIN_CLASS=cn.universal.CnUniversalIoTApplication

rem 获取脚本所在目录
set BIN_PATH=%~dp0
rem 获取项目根目录
set BASE_PATH=%BIN_PATH%..

rem 配置文件目录
set CONFIG_DIR=%BASE_PATH%\config

rem 日志目录
set LOG_DIR=%BASE_PATH%\logs

rem 创建日志目录
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"

rem 检查jar包是否存在
if not exist "%BASE_PATH%\%APPLICATION%.jar" (
    echo Error: JAR file %BASE_PATH%\%APPLICATION%.jar not found!
    pause
    exit /b 1
)

rem 检查lib目录是否存在
if not exist "%BASE_PATH%\lib" (
    echo Error: lib directory %BASE_PATH%\lib not found!
    pause
    exit /b 1
)

rem 检查配置文件目录是否存在
if not exist "%CONFIG_DIR%" (
    echo Warning: Config directory %CONFIG_DIR% not found!
)

rem 构建classpath (Windows使用分号分隔)
set CLASSPATH=%BASE_PATH%\%APPLICATION%.jar;%BASE_PATH%\lib\*

echo Starting %APPLICATION%...

rem 启动应用
java -server -Xms4096m -Xmx4096m -Xmn2048m -XX:MetaspaceSize=2048m -XX:MaxMetaspaceSize=2048m -XX:-OmitStackTraceInFastThrow -cp "%CLASSPATH%" %MAIN_CLASS% --spring.config.location="%CONFIG_DIR%/"

if %ERRORLEVEL% neq 0 (
    echo Error: Failed to start %APPLICATION%
    pause
    exit /b 1
)

echo %APPLICATION% started successfully!
pause