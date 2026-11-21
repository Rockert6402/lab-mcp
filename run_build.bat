@echo off
chcp 65001 >nul
cd /d "%~dp0"
set "LOG=%~dp0build_log.txt"
echo ==== %date% %time% > "%LOG%"

rem ---- Java para compilar y ejecutar: el JDK de JAVA_HOME (21) si existe, si no el del PATH
set "JAVA_EXE=java"
if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
echo JAVA_EXE=%JAVA_EXE% >> "%LOG%"
"%JAVA_EXE%" -version >> "%LOG%" 2>&1

rem ---- Buscar Maven: PATH, luego instalaciones conocidas, luego descarga oficial
set "MVN="
for /f "delims=" %%i in ('where mvn.cmd 2^>nul') do if not defined MVN set "MVN=%%i"
if not defined MVN for /f "delims=" %%i in ('dir /s /b "C:\Program Files\NetBeans*\mvn.cmd" "C:\Program Files\JetBrains\mvn.cmd" "C:\Program Files\Apache*\mvn.cmd" 2^>nul') do if not defined MVN set "MVN=%%i"
set "MVN_DIR=%USERPROFILE%\.m2\apache-maven-3.9.11"
if not defined MVN if exist "%MVN_DIR%\bin\mvn.cmd" set "MVN=%MVN_DIR%\bin\mvn.cmd"
if not defined MVN (
  echo Descargando Maven 3.9.11 desde apache.org...
  echo Descargando Maven 3.9.11 >> "%LOG%"
  powershell -NoProfile -Command "$ProgressPreference='SilentlyContinue'; Invoke-WebRequest 'https://archive.apache.org/dist/maven/maven-3/3.9.11/binaries/apache-maven-3.9.11-bin.zip' -OutFile \"$env:TEMP\maven.zip\"; Expand-Archive \"$env:TEMP\maven.zip\" -DestinationPath \"$env:USERPROFILE\.m2\" -Force" >> "%LOG%" 2>&1
  if exist "%MVN_DIR%\bin\mvn.cmd" set "MVN=%MVN_DIR%\bin\mvn.cmd"
)
echo MVN=%MVN% >> "%LOG%"
if not defined MVN (echo No se pudo obtener Maven. & echo SIN_MAVEN >> "%LOG%" & pause & exit /b 1)
call "%MVN%" -version >> "%LOG%" 2>&1

set SKIP_BANCO=1
netstat -ano | findstr "LISTENING" | findstr ":3306" >nul 2>&1 && set SKIP_BANCO=0
echo SKIP_BANCO=%SKIP_BANCO% >> "%LOG%"

echo Compilando (la primera vez Maven descarga dependencias, puede tardar unos minutos)...
echo ---- BUILD TDEA >> "%LOG%"
pushd mcp-server-tdea
call "%MVN%" -B -ntp clean package >> "%LOG%" 2>&1
echo EXIT_TDEA=%errorlevel% >> "%LOG%"
popd
echo ---- BUILD BANCO >> "%LOG%"
pushd mcp-server-banco
call "%MVN%" -B -ntp clean package >> "%LOG%" 2>&1
echo EXIT_BANCO=%errorlevel% >> "%LOG%"
popd

if "%SKIP_BANCO%"=="0" set /p "DB_PASSWORD=Clave de MySQL para root (Enter si no tiene): "
echo Probando los servidores MCP...
node test_mcp.mjs >nul 2>&1
echo ==== FIN >> "%LOG%"
echo Listo. Ya puedes volver al chat.
pause
