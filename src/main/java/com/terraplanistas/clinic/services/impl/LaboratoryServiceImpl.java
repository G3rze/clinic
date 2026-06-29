package com.terraplanistas.clinic.services.impl;

import com.terraplanistas.clinic.domain.dto.response.LaboratoryResponse;
import com.terraplanistas.clinic.domain.entities.Laboratory;
import com.terraplanistas.clinic.repositories.LaboratoryRepository;
import com.terraplanistas.clinic.services.LaboratoryService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LaboratoryServiceImpl implements LaboratoryService {

    private final LaboratoryRepository laboratoryRepository;

    public LaboratoryServiceImpl(LaboratoryRepository laboratoryRepository) {
        this.laboratoryRepository = laboratoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LaboratoryResponse> getAllLaboratories() {
        return laboratoryRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public LaboratoryResponse getLaboratoryById(UUID id) {
        return laboratoryRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Laboratory not found: " + id));
    }

    @Override
    @Transactional
    public LaboratoryResponse createLaboratory(String name, UUID currentUserId) {
        if (laboratoryRepository.findByName(name).isPresent()) {
            throw new IllegalArgumentException("Laboratory with this name already exists");
        }

        Laboratory laboratory = new Laboratory();
        laboratory.setName(name);
        laboratory.setCreatedBy(currentUserId);
        laboratory.setUpdatedBy(currentUserId);

        return toResponse(laboratoryRepository.save(laboratory));
    }

    @Override
    @Transactional
    public LaboratoryResponse updateLaboratory(UUID id, String name, UUID currentUserId) {
        Laboratory laboratory = laboratoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Laboratory not found: " + id));

        if (!laboratory.getName().equals(name) && laboratoryRepository.findByName(name).isPresent()) {
            throw new IllegalArgumentException("Laboratory with this name already exists");
        }

        laboratory.setName(name);
        laboratory.setUpdatedBy(currentUserId);

        return toResponse(laboratoryRepository.save(laboratory));
    }

    @Override
    @Transactional
    public void deleteLaboratory(UUID id) {
        Laboratory laboratory = laboratoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Laboratory not found: " + id));
        laboratoryRepository.delete(laboratory);
    }

    private LaboratoryResponse toResponse(Laboratory laboratory) {
        return new LaboratoryResponse(
                laboratory.getId(),
                laboratory.getName()
        );
    }
}