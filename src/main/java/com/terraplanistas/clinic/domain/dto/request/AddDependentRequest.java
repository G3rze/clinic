package com.terraplanistas.clinic.domain.dto.request;

import com.terraplanistas.clinic.domain.enums.IdType;
import com.terraplanistas.clinic.domain.enums.RelationshipType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "DTO para registrar un paciente dependiente asociado a un usuario titular. " +
        "Utilizado para agregar familiares (hijos, cónyuges, adultos mayores a cargo) " +
        "al perfil del paciente principal")
public record AddDependentRequest(
        @NotBlank(message = "First name is required")
        @Schema(description = "Nombres del paciente dependiente",
                example = "Ana María",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String firstName,

        @NotBlank(message = "Last name is required")
        @Schema(description = "Apellidos del paciente dependiente",
                example = "González López",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String lastName,

        @NotBlank(message = "ID number is required")
        @Pattern(regexp = "\\d{9}", message = "ID number must be exactly 9 digits")
        @Schema(description = "Número de documento de identidad del dependiente. Debe contener exactamente 9 dígitos",
                example = "123456789",
                pattern = "\\d{9}",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String idNumber,

        @NotNull(message = "ID type is required")
        @Schema(description = "Tipo de documento de identidad del dependiente",
                example = "CC",
                requiredMode = Schema.RequiredMode.REQUIRED)
        IdType idType,

        @NotNull(message = "Birthdate is required")
        @Past(message = "Birthdate must be in the past")
        @Schema(description = "Fecha de nacimiento del dependiente en formato ISO (YYYY-MM-DD). " +
                "Debe ser una fecha pasada",
                example = "2020-03-15",
                requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDate birthdate,

        @Schema(description = "Dirección de residencia del dependiente. Opcional, puede heredar la del titular",
                example = "Calle 123 #45-67, Bogotá",
                nullable = true)
        String address,

        @Schema(description = "Números de teléfono de contacto del dependiente",
                example = "+573001234567",
                nullable = true)
        String phones,

        @NotNull(message = "Relationship type is required")
        @Schema(description = "Tipo de relación o parentesco entre el titular y el dependiente. " +
                "Valores: CHILD (hijo/a), SPOUSE (cónyuge), PARENT (padre/madre), " +
                "SIBLING (hermano/a), OTHER (otro)",
                example = "CHILD",
                requiredMode = Schema.RequiredMode.REQUIRED)
        RelationshipType relationshipType,

        @Schema(description = "UUID del paciente representante legal cuando el dependiente es menor de edad " +
                "o requiere un tutor legal. Opcional si el titular es el representante",
                example = "770e8400-e29b-41d4-a716-446655440002",
                nullable = true)
        UUID representativeId
) {}