@echo off
REM ============================================================
REM Distributed Order Management System — Benchmark Script
REM Simulates concurrent load and measures API performance.
REM ============================================================
echo.
echo  ╔══════════════════════════════════════════════════════╗
echo  ║   API Performance Benchmark                          ║
echo  ╚══════════════════════════════════════════════════════╝
echo.

set GATEWAY=http://localhost:8080

REM Step 1: Login to get token
echo [1/6] Authenticating...
for /f "tokens=*" %%i in ('curl -s -X POST %GATEWAY%/api/auth/login -H "Content-Type: application/json" -d "{\"username\":\"user1\",\"password\":\"pass123\"}"') do set LOGIN_RESP=%%i
echo Response: %LOGIN_RESP%
echo.

REM Step 2: Benchmark product listing
echo [2/6] Benchmarking GET /api/products (10 requests)...
echo.
for /L %%i in (1,1,10) do (
    curl -s -o nul -w "  Request %%i: %%{time_total}s (HTTP %%{http_code})" %GATEWAY%/api/products
    echo.
)
echo.

REM Step 3: Benchmark single product
echo [3/6] Benchmarking GET /api/products/1 (10 requests)...
echo.
for /L %%i in (1,1,10) do (
    curl -s -o nul -w "  Request %%i: %%{time_total}s (HTTP %%{http_code})" %GATEWAY%/api/products/1
    echo.
)
echo.

REM Step 4: Benchmark inventory
echo [4/6] Benchmarking GET /api/inventory/1 (10 requests)...
echo.
for /L %%i in (1,1,10) do (
    curl -s -o nul -w "  Request %%i: %%{time_total}s (HTTP %%{http_code})" %GATEWAY%/api/inventory/1
    echo.
)
echo.

REM Step 5: Benchmark health check
echo [5/6] Benchmarking GET /health (5 requests)...
echo.
for /L %%i in (1,1,5) do (
    curl -s -o nul -w "  Request %%i: %%{time_total}s (HTTP %%{http_code})" %GATEWAY%/health
    echo.
)
echo.

REM Step 6: Search products
echo [6/6] Benchmarking product search (5 requests)...
echo.
for /L %%i in (1,1,5) do (
    curl -s -o nul -w "  Request %%i: %%{time_total}s (HTTP %%{http_code})" "%GATEWAY%/api/products?searchType=category&category=ELECTRONICS"
    echo.
)
echo.

echo  ╔══════════════════════════════════════════════════════╗
echo  ║   Benchmark Complete!                                ║
echo  ║   Target: all responses under 200ms (0.200s)         ║
echo  ╚══════════════════════════════════════════════════════╝
pause
