Set WshShell = CreateObject("WScript.Shell")
' Get the directory where this script is located
ScriptDir = CreateObject("Scripting.FileSystemObject").GetParentFolderName(WScript.ScriptFullName)
' Run the batch file hidden
WshShell.Run chr(34) & ScriptDir & "\start-matelas-pro.bat" & Chr(34), 0
Set WshShell = Nothing
