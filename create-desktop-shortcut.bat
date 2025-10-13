@echo off
:: ==============================================================================
:: Create Desktop Shortcut for MatelasPro
:: ==============================================================================

echo.
echo Creating desktop shortcut for MatelasPro...
echo.

:: Get the current directory
set "SCRIPT_DIR=%~dp0"

:: Get the Desktop path
set "DESKTOP=%USERPROFILE%\Desktop"

:: Create VBS script to make shortcut
set "VBS_SCRIPT=%TEMP%\create_shortcut.vbs"

echo Set oWS = WScript.CreateObject("WScript.Shell") > "%VBS_SCRIPT%"
echo sLinkFile = "%DESKTOP%\MatelasPro.lnk" >> "%VBS_SCRIPT%"
echo Set oLink = oWS.CreateShortcut(sLinkFile) >> "%VBS_SCRIPT%"
echo oLink.TargetPath = "wscript.exe" >> "%VBS_SCRIPT%"
echo oLink.Arguments = """%SCRIPT_DIR%MatelasPro-Launcher.vbs""" >> "%VBS_SCRIPT%"
echo oLink.WorkingDirectory = "%SCRIPT_DIR%" >> "%VBS_SCRIPT%"
echo oLink.Description = "MatelasPro - Gestion d'Entrepot STE Habiba" >> "%VBS_SCRIPT%"
echo oLink.Save >> "%VBS_SCRIPT%"

:: Run the VBS script
cscript //nologo "%VBS_SCRIPT%"

:: Clean up
del "%VBS_SCRIPT%"

echo.
echo [SUCCESS] Desktop shortcut created!
echo.
echo Shortcut location: %DESKTOP%\MatelasPro.lnk
echo.
echo You can now launch MatelasPro from your desktop!
echo.
pause
