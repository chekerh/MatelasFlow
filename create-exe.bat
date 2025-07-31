@echo off
echo ========================================
echo    Creation de l'executable MatelasPro
echo ========================================
echo.

REM Vérifier si jpackage est disponible (Java 14+)
jpackage --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERREUR: jpackage n'est pas disponible
    echo Veuillez installer Java 14 ou supérieur
    pause
    exit /b 1
)

echo Compilation de l'application...
mvn clean package

if %errorlevel% neq 0 (
    echo ERREUR: Compilation echouee
    pause
    exit /b 1
)

echo.
echo Creation de l'executable avec jpackage...
echo.

REM Créer l'exécutable avec jpackage
jpackage ^
  --input target ^
  --name "MatelasPro" ^
  --main-jar warehouse-mattress-app-1.0-SNAPSHOT-jar-with-dependencies.jar ^
  --main-class com.warehouse.App ^
  --type exe ^
  --dest . ^
  --java-options "--module-path %JAVA_HOME%\lib --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base" ^
  --java-options "-Dfile.encoding=UTF-8" ^
  --java-options "-Xmx1024m" ^
  --java-options "-Xms256m" ^
  --icon "src/main/resources/images/icon.ico" ^
  --app-version "1.0.0" ^
  --vendor "MatelasPro" ^
  --description "Gestion d'Entrepot de Matelas" ^
  --win-dir-chooser ^
  --win-menu ^
  --win-shortcut

if %errorlevel% equ 0 (
    echo.
    echo ✅ Executable cree avec succes !
    echo Fichier: MatelasPro-1.0.exe
    echo.
    echo Vous pouvez maintenant distribuer cet executable.
) else (
    echo.
    echo ❌ Erreur lors de la creation de l'executable
    echo.
    echo Alternative: Utilisez Launch4j
    echo 1. Telechargez Launch4j depuis https://launch4j.sourceforge.net/
    echo 2. Ouvrez launch4j-config.xml avec Launch4j
    echo 3. Cliquez sur "Build wrapper"
)

pause 