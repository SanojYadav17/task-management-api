package com.ikaansh.taskmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ikaansh.taskmanagement.dto.request.LoginRequest;
import com.ikaansh.taskmanagement.dto.request.RegisterRequest;
import com.ikaansh.taskmanagement.entity.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/auth/register - Should register user and return 201 Created with JWT")
    void register_Returns201Created() throws Exception {
        String randomSuffix = UUID.randomUUID().toString().substring(0, 8);
        RegisterRequest request = RegisterRequest.builder()
                .username("candidate_" + randomSuffix)
                .email("candidate_" + randomSuffix + "@example.com")
                .password("Password123!")
                .role(Role.ROLE_USER)
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isString())
                .andExpect(jsonPath("$.data.username").value(request.getUsername()))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
    }

    @Test
    @DisplayName("POST /api/auth/register - Should return 400 Bad Request on invalid email")
    void register_Returns400_WhenInvalidEmail() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("invalid_user")
                .email("not-an-email")
                .password("Password123!")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.validationErrors.email").exists());
    }

    @Test
    @DisplayName("POST /api/auth/login - Should return 200 OK with JWT token on valid credentials")
    void login_Returns200Ok() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .usernameOrEmail("admin")
                .password("AdminPassword123!")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isString())
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.role").value("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("POST /api/auth/login - Should return 401 Unauthorized on bad credentials")
    void login_Returns401_OnBadCredentials() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .usernameOrEmail("admin")
                .password("WrongPasswordXYZ")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }
}

