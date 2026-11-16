# Lab 04 — Docker Compose

## Overview

This lab demonstrates how to manage multiple Docker containers using **Docker Compose**.

Instead of creating containers, networks, and volumes manually with separate Docker commands, Docker Compose allows us to define and manage them in a single `compose.yaml` file.

The application consists of three services:

- **Spring Boot:** REST API for managing products.
- **PostgreSQL:** Relational database for storing products.
- **Redis:** In-memory data store.

> Redis runs as a separate service in this lab. It is not yet integrated into the Spring Boot application.

## Technologies

- Java & Spring Boot
- Spring Data JPA / Hibernate
- PostgreSQL 16
- Redis 7
- Docker & Docker Compose
- Maven
- Postman

## Architecture

```text
                   Postman
                      |
               localhost:8081
                      |
                Spring Boot
                   (api)
                      |
                Docker Network
                 /         \
                /           \
          PostgreSQL       Redis
           (postgres)      (redis)
                |
          Docker Volume
         (postgres-data)
```

Docker Compose automatically creates a network that allows services to communicate using their service names.

In this lab, Spring Boot connects to PostgreSQL using `postgres:5432`.

Redis is running independently and can be integrated into the application in a future exercise.

## Project Structure

```text
04-docker-compose/
├── .mvn/
├── src/
├── .gitignore
├── Dockerfile
├── compose.yaml
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
```

## Step 1 — Configure Docker Compose

Create a `compose.yaml` file in the project root:

```yaml
services:
  postgres:
    image: postgres:16
    environment:
      POSTGRES_USER: labuser
      POSTGRES_PASSWORD: labpass123
      POSTGRES_DB: labdb
    volumes:
      - postgres-data:/var/lib/postgresql/data

  api:
    build: .
    ports:
      - "8081:8080"
    environment:
      DB_URL: jdbc:postgresql://postgres:5432/labdb
      DB_PASSWORD: labpass123
    depends_on:
      - postgres

  redis:
    image: redis:7-alpine

volumes:
  postgres-data:
```

### Configuration Explained

| Setting | Purpose |
|---|---|
| `services` | Defines the containers managed by Compose |
| `image` | Specifies an existing Docker image |
| `build: .` | Builds the Spring Boot image using the local Dockerfile |
| `ports` | Maps a host port to a container port |
| `environment` | Provides configuration values to a container |
| `depends_on` | Controls service startup order |
| `volumes` | Defines persistent storage |

The `depends_on` option starts PostgreSQL before Spring Boot, but does not guarantee that PostgreSQL is fully ready to accept connections.

The database credentials are included only for local learning purposes and should not be used in production.

## Step 2 — Package Spring Boot

Create the application JAR:

```bash
./mvnw clean package -DskipTests
```

The `-DskipTests` option skips test execution during packaging. It does not indicate that the tests have passed.

The Dockerfile copies the packaged JAR into the Spring Boot image.

## Step 3 — Start the Services

Run:

```bash
docker compose up -d --build
```

This command:

1. Reads the `compose.yaml` configuration.
2. Builds the Spring Boot Docker image.
3. Creates the default Docker network.
4. Creates the PostgreSQL named volume if needed.
5. Creates and starts the configured containers.

The `-d` option runs the containers in detached mode.

The `--build` option builds the application image before starting the services.

## Step 4 — Verify Running Services

```bash
docker compose ps
```

The output should include three running services:

| Service | Image | Internal Port |
|---|---|---|
| `api` | Custom Spring Boot image | 8080 |
| `postgres` | `postgres:16` | 5432 |
| `redis` | `redis:7-alpine` | 6379 |

Only the Spring Boot API is published to the host at port `8081`.

PostgreSQL and Redis remain accessible to other services on the Compose network without publishing their ports to the host.

## Step 5 — Test the Spring Boot API

Using Postman:

```http
GET http://localhost:8081/products
```

Expected response for an empty database:

```json
[]
```

This verifies that the Spring Boot API is reachable from the host and can retrieve data from PostgreSQL.

## Step 6 — Test Redis

Execute a Redis command inside the running Redis container:

```bash
docker compose exec redis redis-cli ping
```

Expected response:

```text
PONG
```

This confirms that the Redis server is running and responding to commands.

It does not mean that Spring Boot is already using Redis for caching.

## Step 7 — Stop and Restart the Environment

Stop and remove the Compose-managed containers and network:

```bash
docker compose down
```

Named volumes are preserved by default.

Restart the services:

```bash
docker compose up -d
```

To stop services without removing their containers:

```bash
docker compose stop
```

To view application logs:

```bash
docker compose logs api
```

To list running services:

```bash
docker compose ps
```

> **Warning:** `docker compose down -v` also removes Compose-managed named volumes, potentially deleting PostgreSQL data.

## Key Learnings

### 1. Docker Compose simplifies multi-container applications

Multiple services can be configured in a single YAML file instead of using individual `docker run` commands.

### 2. Compose provides service discovery

Containers on the default Compose network can communicate using service names.

For example, Spring Boot connects to PostgreSQL through:

```text
jdbc:postgresql://postgres:5432/labdb
```

### 3. Each service runs in its own container

Spring Boot, PostgreSQL, and Redis remain independent applications even though Compose manages them together.

### 4. Volumes preserve database data

PostgreSQL stores its data in a named Docker volume that survives container removal and recreation.

### 5. Host ports are not required for internal communication

PostgreSQL and Redis do not need published host ports to communicate with other containers on the Compose network.

### 6. Redis integration is separate from running Redis

Starting a Redis container makes the Redis server available, but Spring Boot requires additional dependencies and configuration to actually use it.

## Result

Successfully created a multi-container environment using Docker Compose.

Verified that:

- Spring Boot and PostgreSQL run together.
- Spring Boot connects to PostgreSQL through the Compose network.
- The REST API responds successfully through Postman.
- Redis runs as a separate service and responds with `PONG`.
- All three services can be managed using Docker Compose commands.

**Next: Lab 05 — Docker Production Basics**