@echo off
title MatelasFlow Startup
echo ================================================
echo Starting MatelasFlow Warehouse Management System
echo ================================================
echo.

REM Check if XAMPP MySQL is already running
echo Checking MySQL status...
netstat -an | find "3306" | find "LISTENING" >nul
if %ERRORLEVEL% EQU 0 (
    echo [OK] MySQL is already running
    goto START_APP
)

REM Start MySQL via XAMPP
echo Starting MySQL (XAMPP)...
if exist "C:\xampp\mysql\bin\mysqld.exe" (
    start "" "C:\xampp\mysql_start.bat"
    echo Waiting for MySQL to start...
    timeout /t 5 /nobreak >nul
    echo [OK] MySQL started
) else (
    echo [ERROR] XAMPP not found at C:\xampp
    echo.
    echo Please install XAMPP or update the path in this script
    echo Download XAMPP from: https://www.apachefriends.org/download.html
    echo.
    pause
    exit /b 1
)

:START_APP
echo.
echo Starting MatelasFlow application...
echo.

REM Check if running as JAR or EXE
if exist "MatelasFlow.exe" (
    start "" "MatelasFlow.exe"
) else if exist "target\matress-1.0-SNAPSHOT.jar" (
    start javaw -jar "target\matress-1.0-SNAPSHOT.jar"
) else (
    echo ERROR: Application not found!
    echo Please make sure MatelasFlow.exe is in this folder
    pause
    exit /b 1
)

echo.
echo [SUCCESS] MatelasFlow is starting...
echo.
echo This window will close in 3 seconds...
timeout /t 3 >nul
exit
