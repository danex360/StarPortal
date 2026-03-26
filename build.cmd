@echo off
setlocal enabledelayedexpansion

set "MAVEN_VERSION=3.9.6"
set "MAVEN_DIR=%~dp0.maven"
set "MAVEN_ZIP=%~dp0maven.zip"
set "MAVEN_URL=https://archive.apache.org/dist/maven/maven-3/!MAVEN_VERSION!/binaries/apache-maven-!MAVEN_VERSION!-bin.zip"

echo [1/4] Checking for Portable Maven...
if not exist "!MAVEN_DIR!" (
    echo [2/4] Downloading Maven !MAVEN_VERSION!...
    powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri !MAVEN_URL! -OutFile !MAVEN_ZIP!"
    
    echo [3/4] Extracting Maven...
    powershell -Command "Expand-Archive -Path !MAVEN_ZIP! -DestinationPath !MAVEN_DIR!_tmp"
    move "!MAVEN_DIR!_tmp\apache-maven-!MAVEN_VERSION!" "!MAVEN_DIR!"
    rd /s /q "!MAVEN_DIR!_tmp"
    del "!MAVEN_ZIP!"
)

echo [4/4] Building StarPortal...
"!MAVEN_DIR!\bin\mvn.cmd" -B clean package -DskipTests

if %ERRORLEVEL% equ 0 (
    echo.
    echo ========================================
    echo   BUILD SUCCESSFUL!
    echo   JAR: target\StarPortal-1.0-SNAPSHOT.jar
    echo ========================================
) else (
    echo.
    echo !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    echo   BUILD FAILED! Check the errors above.
    echo !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
)

endlocal
