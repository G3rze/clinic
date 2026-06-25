package com.terraplanistas.clinic.repositories;

import com.terraplanistas.clinic.domain.entities.PendingUserConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PendingUserConfigRepository extends JpaRepository<PendingUserConfig, UUID> {

    Optional<PendingUserConfig> findByGoogleUserId(String googleUserId);

    void deleteByGoogleUserId(String googleUserId);
}
