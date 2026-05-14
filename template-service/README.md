# Template Application

`template-service` — это шаблонный микросервис, который реализует паттерн **Microservice Chassis** и служит основой для создания всех остальных сервисов в проекте.

Он содержит базовую структуру, конфигурации, зависимости и инфраструктурные файлы, которые используются во всех микросервисах.

---

## 🔧 Что входит в шаблон

- Базовая структура каталогов:
  ```
  template-service/
  ├── src/
  │   ├── main/
  │   │   ├── java/ru/ugaforever/bank/template/
  │   │   │   ├── controller/
  │   │   │   ├── dto/
  │   │   │   ├── exception/
  │   │   │   ├── mapper/
  │   │   │   ├── model/
  │   │   │   ├── repository/
  │   │   │   └── service/
  │   │   └── resources/
  │   │       └── application.yml
  ├── Dockerfile
  ├── docker-compose.yml
  ├── pom.xml
  ```

- Стандартные зависимости:
    - Spring Boot Web
    - Spring Boot Test

- Конфигурация подключения к базе данных через переменные окружения
- Dockerfile для сборки
- docker-compose.yml для локального запуска с PostgreSQL

---

## 🚀 Как использовать

1. Скопируйте директорию `template-service` в новый сервис:
   ```bash
   cp -r template-service my-new-service
   ```

2. Замените имя пакета `ru.ugaforever.bank.template` на новое имя пакета.
3. Переименуйте проект в `pom.xml` и `application.yml`.
4. Реализуйте бизнес-логику: контроллеры, модели, сервисы и мапперы.
5. Добавьте сервис в `docker-compose.yml` и `pom.xml` в корне проекта.

---

## 📦 Быстрый старт

```bash
# Собрать
.mvn clean package

# Запустить
docker compose up --build
```

---

Этот шаблон позволяет быстро создавать новые микросервисы в едином стиле, соблюдая архитектурные принципы проекта.
