@echo off
setlocal
set DIR=%~dp0
set WRAPPER_JAR=%DIR%\.mvn\wrapper\maven-wrapper.jar
if not exist "%WRAPPER_JAR%" (
  echo Downloading Maven Wrapper jar...
  powershell -Command "Invoke-WebRequest -Uri https://repo.maven.apache.org/maven2/io/takari/maven-wrapper/0.5.6/maven-wrapper-0.5.6.jar -OutFile \"%WRAPPER_JAR%\""
)
java -jar "%WRAPPER_JAR%" %*
endlocal
