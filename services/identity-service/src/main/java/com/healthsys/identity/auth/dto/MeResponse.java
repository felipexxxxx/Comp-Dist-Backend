package com.healthsys.identity.auth.dto;

import com.healthsys.identity.user.domain.UserRole;
import java.util.UUID;

public record MeResponse(
    UUID id,
    String name,
    String email,
    UserRole role
) {
}
