# Cache Testing Guide

This guide explains how to verify that caching is working correctly for the dashboard API.

## Prerequisites

1. **Redis must be running**
   ```bash
   # Check if Redis is running
   redis-cli ping
   # Should return: PONG
   ```

2. **Application must be running** with Redis configured
   - Check `application-dev.properties` or `application-prod.properties`
   - Verify `spring.cache.type=redis` is set

## Method 1: Manual API Testing

### Step 1: Make First Request (Cache Miss)
```bash
# First request - will hit database and populate cache
curl -X GET "http://localhost:8080/api/v1/leads/dashboard?offset=0&limit=20" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -w "\nTime: %{time_total}s\n"
```

**Expected**: Slower response time (e.g., 500-2000ms depending on data)

### Step 2: Make Second Request (Cache Hit)
```bash
# Second request with same parameters - should hit cache
curl -X GET "http://localhost:8080/api/v1/leads/dashboard?offset=0&limit=20" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -w "\nTime: %{time_total}s\n"
```

**Expected**: Much faster response time (e.g., 50-200ms)

### Step 3: Verify Cache in Redis
```bash
# Connect to Redis
redis-cli

# List all dashboard cache keys
KEYS leadDashboard:*

# Get a specific cache entry (replace with actual key from above)
GET "leadDashboard:..."

# Check cache statistics
INFO stats
```

## Method 2: Using the Test Script

```bash
# Set your auth token
export AUTH_TOKEN="your-jwt-token"
export BASE_URL="http://localhost:8080"

# Run the test script
./test-cache.sh
```

The script will:
1. Make 3 consecutive requests
2. Measure response times
3. Compare cache miss vs cache hit performance
4. Report if caching is working

## Method 3: Check Application Logs

Enable cache logging in `application-dev.properties`:
```properties
logging.level.org.springframework.cache=DEBUG
```

Then check logs for:
- `Cache miss` - First request
- `Cache hit` - Subsequent requests

## Method 4: Verify Cache Eviction

### Test Cache Eviction on Lead Update

1. **Get cached dashboard data**
   ```bash
   curl -X GET "http://localhost:8080/api/v1/leads/dashboard?offset=0&limit=20" \
     -H "Authorization: Bearer YOUR_TOKEN"
   ```

2. **Update a lead** (this should evict cache)
   ```bash
   curl -X PUT "http://localhost:8080/api/v1/leads/{leadId}" \
     -H "Authorization: Bearer YOUR_TOKEN" \
     -H "Content-Type: application/json" \
     -d '{"requestedAmount": 100000, "officeKey": "HQ", "owner": "user1"}'
   ```

3. **Check Redis** - cache should be cleared
   ```bash
   redis-cli
   KEYS leadDashboard:*
   # Should return empty or fewer keys
   ```

4. **Make dashboard request again** - should be slower (cache miss)

## Expected Cache Keys

The following cache keys should be created:

- `leadDashboard:*` - Dashboard query results
- `leadDashboardFilters:*` - Dashboard filter options
- `codeValues:*` - Code value lookups (priority, onHoldReason, etc.)
- `currentStaff:*` - Current user's staff info
- `staffByOfficeKeys:*` - Staff by office keys
- `offices:*` - Office by key
- `officesByCodePrefix:*` - Offices by code prefix

## Troubleshooting

### Cache Not Working?

1. **Check Redis connection**
   ```bash
   redis-cli ping
   ```

2. **Check application logs** for Redis connection errors

3. **Verify cache configuration**
   - Check `spring.cache.type=redis` in properties
   - Check `@EnableCaching` is present in `AppConfig`

4. **Check cache manager**
   - Verify `CacheManager` bean is created
   - Check for any cache-related errors in logs

### Cache Always Miss?

- Verify Redis is accessible from application
- Check network connectivity
- Verify Redis credentials if using authentication

### Cache Not Evicting?

- Check that `@CacheEvict` annotations are present on write methods
- Verify transactions are committing (cache eviction happens after commit)
- Check for any errors in application logs

## Performance Benchmarks

Expected performance improvements:

- **First request (cache miss)**: 500-2000ms
- **Cached request (cache hit)**: 50-200ms
- **Improvement**: 70-90% faster

Actual results depend on:
- Database query complexity
- Amount of data
- Network latency
- Redis performance

