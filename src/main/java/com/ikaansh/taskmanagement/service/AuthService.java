package com.ikaansh.taskmanagement.service;

import com.ikaansh.taskmanagement.dto.request.LoginRequest;
import com.ikaansh.taskmanagement.dto.request.RegisterRequest;
import com.ikaansh.taskmanagement.dto.response.AuthResponse;
import com.ikaansh.taskmanagement.dto.response.UserResponse;
import com.ikaansh.taskmanagement.security.UserPrincipal;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    UserResponse getCurrentUserProfile(UserPrincipal currentUser);
}

