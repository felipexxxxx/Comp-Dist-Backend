package com.healthsys.identity.user.repository;

import com.healthsys.identity.user.domain.UserAccount;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserAccount, UUID> {

    Optional<UserAccount> findByEmailIgnoreCase(String email);

    List<UserAccount> findAllByOrderByCreatedAtDesc();
}
