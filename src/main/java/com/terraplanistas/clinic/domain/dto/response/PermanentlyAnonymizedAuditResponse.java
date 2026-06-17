package com.terraplanistas.clinic.domain.dto.response;

import com.terraplanistas.clinic.domain.entities.User;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PermanentlyAnonymizedAuditResponse(
    UUID userId,
    OffsetDateTime deletedAt,
    UUID deletedBy,
    OffsetDateTime anonymizationPermanentAt
) {
    public static PermanentlyAnonymizedAuditResponse from(User user) {
        return new PermanentlyAnonymizedAuditResponse(
                user.getId(),
                user.getDeletedAt(),
                user.getDeletedBy(),
                user.getAnonymizationPermanentAt()
        );
    }
}
