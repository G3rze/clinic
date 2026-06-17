package com.terraplanistas.clinic.repositories;

import com.terraplanistas.clinic.domain.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByCode(String code);
    Optional<Role> findByName(String name);
}