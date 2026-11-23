# Funky-Webshop

Funky-Webshop is a Java Spring Boot rebuild of **KeyScout24**, a webshop for game keys. 

## What the shop does

- **Accounts:** register and log in with a username and password.
- **Games:** browse game key offers, view details, and list your own keys for sale (name, platform,
  store, price, discount, key).
- **Cart and checkout:** add games to a cart. Checkout always fails, because the payment provider is "down".
- **Gift codes:** create a gift code with a personal message for a game you own. Another user redeems it on
  their profile page to get the key.
- **Support tickets:** open a ticket and chat with a keyword-based support bot. It can answer key
  questions, take reports of a seller, and export your chat history as JSON.
- **Government API:** `POST /govapi/report` returns an encrypted report on up to 5 products. It is built
  by a bundled C binary.

## Tech stack

| Part | Technology |
| --- | --- |
| Backend | Java 21, Spring Boot 3.5 (Web, Security, Data JPA) |
| Frontend | Thymeleaf templates, plain CSS |
| Database | PostgreSQL 18 (users, games, cart, keys, support tickets and messages) |
| Report generator | Prebuilt C binary (`src/main/resources/govapi/binary`), linked against libpq |
| Build and run | Maven, Docker, Docker Compose |

## Project structure

```text
dist/
├── docker-compose.yaml            web + PostgreSQL
└── web/
    ├── Dockerfile                 Maven build stage + Java 21 runtime, port 10088
    ├── pom.xml
    ├── developer-tools/           local PostgreSQL + dummy data for development
    └── src/main/
        ├── java/com/keyscout24/
        │   ├── KeyScoutApplication.java
        │   ├── config/            security, secrets, shared model attributes
        │   ├── routes/            home page, terms, privacy
        │   ├── auth/              login, register, logout, roles and permissions
        │   ├── games/             game list, details, add game, gift codes
        │   ├── cart/              cart and checkout
        │   ├── profile/           profile page and gift code redemption
        │   ├── support/           support tickets and the chat bot
        │   ├── govapi/            government report API
        │   ├── health/            /healthz
        │   ├── error/             403 / 404 / 500 pages
        │   ├── db/                JPA entities and repositories (PostgreSQL)
        │   └── util/              gift code encoding
        └── resources/
            ├── application.yml
            ├── govapi/binary
            ├── static/            css, svg
            └── templates/         Thymeleaf pages (auth, cart, games, profile, support, ...)
```

## Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) with Docker Compose
- For local development without Docker: JDK 21 and [Maven 3.9+](https://maven.apache.org/download.cgi)

## Run everything with Docker (recommended)

```bash
cd dist
docker compose up --build
```

- Shop: http://localhost:10088
- Health check: http://localhost:10088/healthz should return `{"status":"alive"}`

Stop with `Ctrl+C`, or run `docker compose down`. Add `-v` to also delete the database volumes.

## Local development (app in your IDE, database in Docker)

1. Start PostgreSQL:

   ```bash
   cd dist/web/developer-tools
   docker compose up -d
   ```

2. Run the app from `dist/web`. In PowerShell:

   ```powershell
   $env:DB_URL = "jdbc:postgresql://localhost:5432/vulndb"
   $env:KEYSCOUT_INSTANCE_PATH = "./instance"
   mvn spring-boot:run
   ```

   On Linux or macOS:

   ```bash
   DB_URL=jdbc:postgresql://localhost:5432/vulndb \
   KEYSCOUT_INSTANCE_PATH=./instance \
   mvn spring-boot:run
   ```

   In IntelliJ, set the same two variables in the run configuration for `KeyScoutApplication`.

3. Optional dummy data: once the app has created the tables, load `developer-tools/dummy_data.sql` into
   PostgreSQL.

## Configuration

All settings are in `src/main/resources/application.yml` and can be overridden with environment variables:

| Variable | Default (Docker network) |
| --- | --- |
| `DB_URL` | `jdbc:postgresql://template-service-db:5432/vulndb` |
| `DB_USER` / `DB_PASSWORD` | `dbuser` / `superSecret` |
| `KEYSCOUT_INSTANCE_PATH` | `/workdir/instance` (generated secrets, kept in a Docker volume) |
| `KEYSCOUT_GOVAPI_BINARY` | `/workdir/app/govapi/binary` |

On first start, the app writes random secrets to the instance folder: the gift code key and the support
bot's login. Delete that folder to get new ones.

## Troubleshooting

- **Port 10088 or 5432 already in use:** stop the other service, or change the port mapping in the
  compose file.


