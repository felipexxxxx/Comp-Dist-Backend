package com.healthsys.identity.auth.service;

import com.healthsys.identity.auth.dto.AuthResponse;
import com.healthsys.identity.auth.dto.LoginRequest;
import com.healthsys.identity.auth.dto.MeResponse;
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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;
    private final TokenRevocationService tokenRevocationService;
    private final IdentityEventPublisher identityEventPublisher;

    public AuthService(
        UserRepository userRepository,
        UserService userService,
        PasswordEncoder passwordEncoder,
        JwtEncoder jwtEncoder,
        JwtProperties jwtProperties,
        TokenRevocationService tokenRevocationService,
        IdentityEventPublisher identityEventPublisher
    ) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.jwtProperties = jwtProperties;
        this.tokenRevocationService = tokenRevocationService;
        this.identityEventPublisher = identityEventPublisher;
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        UserAccount userAccount = userRepository.findByEmailIgnoreCase(normalizedEmail)
            .filter(UserAccount::isActive)
            .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));

        if (!passwordEncoder.matches(request.password(), userAccount.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(jwtProperties.getExpirationMinutes(), ChronoUnit.MINUTES);
        UUID tokenId = UUID.randomUUID();

        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
            .id(tokenId.toString())
            .issuer("healthsys-identity")
            .issuedAt(issuedAt)
            .expiresAt(expiresAt)
            .subject(userAccount.getId().toString())
            .claim("email", userAccount.getEmail())
            .claim("name", userAccount.getName())
            .claim("roles", List.of(userAccount.getRole().name()))
            .build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(
            JwsHeader.with(MacAlgorithm.HS256).build(),
            claimsSet
        )).getTokenValue();

        UserResponse userResponse = userService.toResponse(userAccount);
        return new AuthResponse(token, "Bearer", expiresAt, userResponse);
    }

    @Transactional
    public LogoutResponse logout(Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        UUID tokenId = UUID.fromString(jwt.getId());
        Instant expiresAt = jwt.getExpiresAt();

        tokenRevocationService.revokeToken(tokenId, userId, expiresAt);
        identityEventPublisher.publishTokenRevoked(userId.toString(), tokenId.toString(), expiresAt);

        return new LogoutResponse("Logout completed and token revoked.");
    }

    public MeResponse me(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        UserRole role = UserRole.valueOf(roles.get(0));

        return new MeResponse(
            UUID.fromString(jwt.getSubject()),
            jwt.getClaimAsString("name"),
            jwt.getClaimAsString("email"),
            role
        );
    }
}
