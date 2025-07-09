#!/bin/bash

echo "Testing Microservices..."
echo "========================"

# Test API Gateway
echo "1. Testing API Gateway endpoints..."

echo "   - Testing GET /sales (should return all properties)"
curl -s http://localhost:7070/sales | head -c 100
echo ""

echo "   - Testing GET /sales/1 (should return property with ID 1)"
curl -s http://localhost:7070/sales/1 | head -c 100
echo ""

echo "   - Testing GET /sales/1/accessed-count (should return access count)"
curl -s http://localhost:7070/sales/1/accessed-count
echo ""

# Test Property Server directly
echo ""
echo "2. Testing Property Server endpoints..."

echo "   - Testing GET /properties (should return all properties)"
curl -s http://localhost:7071/properties | head -c 100
echo ""

echo "   - Testing GET /properties/1 (should return property with ID 1)"
curl -s http://localhost:7071/properties/1 | head -c 100
echo ""

# Test Analytics Server directly
echo ""
echo "3. Testing Analytics Server endpoints..."

echo "   - Testing GET /analytics/property/1/count (should return access count)"
curl -s http://localhost:7072/analytics/property/1/count
echo ""

echo "   - Testing POST /analytics/property/1/increment (should increment count)"
curl -s -X POST http://localhost:7072/analytics/property/1/increment
echo ""

echo "   - Testing GET /analytics/property/1/count (should show incremented count)"
curl -s http://localhost:7072/analytics/property/1/count
echo ""

echo ""
echo "Test completed!" 