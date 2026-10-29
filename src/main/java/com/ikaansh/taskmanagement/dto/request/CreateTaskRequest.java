package com.ikaansh.taskmanagement.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ikaansh.taskmanagement.entity.enums.Priority;
import com.ikaansh.taskmanagement.entity.enums.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTaskRequest {

    @NotBlank(message = "Task title cannot be blank")
    @Size(min = 3, max = 150, message = "Task title must be between 3 and 150 characters")
    @Schema(example = "Complete Spring Boot Assignment", description = "Title of the task")
    private String title;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    @Schema(example = "Implement JWT authentication, CRUD endpoints, and Dockerfile", description = "Detailed description")
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd['T'[HH:mm[:ss]]]")
    @Schema(example = "2026-10-31T18:00:00", description = "Target due date and time (ISO format)")
    private LocalDateTime dueDate;

    @Schema(example = "HIGH", description = "Task priority level (LOW, MEDIUM, HIGH)")
    @Builder.Default
    private Priority priority = Priority.MEDIUM;

    @Schema(example = "PENDING", description = "Initial status (PENDING, IN_PROGRESS, COMPLETED)")
    @Builder.Default
    private TaskStatus status = TaskStatus.PENDING;
}

