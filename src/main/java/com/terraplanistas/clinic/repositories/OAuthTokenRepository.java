package com.terraplanistas.clinic.repositories;

import com.terraplanistas.clinic.domain.entities.OAuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OAuthTokenRepository extends JpaRepository<OAuthToken, UUID> {
    Optional<OAuthToken> findByGoogleUserId(String googleUserId);
}
