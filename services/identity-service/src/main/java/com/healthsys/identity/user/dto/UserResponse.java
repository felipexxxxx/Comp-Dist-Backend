package com.healthsys.identity.user.dto;

import com.healthsys.identity.user.domain.UserRole;
import java.time.OffsetDateTime;
import java.util.UUID;

public record UserResponse(
    UUID id,
    String name,
    String email,
    UserRole role,
    boolean active,
    OffsetDateTime createdAt
) {
}
