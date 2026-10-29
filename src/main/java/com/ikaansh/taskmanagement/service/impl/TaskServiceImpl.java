package com.ikaansh.taskmanagement.service.impl;

import com.ikaansh.taskmanagement.dto.request.CreateTaskRequest;
import com.ikaansh.taskmanagement.dto.request.UpdateTaskRequest;
import com.ikaansh.taskmanagement.dto.response.PagedResponse;
import com.ikaansh.taskmanagement.dto.response.TaskResponse;
import com.ikaansh.taskmanagement.entity.Task;
import com.ikaansh.taskmanagement.entity.User;
import com.ikaansh.taskmanagement.entity.enums.Priority;
import com.ikaansh.taskmanagement.entity.enums.Role;
import com.ikaansh.taskmanagement.entity.enums.TaskStatus;
import com.ikaansh.taskmanagement.exception.ResourceNotFoundException;
import com.ikaansh.taskmanagement.exception.UnauthorizedAccessException;
import com.ikaansh.taskmanagement.repository.TaskRepository;
import com.ikaansh.taskmanagement.repository.UserRepository;
import com.ikaansh.taskmanagement.security.UserPrincipal;
import com.ikaansh.taskmanagement.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    private static final Set<String> ALLOWED_SORT_PROPERTIES = Set.of(
            "id", "title", "dueDate", "priority", "status", "completed", "createdAt", "updatedAt"
    );

    @Override
    @Transactional
    public TaskResponse createTask(CreateTaskRequest request, UserPrincipal currentUser) {
        log.info("Creating new task '{}' for user id {}", request.getTitle(), currentUser.getId());

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        Task task = Task.builder()
                .title(request.getTitle().trim())
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .dueDate(request.getDueDate())
                .priority(request.getPriority() != null ? request.getPriority() : Priority.MEDIUM)
                .status(request.getStatus() != null ? request.getStatus() : TaskStatus.PENDING)
                .completed(request.getStatus() == TaskStatus.COMPLETED)
                .user(user)
                .build();

        Task savedTask = taskRepository.save(task);
        return TaskResponse.fromEntity(savedTask);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long taskId, UserPrincipal currentUser) {
        log.info("Fetching task id {} for user id {}", taskId, currentUser.getId());

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        verifyTaskOwnership(task, currentUser);

        return TaskResponse.fromEntity(task);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<TaskResponse> getAllTasks(
            UserPrincipal currentUser,
            TaskStatus status,
            Priority priority,
            Boolean completed,
            String search,
            int page,
            int size,
            String sortBy,
            String sortDir) {

        log.info("Listing tasks for user id {} (page: {}, size: {}, sort: {} {})", 
                currentUser.getId(), page, size, sortBy, sortDir);

        String validatedSortBy = ALLOWED_SORT_PROPERTIES.contains(sortBy) ? sortBy : "createdAt";
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, validatedSortBy));

        String sanitizedSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;

        Page<Task> taskPage = taskRepository.findTasksScoped(
                currentUser.getId(),
                status,
                priority,
                completed,
                sanitizedSearch,
                pageable
        );

        Page<TaskResponse> responsePage = taskPage.map(TaskResponse::fromEntity);
        return PagedResponse.of(responsePage);
    }

    @Override
    @Transactional
    public TaskResponse updateTask(Long taskId, UpdateTaskRequest request, UserPrincipal currentUser) {
        log.info("Updating task id {} for user id {}", taskId, currentUser.getId());

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        verifyTaskOwnership(task, currentUser);

        if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
            task.setTitle(request.getTitle().trim());
        }

        if (request.getDescription() != null) {
            task.setDescription(request.getDescription().trim());
        }

        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }

        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }

        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }

        if (request.getCompleted() != null) {
            task.setCompleted(request.getCompleted());
        }

        Task updatedTask = taskRepository.save(task);
        return TaskResponse.fromEntity(updatedTask);
    }

    @Override
    @Transactional
    public void deleteTask(Long taskId, UserPrincipal currentUser) {
        log.info("Deleting task id {} for user id {}", taskId, currentUser.getId());

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        verifyTaskOwnership(task, currentUser);

        taskRepository.delete(task);
    }

    @Override
    @Transactional
    public TaskResponse toggleTaskCompletion(Long taskId, UserPrincipal currentUser) {
        log.info("Toggling completion status for task id {} by user id {}", taskId, currentUser.getId());

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        verifyTaskOwnership(task, currentUser);

        boolean newCompletedStatus = !task.isCompleted();
        task.setCompleted(newCompletedStatus);

        Task savedTask = taskRepository.save(task);
        return TaskResponse.fromEntity(savedTask);
    }

    /**
     * Enforce strict ownership scoping: regular users can ONLY access/modify their own tasks.
     * Admins (ROLE_ADMIN) have elevated privileges to inspect or manage tasks across the system.
     */
    private void verifyTaskOwnership(Task task, UserPrincipal currentUser) {
        boolean isOwner = task.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ROLE_ADMIN;

        if (!isOwner && !isAdmin) {
            log.warn("Access denied: User id {} tried to access task id {} owned by user id {}",
                    currentUser.getId(), task.getId(), task.getUser().getId());
            throw new UnauthorizedAccessException("You do not have permission to access or modify this task");
        }
    }
}

