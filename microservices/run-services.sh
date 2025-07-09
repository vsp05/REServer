#!/bin/bash

echo "Building all microservices..."
mvn clean package

echo "Starting Property Server on port 7071..."
cd property-server
java -jar target/property-server-1.0-SNAPSHOT-jar-with-dependencies.jar &
PROPERTY_PID=$!
cd ..

echo "Starting Analytics Server on port 7072..."
cd analytics-server
java -jar target/analytics-server-1.0-SNAPSHOT-jar-with-dependencies.jar &
ANALYTICS_PID=$!
cd ..

echo "Starting API Gateway on port 7070..."
cd api-gateway
java -jar target/api-gateway-1.0-SNAPSHOT-jar-with-dependencies.jar &
GATEWAY_PID=$!
cd ..

echo "All services started!"
echo "API Gateway: http://localhost:7070"
echo "Property Server: http://localhost:7071"
echo "Analytics Server: http://localhost:7072"
echo ""
echo "Press Ctrl+C to stop all services"

# Wait for interrupt signal
trap "echo 'Stopping all services...'; kill $PROPERTY_PID $ANALYTICS_PID $GATEWAY_PID; exit" INT

# Keep script running
wait 