#!/bin/bash
# ============================================================
# Distributed Order Management System — Benchmark Script
# ============================================================

set -e

GATEWAY="http://localhost:8080"
TOTAL_REQUESTS=50
CONCURRENT=10

echo ""
echo "╔══════════════════════════════════════════════════════╗"
echo "║   API Performance Benchmark                          ║"
echo "║   Requests: $TOTAL_REQUESTS | Concurrency: $CONCURRENT              ║"
echo "╚══════════════════════════════════════════════════════╝"
echo ""

# Step 1: Login
echo "[1/5] Authenticating..."
TOKEN=$(curl -s -X POST $GATEWAY/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user1","password":"pass123"}' | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
echo "Token: ${TOKEN:0:20}..."
echo ""

# Step 2: Product listing benchmark
echo "[2/5] GET /api/products — $TOTAL_REQUESTS requests, $CONCURRENT concurrent"
echo "────────────────────────────────────────"
for i in $(seq 1 $TOTAL_REQUESTS); do
    curl -s -o /dev/null -w "%{time_total}" \
        -H "Authorization: Bearer $TOKEN" \
        $GATEWAY/api/products &
    if (( i % CONCURRENT == 0 )); then wait; fi
done
wait
echo ""

# Detailed single-request timing
echo ""
echo "Sample detailed timing (GET /api/products):"
curl -s -o /dev/null -w "  DNS:        %{time_namelookup}s\n  Connect:    %{time_connect}s\n  TLS:        %{time_appconnect}s\n  TTFB:       %{time_starttransfer}s\n  Total:      %{time_total}s\n  HTTP Code:  %{http_code}\n" \
    -H "Authorization: Bearer $TOKEN" \
    $GATEWAY/api/products

echo ""
echo "[3/5] GET /api/products/1 — single product"
for i in $(seq 1 10); do
    TIME=$(curl -s -o /dev/null -w "%{time_total}" \
        -H "Authorization: Bearer $TOKEN" \
        $GATEWAY/api/products/$((i % 20 + 1)))
    echo "  Request $i: ${TIME}s"
done

echo ""
echo "[4/5] GET /api/inventory/{id} — stock checks"
for i in $(seq 1 10); do
    TIME=$(curl -s -o /dev/null -w "%{time_total}" \
        -H "Authorization: Bearer $TOKEN" \
        $GATEWAY/api/inventory/$((i % 20 + 1)))
    echo "  Request $i: ${TIME}s"
done

echo ""
echo "[5/5] Full order flow — end-to-end"
START=$(date +%s%N)

# Add to cart
curl -s -o /dev/null -X POST $GATEWAY/api/cart/99/items \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"productId":5,"quantity":1}'

# Place order
curl -s -o /dev/null -X POST $GATEWAY/api/orders \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"userId":99,"shippingAddress":"456 Benchmark Ave","orderType":"EXPRESS"}'

END=$(date +%s%N)
ELAPSED=$(( (END - START) / 1000000 ))
echo "  Full order flow completed in ${ELAPSED}ms"

echo ""
echo "╔══════════════════════════════════════════════════════╗"
echo "║   Benchmark Complete!                                ║"
echo "║   Target SLA: < 200ms per request                    ║"
echo "╚══════════════════════════════════════════════════════╝"
