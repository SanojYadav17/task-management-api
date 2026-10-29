package com.ikaansh.taskmanagement.entity;

import com.ikaansh.taskmanagement.entity.enums.Priority;
import com.ikaansh.taskmanagement.entity.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Priority priority = Priority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TaskStatus status = TaskStatus.PENDING;

    @Column(nullable = false)
    @Builder.Default
    private boolean completed = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Helper method to keep status and completed in sync
    public void setCompleted(boolean completed) {
        this.completed = completed;
        if (completed && this.status != TaskStatus.COMPLETED) {
            this.status = TaskStatus.COMPLETED;
        } else if (!completed && this.status == TaskStatus.COMPLETED) {
            this.status = TaskStatus.PENDING;
        }
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
        this.completed = (status == TaskStatus.COMPLETED);
    }
}

