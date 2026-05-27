package com.terraplanistas.clinic.domain.mapper;

import com.terraplanistas.clinic.domain.dto.request.LaboratoryRequest;
import com.terraplanistas.clinic.domain.dto.response.LaboratoryResponse;
import com.terraplanistas.clinic.domain.entities.Laboratory;

public class LaboratoryMapper {

    public static Laboratory toEntity(LaboratoryRequest request) {
        Laboratory laboratory = new Laboratory();
        laboratory.setName(request.name());
        return laboratory;
    }

    public static Laboratory toUpgrade(LaboratoryRequest request, Laboratory laboratory) {
        laboratory.setName(request.name());
        return laboratory;
    }

    public static LaboratoryResponse toResponse(Laboratory laboratory) {
        return new LaboratoryResponse(
            laboratory.getId(),
            laboratory.getName()
        );
    }
}