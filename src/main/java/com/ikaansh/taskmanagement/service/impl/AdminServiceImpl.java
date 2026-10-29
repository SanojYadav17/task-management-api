package com.ikaansh.taskmanagement.service.impl;

import com.ikaansh.taskmanagement.dto.response.PagedResponse;
import com.ikaansh.taskmanagement.dto.response.SystemStatsResponse;
import com.ikaansh.taskmanagement.dto.response.TaskResponse;
import com.ikaansh.taskmanagement.dto.response.UserResponse;
import com.ikaansh.taskmanagement.entity.Task;
import com.ikaansh.taskmanagement.entity.User;
import com.ikaansh.taskmanagement.entity.enums.Priority;
import com.ikaansh.taskmanagement.entity.enums.TaskStatus;
import com.ikaansh.taskmanagement.exception.ResourceNotFoundException;
import com.ikaansh.taskmanagement.repository.TaskRepository;
import com.ikaansh.taskmanagement.repository.UserRepository;
import com.ikaansh.taskmanagement.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> getAllUsers(int page, int size) {
        log.info("Admin fetching users page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> userPage = userRepository.findAll(pageable);
        return PagedResponse.of(userPage.map(UserResponse::fromEntity));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<TaskResponse> getAllTasks(
            Long userId,
            TaskStatus status,
            Priority priority,
            Boolean completed,
            String search,
            int page,
            int size,
            String sortBy,
            String sortDir) {

        log.info("Admin fetching all system tasks");
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy != null ? sortBy : "createdAt"));

        String sanitizedSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;

        Page<Task> taskPage = taskRepository.findAllTasksAdmin(
                userId,
                status,
                priority,
                completed,
                sanitizedSearch,
                pageable
        );

        return PagedResponse.of(taskPage.map(TaskResponse::fromEntity));
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        log.info("Admin deleting user id: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        userRepository.delete(user);
    }

    @Override
    @Transactional(readOnly = true)
    public SystemStatsResponse getSystemStatistics() {
        long totalUsers = userRepository.count();
        long totalTasks = taskRepository.count();

        // Calculate task stats
        long completedTasks = taskRepository.findAll().stream().filter(Task::isCompleted).count();
        long pendingTasks = taskRepository.findAll().stream().filter(t -> t.getStatus() == TaskStatus.PENDING).count();
        long inProgressTasks = taskRepository.findAll().stream().filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS).count();

        return SystemStatsResponse.builder()
                .totalUsers(totalUsers)
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .pendingTasks(pendingTasks)
                .inProgressTasks(inProgressTasks)
                .build();
    }
}

