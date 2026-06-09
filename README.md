# Микросервисное приложение «Банк» с использованием Spring Boot, интеграций Spring Cloud и паттернов микросервисной архитектуры.

## Задание
На основе существующего микросервисного приложения «Банк» девятого спринта реализуйте развёртывание микросервисов в Kubernetes с использованием Helm-чартов.

1. Jenkins
```bash
   java -Dhudson.plugins.git.GitSCM.ALLOW_LOCAL_CHECKOUT=true -jar jenkins.war
```
2. Keycloak
```bash
   cd ./keycloak
   docker compose up -d
   ./create_users.sh
```   
3. Helm
```bash
cd ./helm/
helm dependency update
helm dependency build
helm dependency list

# Проверка корректности
helm template my-app .
helm install my-app . --dry-run --debug

# Установка со ВСЕМИ компонентами (именно bank, пример URL bank-account-service:9005)
helm upgrade --install bank helm/ \
  --namespace test --create-namespace \
  --set kafka.enabled=true \
  --set account-db.enabled=true \
  --set cash-db.enabled=true \
  --set transfer-db.enabled=true \
  --set notification-db.enabled=true
```

4. etc
```bash
sudo nano /etc/hosts
127.0.0.1 account.bank.local cash.bank.local transfer.bank.local notification.bank.local
```

## Запуск сервисов
```bash
docker compose up
```
## Что выполнено
Смотри pull-реквесты.


