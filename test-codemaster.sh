#!/bin/bash

echo "Testing Code Master System..."

# Start the application in background
echo "Starting application..."
./gradlew :main:bootRun --no-daemon &
APP_PID=$!

# Wait for application to start
echo "Waiting for application to start..."
sleep 30

# Test the code master endpoint
echo "Testing code master endpoint..."
curl -s "http://localhost:8080/api/code-master/code-name/ENTITY_TYPE" | jq '.' || echo "Failed to get response"

# Test another endpoint
echo "Testing payment status codes..."
curl -s "http://localhost:8080/api/code-master/code-name/PAYMENT_STATUS" | jq '.' || echo "Failed to get response"

# Stop the application
echo "Stopping application..."
kill $APP_PID

echo "Test completed!"
