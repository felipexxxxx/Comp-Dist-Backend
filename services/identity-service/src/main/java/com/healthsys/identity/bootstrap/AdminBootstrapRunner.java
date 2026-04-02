package com.healthsys.identity.bootstrap;

import com.healthsys.identity.config.BootstrapAdminProperties;
import com.healthsys.identity.user.domain.UserAccount;
import com.healthsys.identity.user.domain.UserRole;
import com.healthsys.identity.user.repository.UserRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminBootstrapRunner {

    @Bean
    public ApplicationRunner bootstrapAdmin(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        BootstrapAdminProperties properties
    ) {
        return args -> {
            String email = properties.getAdminEmail().trim().toLowerCase();

            userRepository.findByEmailIgnoreCase(email).orElseGet(() -> {
                UserAccount admin = UserAccount.builder()
                    .name(properties.getAdminName().trim())
                    .email(email)
                    .passwordHash(passwordEncoder.encode(properties.getAdminPassword()))
                    .role(UserRole.ADMIN)
                    .active(true)
                    .build();
                return userRepository.save(admin);
            });
        };
    }
}
