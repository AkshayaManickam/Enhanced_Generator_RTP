@echo off
echo ========================================
echo Galaxy RTP Validator - Starting...
echo ========================================
echo.

REM Check if Java is installed
where java >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java 17 or higher
    pause
    exit /b 1
)

REM Check if Node is installed
where node >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Node.js is not installed or not in PATH
    echo Please install Node.js 20.x or higher
    pause
    exit /b 1
)

echo [1/4] Checking Java version...
java -version
echo.

echo [2/4] Starting Backend (Spring Boot)...
start "Galaxy RTP Validator - Backend" cmd /k "cd galaxy-rtp-validator-service && mvn spring-boot:run"
echo Backend starting on http://localhost:8080
echo.

echo Waiting for backend to initialize (10 seconds)...
timeout /t 10 /nobreak >nul

echo [3/4] Installing Frontend dependencies (if needed)...
cd galaxy-rtp-validator-ui
if not exist "node_modules" (
    echo Installing npm packages...
    call npm install
)
echo.

echo [4/4] Starting Frontend (Angular)...
start "Galaxy RTP Validator - Frontend" cmd /k "npm start"
echo Frontend starting on http://localhost:4200
echo.

echo ========================================
echo Application is starting!
echo ========================================
echo.
echo Backend:  http://localhost:8080
echo Frontend: http://localhost:4200
echo.
echo The browser will open automatically in a few seconds...
echo.
echo Press any key to open the application manually
echo or close this window if windows are already open.
pause

start http://localhost:4200

exit /b 0

