@echo off
echo ================================================
echo MatelasFlow - MySQL Service Installation
echo ================================================
echo.
echo This script will install MySQL as a Windows Service
echo so it starts automatically with Windows.
echo.
echo ⚠️ IMPORTANT: This script must be run as ADMINISTRATOR
echo.
pause

REM Check if running as administrator
net session >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ ERROR: This script must be run as Administrator!
    echo.
    echo Right-click this file and select "Run as administrator"
    echo.
    pause
    exit /b 1
)

echo.
echo Checking XAMPP installation...

REM Check if XAMPP is installed
if not exist "C:\xampp\mysql\bin\mysqld.exe" (
    echo.
    echo ❌ ERROR: XAMPP is not installed or not found at C:\xampp
    echo.
    echo Please install XAMPP first:
    echo https://www.apachefriends.org/download.html
    echo.
    pause
    exit /b 1
)

echo ✅ XAMPP found at C:\xampp
echo.

echo Step 1: Stopping any running MySQL instances...
net stop MySQL 2>nul
taskkill /F /IM mysqld.exe 2>nul
timeout /t 2 >nul
echo ✅ Stopped

echo.
echo Step 2: Installing MySQL as Windows Service...
"C:\xampp\mysql\bin\mysqld.exe" --install MySQL --defaults-file="C:\xampp\mysql\bin\my.ini"
if %ERRORLEVEL% EQU 0 (
    echo ✅ MySQL service installed successfully
) else (
    echo ⚠️ Service may already be installed
)

echo.
echo Step 3: Configuring service to start automatically...
sc config MySQL start= auto
if %ERRORLEVEL% EQU 0 (
    echo ✅ Service configured to auto-start
) else (
    echo ❌ Failed to configure auto-start
)

echo.
echo Step 4: Starting MySQL service...
net start MySQL
if %ERRORLEVEL% EQU 0 (
    echo ✅ MySQL service started successfully
) else (
    echo ❌ Failed to start service
    echo.
    echo Troubleshooting:
    echo 1. Check if port 3306 is already in use
    echo 2. Check MySQL error log: C:\xampp\mysql\data\*.err
    echo 3. Try starting MySQL from XAMPP Control Panel
)

echo.
echo ================================================
echo Installation Complete!
echo ================================================
echo.
echo MySQL is now installed as a Windows Service.
echo It will start automatically when Windows starts.
echo.
echo You can manage the service:
echo - Start: net start MySQL
echo - Stop: net stop MySQL
echo - Remove: sc delete MySQL
echo.
echo To verify, open Services (services.msc)
echo and look for "MySQL" service.
echo.
pause
