@REM Maven wrapper script
@echo off
setlocal
set MAVEN_PROJECTBASEDIR=%~dp0
echo [INFO] Starting Spring Boot Maven Runner for Fraud Detection System...
where mvn >nul 2>&1
if %ERRORLEVEL% equ 0 (
    mvn %*
) else (
    echo [WARNING] 'mvn' command not found in PATH.
    echo Please ensure Maven 3.8+ or 3.9+ is installed, or use standard Maven.
    exit /b 1
)
