# Tour Planner API

Backend for the Tour Planner application. Built with Spring Boot and PostgreSQL, following a three-layer architecture (Controller / Service / Repository).

## Tech Stack

- Java 21, Spring Boot
- Spring Data JPA (Hibernate)
- PostgreSQL 17
- OpenRouteService API (route distance, time, geometry, elevation)
- JUnit 5 + H2 (in-memory) for tests

## Setup

1. Get a free API key from [OpenRouteService](https://account.heigit.org/manage/key).
2. Rename `.env-copy` to `.env` and paste the key into `TP_ORS_API_KEY`.

## Run with Docker

```powershell
docker compose up --build
```

This starts PostgreSQL on port `5433` and the API on port `8080`.

## Run Tests

```powershell
.\mvnw.cmd clean test
```

Tests run against an in-memory H2 database, so no Docker container is required.

## Configuration

All environment-specific settings (DB connection, ORS API key, image storage path) are read from environment variables in `application.properties`, not hardcoded in source code. See `docker-compose.yml` for the variables used in production.

## GitHub Repositories

- API: https://github.com/David-Velichka/Tour-Planner-API
- UI: https://github.com/David-Velichka/Tour-Planner-UI
