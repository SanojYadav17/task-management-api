# TaskFlow Pro — Java REST API for Task Management System

[![Java](https://img.shields.io/badge/Java-21%20LTS-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Security](https://img.shields.io/badge/Spring%20Security-JWT-blue.svg)](https://spring.io/projects/spring-security)
[![Database](https://img.shields.io/badge/Database-PostgreSQL%20%7C%20H2-blue.svg)](https://www.postgresql.org/)
[![Swagger](https://img.shields.io/badge/OpenAPI-Swagger%203.0-green.svg)](http://localhost:8080/swagger-ui.html)
[![License](https://img.shields.io/badge/License-Apache%202.0-lightgrey.svg)](LICENSE)

> **Candidate**: Sanoj Yadav  
> **Position**: Java Development  
> **Company**: Ikaansh Edutech Private Limited  
> **Submission Date**: 29 Oct 2026  

---

## 📌 Table of Contents
1. [Project Overview](#-project-overview)
2. [Key Architecture & Design](#-key-architecture--design)
3. [Features Implemented](#-features-implemented)
   - [Core Features (Primary Goals)](#core-features-primary-goals)
   - [Bonus Features (Secondary Goals)](#bonus-features-secondary-goals)
4. [Tech Stack](#-tech-stack)
5. [Quick Start & Setup Instructions](#-quick-start--setup-instructions)
   - [Option A: One-Command Run (Local Development with H2)](#option-a-one-command-run-local-development-with-h2)
   - [Option B: Containerized Run (Docker & Docker Compose)](#option-b-containerized-run-docker--docker-compose)
   - [Option C: Local Run with PostgreSQL](#option-c-local-run-with-postgresql)
6. [Interactive Web UI (Frontend)](#-interactive-web-ui-frontend)
7. [API Documentation & Swagger UI](#-api-documentation--swagger-ui)
8. [Database Configuration & Schema Scripts](#-database-configuration--schema-scripts)
9. [REST API Endpoints Reference](#-rest-api-endpoints-reference)
10. [Running Automated Tests](#-running-automated-tests)
11. [Postman Collection Guide](#-postman-collection-guide)
12. [Interview Preparation Guide](#-interview-preparation-guide)

---

## 🎯 Project Overview

This project is a secure, production-grade **RESTful Web Service for a Task Management System** developed using **Spring Boot 3.3**, **Java 21**, **Spring Security 6 with stateless JWT authentication**, and **Spring Data JPA**. 

It allows users to register, authenticate, and manage tasks (Create, Read, Update, Delete) with **strict user-scoping** (users can strictly access only their own tasks). It also features **Role-Based Access Control (RBAC)** where Administrators (`ROLE_ADMIN`) have system-wide task auditing and user management capabilities.

The service comes with a **built-in responsive Web UI (Frontend)** served directly from the Spring Boot application, **Swagger / OpenAPI 3.0 interactive documentation**, **PostgreSQL & H2 dual-database profiles**, and a **multi-stage Docker containerization setup**.

---

## 🏗 Key Architecture & Design

The application follows the clean **N-Tier Layered Architecture Pattern**:

```
                       ┌───────────────────────────────┐
                       │    Client (Web UI / Postman)  │
                       └──────────────┬────────────────┘
                                      │ HTTP / JSON
                                      ▼
                       ┌───────────────────────────────┐
                       │    JwtAuthenticationFilter    │ (Security & Token Validation)
                       └──────────────┬────────────────┘
                                      │
                                      ▼
                       ┌───────────────────────────────┐
                       │      Controller Layer         │ (REST Endpoints, DTO Validation)
                       │ (TaskController, Auth, Admin) │
                       └──────────────┬────────────────┘
                                      │
                                      ▼
                       ┌───────────────────────────────┐
                       │       Service Layer           │ (Business Logic, Scoping, RBAC)
                       │ (TaskService, AuthService)    │
                       └──────────────┬────────────────┘
                                      │
                                      ▼
                       ┌───────────────────────────────┐
                       │      Repository Layer         │ (Spring Data JPA, Native Queries)
                       │ (TaskRepository, UserRepository)│
                       └──────────────┬────────────────┘
                                      │
                                      ▼
                       ┌───────────────────────────────┐
                       │    Database (PostgreSQL / H2) │
                       └───────────────────────────────┘
```

---

## 🧩 Features Implemented

### Core Features (Primary Goals)
- [x] **Spring Boot REST API**: Complete CRUD endpoints for task management.
- [x] **Strict User Scoping**: Authenticated users can ONLY view, update, or delete their own tasks. Cross-user operations return HTTP 403 Forbidden.
- [x] **Rich Task Entity Modeling**: Fields include `id`, `title`, `description`, `dueDate`, `priority` (`LOW`, `MEDIUM`, `HIGH`), `status` (`PENDING`, `IN_PROGRESS`, `COMPLETED`), `completed` (boolean), `createdAt`, `updatedAt`, and `user` association.
- [x] **Relational Database Persistence**: Spring Data JPA with PostgreSQL support and H2 in-memory zero-configuration fallback.
- [x] **Database Schema Initialization**: Provided PostgreSQL DDL script (`schema.sql`) and seed data (`src/main/resources/db/data.sql`).
- [x] **Secure JWT Authentication**: Stateless token generation, extraction, signing (HMAC-SHA256), and expiration checking using modern `jjwt-api 0.12.5`.
- [x] **Hashed Password Storage**: Strong one-way password hashing using `BCryptPasswordEncoder` (strength factor 10).
- [x] **Bean Validation & Consistent Error Handling**: Jakarta Bean Validation (`@Valid`, `@NotBlank`, `@Size`, `@Email`) with centralized `@RestControllerAdvice` returning standardized HTTP response codes (200, 201, 204, 400, 401, 403, 404, 409, 500).
- [x] **Unit & Integration Testing**: Service and controller layer tests using JUnit 5, Mockito, and Spring `MockMvc`.

### Bonus Features (Secondary Goals)
- [x] **Pagination & Sorting**: Task list endpoint supports page number (`page`), page size (`size`), sort property (`sortBy`), sort direction (`sortDir`), priority filter (`priority`), status filter (`status`), and search keyword (`search`).
- [x] **Swagger / OpenAPI 3.0 Documentation**: Complete interactive documentation with JWT Bearer authentication scheme at `/swagger-ui.html`.
- [x] **Docker & Containerization**: Multi-stage `Dockerfile` and `docker-compose.yml` orchestrating the Spring Boot application and a PostgreSQL 16 container.
- [x] **Role-Based Access Control (RBAC)**: Distinguishes between `ROLE_USER` and `ROLE_ADMIN` with dedicated `/api/admin` endpoints for system metrics, user management, and system-wide task auditing.

---

## 🛠 Tech Stack

| Component | Technology | Version |
|---|---|---|
| Language | Java (LTS) | 21 |
| Framework | Spring Boot | 3.3.2 |
| Security | Spring Security 6 + JJWT | 0.12.5 |
| Persistence | Spring Data JPA / Hibernate | 6.5.x |
| Database | PostgreSQL 16 / H2 Database | Runtime |
| API Docs | SpringDoc OpenAPI (Swagger UI) | 2.5.0 |
| Testing | JUnit 5 Jupiter + Mockito + MockMvc | Latest |
| Build Tool | Apache Maven | 3.9.x |
| Container | Docker & Docker Compose | Multi-Stage |

---

## 🚀 Quick Start & Setup Instructions

### Pre-requisites
- **Java 21** (or Java 17+)
- **Git**

---

### Option A: One-Command Run (Local Development with H2)
*No database installation required! Uses embedded in-memory PostgreSQL-mode H2 database.*

1. **Clone repository**:
   ```bash
   git clone <repo-url>
   cd "Ikaansh Edutech Private Limited"
   ```

2. **Run using Maven Wrapper**:
   - On Windows:
     ```cmd
     mvnw.cmd spring-boot:run
     ```
   - On Linux/macOS:
     ```bash
     chmod +x mvnw
     ./mvnw spring-boot:run
     ```

3. **Open in Browser**:
   - **Interactive Web App (Frontend)**: [http://localhost:8080](http://localhost:8080)
   - **Swagger API Docs**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
   - **H2 DB Console**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)  
     *(JDBC URL: `jdbc:h2:mem:taskdb`, User: `sa`, Password: `<empty>`)*

---

### Option B: Containerized Run (Docker & Docker Compose)
*Spins up both the Spring Boot REST API and PostgreSQL 16 container automatically with one command.*

```bash
docker compose up --build
```

- Backend API: [http://localhost:8080](http://localhost:8080)
- PostgreSQL Database: `localhost:5432` (`task_management_db` / `postgres` / `postgrespassword`)

To stop:
```bash
docker compose down
```

---

### Option C: Local Run with PostgreSQL

1. **Create PostgreSQL database**:
   ```sql
   CREATE DATABASE task_management_db;
   ```

2. **Initialize schema**:
   Execute `schema.sql` (located in the root directory) or let Spring Boot automatically create the schema.

3. **Run with `prod` profile**:
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=prod -Dspring-boot.run.arguments="--DB_HOST=localhost --DB_PORT=5432 --DB_NAME=task_management_db --DB_USERNAME=postgres --DB_PASSWORD=yourpassword"
   ```

---

## 💻 Interactive Web UI (Frontend)

The application includes a built-in single-page web interface served at `http://localhost:8080/`.

### Built-in Credentials:
| Role | Username | Password | Email |
|---|---|---|---|
| **Administrator** | `admin` | `AdminPassword123!` | `admin@ikaansh.com` |
| **Standard User** | `testuser` | `UserPassword123!` | `user@ikaansh.com` |

### Capabilities:
- **Authentication**: Seamless tab-based login and registration with validation alerts.
- **Task Operations**: Create tasks with titles, descriptions, due dates, priorities, and statuses.
- **Dynamic Filtering & Search**: Instant filtering by priority, status, and live search.
- **Pagination & Sorting**: Navigate through task pages and sort by title, date, or priority.
- **One-Click Completion**: Toggle completion with checkboxes that automatically update backend status.
- **Admin Dashboard**: Real-time KPI statistics cards (Total Users, Total Tasks, Completed, Pending).

---

## 📖 API Documentation & Swagger UI

Interactive OpenAPI 3.0 documentation is available at:
**[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**

### Authenticating in Swagger UI:
1. Call `POST /api/auth/login` with your credentials.
2. Copy the `token` string from the response JSON.
3. Click the green **Authorize** button at the top-right of Swagger UI.
4. Enter your token (or `Bearer <token>`) and click **Authorize**.
5. All protected endpoints can now be executed directly within the browser!

---

## 🗄 Database Configuration & Schema Scripts

The repository includes explicit schema DDL scripts:
- **`schema.sql`** (Root directory): Complete PostgreSQL schema definition with tables, foreign keys, constraints, and performance indexes.
- **`src/main/resources/db/schema.sql`**: Application-bundled schema.
- **`src/main/resources/db/data.sql`**: Sample seed users and tasks.

### Database Tables:
1. **`users`**:
   - `id` (BIGSERIAL PK)
   - `username` (VARCHAR 50, UNIQUE)
   - `email` (VARCHAR 100, UNIQUE)
   - `password` (VARCHAR 255, BCrypt hashed)
   - `role` (VARCHAR 20: `ROLE_USER`, `ROLE_ADMIN`)
   - `enabled` (BOOLEAN)
   - `created_at`, `updated_at` (TIMESTAMP)

2. **`tasks`**:
   - `id` (BIGSERIAL PK)
   - `title` (VARCHAR 150, NOT NULL)
   - `description` (TEXT)
   - `due_date` (TIMESTAMP)
   - `priority` (VARCHAR 20: `LOW`, `MEDIUM`, `HIGH`)
   - `status` (VARCHAR 20: `PENDING`, `IN_PROGRESS`, `COMPLETED`)
   - `completed` (BOOLEAN)
   - `user_id` (BIGINT, FK -> users.id ON DELETE CASCADE)
   - `created_at`, `updated_at` (TIMESTAMP)

---

## 📡 REST API Endpoints Reference

### 1. Authentication Endpoints (`/api/auth`)
| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `POST` | `/api/auth/register` | Register new user account | No |
| `POST` | `/api/auth/login` | Authenticate and obtain JWT token | No |
| `GET` | `/api/auth/me` | Fetch authenticated user profile | Yes (Bearer) |

### 2. Task Management Endpoints (`/api/tasks`) — *User Scoped*
| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `POST` | `/api/tasks` | Create task for current user | Yes (Bearer) |
| `GET` | `/api/tasks` | Get paginated & filtered tasks | Yes (Bearer) |
| `GET` | `/api/tasks/{id}` | Get task by ID (ownership checked) | Yes (Bearer) |
| `PUT` | `/api/tasks/{id}` | Update task (ownership checked) | Yes (Bearer) |
| `DELETE` | `/api/tasks/{id}` | Delete task (ownership checked) | Yes (Bearer) |
| `PATCH` | `/api/tasks/{id}/toggle-complete` | Quick-toggle task completion | Yes (Bearer) |

### 3. Administrator Endpoints (`/api/admin`) — *ROLE_ADMIN Only*
| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `GET` | `/api/admin/stats` | Dashboard statistics & KPI metrics | Yes (ROLE_ADMIN) |
| `GET` | `/api/admin/users` | List all users (paginated) | Yes (ROLE_ADMIN) |
| `GET` | `/api/admin/tasks` | List all tasks across system | Yes (ROLE_ADMIN) |
| `DELETE` | `/api/admin/users/{id}` | Delete user & cascade tasks | Yes (ROLE_ADMIN) |

---

## 🧪 Running Automated Tests

Run the full test suite using Maven:

```bash
mvn test
```

### Test Coverage Summary:
- **`TaskServiceTest`**:
  - `createTask_Success`
  - `getTaskById_Success_WhenOwner`
  - `getTaskById_ThrowsUnauthorizedAccessException_WhenNotOwner` *(Verifies user scoping)*
  - `getTaskById_ThrowsResourceNotFoundException_WhenTaskNotFound`
  - `getAllTasks_ReturnsPagedTasks`
  - `updateTask_Success_WhenOwner`
  - `deleteTask_Success_WhenOwner`
  - `toggleTaskCompletion_Success`
- **`AuthServiceTest`**:
  - `register_Success`
  - `register_ThrowsException_WhenUsernameTaken`
  - `register_ThrowsException_WhenEmailTaken`
  - `login_Success`
- **`TaskControllerTest`** (Spring MockMvc):
  - HTTP 201 Created on task creation
  - HTTP 400 Bad Request on validation errors (e.g. blank title)
  - HTTP 200 OK with pagination metadata
  - HTTP 401 Unauthorized when JWT token is missing
- **`AuthControllerTest`** (Spring MockMvc):
  - HTTP 201 on valid user registration
  - HTTP 400 on malformed email
  - HTTP 200 on valid login credentials
  - HTTP 401 on incorrect password

---

## 📬 Postman Collection Guide

An importable collection file **`postman_collection.json`** is included in the project root.

1. Open Postman -> Click **Import** -> Select `postman_collection.json`.
2. The collection includes pre-configured requests organized into folders:
   - `1. Authentication`
   - `2. Tasks (Scoped to User)`
   - `3. Admin Operations (ROLE_ADMIN)`
3. When you run `POST /api/auth/login`, Postman test scripts **automatically save the JWT token** into the collection variable `{{authToken}}`.
4. Subsequent requests to `/api/tasks` will automatically pass the token in the `Authorization: Bearer {{authToken}}` header!

---

## 🎓 Interview Preparation Guide

A companion guide **`INTERVIEW_PREP.md`** has been prepared to help you confidently answer technical interview questions regarding:
- Spring Boot architecture & dependency injection
- Stateless authentication with JWT vs Stateful session cookies
- Spring Security 6 filter chain flow & `OncePerRequestFilter`
- JPA query scoping, indexing, and prevention of N+1 problems
- Exception hierarchy with `@RestControllerAdvice`
- Microservice containerization best practices with Docker multi-stage builds

---

## 📤 Submission Details
- **GitHub Repository**: [https://github.com/SanojYadav17/task-management-api](https://github.com/SanojYadav17/task-management-api)
- **Collaborator Invited**: `iks-hiring`
- **Candidate**: Sanoj Yadav
- **Company**: Ikaansh Edutech Private Limited

