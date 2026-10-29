package com.ikaansh.taskmanagement.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    @NotBlank(message = "Username or email is required")
    @Schema(example = "sanoj_yadav", description = "Username or email address")
    private String usernameOrEmail;

    @NotBlank(message = "Password is required")
    @Schema(example = "SecurePass123!", description = "User password")
    private String password;
}

