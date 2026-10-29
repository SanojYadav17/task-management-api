package com.ikaansh.taskmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ikaansh.taskmanagement.dto.request.CreateTaskRequest;
import com.ikaansh.taskmanagement.dto.request.LoginRequest;
import com.ikaansh.taskmanagement.entity.enums.Priority;
import com.ikaansh.taskmanagement.entity.enums.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String userToken;

    @BeforeEach
    void setUp() throws Exception {
        // Log in with default seeded user to get JWT token
        LoginRequest loginRequest = LoginRequest.builder()
                .usernameOrEmail("testuser")
                .password("UserPassword123!")
                .build();

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        userToken = objectMapper.readTree(responseBody).get("data").get("token").asText();
    }

    @Test
    @DisplayName("POST /api/tasks - Should create task and return 201 Created")
    void createTask_Returns201Created() throws Exception {
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("Controller Integration Task")
                .description("Testing task creation endpoint")
                .dueDate(LocalDateTime.now().plusDays(2))
                .priority(Priority.HIGH)
                .status(TaskStatus.PENDING)
                .build();

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Controller Integration Task"))
                .andExpect(jsonPath("$.data.priority").value("HIGH"))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @DisplayName("POST /api/tasks - Should return 400 Bad Request when title is blank")
    void createTask_Returns400_WhenTitleIsBlank() throws Exception {
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("") // Blank title should fail validation
                .description("Invalid Task")
                .build();

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.validationErrors.title").exists());
    }

    @Test
    @DisplayName("GET /api/tasks - Should return 200 OK with paginated list")
    void getTasks_Returns200OkWithPagination() throws Exception {
        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + userToken)
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.pageNumber").value(0))
                .andExpect(jsonPath("$.data.pageSize").value(5));
    }

    @Test
    @DisplayName("GET /api/tasks - Should return 401 Unauthorized when JWT is missing")
    void getTasks_Returns401_WhenNoToken() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isUnauthorized());
    }
}

