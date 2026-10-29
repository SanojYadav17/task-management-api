package com.ikaansh.taskmanagement.service.impl;

import com.ikaansh.taskmanagement.dto.request.LoginRequest;
import com.ikaansh.taskmanagement.dto.request.RegisterRequest;
import com.ikaansh.taskmanagement.dto.response.AuthResponse;
import com.ikaansh.taskmanagement.dto.response.UserResponse;
import com.ikaansh.taskmanagement.entity.User;
import com.ikaansh.taskmanagement.entity.enums.Role;
import com.ikaansh.taskmanagement.exception.DuplicateResourceException;
import com.ikaansh.taskmanagement.exception.ResourceNotFoundException;
import com.ikaansh.taskmanagement.repository.UserRepository;
import com.ikaansh.taskmanagement.security.JwtTokenProvider;
import com.ikaansh.taskmanagement.security.UserPrincipal;
import com.ikaansh.taskmanagement.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Processing registration request for username: {}", request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username '" + request.getUsername() + "' is already taken");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email '" + request.getEmail() + "' is already registered");
        }

        Role assignedRole = request.getRole() != null ? request.getRole() : Role.ROLE_USER;

        User user = User.builder()
                .username(request.getUsername().trim())
                .email(request.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(assignedRole)
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);

        // Authenticate immediately after successful registration
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        return AuthResponse.builder()
                .token(jwt)
                .tokenType("Bearer")
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .expiresInMs(tokenProvider.getExpirationInMs())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("Processing login request for: {}", request.getUsernameOrEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsernameOrEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        return AuthResponse.builder()
                .token(jwt)
                .tokenType("Bearer")
                .id(principal.getId())
                .username(principal.getUsername())
                .email(principal.getEmail())
                .role(principal.getRole())
                .expiresInMs(tokenProvider.getExpirationInMs())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUserProfile(UserPrincipal currentUser) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        return UserResponse.fromEntity(user);
    }
}

