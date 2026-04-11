package com.healthsys.identity.auth.revocation;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, UUID> {

    boolean existsByTokenIdAndExpiresAtAfter(UUID tokenId, OffsetDateTime cutoff);

    boolean existsByTokenId(UUID tokenId);
}
