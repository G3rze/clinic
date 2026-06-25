package com.terraplanistas.clinic.services.impl;

import com.terraplanistas.clinic.domain.entities.Specialty;
import com.terraplanistas.clinic.repositories.SpecialtyRepository;
import com.terraplanistas.clinic.services.SpecialtyService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class SpecialtyServiceImpl implements SpecialtyService {

    private final SpecialtyRepository specialtyRepository;

    public SpecialtyServiceImpl(SpecialtyRepository specialtyRepository) {
        this.specialtyRepository = specialtyRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Specialty> getAllSpecialties() {
        return specialtyRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Specialty getSpecialtyById(UUID id) {
        return specialtyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Specialty not found: " + id));
    }

    @Override
    @Transactional
    public Specialty createSpecialty(String code, String name, UUID currentUserId) {
        if (specialtyRepository.findByCode(code).isPresent()) {
            throw new IllegalArgumentException("Specialty with this code already exists");
        }
        if (specialtyRepository.findByName(name).isPresent()) {
            throw new IllegalArgumentException("Specialty with this name already exists");
        }

        Specialty specialty = new Specialty();
        specialty.setCode(code);
        specialty.setName(name);
        specialty.setCreatedBy(currentUserId);
        specialty.setUpdatedBy(currentUserId);

        return specialtyRepository.save(specialty);
    }

    @Override
    @Transactional
    public void deleteSpecialty(UUID id) {
        Specialty specialty = getSpecialtyById(id);
        specialtyRepository.delete(specialty);
    }
}