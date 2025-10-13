@echo off
echo ========================================
echo  Creating warehouse_db Database
echo ========================================
echo.

REM Find MySQL executable
set MYSQL_PATH=
if exist "C:\xampp\mysql\bin\mysql.exe" set MYSQL_PATH=C:\xampp\mysql\bin\mysql.exe
if exist "C:\XAMPP\mysql\bin\mysql.exe" set MYSQL_PATH=C:\XAMPP\mysql\bin\mysql.exe

if "%MYSQL_PATH%"=="" (
    echo ERROR: MySQL not found! Please install XAMPP first.
    pause
    exit /b 1
)

echo [1/2] Checking MySQL service...
tasklist /FI "IMAGENAME eq mysqld.exe" 2>NUL | find /I /N "mysqld.exe">NUL
if "%ERRORLEVEL%"=="0" (
    echo MySQL is running ✓
) else (
    echo MySQL is NOT running!
    echo Please start MySQL in XAMPP Control Panel first.
    pause
    exit /b 1
)

echo.
echo [2/2] Creating database warehouse_db...
"%MYSQL_PATH%" -u root -e "CREATE DATABASE IF NOT EXISTS warehouse_db CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo  ✓ Database created successfully!
    echo ========================================
    echo.
    echo Database: warehouse_db
    echo Character Set: utf8mb4
    echo.
) else (
    echo.
    echo ERROR: Failed to create database
    echo Please check MySQL is running and try again
)

echo.
pause
