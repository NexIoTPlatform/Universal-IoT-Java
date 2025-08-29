@echo off
rem ======================================================================
rem Windows stop script
rem
rem ======================================================================

rem 项目名称
set APPLICATION=cn-universal-web

rem 主类名
set MAIN_CLASS=cn.universal.CnUniversalIoTApplication

rem 获取脚本所在目录
set BIN_PATH=%~dp0
rem 获取项目根目录
set BASE_PATH=%BIN_PATH%..

echo Stopping %APPLICATION%...

rem 查找进程ID，使用主类名匹配
for /f "tokens=2" %%i in ('tasklist /fi "imagename eq java.exe" /fo csv ^| find "%MAIN_CLASS%"') do (
    set PID=%%i
    goto :found
)

echo %APPLICATION% is not running
goto :end

:found
echo Found process: %PID%

rem 停止进程
taskkill /PID %PID% /F

if %ERRORLEVEL% equ 0 (
    echo %APPLICATION% stopped successfully
) else (
    echo Warning: Failed to stop %APPLICATION%
    exit /b 1
)

:end
pause 