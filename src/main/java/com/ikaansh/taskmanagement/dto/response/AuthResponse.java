package com.ikaansh.taskmanagement.dto.response;

import com.ikaansh.taskmanagement.entity.enums.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String token;

    @Builder.Default
    private String tokenType = "Bearer";

    private Long id;
    private String username;
    private String email;
    private Role role;
    private long expiresInMs;
}

