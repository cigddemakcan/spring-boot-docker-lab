# Lab 03 — Docker Networking

## Overview

This lab demonstrates how to connect a Spring Boot application to a PostgreSQL database using a user-defined Docker network.

Unlike Lab 02, where Spring Boot ran locally and PostgreSQL ran inside Docker, **both applications run in separate Docker containers** in this lab.

## Technologies

- Java & Spring Boot
- Spring Data JPA / Hibernate
- PostgreSQL 16
- Docker & Docker Networking
- Maven
- Postman

## Architecture

```text
             Postman
                |
        localhost:8080
                |
       Spring Boot Container
         lab03-api-container
                |
          lab03-network
                |
       PostgreSQL Container
          lab03-postgres
                |
       lab03-postgres-data
          (Docker Volume)
```

Both containers are connected to the same user-defined Docker network. Spring Boot communicates with PostgreSQL using the PostgreSQL container name instead of `localhost`.

## Step 1 — Create a Docker Network

```bash
docker network create lab03-network
```

Verify the network:

```bash
docker network ls
```

## Step 2 — Run PostgreSQL

```bash
docker run -d \
  --name lab03-postgres \
  --network lab03-network \
  -e POSTGRES_USER=labuser \
  -e POSTGRES_PASSWORD=labpass123 \
  -e POSTGRES_DB=labdb \
  -v lab03-postgres-data:/var/lib/postgresql/data \
  postgres:16
```

This command creates a PostgreSQL container connected to `lab03-network`.

A named Docker volume stores database data independently of the container lifecycle.

The PostgreSQL port is not published to the host because Spring Boot connects to it through the Docker network.

> **Note:** The credentials are for local development and learning purposes only.

## Step 3 — Configure Spring Boot

In `application.properties`:

```properties
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/labdb}
spring.datasource.username=labuser
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

The `DB_URL` environment variable allows Spring Boot to use different database addresses depending on its execution environment.

When running inside Docker, the database URL is:

```text
jdbc:postgresql://lab03-postgres:5432/labdb
```

Here:

- `lab03-postgres` is the PostgreSQL container name.
- `5432` is the PostgreSQL server port.
- `labdb` is the database name.

## Step 4 — Create a Docker Image for Spring Boot

The `Dockerfile` uses a Java runtime image and starts the packaged Spring Boot application.

```dockerfile
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Ensure the Java runtime version is compatible with the version used to compile the application.

Package the application:

```bash
./mvnw clean package -DskipTests
```

Tests are skipped for this lab's packaging step because the default test context requires a database connection. This does not mean the tests have passed.

Build the Docker image:

```bash
docker build -t lab03-api .
```

## Step 5 — Run Spring Boot in Docker

```bash
docker run -d \
  --name lab03-api-container \
  --network lab03-network \
  -p 8080:8080 \
  -e DB_URL=jdbc:postgresql://lab03-postgres:5432/labdb \
  -e DB_PASSWORD=labpass123 \
  lab03-api
```

Important options:

| Option | Purpose |
|---|---|
| `--name` | Assigns a name to the container |
| `--network` | Connects the container to the Docker network |
| `-p 8080:8080` | Publishes the Spring Boot API port to the host |
| `-e DB_URL=...` | Provides the PostgreSQL connection URL |
| `-e DB_PASSWORD=...` | Provides the database password |

## Step 6 — Verify the Connection

List running containers:

```bash
docker ps
```

Check Spring Boot logs:

```bash
docker logs lab03-api-container
```

The application logs confirmed that Spring Boot successfully connected to PostgreSQL and initialized the database table.

Test the API using Postman:

```http
GET http://localhost:8080/products
```

Expected response when the database is empty:

```json
[]
```

This confirms that the request successfully travels from Postman to Spring Boot and that Spring Boot can retrieve data from PostgreSQL.

## Key Learnings

### 1. Each container has its own localhost

Inside a container, `localhost` refers to that container itself, not another container or the host computer.

### 2. Containers can communicate through Docker networks

Containers attached to the same user-defined Docker network can communicate using container names.

### 3. Host ports and container ports are different concepts

Publishing `8080:8080` allows Postman on the host to access Spring Boot.

PostgreSQL does not require a published host port because Spring Boot accesses it over the Docker network.

### 4. Environment variables enable flexible configuration

The same Spring Boot application can use different database URLs without modifying its source code.

### 5. Docker volumes persist database data

The named volume preserves PostgreSQL data independently of its container.

## Result

Successfully containerized the Spring Boot application and connected it to a separate PostgreSQL container through a user-defined Docker network.

Verified connectivity through application logs and a successful REST API request.

**Next: Lab 04 — Docker Compose**