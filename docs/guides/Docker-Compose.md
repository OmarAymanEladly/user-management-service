## Prerequisites

Before running this project, make sure you have Docker and Docker Compose installed and running on your machine.

## Start

docker compose up -d

## Stop

docker compose down

## Reset database

docker compose down -v

## Keycloak

http://localhost:8081

Admin username: admin
Admin password: admin

## Credentials (Optional)
By default, the Docker Compose setup uses the following development credentials:

| Variable            | Default    |
| ------------------- | ---------- |
| `POSTGRES_DB`       | `keycloak` |
| `POSTGRES_USER`     | `keycloak` |
| `POSTGRES_PASSWORD` | `keycloak` |
| `KEYCLOAK_USER`     | `admin`    |
| `KEYCLOAK_PASSWORD` | `admin`    |

If you want to use different credentials, create a `.env` file in the same directory as `docker-compose.yml` and define any of the variables below:

```env
POSTGRES_DB=keycloak
POSTGRES_USER=keycloak
POSTGRES_PASSWORD=my-secure-password

KEYCLOAK_USER=admin
KEYCLOAK_PASSWORD=my-admin-password
```

Docker Compose will automatically load the `.env` file and use these values instead of the built-in defaults.

---