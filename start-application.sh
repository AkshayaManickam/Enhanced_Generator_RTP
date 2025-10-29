#!/bin/bash

echo "========================================"
echo "Galaxy RTP Validator - Starting..."
echo "========================================"
echo ""

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "ERROR: Java is not installed or not in PATH"
    echo "Please install Java 17 or higher"
    exit 1
fi

# Check if Node is installed
if ! command -v node &> /dev/null; then
    echo "ERROR: Node.js is not installed or not in PATH"
    echo "Please install Node.js 20.x or higher"
    exit 1
fi

echo "[1/4] Checking Java version..."
java -version
echo ""

echo "[2/4] Starting Backend (Spring Boot)..."
cd galaxy-rtp-validator-service || exit
gnome-terminal -- bash -c "mvn spring-boot:run; exec bash" 2>/dev/null || \
xterm -e "mvn spring-boot:run" 2>/dev/null || \
osascript -e 'tell app "Terminal" to do script "cd '$(pwd)' && mvn spring-boot:run"' 2>/dev/null &
echo "Backend starting on http://localhost:8080"
echo ""

echo "Waiting for backend to initialize (10 seconds)..."
sleep 10

echo "[3/4] Installing Frontend dependencies (if needed)..."
cd ../galaxy-rtp-validator-ui || exit
if [ ! -d "node_modules" ]; then
    echo "Installing npm packages..."
    npm install
fi
echo ""

echo "[4/4] Starting Frontend (Angular)..."
gnome-terminal -- bash -c "npm start; exec bash" 2>/dev/null || \
xterm -e "npm start" 2>/dev/null || \
osascript -e 'tell app "Terminal" to do script "cd '$(pwd)' && npm start"' 2>/dev/null &
echo "Frontend starting on http://localhost:4200"
echo ""

echo "========================================"
echo "Application is starting!"
echo "========================================"
echo ""
echo "Backend:  http://localhost:8080"
echo "Frontend: http://localhost:4200"
echo ""
echo "The browser will open automatically in a few seconds..."
sleep 5

# Open browser
if command -v xdg-open &> /dev/null; then
    xdg-open http://localhost:4200
elif command -v open &> /dev/null; then
    open http://localhost:4200
fi

exit 0

