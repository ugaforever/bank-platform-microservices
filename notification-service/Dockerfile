FROM maven:3.9-eclipse-temurin-21 AS build

COPY mirror-settings.xml /usr/share/maven/conf/settings.xml

WORKDIR /app

COPY pom.xml .
RUN mvn install -N

COPY chassis/pom.xml chassis/
COPY chassis/src chassis/src

WORKDIR /app/chassis
RUN mvn install -DskipTests

WORKDIR /app
COPY notification-service/pom.xml notification-service/
COPY notification-service/src notification-service/src

WORKDIR /app/notification-service
RUN mvn package -DskipTests

FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app
COPY --from=build /app/notification-service/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
