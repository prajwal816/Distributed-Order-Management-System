#!/bin/bash
# ============================================================
# Distributed Order Management System — Startup Script
# ============================================================

set -e

echo ""
echo "╔══════════════════════════════════════════════════════╗"
echo "║   Distributed Order Management System               ║"
echo "║   Starting all services with Docker Compose...      ║"
echo "╚══════════════════════════════════════════════════════╝"
echo ""

cd "$(dirname "$0")/.."

echo "[1/3] Building Maven modules..."
mvn clean package -DskipTests -B

echo ""
echo "[2/3] Building Docker images..."
docker-compose build

echo ""
echo "[3/3] Starting containers..."
docker-compose up -d

echo ""
echo "╔══════════════════════════════════════════════════════╗"
echo "║   All services are starting up!                     ║"
echo "║                                                     ║"
echo "║   API Gateway:      http://localhost:8080            ║"
echo "║   Product Service:  http://localhost:8081            ║"
echo "║   Cart Service:     http://localhost:8082            ║"
echo "║   Order Service:    http://localhost:8083            ║"
echo "║   Inventory Service:http://localhost:8084            ║"
echo "║                                                     ║"
echo "║   Health Check:     http://localhost:8080/health     ║"
echo "║                                                     ║"
echo "║   Wait ~60s for services to be fully ready.         ║"
echo "╚══════════════════════════════════════════════════════╝"
echo ""

echo "Waiting for services to be healthy..."
sleep 60

echo ""
echo "Checking health status..."
curl -s http://localhost:8080/health | python3 -m json.tool 2>/dev/null || curl -s http://localhost:8080/health
echo ""
echo "Done! Run 'docker-compose logs -f' to view logs."
