#!/bin/bash

# Script to test if caching is working for the dashboard API
# This script makes API calls and measures response times to verify caching

echo "=========================================="
echo "Testing Dashboard API Caching"
echo "=========================================="
echo ""

# Configuration
BASE_URL="${BASE_URL:-http://localhost:8080}"
API_ENDPOINT="/api/v1/leads/dashboard"
AUTH_TOKEN="${AUTH_TOKEN:-}"  # Set your auth token here

if [ -z "$AUTH_TOKEN" ]; then
    echo "⚠️  Warning: AUTH_TOKEN not set. Some tests may fail."
    echo "   Set it with: export AUTH_TOKEN='your-token'"
    echo ""
fi

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Function to make API call and measure time
make_request() {
    local request_num=$1
    local start_time=$(date +%s%N)
    
    if [ -z "$AUTH_TOKEN" ]; then
        response=$(curl -s -w "\n%{http_code}" -X GET \
            "${BASE_URL}${API_ENDPOINT}?offset=0&limit=20" \
            -H "Content-Type: application/json" 2>&1)
    else
        response=$(curl -s -w "\n%{http_code}" -X GET \
            "${BASE_URL}${API_ENDPOINT}?offset=0&limit=20" \
            -H "Content-Type: application/json" \
            -H "Authorization: Bearer ${AUTH_TOKEN}" 2>&1)
    fi
    
    local end_time=$(date +%s%N)
    local duration=$(( (end_time - start_time) / 1000000 )) # Convert to milliseconds
    
    local http_code=$(echo "$response" | tail -n1)
    local body=$(echo "$response" | sed '$d')
    
    echo "$duration|$http_code|$body"
}

echo "Test 1: First Request (Cache Miss - Should be slower)"
echo "---------------------------------------------------"
result1=$(make_request 1)
time1=$(echo "$result1" | cut -d'|' -f1)
http_code1=$(echo "$result1" | cut -d'|' -f2)

if [ "$http_code1" = "200" ]; then
    echo -e "${GREEN}✓ Request 1 successful${NC} - Time: ${time1}ms"
else
    echo -e "${RED}✗ Request 1 failed${NC} - HTTP Code: $http_code1"
    echo "Response: $(echo "$result1" | cut -d'|' -f3)"
    exit 1
fi

echo ""
echo "Waiting 1 second..."
sleep 1

echo ""
echo "Test 2: Second Request (Cache Hit - Should be faster)"
echo "---------------------------------------------------"
result2=$(make_request 2)
time2=$(echo "$result2" | cut -d'|' -f1)
http_code2=$(echo "$result2" | cut -d'|' -f2)

if [ "$http_code2" = "200" ]; then
    echo -e "${GREEN}✓ Request 2 successful${NC} - Time: ${time2}ms"
else
    echo -e "${RED}✗ Request 2 failed${NC} - HTTP Code: $http_code2"
    exit 1
fi

echo ""
echo "Test 3: Third Request (Cache Hit - Should be faster)"
echo "---------------------------------------------------"
result3=$(make_request 3)
time3=$(echo "$result3" | cut -d'|' -f1)
http_code3=$(echo "$result3" | cut -d'|' -f3)

if [ "$http_code3" = "200" ]; then
    echo -e "${GREEN}✓ Request 3 successful${NC} - Time: ${time3}ms"
else
    echo -e "${RED}✗ Request 3 failed${NC} - HTTP Code: $http_code3"
    exit 1
fi

echo ""
echo "=========================================="
echo "Results Summary"
echo "=========================================="
echo "Request 1 (Cache Miss): ${time1}ms"
echo "Request 2 (Cache Hit):  ${time2}ms"
echo "Request 3 (Cache Hit):  ${time3}ms"
echo ""

# Calculate improvement
if [ "$time1" -gt 0 ] && [ "$time2" -gt 0 ]; then
    improvement=$(( ((time1 - time2) * 100) / time1 ))
    avg_cache_time=$(( (time2 + time3) / 2 ))
    
    echo "Average cache hit time: ${avg_cache_time}ms"
    echo "Performance improvement: ${improvement}%"
    echo ""
    
    if [ "$time2" -lt "$time1" ]; then
        echo -e "${GREEN}✓ Caching is working!${NC} Second request was faster."
        if [ "$improvement" -gt 10 ]; then
            echo -e "${GREEN}✓ Significant performance improvement detected${NC}"
        fi
    else
        echo -e "${YELLOW}⚠ Warning: Second request was not faster.${NC}"
        echo "This could mean:"
        echo "  - Cache is not configured properly"
        echo "  - Redis is not running"
        echo "  - Response times are too variable"
    fi
fi

echo ""
echo "=========================================="
echo "Cache Verification"
echo "=========================================="
echo "To verify cache in Redis, run:"
echo "  redis-cli"
echo "  KEYS leadDashboard:*"
echo "  GET <cache-key>"
echo ""

