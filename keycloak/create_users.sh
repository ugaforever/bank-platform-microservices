ADMIN_TOKEN=$(curl -s -X POST http://localhost:9002/realms/master/protocol/openid-connect/token \
  -d "client_id=admin-cli" \
  -d "username=admin" \
  -d "password=admin" \
  -d "grant_type=password" | jq -r '.access_token')

curl -X POST http://localhost:9002/admin/realms/bank-realm/users \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "ivanov",
    "email": "ivanov@example.com",
    "enabled": true,
    "firstName": "Ivan",
    "lastName": "Ivanov",
    "credentials": [{
      "type": "password",
      "value": "ivanov",
      "temporary": false
    }]
  }'

curl -X POST http://localhost:9002/admin/realms/bank-realm/users \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "petrov",
    "email": "petrov@example.com",
    "enabled": true,
    "firstName": "Petr",
    "lastName": "Petrov",
    "credentials": [{
      "type": "password",
      "value": "petrov",
      "temporary": false
    }]
  }'