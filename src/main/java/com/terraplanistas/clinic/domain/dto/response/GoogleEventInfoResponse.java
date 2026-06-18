package com.terraplanistas.clinic.domain.dto.response;

import java.time.OffsetDateTime;

public record GoogleEventInfoResponse(
    String eventId,
    String summary,
    String description,
    String location,
    OffsetDateTime startTime,
    OffsetDateTime endTime,
    String meetLink
) {}
