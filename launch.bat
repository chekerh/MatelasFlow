@echo off
echo ========================================
echo    MatelasPro - Gestion d'Entrepot
echo ========================================
echo.
echo Lancement de l'application...
echo.

REM Vérifier si Java est installé
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERREUR: Java n'est pas installé ou n'est pas dans le PATH
    echo Veuillez installer Java 17 ou supérieur
    pause
    exit /b 1
)

REM Lancer l'application avec les modules JavaFX
java --module-path "%JAVA_HOME%\lib" --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base -jar target/warehouse-mattress-app-1.0-SNAPSHOT-jar-with-dependencies.jar

if %errorlevel% neq 0 (
    echo.
    echo ERREUR: Impossible de lancer l'application
    echo Vérifiez que le fichier JAR existe dans le dossier target/
    echo Ou essayez: java -jar target/warehouse-mattress-app-1.0-SNAPSHOT-jar-with-dependencies.jar
    pause
) 