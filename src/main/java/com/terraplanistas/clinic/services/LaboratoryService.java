package com.terraplanistas.clinic.services;

import com.terraplanistas.clinic.domain.dto.response.LaboratoryResponse;

import java.util.List;
import java.util.UUID;

public interface LaboratoryService {

    List<LaboratoryResponse> getAllLaboratories();

    LaboratoryResponse getLaboratoryById(UUID id);

    LaboratoryResponse createLaboratory(String name, UUID currentUserId);

    LaboratoryResponse updateLaboratory(UUID id, String name, UUID currentUserId);

    void deleteLaboratory(UUID id);
}