@echo off
REM ============================================================
REM Distributed Order Management System — Startup Script
REM ============================================================
echo.
echo  ╔══════════════════════════════════════════════════════╗
echo  ║   Distributed Order Management System               ║
echo  ║   Starting all services with Docker Compose...      ║
echo  ╚══════════════════════════════════════════════════════╝
echo.

cd /d "%~dp0.."

echo [1/3] Building Maven modules...
call mvn clean package -DskipTests -B
if errorlevel 1 (
    echo ERROR: Maven build failed!
    pause
    exit /b 1
)

echo.
echo [2/3] Building Docker images...
docker-compose build
if errorlevel 1 (
    echo ERROR: Docker build failed!
    pause
    exit /b 1
)

echo.
echo [3/3] Starting containers...
docker-compose up -d

echo.
echo  ╔══════════════════════════════════════════════════════╗
echo  ║   All services are starting up!                     ║
echo  ║                                                     ║
echo  ║   API Gateway:      http://localhost:8080            ║
echo  ║   Product Service:  http://localhost:8081            ║
echo  ║   Cart Service:     http://localhost:8082            ║
echo  ║   Order Service:    http://localhost:8083            ║
echo  ║   Inventory Service:http://localhost:8084            ║
echo  ║                                                     ║
echo  ║   Health Check:     http://localhost:8080/health     ║
echo  ║                                                     ║
echo  ║   Wait ~60s for services to be fully ready.         ║
echo  ╚══════════════════════════════════════════════════════╝
echo.

echo Waiting for services to be healthy...
timeout /t 60 /nobreak > nul

echo.
echo Checking health status...
curl -s http://localhost:8080/health
echo.
echo.
echo Done! Run 'docker-compose logs -f' to view logs.
pause
