@echo off
title Pharmacy Management System - Zero Config Launcher
color 0B
echo =================================================================
echo        PHARMACY MANAGEMENT SYSTEM - AUTO-CONFIG LAUNCHER
echo =================================================================
echo.

:: 1. SEARCH FOR JAVA / JDK
java -version >nul 2>&1
if %errorlevel% equ 0 (
    echo [STATUS] Found global Java installation.
    goto :java_found
)

echo [STATUS] Global Java command not found in PATH. Scanning system for JRE fallbacks...

:: Try to scan VS Code's Red Hat Java Extension JRE path
for /d %%d in ("%USERPROFILE%\.vscode\extensions\redhat.java-*") do (
    for /d %%j in ("%%d\jre\*") do (
        if exist "%%j\bin\java.exe" (
            set "JAVA_HOME=%%j"
            set "PATH=%%j\bin;%PATH%"
            echo [STATUS] Successfully located VS Code embedded JRE: %%j
            goto :java_found
        )
    )
)

:: Try standard Program Files paths
if exist "C:\Program Files\Java" (
    for /d %%j in ("C:\Program Files\Java\jdk-*") do (
        if exist "%%j\bin\java.exe" (
            set "JAVA_HOME=%%j"
            set "PATH=%%j\bin;%PATH%"
            echo [STATUS] Successfully located JDK in Program Files: %%j
            goto :java_found
        )
    )
)

:: If all scan fallbacks failed
color 0C
echo [ERROR] No Java Development Kit (JDK) found on your machine!
echo.
pause
exit /b 1

:java_found
echo.

:: 2. SEARCH OR BOOTSTRAP PORTABLE MAVEN
call mvn -version >nul 2>&1
if %errorlevel% equ 0 (
    echo [STATUS] Found global Maven installation.
    goto :maven_found
)

:: Check if local portable Maven already exists in project folder
if exist "%~dp0.maven\apache-maven-3.9.6\bin\mvn.cmd" (
    set "PATH=%~dp0.maven\apache-maven-3.9.6\bin;%PATH%"
    echo [STATUS] Found local portable Maven.
    goto :maven_found
)

echo [STATUS] Maven is not installed globally or locally.
echo [STATUS] Downloading a portable Maven distribution automatically...
echo.

:: Download portable Maven zip using PowerShell
powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip' -OutFile '%~dp0maven.zip'"

if %errorlevel% neq 0 (
    color 0C
    echo [ERROR] Failed to download Maven. Please verify your internet connection.
    pause
    exit /b 1
)

echo [STATUS] Extracting Maven packages...
powershell -Command "Expand-Archive -Path '%~dp0maven.zip' -DestinationPath '%~dp0.maven' -Force"
del "%~dp0maven.zip"

set "PATH=%~dp0.maven\apache-maven-3.9.6\bin;%PATH%"
echo [STATUS] Portable Maven is successfully configured!
echo.

:maven_found

:: 3. RUN THE APPLICATION
echo [STATUS] Compiling and starting JavaFX application using Maven...
echo [STATUS] Output is also being logged to build_output.log
echo.

:: Run Maven and tee output to a log file
cd /d "%~dp0"
call mvn -U clean compile org.openjfx:javafx-maven-plugin:0.0.8:run 2>&1 | powershell -Command "$input | Tee-Object -FilePath '%~dp0build_output.log'"

echo.
echo =================================================================
if %errorlevel% neq 0 (
    color 0C
    echo [ERROR] Application failed. Check build_output.log for details.
) else (
    echo [INFO] Application closed normally.
)
echo =================================================================
echo.
echo Press any key to close this window...
pause >nul
