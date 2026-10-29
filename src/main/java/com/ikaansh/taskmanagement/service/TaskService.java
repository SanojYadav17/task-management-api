package com.ikaansh.taskmanagement.service;

import com.ikaansh.taskmanagement.dto.request.CreateTaskRequest;
import com.ikaansh.taskmanagement.dto.request.UpdateTaskRequest;
import com.ikaansh.taskmanagement.dto.response.PagedResponse;
import com.ikaansh.taskmanagement.dto.response.TaskResponse;
import com.ikaansh.taskmanagement.entity.enums.Priority;
import com.ikaansh.taskmanagement.entity.enums.TaskStatus;
import com.ikaansh.taskmanagement.security.UserPrincipal;

public interface TaskService {

    TaskResponse createTask(CreateTaskRequest request, UserPrincipal currentUser);

    TaskResponse getTaskById(Long taskId, UserPrincipal currentUser);

    PagedResponse<TaskResponse> getAllTasks(
            UserPrincipal currentUser,
            TaskStatus status,
            Priority priority,
            Boolean completed,
            String search,
            int page,
            int size,
            String sortBy,
            String sortDir
    );

    TaskResponse updateTask(Long taskId, UpdateTaskRequest request, UserPrincipal currentUser);

    void deleteTask(Long taskId, UserPrincipal currentUser);

    TaskResponse toggleTaskCompletion(Long taskId, UserPrincipal currentUser);
}

