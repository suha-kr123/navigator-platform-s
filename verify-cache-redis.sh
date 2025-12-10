#!/bin/bash

# Script to verify cache entries in Redis
# This helps verify that caching is working by checking Redis directly

echo "=========================================="
echo "Redis Cache Verification"
echo "=========================================="
echo ""

# Check if redis-cli is available
if ! command -v redis-cli &> /dev/null; then
    echo "❌ redis-cli not found. Please install Redis client tools."
    exit 1
fi

# Test Redis connection
echo "Testing Redis connection..."
if redis-cli ping &> /dev/null; then
    echo "✓ Redis is running"
else
    echo "❌ Cannot connect to Redis. Is Redis running?"
    echo "   Start Redis with: redis-server"
    exit 1
fi

echo ""
echo "=========================================="
echo "Dashboard Cache Keys"
echo "=========================================="

# Check for dashboard cache keys
dashboard_keys=$(redis-cli KEYS "leadDashboard:*" 2>/dev/null)
if [ -z "$dashboard_keys" ]; then
    echo "No dashboard cache keys found."
    echo "Make a dashboard API request first to populate cache."
else
    key_count=$(echo "$dashboard_keys" | wc -l)
    echo "Found $key_count dashboard cache key(s):"
    echo "$dashboard_keys" | head -5
    if [ "$key_count" -gt 5 ]; then
        echo "... and $((key_count - 5)) more"
    fi
fi

echo ""
echo "=========================================="
echo "Other Cache Keys"
echo "=========================================="

# Check for other cache types
echo "Code Values:"
code_value_keys=$(redis-cli KEYS "codeValues:*" 2>/dev/null | wc -l)
echo "  - codeValues: $code_value_keys key(s)"

echo "Staff:"
staff_keys=$(redis-cli KEYS "currentStaff:*" 2>/dev/null | wc -l)
staff_office_keys=$(redis-cli KEYS "staffByOfficeKeys:*" 2>/dev/null | wc -l)
echo "  - currentStaff: $staff_keys key(s)"
echo "  - staffByOfficeKeys: $staff_office_keys key(s)"

echo "Offices:"
office_keys=$(redis-cli KEYS "offices:*" 2>/dev/null | wc -l)
office_prefix_keys=$(redis-cli KEYS "officesByCodePrefix:*" 2>/dev/null | wc -l)
echo "  - offices: $office_keys key(s)"
echo "  - officesByCodePrefix: $office_prefix_keys key(s)"

echo "Dashboard Filters:"
filter_keys=$(redis-cli KEYS "leadDashboardFilters:*" 2>/dev/null | wc -l)
echo "  - leadDashboardFilters: $filter_keys key(s)"

echo ""
echo "=========================================="
echo "Cache Statistics"
echo "=========================================="

# Get Redis info
redis-cli INFO stats | grep -E "(keyspace_hits|keyspace_misses|total_keys)" | head -3

echo ""
echo "=========================================="
echo "Sample Cache Entry"
echo "=========================================="

# Try to get a sample cache entry
first_key=$(redis-cli KEYS "leadDashboard:*" 2>/dev/null | head -1)
if [ -n "$first_key" ]; then
    echo "Sample key: $first_key"
    echo "TTL (Time To Live): $(redis-cli TTL "$first_key" 2>/dev/null) seconds"
    echo ""
    echo "Cache value (first 200 chars):"
    redis-cli GET "$first_key" 2>/dev/null | head -c 200
    echo "..."
else
    echo "No cache entries found. Make API requests to populate cache."
fi

echo ""
echo ""
echo "=========================================="
echo "How to Test"
echo "=========================================="
echo "1. Make a dashboard API request:"
echo "   curl -X GET 'http://localhost:8080/api/v1/leads/dashboard?offset=0&limit=20' \\"
echo "     -H 'Authorization: Bearer YOUR_TOKEN'"
echo ""
echo "2. Run this script again to see cache entries"
echo ""
echo "3. Make the same request again - it should be faster (cache hit)"
echo ""

