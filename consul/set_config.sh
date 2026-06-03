#!/bin/bash

CONSUL_URL="http://localhost:8500/v1/kv"

echo "Загрузка общих настроек в Consul..."
curl -X PUT -d "
GATEWAY_URL=http://gateway:9001
TRANSFER_SERVICE_URL=http://transfer-service:9003
CASH_SERVICE_URL=http://cash-service:9004
ACCOUNT_SERVICE_URL=http://account-service:9005
NOTIFICATION_SERVICE_URL=http://notification-service:9006
" http://localhost:8500/v1/kv/config/application/data

echo ""
echo "Загрузка настроек transfer-service в Consul..."
curl -X PUT -d "
spring.datasource.url=jdbc:postgresql://bank-transfer-db:5432/transfers
spring.datasource.username=postgres
spring.datasource.password=postgres
" http://localhost:8500/v1/kv/config/transfer-service/data

echo ""
echo "Загрузка настроек cash-service в Consul..."
curl -X PUT -d "
spring.datasource.url=jdbc:postgresql://bank-cash-db:5432/cashes
spring.datasource.username=postgres
spring.datasource.password=postgres
" http://localhost:8500/v1/kv/config/cash-service/data

echo ""
echo "Загрузка настроек account-service в Consul..."
curl -X PUT -d "
spring.datasource.url=jdbc:postgresql://bank-account-db:5432/accounts
spring.datasource.username=postgres
spring.datasource.password=postgres
" http://localhost:8500/v1/kv/config/account-service/data

echo ""
echo "Загрузка настроек notification-service в Consul..."
curl -X PUT -d "
spring.datasource.url=jdbc:postgresql://bank-notification-db:5432/notifications
spring.datasource.username=postgres
spring.datasource.password=postgres
" http://localhost:8500/v1/kv/config/notification-service/data

echo ""
echo "Настройки загружены."
