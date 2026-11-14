# Lab 02 — PostgreSQL with Docker Volumes

## Overview

This lab demonstrates how to run PostgreSQL inside a Docker container, connect it to a Spring Boot REST API, and persist database data using Docker named volumes.

The main goal is to understand **why database data should be stored independently of a container** and verify that records remain available after a container is removed and recreated.

## Technologies

- Java
- Spring Boot
- Spring Data JPA / Hibernate
- PostgreSQL 16
- Docker
- Docker Volumes
- Maven
- Postman

## Architecture

```text
          Postman
             |
             | HTTP requests
             v
     Spring Boot REST API
       (localhost:8080)
             |
             | JDBC / JPA
             v
     PostgreSQL Container
       (localhost:5432)
             |
             | Volume mount
             v
     lab02-postgres-data
        (Named Volume)
```

Spring Boot runs locally in IntelliJ, while PostgreSQL runs inside Docker. A named volume stores PostgreSQL's database files independently of the container lifecycle.

## Project Structure

```text
02-postgresql-volume/
├── src/
│   └── main/
│       ├── java/
│       │   └── .../
│       │       ├── controller/
│       │       │   └── ProductController.java
│       │       ├── entity/
│       │       │   └── Product.java
│       │       └── repository/
│       │           └── ProductRepository.java
│       └── resources/
│           └── application.properties
├── pom.xml
├── .gitignore
└── README.md
```

## 1. Create a Docker Volume

Create a named volume to persist PostgreSQL data:

```bash
docker volume create lab02-postgres-data
```

List existing volumes:

```bash
docker volume ls
```

The volume exists independently of any individual container.

## 2. Run PostgreSQL in Docker

Use the official PostgreSQL image:

```bash
docker run -d \
  --name lab02-postgres \
  -e POSTGRES_USER=labuser \
  -e POSTGRES_PASSWORD=labpass123 \
  -e POSTGRES_DB=labdb \
  -p 5432:5432 \
  -v lab02-postgres-data:/var/lib/postgresql/data \
  postgres:16
```

The PostgreSQL image is already available on Docker Hub, so no custom Dockerfile is required.

If the image is not installed locally, `docker run` automatically downloads it.

### Key Docker Options

| Option | Purpose |
|---|---|
| `-d` | Runs the container in the background |
| `--name` | Assigns a name to the container |
| `-e` | Sets PostgreSQL initialization environment variables |
| `-p 5432:5432` | Makes PostgreSQL accessible from the host |
| `-v` | Mounts the named volume into PostgreSQL's data directory |
| `postgres:16` | Selects the PostgreSQL 16 image |

Check container status and logs:

```bash
docker ps
docker logs lab02-postgres
```

The PostgreSQL logs should eventually indicate that the database system is ready to accept connections.

## 3. Configure Spring Boot

Add the following properties to `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/labdb
spring.datasource.username=labuser
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

Set the environment variable in your IDE's run configuration:

```text
DB_PASSWORD=labpass123
```

Then start the Spring Boot application.

**Note:** The password shown here is only for local educational use. Do not commit real credentials or secrets to Git.

## 4. REST API Endpoints

The application contains a simple `Product` entity, a Spring Data JPA repository, and a REST controller.

| Method | Endpoint | Description |
|---|---|---|
| POST | `/products` | Create a product |
| GET | `/products` | Retrieve all products |

### Create a product

Send a POST request to:

```http
POST http://localhost:8080/products
Content-Type: application/json
```

Request body:

```json
{
  "name": "MacBook Air"
}
```

Example response:

```json
{
  "id": 1,
  "name": "MacBook Air"
}
```

### Retrieve products

Send a GET request to:

```http
GET http://localhost:8080/products
```

Example response:

```json
[
  {
    "id": 1,
    "name": "MacBook Air"
  }
]
```

The product is stored in PostgreSQL, not in the Spring Boot application's memory.

## 5. Verify Data Persistence

This is the main experiment of the lab.

### Step 1 — Stop the original container

```bash
docker stop lab02-postgres
```

### Step 2 — Remove the container

```bash
docker rm lab02-postgres
```

The container is removed, but the named volume remains available.

Verify it:

```bash
docker volume ls
```

### Step 3 — Create a new PostgreSQL container

```bash
docker run -d \
  --name lab02-postgres-new \
  -e POSTGRES_USER=labuser \
  -e POSTGRES_PASSWORD=labpass123 \
  -e POSTGRES_DB=labdb \
  -p 5432:5432 \
  -v lab02-postgres-data:/var/lib/postgresql/data \
  postgres:16
```

Notice that the **container name is different**, but the **volume name remains the same**.

### Step 4 — Verify the existing product

Once PostgreSQL is ready, send:

```http
GET http://localhost:8080/products
```

The previously saved product should still be returned.

**Result:** The original container was removed, but the database records remained available because both containers used the same Docker named volume.

## Key Concepts

| Concept | Description |
|---|---|
| Docker Hub image | A prebuilt image that can be downloaded and used directly |
| Container | An instance created from a Docker image |
| Named volume | Docker-managed persistent storage |
| Volume mount | Connects a volume to a path inside the container |
| Environment variable | Supplies configuration values to an application |
| Port publishing | Allows applications on the host to connect to container services |
| Data persistence | Keeps data available independently of a container's lifecycle |

## What I Learned

- How to use a prebuilt PostgreSQL image without writing a Dockerfile.
- How to run PostgreSQL inside a Docker container.
- How to connect a locally running Spring Boot application to a containerized database.
- How to create and mount a Docker named volume.
- Why stopping or deleting a container does not necessarily delete persistent data.
- How to recreate a database container using an existing volume.
- How to verify database persistence through REST API requests.

## Outcome

Successfully connected a Spring Boot REST API to PostgreSQL running in Docker and verified that database records remained available after deleting and recreating the PostgreSQL container using the same named volume.