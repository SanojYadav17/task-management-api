# 🎓 Master Interview Preparation Guide — Task Management REST API
### Prepared for: Sanoj Yadav (Candidate for Java Development)
### Company: Ikaansh Edutech Private Limited
### Assignment: Java REST API for Task Management System

---

## 📌 Introduction & Key Focus

The interviewers at Ikaansh Edutech Private Limited stated:
> *"Your next interview round will be entirely based on this assignment. We may ask you about any part of your code. You should know what's written, how it works, and why."*

This guide breaks down **every single design decision, line of security logic, database query, and architectural choice** in your codebase so you can answer any question with clarity, confidence, and senior-level depth.

---

## 1. 🏛 Architecture Overview

### Q: Can you explain the high-level architecture of your project?
**Answer**:
The application follows the **N-Tier Layered Architecture Pattern**, strictly adhering to the **Single Responsibility Principle (SRP)** and **Separation of Concerns (SoC)**:

1. **Client / Presentation Layer**:
   - Web UI (`/index.html` via Tailwind CSS & Fetch API) or Postman / Swagger UI.
   - Communicates strictly via JSON over HTTP/HTTPS.

2. **Security Filter Layer (`JwtAuthenticationFilter`)**:
   - Sits in front of all controllers inside Spring Security's `SecurityFilterChain`.
   - Intercepts requests, extracts the JWT `Bearer` token, validates signatures and expiry, and populates the `SecurityContextHolder`.

3. **Controller Layer (`controller/`)**:
   - `AuthController`, `TaskController`, `AdminController`.
   - Responsible strictly for HTTP routing, request deserialization, Jakarta Bean Validation (`@Valid`), and mapping response status codes (e.g. 201 Created, 204 No Content, 200 OK).
   - No business logic or database queries reside in controllers.

4. **Service Layer (`service/` & `service/impl/`)**:
   - `AuthServiceImpl`, `TaskServiceImpl`, `AdminServiceImpl`.
   - Contains core business rules, transactional boundaries (`@Transactional`), **user scoping verification**, and domain state transitions.

5. **Repository Layer (`repository/`)**:
   - `UserRepository`, `TaskRepository`.
   - Extends Spring Data JPA's `JpaRepository`. Translates method calls and JPQL queries into optimized SQL queries executed against the relational database.

6. **Cross-Cutting Concerns**:
   - `GlobalExceptionHandler` (`@RestControllerAdvice`): Uniform error interceptor.
   - `OpenApiConfig`: Swagger / OpenAPI 3.0 documentation.
   - `DataInitializer`: Startup database seeding.

---

## 2. 🔐 Authentication, JWT & Spring Security 6

### Q: Why did you choose JWT over traditional server-side HTTP Sessions?
**Answer**:
1. **Stateless Scalability**: Traditional session-based authentication requires the server to store session state in memory or a distributed cache (like Redis). If you scale out with multiple backend instances, you need sticky sessions or a shared session store. With JWT, all required claims (username, userId, roles) are signed inside the self-contained cryptographic token. The backend remains 100% stateless.
2. **Decoupled Architecture**: Enables seamless consumption by multiple client types (Web SPAs, mobile apps, third-party microservices) across different domains without CORS/CSRF cookie complications.

### Q: Walk me through the exact JWT Authentication Flow in your code.
**Answer**:
1. **Login Request**:
   - Client sends credentials (`POST /api/auth/login`).
   - `AuthServiceImpl` invokes `authenticationManager.authenticate(...)`.
   - Spring Security checks credentials against `CustomUserDetailsService` using `BCryptPasswordEncoder`.
2. **Token Generation**:
   - `JwtTokenProvider.generateToken(authentication)` creates a JWT using `io.jsonwebtoken (JJWT 0.12.5)`.
   - The token contains:
     - **Subject**: username
     - **Claims**: `userId`, `email`, `roles`
     - **Timestamps**: `issuedAt`, `expiration` (configured to 24 hours)
     - **Signature**: HMAC-SHA256 (`Jwts.SIG.HS256`) signed with a 256-bit secret key.
3. **Subsequent API Request**:
   - Client sends `Authorization: Bearer <jwt-token>` header.
   - `JwtAuthenticationFilter` (which extends `OncePerRequestFilter`) intercepts the request.
   - It strips `"Bearer "`, validates signature and expiration with `tokenProvider.validateToken()`.
   - If valid, extracts username, loads `UserDetails` via `CustomUserDetailsService`, creates a `UsernamePasswordAuthenticationToken`, and loads it into `SecurityContextHolder.getContext().setAuthentication(...)`.
   - The request proceeds to the controller with `@AuthenticationPrincipal UserPrincipal currentUser`.

### Q: How is password storage secured?
**Answer**:
- We use **`BCryptPasswordEncoder`** with a strength factor of **10**.
- **Why BCrypt over MD5/SHA-256?**
  - MD5 and SHA-256 are fast hash functions designed for checksums, making them vulnerable to brute-force attacks and GPU rainbow table attacks.
  - BCrypt is an **adaptive key-derivation function** based on the Blowfish cipher. It incorporates a **randomly generated 128-bit salt** directly into the hash string and introduces a configurable computational work factor ($2^{10} = 1024$ rounds), making rainbow table lookup attacks computationally impossible.

---

## 3. 🎯 Core Task Features & Strict User Scoping

### Q: What is "User Scoping" and how did you prevent IDOR (Insecure Direct Object Reference) vulnerabilities?
**Answer**:
- **The Problem (IDOR)**: In a naive implementation, `GET /api/tasks/{id}` only looks up the task by ID (`taskRepository.findById(id)`). A malicious User A could query `/api/tasks/100` and view or edit User B's confidential task by guessing the ID.
- **Our Solution**:
  1. In `TaskServiceImpl`:
     ```java
     private void verifyTaskOwnership(Task task, UserPrincipal currentUser) {
         boolean isOwner = task.getUser().getId().equals(currentUser.getId());
         boolean isAdmin = currentUser.getRole() == Role.ROLE_ADMIN;

         if (!isOwner && !isAdmin) {
             throw new UnauthorizedAccessException("You do not have permission to access or modify this task");
         }
     }
     ```
  2. In `TaskRepository`:
     For listing tasks, we do not fetch all tasks into memory; we filter at the database level with a query scoped directly to `t.user.id = :userId`:
     ```java
     @Query("SELECT t FROM Task t WHERE t.user.id = :userId AND ...")
     Page<Task> findTasksScoped(@Param("userId") Long userId, ...);
     ```
  3. If an unauthorized user attempts to view, edit, or delete another user's task, an `UnauthorizedAccessException` is thrown, and the `GlobalExceptionHandler` immediately returns **`HTTP 403 Forbidden`**.

### Q: How did you implement Pagination, Sorting, and Filtering?
**Answer**:
- We leverage Spring Data's `Pageable` and `PageRequest.of(page, size, Sort.by(direction, sortBy))`.
- **Security Whitelist on Sort Fields**: To prevent property injection or 500 runtime errors from arbitrary user input, `TaskServiceImpl` checks `sortBy` against an `ALLOWED_SORT_PROPERTIES` Set (`id`, `title`, `dueDate`, `priority`, `status`, `completed`, `createdAt`). If an unrecognized field is passed, it safely defaults to `createdAt`.
- **Dynamic Query in `TaskRepository`**:
  Uses null-safe SQL clauses:
  ```sql
  (:status IS NULL OR t.status = :status)
  AND (:priority IS NULL OR t.priority = :priority)
  AND (:search IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')) ...)
  ```
  This allows users to combine any set of filters (e.g. only High priority + completed + keyword search "Spring") in a single database query.

---

## 4. 🗄 Database Design & Persistence

### Q: Why did you provide both PostgreSQL and H2 databases?
**Answer**:
- **PostgreSQL**: Industry-standard, ACID-compliant relational database for production and Docker deployments.
- **H2 Database**: Lightweight in-memory database configured in PostgreSQL-compatibility mode (`MODE=PostgreSQL`).
- **Benefit for the Evaluator**: The assignment review team can clone the project and execute it immediately with `./mvnw spring-boot:run` without installing PostgreSQL locally, while keeping the full production PostgreSQL setup available in Docker Compose.

### Q: Can you explain the schema design and indexes?
**Answer**:
1. **`users` table**:
   - `id`: BIGSERIAL primary key.
   - `username` & `email`: UNIQUE constraints and B-Tree indexes (`idx_users_username`, `idx_users_email`) for $O(\log n)$ lookup speed during authentication.
2. **`tasks` table**:
   - `user_id`: Foreign key referencing `users(id)` with `ON DELETE CASCADE`. If a user account is deleted by an admin, all their tasks are automatically removed, preventing orphaned records.
   - **Composite Indexes**:
     - `idx_tasks_user_status (user_id, status)`
     - `idx_tasks_user_priority (user_id, priority)`
     These compound indexes dramatically optimize the filtered pagination queries because the query filter always evaluates `user_id` alongside `status` or `priority`.

---

## 5. ⚠️ Validation and Exception Handling

### Q: How do you handle input validation and error responses?
**Answer**:
1. **Input Validation**:
   - Jakarta Bean Validation annotations on DTOs:
     - `@NotBlank`, `@Size(min=3, max=150)` on titles.
     - `@Email`, `@NotBlank` on registration email.
     - `@Size(min=6)` on passwords.
   - Controllers apply `@Valid @RequestBody`.
2. **Centralized `@RestControllerAdvice` (`GlobalExceptionHandler`)**:
   - Intercepts `MethodArgumentNotValidException` and extracts individual field errors into a structured map:
     ```json
     {
       "success": false,
       "status": 400,
       "error": "Bad Request",
       "message": "Validation failed for input data",
       "path": "/api/tasks",
       "validationErrors": {
         "title": "Task title cannot be blank"
       }
     }
     ```
   - Intercepts domain exceptions:
     - `ResourceNotFoundException` -> 404 Not Found
     - `DuplicateResourceException` -> 409 Conflict
     - `UnauthorizedAccessException` -> 403 Forbidden
     - `BadCredentialsException` -> 401 Unauthorized
     - Generic unhandled exceptions -> 500 Internal Server Error

---

## 6. 🐳 Docker and Containerization

### Q: Explain the multi-stage Docker build you implemented.
**Answer**:
A multi-stage build solves two major production problems: **image size** and **security vulnerabilities**:
1. **Stage 1 (Builder)**: Uses `eclipse-temurin:21-jdk-alpine`. Copies Maven wrapper and `pom.xml`, downloads dependencies offline, compiles Java code, and packages the executable JAR file.
2. **Stage 2 (Runtime)**: Uses `eclipse-temurin:21-jre-alpine` (lightweight JRE without compiler tools or build cache). Copies *only* the finished `app.jar` from Stage 1.
3. **Security Best Practice**: Runs under a dedicated non-root user (`appuser` / `appgroup`) rather than `root`.
4. **Memory Optimization**: Passes `-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0` to respect Docker container memory limits.

---

## 7. 🧪 Testing Strategy

### Q: What is your approach to unit and integration testing?
**Answer**:
- **Service Layer Unit Tests (`TaskServiceTest`, `AuthServiceTest`)**:
  - Uses `@ExtendWith(MockitoExtension.class)`.
  - Mocks `TaskRepository`, `UserRepository`, `PasswordEncoder`, and `JwtTokenProvider`.
  - Verifies business assertions and security checks in complete isolation from the database.
- **Controller Integration Tests (`TaskControllerTest`, `AuthControllerTest`)**:
  - Uses `@SpringBootTest` and `@AutoConfigureMockMvc`.
  - Tests the actual HTTP request pipeline, JSON serialization/deserialization, filter chain, and HTTP status codes.
- **Results**: 20 comprehensive automated tests passing with 0 failures and 0 errors.

---

## 8. 🌟 Rapid-Fire Interview Questions & Answers

| Question | Short & Confident Answer |
|---|---|
| **What is `@Transactional`?** | Manages database transactions. If an unhandled `RuntimeException` occurs during method execution, all database changes within the transaction are rolled back automatically to maintain consistency. |
| **Why use DTOs instead of exposing JPA Entities directly?** | 1. Security: Prevents mass-assignment vulnerabilities. 2. Performance: Prevents lazy-loading exceptions and circular serialization loops (Jackson infinite recursion). 3. Decoupling: API contracts can evolve without altering internal DB table schemas. |
| **What does `OncePerRequestFilter` do?** | Guarantees that the JWT authentication filter executes exactly once per incoming HTTP request, even if internal request dispatching or forward/include occurs. |
| **What is the difference between `ROLE_USER` and `ROLE_ADMIN`?** | Regular users only have access to their own tasks (`/api/tasks`). Administrators have access to `/api/admin/**` endpoints (system statistics, listing all users, system-wide task auditing). |
| **How did you synchronize `status` and `completed` fields in Task?** | In the `Task` entity, `setCompleted(true)` automatically sets `status = TaskStatus.COMPLETED`, and `setStatus(TaskStatus.COMPLETED)` automatically sets `completed = true`. |

---
**Best of luck, Sanoj! You have built a clean, production-grade Spring Boot project that covers 100% of the core requirements and 100% of the bonus goals.**

