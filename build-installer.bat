@echo off
echo ================================================
echo Building MatelasFlow Windows Installer
echo ================================================

REM Set variables
set APP_NAME=MatelasFlow
set APP_VERSION=1.0
set VENDOR=SuperMousse
set MAIN_JAR=target\matress-1.0-SNAPSHOT.jar
set MAIN_CLASS=com.warehouse.App

echo.
echo Step 1: Building JAR with Maven...
call mvn clean package
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Maven build failed!
    pause
    exit /b 1
)

echo.
echo Step 2: Creating installer with jpackage...
jpackage ^
    --type exe ^
    --name "%APP_NAME%" ^
    --app-version "%APP_VERSION%" ^
    --vendor "%VENDOR%" ^
    --input target ^
    --main-jar matress-1.0-SNAPSHOT.jar ^
    --main-class %MAIN_CLASS% ^
    --dest installer ^
    --win-dir-chooser ^
    --win-menu ^
    --win-shortcut ^
    --win-per-user-install ^
    --java-options "-Xmx512m" ^
    --java-options "-Xms256m"

if %ERRORLEVEL% EQ 0 (
    echo.
    echo ================================================
    echo SUCCESS! Installer created in 'installer' folder
    echo ================================================
    echo.
    echo File: installer\%APP_NAME%-%APP_VERSION%.exe
    echo.
    echo You can now send this .exe file to your friend!
    echo.
) else (
    echo.
    echo ERROR: jpackage failed!
    echo Make sure you have JDK 17+ installed
    pause
    exit /b 1
)

pause
