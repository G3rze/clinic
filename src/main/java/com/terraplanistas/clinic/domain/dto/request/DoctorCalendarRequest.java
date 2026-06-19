package com.terraplanistas.clinic.domain.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DoctorCalendarRequest(
        @NotNull
        UUID specialtyId,

        @NotNull
        OffsetDateTime from,

        @NotNull
        OffsetDateTime to
) {
}