@echo off
REM Money Manager Backend - Windows Setup Script

echo.
echo ==========================================
echo Money Manager Backend - Windows Setup
echo ==========================================
echo.

REM Check if Java is installed
java -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo Error: Java is not installed or not in PATH
    echo Please install Java 17 or higher
    pause
    exit /b 1
)

REM Check if Maven is installed
mvn -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo Error: Maven is not installed or not in PATH
    echo Please install Maven 3.6 or higher
    pause
    exit /b 1
)

echo.
echo Checking prerequisites...
java -version
mvn -version
echo.

REM Create .env file if it doesn't exist
if not exist .env (
    echo Creating .env file from template...
    copy .env.example .env
    echo .env file created. Please review and update if needed.
    echo.
)

REM Create logs directory
if not exist logs (
    echo Creating logs directory...
    mkdir logs
)

REM Build the application
echo Building the application...
call mvn clean install

echo.
echo ==========================================
echo Setup Complete!
echo ==========================================
echo.
echo To start the application, run one of:
echo   mvn spring-boot:run
echo   java -jar target/money-manager-backend-1.0.0.jar
echo.
echo Application URL: http://localhost:8080
echo.
pause
