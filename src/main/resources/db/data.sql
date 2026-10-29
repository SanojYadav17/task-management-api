-- ==========================================================
-- Sample Seed Data for Task Management System
-- Passwords are hashed using BCrypt (strength 10)
-- admin / AdminPassword123!
-- testuser / UserPassword123!
-- ==========================================================

INSERT INTO users (username, email, password, role, enabled, created_at, updated_at)
VALUES 
('admin', 'admin@ikaansh.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'ROLE_ADMIN', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('testuser', 'user@ikaansh.com', '$2a$10$gKzQ6sBq9E0tBw1pGfQXeOXw3Q9/hK5KqC2wZp3vC9u2vJ9qI9n7m', 'ROLE_USER', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (username) DO NOTHING;

INSERT INTO tasks (title, description, due_date, priority, status, completed, user_id, created_at, updated_at)
VALUES
('Setup Spring Boot Architecture', 'Initialize Spring Boot 3 with Spring Data JPA and Spring Security', CURRENT_TIMESTAMP + INTERVAL '2 day', 'HIGH', 'COMPLETED', true, (SELECT id FROM users WHERE username = 'testuser'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Implement JWT Authentication', 'Add register, login endpoints and JwtAuthenticationFilter', CURRENT_TIMESTAMP + INTERVAL '3 day', 'HIGH', 'IN_PROGRESS', false, (SELECT id FROM users WHERE username = 'testuser'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Add Task CRUD Endpoints', 'Implement create, read, update, delete endpoints scoped to user', CURRENT_TIMESTAMP + INTERVAL '4 day', 'MEDIUM', 'PENDING', false, (SELECT id FROM users WHERE username = 'testuser'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Configure Swagger OpenAPI 3.0', 'Add API documentation with BearerAuth security scheme', CURRENT_TIMESTAMP + INTERVAL '5 day', 'LOW', 'PENDING', false, (SELECT id FROM users WHERE username = 'testuser'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

