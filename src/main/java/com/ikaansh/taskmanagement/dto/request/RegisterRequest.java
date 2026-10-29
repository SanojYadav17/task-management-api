package com.ikaansh.taskmanagement.dto.request;

import com.ikaansh.taskmanagement.entity.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Schema(example = "sanoj_yadav", description = "Unique alphanumeric username")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email format")
    @Schema(example = "sanoj.yadav@example.com", description = "Valid unique email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be at least 6 characters long")
    @Schema(example = "SecurePass123!", description = "User password (minimum 6 characters)")
    private String password;

    @Schema(example = "ROLE_USER", description = "Role of the user (ROLE_USER or ROLE_ADMIN)")
    private Role role;
}

