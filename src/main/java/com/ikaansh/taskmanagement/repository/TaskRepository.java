package com.ikaansh.taskmanagement.repository;

import com.ikaansh.taskmanagement.entity.Task;
import com.ikaansh.taskmanagement.entity.enums.Priority;
import com.ikaansh.taskmanagement.entity.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * Find task by ID and owner's user ID for secure scoped access.
     */
    Optional<Task> findByIdAndUserId(Long id, Long userId);

    /**
     * Check if task exists and belongs to specified user.
     */
    boolean existsByIdAndUserId(Long id, Long userId);

    /**
     * Delete task by ID and owner's user ID.
     */
    void deleteByIdAndUserId(Long id, Long userId);

    /**
     * Flexible filtered and paginated search for tasks belonging to a specific user.
     */
    @Query("SELECT t FROM Task t WHERE t.user.id = :userId " +
           "AND (:status IS NULL OR t.status = :status) " +
           "AND (:priority IS NULL OR t.priority = :priority) " +
           "AND (:completed IS NULL OR t.completed = :completed) " +
           "AND (:search IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "     OR LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Task> findTasksScoped(
            @Param("userId") Long userId,
            @Param("status") TaskStatus status,
            @Param("priority") Priority priority,
            @Param("completed") Boolean completed,
            @Param("search") String search,
            Pageable pageable
    );

    /**
     * Flexible filtered and paginated search across ALL tasks (for Admin role).
     */
    @Query("SELECT t FROM Task t WHERE " +
           "(:userId IS NULL OR t.user.id = :userId) " +
           "AND (:status IS NULL OR t.status = :status) " +
           "AND (:priority IS NULL OR t.priority = :priority) " +
           "AND (:completed IS NULL OR t.completed = :completed) " +
           "AND (:search IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "     OR LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Task> findAllTasksAdmin(
            @Param("userId") Long userId,
            @Param("status") TaskStatus status,
            @Param("priority") Priority priority,
            @Param("completed") Boolean completed,
            @Param("search") String search,
            Pageable pageable
    );

    long countByUserId(Long userId);

    long countByUserIdAndCompleted(Long userId, boolean completed);
}

