package com.terraplanistas.clinic.domain.dto.response;

import java.util.Map;
import java.util.UUID;

public record MedicineResponse(
    UUID id,
    UUID laboratoryId,
    String brandName,
    String genericName,
    Map<String, Object> composition,
    String useFrom,
    String atcCode
) {}