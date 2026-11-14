# Lab 01 — Spring Boot Containerization

## Overview

This lab demonstrates how to containerize a simple Spring Boot REST API using Docker.

The goal is to understand the process of packaging a Java application, building a Docker image, and running the application inside a container.

## Technologies

- Java 17
- Spring Boot
- Maven
- Docker

## Project Structure

```text
01-spring-boot-containerization/
├── src/
├── pom.xml
├── Dockerfile
├── .dockerignore
└── README.md
```

## REST API

The application exposes a simple endpoint:

| Method | Endpoint | Response |
|---|---|---|
| GET | `/hello` | `Hello from Docker!` |

## Dockerfile

```dockerfile
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/docker-demo-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Explanation:**

- `FROM`: Uses a Java 17 runtime image.
- `WORKDIR`: Sets the working directory inside the image.
- `COPY`: Copies the packaged Spring Boot JAR into the image.
- `EXPOSE`: Documents the application's listening port.
- `ENTRYPOINT`: Defines the command executed when the container starts.

## Running the Application

### 1. Package the Spring Boot application

Using Maven:

```bash
mvn clean package
```

This generates the executable JAR file inside the `target/` directory.

### 2. Build the Docker image

```bash
docker build -t spring-boot-lab01 .
```

Verify that the image was created:

```bash
docker images
```

### 3. Run the container

```bash
docker run --name lab01-container -p 8080:8080 spring-boot-lab01
```

### 4. Test the REST API

Open the following URL:

http://localhost:8080/hello

Expected response:

```text
Hello from Docker!
```

### 5. Manage the container

List running containers:

```bash
docker ps
```

Stop the container:

```bash
docker stop lab01-container
```

Restart the existing container:

```bash
docker start lab01-container
```

View container logs:

```bash
docker logs lab01-container
```

## Key Concepts

| Concept | Description |
|---|---|
| JAR | Packaged Java application |
| Dockerfile | Instructions for building a Docker image |
| Image | Packaged application and required runtime environment |
| Container | An instance created from a Docker image |
| `docker build` | Builds an image from a Dockerfile |
| `docker run` | Creates and starts a new container |
| `docker start` | Starts an existing stopped container |
| `.dockerignore` | Excludes unnecessary files from the Docker build context |

## What I Learned

- How to package a Spring Boot application as an executable JAR.
- How to write and understand a Dockerfile.
- How to build a custom Docker image.
- The difference between Docker images and containers.
- How to run and manage Docker containers.
- How to verify a containerized REST API.

## Outcome

Successfully containerized a Spring Boot REST API and accessed its endpoint through a running Docker container.