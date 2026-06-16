package com.terraplanistas.clinic.repositories;

import com.terraplanistas.clinic.domain.entities.Specialty;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface SpecialtyRepository extends JpaRepository<Specialty, UUID> {
    Optional<Specialty> findByCode(String code);
    Optional<Specialty> findByName(String name);
}