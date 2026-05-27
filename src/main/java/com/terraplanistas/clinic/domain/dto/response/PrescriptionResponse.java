package com.terraplanistas.clinic.domain.dto.response;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record PrescriptionResponse(
    UUID id,
    UUID appointmentId,
    UUID medicineId,
    Map<String, Object> medicineSnapshot,
    String dosageInstructions,
    String digitalSignature,
    Integer usageCount,
    Integer maxUsages,
    OffsetDateTime createdAt,
    UUID createdBy
) {}