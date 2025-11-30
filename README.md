# Money Manager Backend

A backend API for the Money Manager application, built using **Spring Boot 3**, **PostgreSQL**, **JWT**, and **Docker**, with CI/CD via GitHub Actions.

---

## 🚀 Features
- Authentication & Authorization (JWT)
- Income & Expense Management
- Email Notification (Brevo SMTP)
- Environment variable management using `.env`
- Fully dockerized (Spring Boot + PostgreSQL)
- Config Profiles: local (default), test (CI), prod
- Build: Maven 3.9+, JDK 21
- CI/CD: GitHub Actions, GHCR (optional)

---

## 📦 1. Prerequisites

Make sure you have installed:

- **Java 21**
- **Maven 3.9+**
- **Docker & Docker Compose**
- **Git**

---

## 📥 2. Clone the Repository

```bash
git clone https://github.com/username/moneymanager.git
cd moneymanager
```

---

## ⚙️ 3. Setup Environment Variables
```bash
SPRING_DATASOURCE_URL_LOCAL=
POSTGRES_USERNAME_LOCAL=
POSTGRES_PASSWORD_LOCAL=

SPRING_PROFILES_ACTIVE=

PROD_DB_HOST=
PROD_DB_PORT=
PROD_DB_NAME=
PROD_DB_USERNAME=
PROD_DB_PASSWORD=

SMTP_HOST=
SMTP_PORT=
SMTP_USERNAME=
SMTP_PASSWORD=
SMTP_MAIL_FROM=

JWT_SECRET=
FRONTEND_URL=

APP_ACTIVATION_URL=
```
Notes:

- Default profile is local. CI sets test automatically.

- For Docker Compose, application connects to `moneymanager-db` (container DNS) on port 5432.

Behavior:

- `spring.profiles.active: ${SPRING_PROFILES_ACTIVE:local}`

- Do NOT set `spring.profiles.active` inside `application-test.yml` (Spring Boot forbids it for profile-specific files).

---

## 🐳 4. Run with Docker
### 4.1 Build and Start the Containers
```bash
docker compose up -d --build
```

### 4.2 Check running containers:
```bash
docker ps
```

### 4.3 Expected output:

```bash
moneymanager      Up   8080/tcp
postgres          Up   5432/tcp
```

---

## 🌍 5. Accessing the API (Postman / Browser)
```bash
http://localhost:8080/api/v1.0/...
```

Examples:
- Login `[POST] http://localhost:8080/api/v1.0/auth/login`
- Get Profile `[GET] http://localhost:8080/api/v1.0/profile`

---

## 🛢 6. Connect to PostgreSQL via DBeaver / TablePlus
Use the following configuration:

| Field     | Value                               |
|-----------|--------------------------------------|
| Host      | 127.0.0.1                            |
| Port      | 5433                                 |
| Username  | (value of `POSTGRES_USERNAME_LOCAL`) |
| Password  | (value of `POSTGRES_PASSWORD_LOCAL`) |
| Database  | moneymanager                         |
| SSL       | Disabled                             |

---

## 📚 7. Docker Compose Structure
```bash
services:
  db:
    image: postgres:16-alpine
    container_name: moneymanager-db
    environment:
      POSTGRES_DB: moneymanager
      POSTGRES_USER: ${POSTGRES_USERNAME_LOCAL}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD_LOCAL}
    ports:
      - "5433:5432"
    volumes:
      - db_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres -d moneymanager"]
      interval: 5s
      timeout: 3s
      retries: 20

  app:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: moneymanager-app
    depends_on:
      db:
        condition: service_healthy
    environment:
      SPRING_PROFILES_ACTIVE: local
      SPRING_DATASOURCE_URL: ${SPRING_DATASOURCE_URL_LOCAL}
      SPRING_DATASOURCE_USERNAME: ${POSTGRES_USERNAME_LOCAL}
      SPRING_DATASOURCE_PASSWORD: ${POSTGRES_PASSWORD_LOCAL}

      SMTP_HOST: ${SMTP_HOST}
      SMTP_PORT: ${SMTP_PORT}
      SMTP_USERNAME: ${SMTP_USERNAME}
      SMTP_PASSWORD: ${SMTP_PASSWORD}
      SMTP_MAIL_FROM: ${SMTP_MAIL_FROM}

      JWT_SECRET: ${JWT_SECRET}
      FRONTEND_URL: ${FRONTEND_URL}
      APP_ACTIVATION_URL: ${APP_ACTIVATION_URL}
    ports:
      - "8080:8080"
    restart: unless-stopped

volumes:
  db_data:

```

## ⚠️ 8. Useful Commands
### 8.1 Stop containers

```bash
docker compose down
```

### 8.2 Stop & remove volumes (reset database)

```bash
docker compose down -v
```

### 8.3 Restart containers

```bash
docker compose restart
```

### 8.4 View application logs

```bash
docker logs moneymanager -f
```

## 🛠 9. Run Backend Without Docker
If you want to run it locally via IntelliJ / VS Code:

- Ensure local MySQL is running on port 3306

- Update the datasource:
```bash
spring.datasource.url=jdbc:mysql://localhost:3306/moneymanager
spring.datasource.username=root
spring.datasource.password=yourpassword
```

- Run
```bash
mvn spring-boot:run
```

- Testing

  - Run all tests:
    ```bash
    mvn clean test
    ```

  - Run build + tests:
    ```bash
    mvn clean verify
    ```

Notes:

- test profile uses H2 (in-memory) with ‎`ddl-auto=create-drop`.

- Jackson JavaTimeModule is registered in controller tests for LocalDateTime.

- Security tests use ‎`spring-security-test` (‎`@WithMockUser` for protected endpoints).

- Repository slice tests (‎`@DataJpaTest`) run on H2 in CI.