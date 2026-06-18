package com.terraplanistas.clinic.domain.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetTime;
import java.util.UUID;

public record AvailableSlotResponse(
    UUID doctorId,
    String doctorFirstName,
    String doctorLastName,
    String specialtyCode,
    String specialtyName,
    LocalDate date,
    OffsetTime startTime,
    OffsetTime endTime,
    BigDecimal feePerHour,
    Integer consultDurationMinutes
) {}
