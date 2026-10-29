@echo off
title FreshMart Supermarket Billing Management System
color 0A

echo =======================================================================
echo          FRESHMART SUPERMARKET BILLING MANAGEMENT SYSTEM
echo            Java Full Stack (JFS) Project Demonstration
echo =======================================================================
echo.
echo [1/3] Verifying Java Runtime...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Java is not detected in your PATH. Please install Java 17+ or 21.
    pause
    exit /b
)
echo       Java is ready!
echo.
echo [2/3] Starting Supermarket Billing Server (Spring Boot + Embedded Database)...
echo       Starting on http://localhost:8085
echo.

REM Automatically open browser after 5 seconds
start "" cmd /c "timeout /t 5 /nobreak >nul & start http://localhost:8085"

REM Check if pre-packaged JAR exists
if exist "target\supermarket-billing-system-1.0.0.jar" (
    echo [3/3] Running pre-built executable JAR...
    java -jar target\supermarket-billing-system-1.0.0.jar
    pause
    exit /b
)

REM Check if system Maven exists
where mvn >nul 2>&1
if %errorlevel% equ 0 (
    echo [3/3] Running via System Maven...
    call mvn spring-boot:run
    pause
    exit /b
)

REM Fallback to Maven Wrapper (no installation required)
if exist "mvnw.cmd" (
    echo [3/3] Running via Maven Wrapper (auto-downloading dependencies)...
    call mvnw.cmd spring-boot:run
    pause
    exit /b
)

echo [ERROR] Could not find Maven or Maven Wrapper to start the project.
pause
