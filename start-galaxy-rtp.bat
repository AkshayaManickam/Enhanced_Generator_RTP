@echo off
echo Starting Galaxy RTP Validator...
echo.

echo Starting Backend Service...
cd galaxy-rtp-validator-service
start "Galaxy RTP Backend" cmd /k "mvn spring-boot:run"
cd ..

echo.
echo Starting Frontend Service...
cd galaxy-rtp-validator-ui
start "Galaxy RTP Frontend" cmd /k "npm start"
cd ..

echo.
echo Services are starting...
echo Backend will be available at: http://localhost:8080
echo Frontend will be available at: http://localhost:4200
echo.
echo Press any key to exit this script...
pause > nul
