@echo off
chcp 65001 >nul
cd /d "%~dp0"
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0conectar_claude.ps1"
echo.
echo Ahora cierra Claude Desktop por completo (tambien desde la bandeja) y abrelo de nuevo.
pause
