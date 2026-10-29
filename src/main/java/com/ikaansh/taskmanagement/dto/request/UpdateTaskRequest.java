package com.ikaansh.taskmanagement.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ikaansh.taskmanagement.entity.enums.Priority;
import com.ikaansh.taskmanagement.entity.enums.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTaskRequest {

    @Size(min = 3, max = 150, message = "Task title must be between 3 and 150 characters")
    @Schema(example = "Updated Task Title", description = "New title of the task (optional)")
    private String title;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    @Schema(example = "Updated detailed description", description = "New detailed description")
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd['T'[HH:mm[:ss]]]")
    @Schema(example = "2026-11-05T12:00:00", description = "Updated due date and time")
    private LocalDateTime dueDate;

    @Schema(example = "HIGH", description = "Updated priority level (LOW, MEDIUM, HIGH)")
    private Priority priority;

    @Schema(example = "COMPLETED", description = "Updated status (PENDING, IN_PROGRESS, COMPLETED)")
    private TaskStatus status;

    @Schema(example = "true", description = "Completion flag (syncs automatically with status)")
    private Boolean completed;
}

