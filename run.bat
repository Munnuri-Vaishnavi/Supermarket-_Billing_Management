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

REM Automatically open browser after 4 seconds
start "" cmd /c "timeout /t 4 /nobreak >nul & start http://localhost:8085"

REM Check if target jar exists, else run via maven
if exist "target\supermarket-billing-system-1.0.0.jar" (
    echo [3/3] Running packaged JAR...
    java -jar target\supermarket-billing-system-1.0.0.jar
) else (
    echo [3/3] Running via Maven plugin...
    call mvn spring-boot:run
)

pause
