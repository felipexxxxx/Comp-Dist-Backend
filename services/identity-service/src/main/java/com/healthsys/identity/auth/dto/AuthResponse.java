package com.healthsys.identity.auth.dto;

import com.healthsys.identity.user.dto.UserResponse;
import java.time.Instant;

public record AuthResponse(
    String token,
    String tokenType,
    Instant expiresAt,
    UserResponse user
) {
}
