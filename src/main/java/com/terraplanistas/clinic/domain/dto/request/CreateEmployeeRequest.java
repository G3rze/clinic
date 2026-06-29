package com.terraplanistas.clinic.domain.dto.request;

import com.terraplanistas.clinic.domain.enums.IdType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "DTO para la creación de un nuevo empleado en el sistema")
public record CreateEmployeeRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Schema(description = "Correo electrónico institucional del empleado",
                example = "juan.perez@clinica.com",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String email,

        @NotBlank(message = "First name is required")
        @Schema(description = "Nombres del empleado",
                example = "Juan Carlos",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String firstName,

        @NotBlank(message = "Last name is required")
        @Schema(description = "Apellidos del empleado",
                example = "Pérez González",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String lastName,

        @NotBlank(message = "ID number is required")
        @Schema(description = "Número de documento de identidad del empleado",
                example = "1234567890",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String idNumber,

        @NotNull(message = "ID type is required")
        @Schema(description = "Tipo de documento de identidad del empleado",
                example = "CC",
                requiredMode = Schema.RequiredMode.REQUIRED)
        IdType idType,

        @Schema(description = "Dirección de residencia del empleado",
                example = "Calle 123 #45-67, Bogotá")
        String address,

        @Schema(description = "Números de teléfono de contacto del empleado",
                example = "+573001234567, +576017654321")
        String phones,

        @Schema(description = "Código del rol que desempeñará el empleado en el sistema",
                example = "DOCTOR")
        String role
) {}