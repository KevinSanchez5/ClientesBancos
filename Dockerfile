# Etapa de compilación
FROM gradle:jdk21-alpine AS build

RUN apk update && apk add bash
RUN apk add --no-cache docker openrc

COPY entrypoint.sh /usr/local/bin/entrypoint.sh
RUN chmod +x /usr/local/bin/entrypoint.sh

WORKDIR /app

COPY build.gradle.kts .
COPY gradlew .
COPY gradle gradle
COPY src src

RUN chmod +x ./gradlew

ARG DOCKER_HOST_ARG=tcp://host.docker.internal:2375
ENV DOCKER_HOST=$DOCKER_HOST_ARG

RUN ./gradlew build

# Etapa de ejecución
FROM eclipse-temurin:21-jre-alpine AS run

WORKDIR /app

COPY --from=build /app/build/libs/*SNAPSHOT.jar /app/my-app.jar

ENTRYPOINT ["java", "-jar", "/app/my-app.jar"]