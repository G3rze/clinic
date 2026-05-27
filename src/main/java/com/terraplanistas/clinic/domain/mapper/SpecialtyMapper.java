package com.terraplanistas.clinic.domain.mapper;

import com.terraplanistas.clinic.domain.dto.request.SpecialtyRequest;
import com.terraplanistas.clinic.domain.dto.response.SpecialtyResponse;
import com.terraplanistas.clinic.domain.entities.Specialty;

public class SpecialtyMapper {

    public static Specialty toEntity(SpecialtyRequest request) {
        Specialty specialty = new Specialty();
        specialty.setCode(request.code());
        specialty.setName(request.name());
        return specialty;
    }

    public static Specialty toUpgrade(SpecialtyRequest request, Specialty specialty) {
        specialty.setCode(request.code());
        specialty.setName(request.name());
        return specialty;
    }

    public static SpecialtyResponse toResponse(Specialty specialty) {
        return new SpecialtyResponse(
            specialty.getId(),
            specialty.getCode(),
            specialty.getName()
        );
    }
}