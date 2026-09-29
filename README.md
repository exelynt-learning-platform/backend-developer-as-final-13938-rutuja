# RESTful Resource Booking System

[![Java Version](https://img.shields.io/badge/Java-17%2B-blue.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x%20%2F%204.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Code Coverage](https://img.shields.io/badge/Code%20Coverage-%3E94%25-success.svg)](#code-coverage)

A production-ready RESTful Resource Booking System API built with **Spring Boot**, **Spring Security**, **JWT Authentication**, **Spring Data JPA**, and **MySQL/PostgreSQL/H2**.

The system enables users to view available resources (e.g., rooms, vehicles, equipment) and manage their own reservations, while administrators have full privilege to manage resources, reservations, and status transitions.

---

## 🚀 Features

- **JWT Authentication**: Stateless authentication via `POST /auth/login`.
- **Role-Based Access Control (RBAC)**:
  - `ADMIN`: Full CRUD permissions on resources and reservations, ability to change reservation statuses (`PENDING`, `CONFIRMED`, `CANCELLED`), view all reservations, update details, and delete resources/reservations.
  - `USER`: Read-only access to resources, create reservations (user identity strictly bound to JWT), view/update only their own reservations.
- **Reservation Ownership & Security**: Users can only access and modify their own reservations. Identity is extracted securely from the JWT.
- **Filtering, Pagination & Sorting**:
  - Filter reservations by `status`, `minPrice`, and `maxPrice`.
  - Pagination support via `page` and `size`.
  - Sorting support via `sortBy` and `sortDir` (`asc` / `desc`).
- **Validation & Exception Handling**:
  - Validates required fields, start/end times (`endTime` must be after `startTime`), resource availability, and price bounds (`BigDecimal`).
  - Global Exception Handler returns structured JSON error responses with proper HTTP status codes (`400`, `401`, `403`, `404`, `500`).
- **Automated Data Seeding**: Pre-configures seed users for immediate testing on startup.
- **Swagger / OpenAPI 3 Documentation**: Interactive API testing UI at `/swagger-ui.html` with JWT Bearer token support.
- **High Test Coverage**: Comprehensive unit and integration test suite achieving **>94% Code Coverage** using JaCoCo.

---

## 🛠️ Tech Stack

- **Language**: Java 17+
- **Framework**: Spring Boot 3.x / 4.x (Web, Security, Data JPA, Validation)
- **Authentication**: JWT (JSON Web Token - `jjwt` 0.11.5)
- **Database**: MySQL / PostgreSQL (Production), H2 (In-Memory for Tests)
- **Documentation**: Springdoc OpenAPI (Swagger UI)
- **Build & Coverage**: Maven, JaCoCo Maven Plugin (0.8.11)
- **Testing**: JUnit 5, Mockito, Spring Security Test, MockMvc

---

## 🔑 Seed User Credentials

Upon application startup, `DataInitializer` seeds default users automatically if they do not exist:

| Role | Email | Password |
|---|---|---|
| **ADMIN** | `admin@booking.com` | `Admin@123` |
| **USER** | `user@booking.com` | `User@123` |

---

## 📁 Project Structure

```
BookingSystem/
├── src/
│   ├── main/
│   │   ├── java/com/booking/
│   │   │   ├── config/             # SecurityConfig, OpenApiConfig, DataInitializer
│   │   │   ├── controller/         # AuthController, ResourceController, ReservationController
│   │   │   ├── dto/                # AuthRequest, AuthResponse, ResourceRequest, ReservationRequest, ReservationResponse
│   │   │   ├── exception/          # GlobalExceptionHandler & Custom Domain Exceptions
│   │   │   ├── model/              # User, Resource, Reservation, Role, Status
│   │   │   ├── repository/         # UserRepository, ResourceRepository, ReservationRepository
│   │   │   ├── security/           # JwtUtils, AuthTokenFilter
│   │   │   └── serviceImpl/        # UserDetailsServiceImpl
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       ├── java/com/booking/       # Unit & Integration Tests (Controllers, Security, Services, Models)
│       └── resources/
│           └── application.properties # H2 Test Configuration
├── pom.xml
└── README.md
```

---

## ⚙️ Configuration & Environment Variables

Key properties are defined in `src/main/resources/application.properties`:

| Property / Environment Variable | Default Value | Description |
|---|---|---|
| `spring.datasource.url` | `jdbc:mysql://localhost:3306/booking_db` | Database connection URL |
| `spring.datasource.username` | `root` | Database username |
| `spring.datasource.password` | `rutuja` | Database password |
| `jwt.secret` | `404E635266556A586E3272357538782F413F4428...` | Secret key for JWT signature (256-bit min) |
| `jwt.expiration` | `86400000` | Token expiration time in milliseconds (24 Hours) |

---

## 🚀 Getting Started

### 1. Prerequisites
- **JDK 17** or higher installed
- **Maven 3.8+** installed (or use `./mvnw`)
- **MySQL** or **PostgreSQL** database service (optional for tests as H2 in-memory DB is used for testing)

### 2. Database Setup
Create MySQL database:
```sql
CREATE DATABASE IF NOT EXISTS booking_db;
```

### 3. Build & Run Application
Clone repository and navigate to root directory:
```bash
# Build project
./mvnw clean package

# Run Spring Boot application
./mvnw spring-boot:run
```
The application will start on `http://localhost:8080`.

---

## 🧪 Testing & Code Coverage (>94%)

The test suite contains 63+ unit and integration tests covering security, controllers, repositories, services, DTOs, and exception handlers.

### Run Tests and Generate Coverage Report
```bash
./mvnw clean test
```

### View JaCoCo Coverage Report
After running tests, open the generated HTML report in your browser:
```
target/site/jacoco/index.html
```

#### Coverage Metrics Achieved:
- **Instruction Coverage**: `~94.6%`
- **Line Coverage**: `~96.9%`
- **Class Coverage**: `100%`

---

## 📖 API Documentation & Endpoints Reference

Interactive Swagger UI documentation is available at:
👉 **[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**

### 1. Authentication
- `POST /auth/login` - Public endpoint to authenticate user and retrieve JWT token.

### 2. Resource Management (`/resources`)
- `GET /resources` - View all resources (`ADMIN`, `USER`).
- `GET /resources/{id}` - View resource by ID (`ADMIN`, `USER`).
- `POST /resources` - Create a new resource (`ADMIN` only).
- `PUT /resources/{id}` - Update a resource (`ADMIN` only).
- `DELETE /resources/{id}` - Delete a resource (`ADMIN` only).

### 3. Reservation Management (`/reservations`)
- `POST /reservations` - Create a reservation (`USER`, `ADMIN`). User derived from JWT token.
- `GET /reservations` - Get reservations (`ADMIN` sees all; `USER` sees only their own). Supports filtering (`status`, `minPrice`, `maxPrice`), pagination (`page`, `size`), and sorting (`sortBy`, `sortDir`).
- `GET /reservations/{id}` - Get reservation by ID (`ADMIN` or owner `USER`).
- `PUT /reservations/{id}/status` - Update reservation status (`ADMIN` only).
- `PUT /reservations/{id}` - Update reservation details (`ADMIN` or owner `USER`).
- `DELETE /reservations/{id}` - Delete reservation (`ADMIN` only).

---

## 📤 Push to GitHub

```bash
git init
git add .
git commit -m "Initial commit: Production-ready Resource Booking System with JWT, RBAC, and >94% Code Coverage"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/BookingSystem.git
git push -u origin main
```
