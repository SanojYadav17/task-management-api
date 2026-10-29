package com.ikaansh.taskmanagement.service;

import com.ikaansh.taskmanagement.dto.request.LoginRequest;
import com.ikaansh.taskmanagement.dto.request.RegisterRequest;
import com.ikaansh.taskmanagement.dto.response.AuthResponse;
import com.ikaansh.taskmanagement.entity.User;
import com.ikaansh.taskmanagement.entity.enums.Role;
import com.ikaansh.taskmanagement.exception.DuplicateResourceException;
import com.ikaansh.taskmanagement.repository.UserRepository;
import com.ikaansh.taskmanagement.security.JwtTokenProvider;
import com.ikaansh.taskmanagement.security.UserPrincipal;
import com.ikaansh.taskmanagement.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .username("sanoj_yadav")
                .email("sanoj@example.com")
                .password("Password123!")
                .role(Role.ROLE_USER)
                .build();

        loginRequest = LoginRequest.builder()
                .usernameOrEmail("sanoj_yadav")
                .password("Password123!")
                .build();

        user = User.builder()
                .id(1L)
                .username("sanoj_yadav")
                .email("sanoj@example.com")
                .password("hashed_password")
                .role(Role.ROLE_USER)
                .enabled(true)
                .build();
    }

    @Test
    @DisplayName("Should register new user and return JWT token")
    void register_Success() {
        when(userRepository.existsByUsername("sanoj_yadav")).thenReturn(false);
        when(userRepository.existsByEmail("sanoj@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenReturn(user);

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn("mocked.jwt.token");
        when(tokenProvider.getExpirationInMs()).thenReturn(86400000L);

        AuthResponse response = authService.register(registerRequest);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("mocked.jwt.token");
        assertThat(response.getUsername()).isEqualTo("sanoj_yadav");
        assertThat(response.getEmail()).isEqualTo("sanoj@example.com");

        verify(passwordEncoder, times(1)).encode("Password123!");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when username is taken")
    void register_ThrowsException_WhenUsernameTaken() {
        when(userRepository.existsByUsername("sanoj_yadav")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Username 'sanoj_yadav' is already taken");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when email is taken")
    void register_ThrowsException_WhenEmailTaken() {
        when(userRepository.existsByUsername("sanoj_yadav")).thenReturn(false);
        when(userRepository.existsByEmail("sanoj@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email 'sanoj@example.com' is already registered");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should authenticate credentials and return JWT token on login")
    void login_Success() {
        UserPrincipal principal = UserPrincipal.create(user);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(principal);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn("jwt.login.token");
        when(tokenProvider.getExpirationInMs()).thenReturn(86400000L);

        AuthResponse response = authService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt.login.token");
        assertThat(response.getUsername()).isEqualTo("sanoj_yadav");
        assertThat(response.getRole()).isEqualTo(Role.ROLE_USER);
    }
}

