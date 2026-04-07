@ECHO OFF
SETLOCAL ENABLEDELAYEDEXPANSION

SET "WRAPPER_DIR=%~dp0.mvn\wrapper"
SET "PROPS_FILE=%WRAPPER_DIR%\maven-wrapper.properties"

IF NOT EXIST "%PROPS_FILE%" (
  ECHO Could not find %PROPS_FILE%
  EXIT /B 1
)

FOR /F "tokens=1,* delims==" %%A IN (%PROPS_FILE%) DO (
  IF "%%A"=="distributionUrl" SET "DIST_URL=%%B"
)

IF "%DIST_URL%"=="" (
  ECHO distributionUrl is missing in maven-wrapper.properties
  EXIT /B 1
)

SET "DIST_FILE=%DIST_URL:~-24%"
IF NOT "%DIST_FILE:~-4%"==".zip" SET "DIST_FILE=apache-maven-bin.zip"

SET "WRAP_HOME=%USERPROFILE%\.m2\wrapper\dists\mycampus-backend"
SET "ZIP_PATH=%WRAP_HOME%\%DIST_FILE%"

IF NOT EXIST "%WRAP_HOME%" MKDIR "%WRAP_HOME%"

FOR /D %%D IN ("%WRAP_HOME%\apache-maven-*") DO (
  IF EXIST "%%~fD\bin\mvn.cmd" SET "MVN_HOME=%%~fD"
)

IF NOT DEFINED MVN_HOME (
  IF NOT EXIST "%ZIP_PATH%" (
    ECHO Downloading Maven from %DIST_URL%
    powershell -NoProfile -ExecutionPolicy Bypass -Command "[Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -UseBasicParsing -Uri '%DIST_URL%' -OutFile '%ZIP_PATH%'"
    IF ERRORLEVEL 1 (
      ECHO Failed to download Maven distribution.
      EXIT /B 1
    )
  )

  ECHO Extracting Maven...
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -Path '%ZIP_PATH%' -DestinationPath '%WRAP_HOME%' -Force"
  IF ERRORLEVEL 1 (
    ECHO Failed to extract Maven distribution.
    EXIT /B 1
  )

  FOR /D %%D IN ("%WRAP_HOME%\apache-maven-*") DO (
    IF EXIST "%%~fD\bin\mvn.cmd" SET "MVN_HOME=%%~fD"
  )
)

IF NOT DEFINED MVN_HOME (
  ECHO Maven home could not be resolved.
  EXIT /B 1
)

"%MVN_HOME%\bin\mvn.cmd" %*
EXIT /B %ERRORLEVEL%


