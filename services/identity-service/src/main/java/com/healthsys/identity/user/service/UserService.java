package com.healthsys.identity.user.service;

import com.healthsys.identity.messaging.IdentityEventPublisher;
import com.healthsys.identity.user.domain.UserAccount;
import com.healthsys.identity.user.dto.CreateUserRequest;
import com.healthsys.identity.user.dto.UserResponse;
import com.healthsys.identity.user.repository.UserRepository;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final IdentityEventPublisher identityEventPublisher;

    public UserService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        IdentityEventPublisher identityEventPublisher
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.identityEventPublisher = identityEventPublisher;
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        userRepository.findByEmailIgnoreCase(normalizedEmail).ifPresent(existing -> {
            throw new IllegalArgumentException("A user with this email already exists.");
        });

        UserAccount userAccount = UserAccount.builder()
            .name(request.name().trim())
            .email(normalizedEmail)
            .passwordHash(passwordEncoder.encode(request.password()))
            .role(request.role())
            .active(true)
            .build();

        UserAccount savedUser = userRepository.save(userAccount);
        identityEventPublisher.publishUserCreated(savedUser);
        return toResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listUsers() {
        return userRepository.findAllByOrderByCreatedAtDesc()
            .stream()
            .map(this::toResponse)
            .toList();
    }

    public UserResponse toResponse(UserAccount userAccount) {
        return new UserResponse(
            userAccount.getId(),
            userAccount.getName(),
            userAccount.getEmail(),
            userAccount.getRole(),
            userAccount.isActive(),
            userAccount.getCreatedAt()
        );
    }
}
