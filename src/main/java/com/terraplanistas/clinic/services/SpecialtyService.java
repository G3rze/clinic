package com.terraplanistas.clinic.services;

import com.terraplanistas.clinic.domain.entities.Specialty;

import java.util.List;
import java.util.UUID;

public interface SpecialtyService {

    List<Specialty> getAllSpecialties();

    Specialty getSpecialtyById(UUID id);

    Specialty createSpecialty(String code, String name, UUID currentUserId);

    void deleteSpecialty(UUID id);
}