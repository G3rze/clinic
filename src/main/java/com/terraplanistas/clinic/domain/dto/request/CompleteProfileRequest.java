package com.terraplanistas.clinic.domain.dto.request;

import com.terraplanistas.clinic.domain.enums.IdType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Schema(description = "DTO para completar el perfil del paciente durante el proceso de registro. " +
        "Contiene los datos personales requeridos para finalizar la configuración de la cuenta")
public record CompleteProfileRequest(
        @NotBlank(message = "First name is required")
        @Schema(description = "Nombres completos del paciente",
                example = "María Fernanda",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String firstName,

        @NotBlank(message = "Last name is required")
        @Schema(description = "Apellidos completos del paciente",
                example = "González López",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String lastName,

        @NotNull(message = "ID type is required")
        @Schema(description = "Tipo de documento de identidad. Valores: CC (Cédula de Ciudadanía), " +
                "TI (Tarjeta de Identidad), CE (Cédula de Extranjería), PASSPORT (Pasaporte)",
                example = "CC",
                requiredMode = Schema.RequiredMode.REQUIRED)
        IdType idType,

        @NotBlank(message = "ID number is required")
        @Pattern(regexp = "\\d{9}", message = "DUI must be exactly 9 digits")
        @Schema(description = "Número de documento de identidad. Debe contener exactamente 9 dígitos numéricos",
                example = "123456789",
                pattern = "\\d{9}",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String idNumber,

        @NotBlank(message = "Address is required")
        @Schema(description = "Dirección de residencia completa del paciente",
                example = "Calle 123 #45-67, Barrio Centro, Bogotá D.C.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String address,

        @Schema(description = "Números de teléfono de contacto del paciente. Pueden ser múltiples separados por coma",
                example = "+573001234567, +576017654321",
                nullable = true)
        String phones
) {}