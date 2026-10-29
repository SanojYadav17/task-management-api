package com.ikaansh.taskmanagement.service;

import com.ikaansh.taskmanagement.dto.response.PagedResponse;
import com.ikaansh.taskmanagement.dto.response.SystemStatsResponse;
import com.ikaansh.taskmanagement.dto.response.TaskResponse;
import com.ikaansh.taskmanagement.dto.response.UserResponse;
import com.ikaansh.taskmanagement.entity.enums.Priority;
import com.ikaansh.taskmanagement.entity.enums.TaskStatus;

public interface AdminService {

    PagedResponse<UserResponse> getAllUsers(int page, int size);

    PagedResponse<TaskResponse> getAllTasks(
            Long userId,
            TaskStatus status,
            Priority priority,
            Boolean completed,
            String search,
            int page,
            int size,
            String sortBy,
            String sortDir
    );

    void deleteUser(Long userId);

    SystemStatsResponse getSystemStatistics();
}

