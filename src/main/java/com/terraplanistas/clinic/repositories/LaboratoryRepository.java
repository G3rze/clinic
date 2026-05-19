package com.terraplanistas.clinic.repositories;

import com.terraplanistas.clinic.domain.entities.Laboratory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LaboratoryRepository extends JpaRepository<Laboratory, UUID> {
    Optional<Laboratory> findByName(String name);
}