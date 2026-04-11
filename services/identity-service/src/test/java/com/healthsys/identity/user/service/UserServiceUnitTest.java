package com.healthsys.identity.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.healthsys.identity.messaging.IdentityEventPublisher;
import com.healthsys.identity.user.domain.UserAccount;
import com.healthsys.identity.user.domain.UserRole;
import com.healthsys.identity.user.dto.CreateUserRequest;
import com.healthsys.identity.user.dto.UserResponse;
import com.healthsys.identity.user.repository.UserRepository;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private IdentityEventPublisher identityEventPublisher;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder, identityEventPublisher);
    }

    @Test
    void shouldCreateUserWithProtectedPassword() {
        CreateUserRequest request = new CreateUserRequest(
            "Reception User",
            "reception@healthsys.local",
            "Recepcao@123",
            UserRole.RECEPTIONIST
        );
        UserAccount savedUser = UserAccount.builder()
            .id(UUID.randomUUID())
            .name("Reception User")
            .email("reception@healthsys.local")
            .passwordHash("encoded-password")
            .role(UserRole.RECEPTIONIST)
            .active(true)
            .createdAt(OffsetDateTime.now())
            .updatedAt(OffsetDateTime.now())
            .build();

        when(userRepository.findByEmailIgnoreCase("reception@healthsys.local")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Recepcao@123")).thenReturn("encoded-password");
        when(userRepository.save(any(UserAccount.class))).thenReturn(savedUser);

        UserResponse response = userService.createUser(request);

        assertThat(response.email()).isEqualTo("reception@healthsys.local");
        assertThat(response.role()).isEqualTo(UserRole.RECEPTIONIST);
        verify(identityEventPublisher).publishUserCreated(savedUser);
    }

    @Test
    void shouldRejectDuplicateEmail() {
        when(userRepository.findByEmailIgnoreCase("admin@healthsys.local"))
            .thenReturn(Optional.of(UserAccount.builder().email("admin@healthsys.local").build()));

        assertThatThrownBy(() -> userService.createUser(new CreateUserRequest(
                "Admin",
                "admin@healthsys.local",
                "Admin@123",
                UserRole.ADMIN
            )))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("A user with this email already exists.");
    }
}
