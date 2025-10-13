@echo off
SETLOCAL EnableDelayedExpansion
cls

:: ==============================================================================
:: MatelasPro Launcher with XAMPP Auto-Start
:: This script automatically starts XAMPP services and launches MatelasPro
:: ==============================================================================

title MatelasPro Launcher
color 0A
echo.
echo  ===============================================================
echo  ^|                                                             ^|
echo  ^|            MatelasPro - Demarrage Automatique              ^|
echo  ^|         Gestion d'Entrepot de Matelas STE Habiba           ^|
echo  ^|                                                             ^|
echo  ===============================================================
echo.

:: Check if running as administrator (required for XAMPP control)
net session >nul 2>&1
if %errorLevel% neq 0 (
    echo  [ATTENTION] Ce script necessite des droits administrateur.
    echo  [INFO] Relancez avec "Executer en tant qu'administrateur"
    echo.
    pause
    exit /b 1
)

:: ==============================================================================
:: CONFIGURATION - Ajustez ces chemins selon votre installation
:: ==============================================================================

:: XAMPP installation path (default)
set "XAMPP_PATH=C:\xampp"

:: Alternative paths to check
set "ALT_XAMPP_PATH_1=C:\Program Files\xampp"
set "ALT_XAMPP_PATH_2=C:\Program Files (x86)\xampp"
set "ALT_XAMPP_PATH_3=D:\xampp"

:: App JAR path (adjust if needed)
set "APP_PATH=%~dp0target\warehouse-mattress-app-1.0-SNAPSHOT.jar"

:: ==============================================================================
:: STEP 1: Detect XAMPP installation
:: ==============================================================================

echo  [1/4] Detection de XAMPP...

if exist "%XAMPP_PATH%\xampp-control.exe" (
    echo  [OK] XAMPP trouve: %XAMPP_PATH%
) else if exist "%ALT_XAMPP_PATH_1%\xampp-control.exe" (
    set "XAMPP_PATH=%ALT_XAMPP_PATH_1%"
    echo  [OK] XAMPP trouve: !XAMPP_PATH!
) else if exist "%ALT_XAMPP_PATH_2%\xampp-control.exe" (
    set "XAMPP_PATH=%ALT_XAMPP_PATH_2%"
    echo  [OK] XAMPP trouve: !XAMPP_PATH!
) else if exist "%ALT_XAMPP_PATH_3%\xampp-control.exe" (
    set "XAMPP_PATH=%ALT_XAMPP_PATH_3%"
    echo  [OK] XAMPP trouve: !XAMPP_PATH!
) else (
    echo  [ERREUR] XAMPP non trouve!
    echo  [INFO] Veuillez installer XAMPP ou modifier le chemin dans ce script.
    echo.
    pause
    exit /b 1
)

:: ==============================================================================
:: STEP 2: Check and start Apache
:: ==============================================================================

echo.
echo  [2/4] Verification Apache...

:: Check if Apache is already running
tasklist /FI "IMAGENAME eq httpd.exe" 2>NUL | find /I /N "httpd.exe">NUL
if "%ERRORLEVEL%"=="0" (
    echo  [OK] Apache est deja en cours d'execution
) else (
    echo  [INFO] Demarrage d'Apache...
    start "" "%XAMPP_PATH%\apache_start.bat"
    timeout /t 3 /nobreak >nul
    
    :: Verify Apache started
    tasklist /FI "IMAGENAME eq httpd.exe" 2>NUL | find /I /N "httpd.exe">NUL
    if "%ERRORLEVEL%"=="0" (
        echo  [OK] Apache demarre avec succes
    ) else (
        echo  [ATTENTION] Apache n'a pas demarre. Verifiez le port 80/443.
    )
)

:: ==============================================================================
:: STEP 3: Check and start MySQL
:: ==============================================================================

echo.
echo  [3/4] Verification MySQL...

:: Check if MySQL is already running
tasklist /FI "IMAGENAME eq mysqld.exe" 2>NUL | find /I /N "mysqld.exe">NUL
if "%ERRORLEVEL%"=="0" (
    echo  [OK] MySQL est deja en cours d'execution
) else (
    echo  [INFO] Demarrage de MySQL...
    start "" "%XAMPP_PATH%\mysql_start.bat"
    timeout /t 4 /nobreak >nul
    
    :: Verify MySQL started
    tasklist /FI "IMAGENAME eq mysqld.exe" 2>NUL | find /I /N "mysqld.exe">NUL
    if "%ERRORLEVEL%"=="0" (
        echo  [OK] MySQL demarre avec succes
    ) else (
        echo  [ATTENTION] MySQL n'a pas demarre. Verifiez le port 3306.
    )
)

:: ==============================================================================
:: STEP 4: Launch MatelasPro application
:: ==============================================================================

echo.
echo  [4/4] Lancement de MatelasPro...

:: Check if JAR exists
if not exist "%APP_PATH%" (
    echo  [ERREUR] Application non trouvee: %APP_PATH%
    echo  [INFO] Compilation necessaire. Lancez: mvn clean package
    echo.
    pause
    exit /b 1
)

:: Launch the application
echo  [INFO] Ouverture de l'application...
timeout /t 2 /nobreak >nul

start "" javaw -jar "%APP_PATH%"

if %errorLevel% neq 0 (
    echo  [ERREUR] Impossible de lancer l'application
    echo  [INFO] Verifiez que Java est installe
    echo.
    pause
    exit /b 1
)

echo.
echo  ===============================================================
echo  [SUCCES] MatelasPro lance avec succes!
echo  ===============================================================
echo.
echo  [INFO] Services XAMPP demarres:
echo         - Apache: Port 80/443
echo         - MySQL:  Port 3306
echo.
echo  [INFO] L'application s'ouvre...
echo.
echo  Fermez cette fenetre une fois l'application lancee.
echo  ===============================================================
echo.

:: Wait a bit then exit
timeout /t 5 /nobreak >nul
exit /b 0
