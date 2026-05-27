package com.terraplanistas.clinic.domain.dto.response;

import com.terraplanistas.clinic.domain.enums.AppointmentStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AppointmentResponse(
    UUID id,
    String googleEventId,
    AppointmentStatus status,
    BigDecimal finalFeePerHour,
    Integer score,
    String review,
    OffsetDateTime registeredAt,
    OffsetDateTime expectedAt,
    UUID employeeId,
    UUID patientId,
    UUID patientCallerUserId
) {}