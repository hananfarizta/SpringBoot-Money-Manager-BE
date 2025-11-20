# Money Manager Backend

A backend API for the Money Manager application, built using **Spring Boot 3**, **MySQL**, and **Docker**.

---

## 🚀 Features
- Authentication & Authorization (JWT)
- Income & Expense Management
- Email Notification (Brevo SMTP)
- Environment variable management using `.env`
- Fully dockerized (Spring Boot + MySQL)

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
SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/moneymanager
MYSQL_USERNAME=root
MYSQL_PASSWORD=yourpassword

SMTP_USERNAME=xxxx@smtp-brevo.com
SMTP_PASSWORD=your-smtp-password
SMTP_MAIL_FROM=your-email@gmail.com

JWT_SECRET=your-jwt-secret-key
FRONTEND_URL=http://localhost:5173
```

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
mysql_db          Up   3306/tcp
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

## 🛢 6. Connect to MySQL via DBeaver / TablePlus
Use the following configuration:

| Field     | Value                               |
|-----------|--------------------------------------|
| Host      | 127.0.0.1                            |
| Port      | 3307                                 |
| Username  | root                                 |
| Password  | (value of `MYSQL_PASSWORD` in .env)  |
| Database  | moneymanager                         |
| SSL       | Disabled                             |

---

## 📚 7. Docker Compose Structure
```bash
services:
  app:
    build: .
    container_name: moneymanager
    ports:
      - "8080:8080"
    depends_on:
      - db
    env_file:
      - .env
    environment:
      SPRING_DATASOURCE_URL: ${SPRING_DATASOURCE_URL}
      SPRING_DATASOURCE_USERNAME: ${MYSQL_USERNAME}
      SPRING_DATASOURCE_PASSWORD: ${MYSQL_PASSWORD}
      SMTP_USERNAME: ${SMTP_USERNAME}
      SMTP_PASSWORD: ${SMTP_PASSWORD}
      SMTP_MAIL_FROM: ${SMTP_MAIL_FROM}
      JWT_SECRET: ${JWT_SECRET}
      FRONTEND_URL: ${FRONTEND_URL}

  db:
    image: mysql:8.3
    container_name: mysql_db
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_PASSWORD}
      MYSQL_DATABASE: moneymanager
    ports:
      - "3307:3306"
    volumes:
      - mysqldata:/var/lib/mysql

volumes:
  mysqldata:
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