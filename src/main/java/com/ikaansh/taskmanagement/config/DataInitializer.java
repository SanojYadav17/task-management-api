package com.ikaansh.taskmanagement.config;

import com.ikaansh.taskmanagement.entity.Task;
import com.ikaansh.taskmanagement.entity.User;
import com.ikaansh.taskmanagement.entity.enums.Priority;
import com.ikaansh.taskmanagement.entity.enums.Role;
import com.ikaansh.taskmanagement.entity.enums.TaskStatus;
import com.ikaansh.taskmanagement.repository.TaskRepository;
import com.ikaansh.taskmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.default-username:admin}")
    private String adminUsername;

    @Value("${app.admin.default-email:admin@ikaansh.com}")
    private String adminEmail;

    @Value("${app.admin.default-password:AdminPassword123!}")
    private String adminPassword;

    @Value("${app.user.default-username:testuser}")
    private String defaultUsername;

    @Value("${app.user.default-email:user@ikaansh.com}")
    private String defaultEmail;

    @Value("${app.user.default-password:UserPassword123!}")
    private String defaultPassword;

    @Override
    public void run(String... args) {
        log.info("Checking database initialization...");

        // 1. Initialize Admin User
        if (!userRepository.existsByUsername(adminUsername)) {
            User admin = User.builder()
                    .username(adminUsername)
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .role(Role.ROLE_ADMIN)
                    .enabled(true)
                    .build();
            userRepository.save(admin);
            log.info("Initialized default ADMIN user: '{}' with password '{}'", adminUsername, adminPassword);
        }

        // 2. Initialize Standard User
        if (!userRepository.existsByUsername(defaultUsername)) {
            User user = User.builder()
                    .username(defaultUsername)
                    .email(defaultEmail)
                    .password(passwordEncoder.encode(defaultPassword))
                    .role(Role.ROLE_USER)
                    .enabled(true)
                    .build();
            User savedUser = userRepository.save(user);
            log.info("Initialized default STANDARD user: '{}' with password '{}'", defaultUsername, defaultPassword);

            // 3. Seed Sample Tasks for default user
            if (taskRepository.countByUserId(savedUser.getId()) == 0) {
                Task task1 = Task.builder()
                        .title("Configure Spring Security & JWT Filter Chain")
                        .description("Implement token generation, validation, and role-based authorization")
                        .dueDate(LocalDateTime.now().plusDays(2))
                        .priority(Priority.HIGH)
                        .status(TaskStatus.COMPLETED)
                        .completed(true)
                        .user(savedUser)
                        .build();

                Task task2 = Task.builder()
                        .title("Implement Task CRUD with User Scoping")
                        .description("Ensure users can only create, view, edit, and delete their own tasks")
                        .dueDate(LocalDateTime.now().plusDays(3))
                        .priority(Priority.HIGH)
                        .status(TaskStatus.IN_PROGRESS)
                        .completed(false)
                        .user(savedUser)
                        .build();

                Task task3 = Task.builder()
                        .title("Add Pagination, Sorting, and Swagger UI")
                        .description("Expose page, size, sortBy, sortDir parameters and interactive OpenAPI docs")
                        .dueDate(LocalDateTime.now().plusDays(5))
                        .priority(Priority.MEDIUM)
                        .status(TaskStatus.PENDING)
                        .completed(false)
                        .user(savedUser)
                        .build();

                taskRepository.save(task1);
                taskRepository.save(task2);
                taskRepository.save(task3);
                log.info("Seeded initial sample tasks for '{}'", defaultUsername);
            }
        }
    }
}

