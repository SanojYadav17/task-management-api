package com.ikaansh.taskmanagement.controller;

import com.ikaansh.taskmanagement.dto.response.*;
import com.ikaansh.taskmanagement.entity.enums.Priority;
import com.ikaansh.taskmanagement.entity.enums.TaskStatus;
import com.ikaansh.taskmanagement.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Operations", description = "Privileged endpoints restricted strictly to users with ROLE_ADMIN")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    @Operation(summary = "List all users", description = "Retrieves a paginated list of all registered users (Admin only)")
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PagedResponse<UserResponse> users = adminService.getAllUsers(page, size);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/tasks")
    @Operation(summary = "List all tasks in system", description = "Retrieves all tasks across all users in the system with full filtering and pagination (Admin only)")
    public ResponseEntity<ApiResponse<PagedResponse<TaskResponse>>> getAllTasks(
            @Parameter(description = "Optional filter by specific user ID")
            @RequestParam(required = false) Long userId,

            @Parameter(description = "Filter by status")
            @RequestParam(required = false) TaskStatus status,

            @Parameter(description = "Filter by priority")
            @RequestParam(required = false) Priority priority,

            @Parameter(description = "Filter by completion flag")
            @RequestParam(required = false) Boolean completed,

            @Parameter(description = "Search keyword")
            @RequestParam(required = false) String search,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        PagedResponse<TaskResponse> tasks = adminService.getAllTasks(
                userId, status, priority, completed, search, page, size, sortBy, sortDir
        );

        return ResponseEntity.ok(ApiResponse.success(tasks));
    }

    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete user", description = "Deletes a user and cascades deletion to all their tasks (Admin only)")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    @Operation(summary = "Get system dashboard metrics", description = "Retrieves high-level counts and statistics of users and tasks (Admin only)")
    public ResponseEntity<ApiResponse<SystemStatsResponse>> getSystemStatistics() {
        SystemStatsResponse stats = adminService.getSystemStatistics();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}

