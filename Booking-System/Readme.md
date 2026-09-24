# Resource Booking System

A secure, production-ready RESTful Resource Booking System built with Spring Boot, Java 17+, Spring Security, JWT, and MySQL.

## 🚀 Features
- **JWT-Based Authentication**: Secure stateless login (`POST /auth/login`).
- **Role-Based Access Control (RBAC)**: Distinct `ADMIN` and `USER` roles with proper endpoint restrictions.
- **Resource Management**: Full CRUD access for administrators to manage bookable items (rooms, vehicles, equipment).
- **Reservation System**: Users can book resources with automatic user binding extracted securely from the JWT token.
- **Advanced Filtering & Pagination**: Filter reservations by status (`PENDING`, `CONFIRMED`, `CANCELLED`) and price range, with customizable sorting and pagination parameters.
- **Global Exception Handling**: Clean, structured error responses for validation failures and access denials.

---

## 🛠️ Tech Stack
- **Language**: Java 17+
- **Framework**: Spring Boot 3+
- **Security**: Spring Security, JJWT (JSON Web Tokens)
- **Database**: MySQL, Spring Data JPA / Hibernate
- **Validation**: Jakarta Bean Validation (`spring-boot-starter-validation`)

---

## ⚙️ Environment Variables & Configuration (`application.properties`)

Ensure your `src/main/resources/application.properties` file is configured with your local MySQL database and JWT secret:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/booking_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=your_mysql_password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# JWT Secret Key (Min 256 bits)
jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
jwt.expiration=86400000