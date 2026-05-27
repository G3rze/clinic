package com.terraplanistas.clinic.domain.dto.response;

import com.terraplanistas.clinic.domain.enums.IdType;
import java.util.UUID;

public record EmployeeResponse(
    UUID id,
    String firstName,
    String lastName,
    String idNumber,
    IdType idType,
    String address,
    String phones,
    Boolean isActive,
    UUID userId
) {}