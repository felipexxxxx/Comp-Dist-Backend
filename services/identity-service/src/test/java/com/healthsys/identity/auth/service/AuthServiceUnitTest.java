package com.healthsys.identity.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.healthsys.identity.auth.dto.AuthResponse;
import com.healthsys.identity.auth.dto.LoginRequest;
import com.healthsys.identity.auth.dto.LogoutResponse;
import com.healthsys.identity.auth.revocation.TokenRevocationService;
import com.healthsys.identity.config.JwtProperties;
import com.healthsys.identity.messaging.IdentityEventPublisher;
import com.healthsys.identity.user.domain.UserAccount;
import com.healthsys.identity.user.domain.UserRole;
import com.healthsys.identity.user.dto.UserResponse;
import com.healthsys.identity.user.repository.UserRepository;
import com.healthsys.identity.user.service.UserService;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

@ExtendWith(MockitoExtension.class)
class AuthServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtEncoder jwtEncoder;

    @Mock
    private TokenRevocationService tokenRevocationService;

    @Mock
    private IdentityEventPublisher identityEventPublisher;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setExpirationMinutes(60);

        authService = new AuthService(
            userRepository,
            userService,
            passwordEncoder,
            jwtEncoder,
            jwtProperties,
            tokenRevocationService,
            identityEventPublisher
        );
    }

    @Test
    void shouldIssueTokenForValidCredentials() {
        UserAccount userAccount = UserAccount.builder()
            .id(UUID.randomUUID())
            .name("Administrator")
            .email("admin@healthsys.local")
            .passwordHash("encoded-password")
            .role(UserRole.ADMIN)
            .active(true)
            .createdAt(OffsetDateTime.now())
            .updatedAt(OffsetDateTime.now())
            .build();
        UserResponse userResponse = new UserResponse(
            userAccount.getId(),
            userAccount.getName(),
            userAccount.getEmail(),
            userAccount.getRole(),
            true,
            OffsetDateTime.now()
        );

        when(userRepository.findByEmailIgnoreCase("admin@healthsys.local")).thenReturn(Optional.of(userAccount));
        when(passwordEncoder.matches("Admin@123", "encoded-password")).thenReturn(true);
        when(userService.toResponse(userAccount)).thenReturn(userResponse);
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(
            Jwt.withTokenValue("generated-token")
                .header("alg", "HS256")
                .subject(userAccount.getId().toString())
                .claim("roles", "ADMIN")
                .build()
        );

        AuthResponse response = authService.login(new LoginRequest("admin@healthsys.local", "Admin@123"));

        assertThat(response.token()).isEqualTo("generated-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.user().email()).isEqualTo("admin@healthsys.local");
    }

    @Test
    void shouldRejectInvalidCredentials() {
        when(userRepository.findByEmailIgnoreCase("admin@healthsys.local")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("admin@healthsys.local", "wrong-password")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid email or password.");
    }

    @Test
    void shouldRevokeTokenOnLogout() {
        UUID userId = UUID.randomUUID();
        UUID tokenId = UUID.randomUUID();
        Instant expiresAt = Instant.now().plusSeconds(900);
        Jwt jwt = Jwt.withTokenValue("jwt-token")
            .header("alg", "HS256")
            .subject(userId.toString())
            .claim(JwtClaimNames.JTI, tokenId.toString())
            .expiresAt(expiresAt)
            .claim("roles", "ADMIN")
            .build();

        LogoutResponse response = authService.logout(jwt);

        assertThat(response.message()).isEqualTo("Logout completed and token revoked.");
        verify(tokenRevocationService).revokeToken(tokenId, userId, expiresAt);
        verify(identityEventPublisher).publishTokenRevoked(userId.toString(), tokenId.toString(), expiresAt);
    }
}
