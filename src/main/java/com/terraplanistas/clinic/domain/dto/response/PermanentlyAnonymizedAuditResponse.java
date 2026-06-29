package com.terraplanistas.clinic.domain.dto.response;

import com.terraplanistas.clinic.domain.entities.User;

import java.time.OffsetDateTime;
import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "PermanentlyAnonymizedAuditResponse",
        description = "Información de auditoría correspondiente a un usuario anonimizado permanentemente."
)
public record PermanentlyAnonymizedAuditResponse(

        @Schema(
                description = "Identificador único del usuario.",
                example = "550e8400-e29b-41d4-a716-446655440000"
        )
        UUID userId,

        @Schema(
                description = "Fecha y hora en que el usuario fue eliminado de forma lógica.",
                example = "2026-05-20T14:32:10Z"
        )
        OffsetDateTime deletedAt,

        @Schema(
                description = "Identificador del administrador que realizó la eliminación lógica.",
                example = "6f7f4e3f-2d0c-4c57-b19b-60f72a39bc52"
        )
        UUID deletedBy,

        @Schema(
                description = "Fecha y hora en que la información del usuario fue anonimizada permanentemente.",
                example = "2026-06-19T09:15:22Z"
        )
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
