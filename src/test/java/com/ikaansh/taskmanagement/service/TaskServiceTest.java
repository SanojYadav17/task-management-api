package com.ikaansh.taskmanagement.service;

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
import com.ikaansh.taskmanagement.service.impl.TaskServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    private User user;
    private User anotherUser;
    private UserPrincipal userPrincipal;
    private UserPrincipal anotherUserPrincipal;
    private Task task;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encoded_pass")
                .role(Role.ROLE_USER)
                .enabled(true)
                .build();

        anotherUser = User.builder()
                .id(2L)
                .username("another")
                .email("another@example.com")
                .password("encoded_pass")
                .role(Role.ROLE_USER)
                .enabled(true)
                .build();

        userPrincipal = UserPrincipal.create(user);
        anotherUserPrincipal = UserPrincipal.create(anotherUser);

        task = Task.builder()
                .id(100L)
                .title("Sample Task")
                .description("Sample Description")
                .dueDate(LocalDateTime.now().plusDays(2))
                .priority(Priority.HIGH)
                .status(TaskStatus.PENDING)
                .completed(false)
                .user(user)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should create task scoped to current user successfully")
    void createTask_Success() {
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("New Task")
                .description("New Description")
                .dueDate(LocalDateTime.now().plusDays(3))
                .priority(Priority.HIGH)
                .status(TaskStatus.PENDING)
                .build();

        when(userRepository.findById(userPrincipal.getId())).thenReturn(Optional.of(user));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task t = invocation.getArgument(0);
            t.setId(101L);
            t.setCreatedAt(LocalDateTime.now());
            t.setUpdatedAt(LocalDateTime.now());
            return t;
        });

        TaskResponse response = taskService.createTask(request, userPrincipal);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(101L);
        assertThat(response.getTitle()).isEqualTo("New Task");
        assertThat(response.getPriority()).isEqualTo(Priority.HIGH);
        assertThat(response.getStatus()).isEqualTo(TaskStatus.PENDING);
        assertThat(response.getUserId()).isEqualTo(user.getId());

        verify(userRepository, times(1)).findById(userPrincipal.getId());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    @DisplayName("Should get task by ID when caller is the owner")
    void getTaskById_Success_WhenOwner() {
        when(taskRepository.findById(100L)).thenReturn(Optional.of(task));

        TaskResponse response = taskService.getTaskById(100L, userPrincipal);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getTitle()).isEqualTo("Sample Task");
        assertThat(response.getUserId()).isEqualTo(userPrincipal.getId());
    }

    @Test
    @DisplayName("Should throw UnauthorizedAccessException when user tries to access another user's task")
    void getTaskById_ThrowsUnauthorizedAccessException_WhenNotOwner() {
        when(taskRepository.findById(100L)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> taskService.getTaskById(100L, anotherUserPrincipal))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessageContaining("You do not have permission");

        verify(taskRepository, times(1)).findById(100L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when task does not exist")
    void getTaskById_ThrowsResourceNotFoundException_WhenTaskNotFound() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTaskById(999L, userPrincipal))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Task not found with id: '999'");
    }

    @Test
    @DisplayName("Should return paginated and filtered tasks for current user")
    void getAllTasks_ReturnsPagedTasks() {
        Page<Task> pagedTasks = new PageImpl<>(List.of(task));
        when(taskRepository.findTasksScoped(eq(userPrincipal.getId()), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(pagedTasks);

        PagedResponse<TaskResponse> response = taskService.getAllTasks(
                userPrincipal, null, null, null, null, 0, 10, "createdAt", "desc"
        );

        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("Should update task successfully when caller is owner")
    void updateTask_Success_WhenOwner() {
        UpdateTaskRequest updateRequest = UpdateTaskRequest.builder()
                .title("Updated Title")
                .priority(Priority.LOW)
                .status(TaskStatus.COMPLETED)
                .completed(true)
                .build();

        when(taskRepository.findById(100L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = taskService.updateTask(100L, updateRequest, userPrincipal);

        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Updated Title");
        assertThat(response.getPriority()).isEqualTo(Priority.LOW);
        assertThat(response.getStatus()).isEqualTo(TaskStatus.COMPLETED);
        assertThat(response.isCompleted()).isTrue();

        verify(taskRepository, times(1)).save(task);
    }

    @Test
    @DisplayName("Should delete task successfully when caller is owner")
    void deleteTask_Success_WhenOwner() {
        when(taskRepository.findById(100L)).thenReturn(Optional.of(task));
        doNothing().when(taskRepository).delete(task);

        taskService.deleteTask(100L, userPrincipal);

        verify(taskRepository, times(1)).delete(task);
    }

    @Test
    @DisplayName("Should toggle task completion status successfully")
    void toggleTaskCompletion_Success() {
        when(taskRepository.findById(100L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = taskService.toggleTaskCompletion(100L, userPrincipal);

        assertThat(response.isCompleted()).isTrue();
        assertThat(response.getStatus()).isEqualTo(TaskStatus.COMPLETED);
    }
}

