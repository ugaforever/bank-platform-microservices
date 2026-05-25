FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn install -N

COPY chassis/pom.xml chassis/
COPY chassis/src chassis/src
WORKDIR /app/chassis
RUN mvn install -DskipTests

WORKDIR /app
COPY front-ui/pom.xml front-ui/
COPY front-ui/src front-ui/src
WORKDIR /app/front-ui
RUN mvn package -DskipTests

FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app
COPY --from=build /app/front-ui/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
