#!/bin/sh

echo "Waiting for Debezium..."

while ! curl -s http://bank-debezium:8083/connectors > /dev/null; do
  echo "Waiting for Debezium API..."
  sleep 2
done

echo "Debezium is ready!"

curl -X POST http://bank-debezium:8083/connectors \
  -H "Content-Type: application/json" \
  -d @/transfer-outbox-connector.json

echo "Connector created!"