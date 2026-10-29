package com.ikaansh.taskmanagement.controller;

import com.ikaansh.taskmanagement.dto.request.CreateTaskRequest;
import com.ikaansh.taskmanagement.dto.request.UpdateTaskRequest;
import com.ikaansh.taskmanagement.dto.response.ApiResponse;
import com.ikaansh.taskmanagement.dto.response.PagedResponse;
import com.ikaansh.taskmanagement.dto.response.TaskResponse;
import com.ikaansh.taskmanagement.entity.enums.Priority;
import com.ikaansh.taskmanagement.entity.enums.TaskStatus;
import com.ikaansh.taskmanagement.security.UserPrincipal;
import com.ikaansh.taskmanagement.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Endpoints for task management (CRUD, search, filtering, and pagination)")
@SecurityRequirement(name = "bearerAuth")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @Operation(summary = "Create a task", description = "Creates a new task scoped to the currently authenticated user")
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
            @Valid @RequestBody CreateTaskRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        TaskResponse taskResponse = taskService.createTask(request, currentUser);
        return new ResponseEntity<>(
                ApiResponse.success(taskResponse, "Task created successfully"),
                HttpStatus.CREATED
        );
    }

    @GetMapping
    @Operation(summary = "Get paginated tasks", description = "Retrieves tasks belonging to the authenticated user with optional filtering by status, priority, completion, search keyword, and custom pagination/sorting")
    public ResponseEntity<ApiResponse<PagedResponse<TaskResponse>>> getTasks(
            @Parameter(description = "Filter by status (PENDING, IN_PROGRESS, COMPLETED)")
            @RequestParam(required = false) TaskStatus status,

            @Parameter(description = "Filter by priority (LOW, MEDIUM, HIGH)")
            @RequestParam(required = false) Priority priority,

            @Parameter(description = "Filter by completion flag (true / false)")
            @RequestParam(required = false) Boolean completed,

            @Parameter(description = "Search keyword in title or description")
            @RequestParam(required = false) String search,

            @Parameter(description = "Page index (zero-based, default: 0)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size (default: 10)")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Field name to sort by (e.g. createdAt, dueDate, priority, title)")
            @RequestParam(defaultValue = "createdAt") String sortBy,

            @Parameter(description = "Sorting direction ('asc' or 'desc')")
            @RequestParam(defaultValue = "desc") String sortDir,

            @AuthenticationPrincipal UserPrincipal currentUser) {

        PagedResponse<TaskResponse> tasks = taskService.getAllTasks(
                currentUser, status, priority, completed, search, page, size, sortBy, sortDir
        );

        return ResponseEntity.ok(ApiResponse.success(tasks));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task by ID", description = "Fetches a specific task by its ID (must belong to authenticated user or caller must be admin)")
    public ResponseEntity<ApiResponse<TaskResponse>> getTaskById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        TaskResponse taskResponse = taskService.getTaskById(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success(taskResponse));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update task", description = "Updates fields of an existing task owned by the authenticated user")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        TaskResponse taskResponse = taskService.updateTask(id, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success(taskResponse, "Task updated successfully"));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete task", description = "Deletes a task by its ID (must belong to authenticated user or caller must be admin)")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        taskService.deleteTask(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle-complete")
    @Operation(summary = "Toggle task completion", description = "Quickly toggles the completed status of a task")
    public ResponseEntity<ApiResponse<TaskResponse>> toggleComplete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        TaskResponse taskResponse = taskService.toggleTaskCompletion(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success(taskResponse, "Task status toggled successfully"));
    }
}

