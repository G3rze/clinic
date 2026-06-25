package com.terraplanistas.clinic.domain.dto.response;

import java.util.List;
import java.util.UUID;

public record PublicDoctorResponse(
    UUID id,
    String firstName,
    String lastName,
    String roleCode,
    List<PublicSpecialtyResponse> specialties
) {}