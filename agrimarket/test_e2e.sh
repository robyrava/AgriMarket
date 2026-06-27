#!/bin/bash

echo "Starting E2E Tests..."

# Wait for services to be ready
echo "Waiting for services to start (sleeping 40 seconds)..."
sleep 40

# 1. Create Product 1 (Apple)
echo "Creating Product 1..."
PROD1_RESP=$(curl -s -X POST http://localhost/api/v1/catalog/products \
  -H "Content-Type: application/json" \
  -d '{"nome": "Mela", "descrizione": "Mela rossa", "prezzo": 1.50}')
echo "Product 1 Response: $PROD1_RESP"
PROD1_ID=$(echo $PROD1_RESP | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)

# 2. Set Inventory for Product 1 (10 items)
echo "Setting Inventory for Product 1..."
curl -s -X POST http://localhost/api/v1/catalog/inventory \
  -H "Content-Type: application/json" \
  -d "{\"productId\": $PROD1_ID, \"quantitaDisponibile\": 10}" | jq . || echo "Failed to parse json"

# 3. Create Product 2 (Pear)
echo "Creating Product 2..."
PROD2_RESP=$(curl -s -X POST http://localhost/api/v1/catalog/products \
  -H "Content-Type: application/json" \
  -d '{"nome": "Pera", "descrizione": "Pera verde", "prezzo": 2.00}')
echo "Product 2 Response: $PROD2_RESP"
PROD2_ID=$(echo $PROD2_RESP | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)

# 4. Set Inventory for Product 2 (2 items)
echo "Setting Inventory for Product 2..."
curl -s -X POST http://localhost/api/v1/catalog/inventory \
  -H "Content-Type: application/json" \
  -d "{\"productId\": $PROD2_ID, \"quantitaDisponibile\": 2}" | jq . || echo "Failed to parse json"


# 5. Create valid Order for Product 1 (3 items)
echo "Creating valid Order for Product 1..."
ORDER1_RESP=$(curl -s -X POST http://localhost/api/v1/orders \
  -H "Content-Type: application/json" \
  -d "{\"customerId\": 100, \"tipoSpedizione\": \"STANDARD\", \"items\": [{\"productId\": $PROD1_ID, \"quantita\": 3, \"prezzo\": 1.50}]}")
echo "Order 1 Response: $ORDER1_RESP"
ORDER1_ID=$(echo $ORDER1_RESP | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)

# 6. Create invalid Order for Product 2 (5 items - out of stock)
echo "Creating invalid Order for Product 2..."
ORDER2_RESP=$(curl -s -X POST http://localhost/api/v1/orders \
  -H "Content-Type: application/json" \
  -d "{\"customerId\": 101, \"tipoSpedizione\": \"REFRIGERATED\", \"items\": [{\"productId\": $PROD2_ID, \"quantita\": 5, \"prezzo\": 2.00}]}")
echo "Order 2 Response: $ORDER2_RESP"
ORDER2_ID=$(echo $ORDER2_RESP | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)

# 7. Wait a few seconds for Saga Choreography
echo "Waiting 5 seconds for async processing..."
sleep 5

# 8. Get Order 1 status (should be CONFIRMED)
echo "Checking Order 1 Status (Expected: CONFIRMED)..."
curl -s http://localhost/api/v1/orders/$ORDER1_ID | jq . || echo "Failed to parse json"

# 9. Get Inventory for Product 1 (should be 7)
echo "Checking Product 1 Inventory (Expected: 7)..."
curl -s http://localhost/api/v1/catalog/inventory/product/$PROD1_ID | jq . || echo "Failed to parse json"

# 10. Get Order 2 status (should be CANCELLED)
echo "Checking Order 2 Status (Expected: CANCELLED)..."
curl -s http://localhost/api/v1/orders/$ORDER2_ID | jq . || echo "Failed to parse json"

echo "E2E Tests completed."
