@echo off
echo ========================================
echo    Build MatelasPro - Gestion d'Entrepot
echo ========================================
echo.

echo Compilation et packaging...
mvn clean package

if %errorlevel% equ 0 (
    echo.
    echo ✅ Build reussi !
    echo.
    echo Fichiers crees dans target/ :
    echo - warehouse-mattress-app-1.0-SNAPSHOT.jar
    echo - warehouse-mattress-app-1.0-SNAPSHOT-shaded.jar (RECOMMANDE)
    echo - warehouse-mattress-app-1.0-SNAPSHOT-jar-with-dependencies.jar
    echo.
    echo Pour tester l'application :
    echo java -jar target/warehouse-mattress-app-1.0-SNAPSHOT-shaded.jar
    echo.
    echo Ou utiliser le script : launch.bat
) else (
    echo.
    echo ❌ Build echoue !
    echo Verifiez les erreurs ci-dessus.
)

pause 