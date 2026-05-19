package com.terraplanistas.clinic.repositories;

import com.terraplanistas.clinic.domain.entities.Specialty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpecialtyRepository extends JpaRepository<Specialty, UUID> {
    Optional<Specialty> findByCode(String code);
    Optional<Specialty> findByName(String name);
}