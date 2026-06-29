package com.terraplanistas.clinic.domain.dto.response;

import com.terraplanistas.clinic.domain.entities.Employee;
import com.terraplanistas.clinic.domain.entities.User;
import com.terraplanistas.clinic.domain.enums.IdType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "DTO de respuesta con la información detallada de un empleado para el panel administrativo")
public record AdminEmployeeResponse(
        @Schema(description = "Identificador único universal del empleado",
                example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,

        @Schema(description = "Nombres del empleado",
                example = "Juan Carlos")
        String firstName,

        @Schema(description = "Apellidos del empleado",
                example = "Pérez González")
        String lastName,

        @Schema(description = "Número de documento de identidad",
                example = "1234567890")
        String idNumber,

        @Schema(description = "Tipo de documento de identidad",
                example = "CC")
        IdType idType,

        @Schema(description = "Dirección de residencia registrada",
                example = "Calle 123 #45-67, Bogotá")
        String address,

        @Schema(description = "Números de teléfono de contacto",
                example = "+573001234567, +576017654321")
        String phones,

        @Schema(description = "Indicador de estado activo del registro de empleado",
                example = "true")
        Boolean isActive,

        @Schema(description = "Identificador único universal del usuario asociado en el sistema de autenticación",
                example = "660e8400-e29b-41d4-a716-446655440001")
        UUID userId,

        @Schema(description = "Correo electrónico institucional del empleado",
                example = "juan.perez@clinica.com")
        String email,

        @Schema(description = "Código del rol asignado al empleado en el sistema",
                example = "DOCTOR")
        String roleCode,

        @Schema(description = "Estado del acceso del usuario al sistema. Valores posibles: 'active' (activo), 'revoked' (revocado)",
                example = "active")
        String status,

        @Schema(description = "Fecha y hora de eliminación lógica del usuario. Nulo si el usuario no ha sido eliminado",
                example = "2024-01-15T14:30:00-05:00",
                nullable = true)
        OffsetDateTime deletedAt
) {
    public static AdminEmployeeResponse from(Employee employee) {
        User user = employee.getUser();
        String status;
        if (user.isAccessRevoked() || user.getDeletedAt() != null) {
            status = "revoked";
        } else {
            status = "active";
        }

        return new AdminEmployeeResponse(
                employee.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getIdNumber(),
                employee.getIdType(),
                employee.getAddress(),
                employee.getPhones(),
                employee.getIsActive(),
                user.getId(),
                user.getEmail(),
                user.getRole().getCode(),
                status,
                user.getDeletedAt()
        );
    }
}