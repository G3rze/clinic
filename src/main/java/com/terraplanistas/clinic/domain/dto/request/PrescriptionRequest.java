package com.terraplanistas.clinic.domain.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.UUID;

public record PrescriptionRequest(
    @NotNull(message = "Appointment ID is required")
    UUID appointmentId,

    @NotNull(message = "Medicine ID is required")
    UUID medicineId,

    @NotNull(message = "Medicine snapshot is required")
    Map<String, Object> medicineSnapshot,

    @NotBlank(message = "Dosage instructions are required")
    String dosageInstructions,

    @NotBlank(message = "Digital signature is required")
    String digitalSignature,

    @Min(value = 0, message = "Usage count cannot be negative")
    Integer usageCount,

    @Min(value = 1, message = "Max usages must be at least 1")
    @Max(value = 3, message = "Max usages cannot exceed 3")
    Integer maxUsages
) {}