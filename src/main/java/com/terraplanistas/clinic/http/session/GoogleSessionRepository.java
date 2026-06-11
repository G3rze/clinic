package com.terraplanistas.clinic.http.session;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GoogleSessionRepository extends JpaRepository<GoogleSession, UUID> {

    Optional<GoogleSession> findByGoogleUserId(String googleUserId);

    Optional<GoogleSession> findByUserId(UUID userId);

    List<GoogleSession> findByExpiresAtBefore(OffsetDateTime threshold);

    void deleteByGoogleUserId(String googleUserId);

    @Query("SELECT s FROM GoogleSession s WHERE s.userId = :userId AND s.expiresAt > :now")
    Optional<GoogleSession> findActiveSessionByUserId(@Param("userId") UUID userId, @Param("now") OffsetDateTime now);

    @Query("SELECT s FROM GoogleSession s WHERE s.googleUserId = :googleUserId AND s.expiresAt > :now")
    Optional<GoogleSession> findActiveSessionByGoogleUserId(@Param("googleUserId") String googleUserId, @Param("now") OffsetDateTime now);

    List<GoogleSession> findByExpiresAtBeforeAndUserIdIsNull(OffsetDateTime threshold);
}