# Микросервисное приложение «Банк» с использованием Spring Boot, интеграций Spring Cloud и паттернов микросервисной архитектуры.

## Задание
На основе существующего микросервисного приложения «Банк» девятого спринта реализуйте развёртывание микросервисов в Kubernetes с использованием Helm-чартов.

1. Jenkins
```bash
   java -Dhudson.plugins.git.GitSCM.ALLOW_LOCAL_CHECKOUT=true -jar jenkins.war
```
В Jenkins через админ-панель добавляем пароли DB_PASSWORD,ACCOUNT_PASSWORD,CASH_PASSWORD,TRANSFER_PASSWORD,NOTIFICATION_PASSWORD

2. Keycloak
```bash
   docker compose up -d bank-keycloak
   ./keycloak/create_users.sh
```   

3. Helm
```bash
cd ./helm/
helm dependency update
helm dependency build
helm dependency list
```

4. Проверка корректности
```bash
helm template my-app .
helm install my-app . --dry-run --debug
helm lint .
```

5. Установка
```bash
helm upgrade --install bank helm/ \
  --namespace test --create-namespace \
  --set kafka.enabled=true \
  --set account-db.enabled=true \
  --set cash-db.enabled=true \
  --set transfer-db.enabled=true \
  --set notification-db.enabled=true
```

6. Записи в hosts
```bash
sudo nano /etc/hosts
127.0.0.1 account.bank.local
127.0.0.1 cash.bank.local
127.0.0.1 transfer.bank.local
127.0.0.1 notification.bank.local
```

## Pipeline Jenkins
Интеграция в Jenkins (Jenkinsfile).

## Что выполнено
Смотри pull-реквесты.


