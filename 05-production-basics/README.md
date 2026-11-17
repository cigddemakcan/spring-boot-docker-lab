# Lab 05 — Docker Multi-Stage Build

## Overview

This lab demonstrates how to build a Spring Boot application using a multi-stage Dockerfile.

Unlike previous labs, Maven runs inside the Docker build process, so the JAR does not need to be packaged manually before building the image.

## Technologies

- Java 17
- Spring Boot
- Maven
- Docker
- Docker Compose
- PostgreSQL 16
- Redis 7

## Multi-Stage Dockerfile

```dockerfile
# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

## How It Works

### Stage 1 — Build

The Maven image provides the tools needed to compile the Spring Boot application and package it into an executable JAR.

### Stage 2 — Runtime

A separate Java runtime image is used to run the packaged application.

Only the JAR is copied from the build stage into the final image. Maven and the build tools are not included in the final image.

## Build the Image

```bash
docker compose build api
```

Docker Compose reads the Dockerfile and builds the application image, including the Maven packaging step.

## Run the Application

```bash
docker compose up -d --build
```

This starts the Spring Boot, PostgreSQL, and Redis services defined in `compose.yaml`.

Verify the services:

```bash
docker compose ps
```

Test the API:

```http
GET http://localhost:8081/products
```

## Key Learnings

- A Dockerfile can contain multiple build stages.
- The build stage compiles and packages the Java application.
- The runtime stage contains only what is needed to run the application.
- `COPY --from=build` copies the JAR from the earlier stage.
- Multi-stage builds help separate build tools from the final runtime environment.
- Docker Compose can build the application image and manage its supporting services.

## Note

Tests are skipped during the Docker build for this learning exercise. In a real CI/CD pipeline, tests should be executed separately.

## Result

Created a multi-stage Dockerfile for a Spring Boot application, eliminating the need to package the JAR manually before building the Docker image.docker compose build api