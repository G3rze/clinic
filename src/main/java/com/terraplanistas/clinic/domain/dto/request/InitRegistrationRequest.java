package com.terraplanistas.clinic.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

@Schema(description = "DTO para iniciar el proceso de registro de un nuevo paciente en el sistema. " +
        "Contiene los datos básicos de identificación obtenidos del proveedor de autenticación (Google)")
public record InitRegistrationRequest(
        @NotBlank(message = "Google user ID is required")
        @Schema(description = "Identificador único del usuario proporcionado por Google (atributo 'sub' del token OAuth2). " +
                "Este ID es inmutable y sirve como referencia principal para vincular la cuenta de Google " +
                "con el perfil del paciente en el sistema",
                example = "123456789012345678901",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String googleUserId,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Schema(description = "Dirección de correo electrónico del usuario asociada a su cuenta de Google. " +
                "Se utilizará como medio de contacto principal para notificaciones del sistema",
                example = "paciente@ejemplo.com",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String email,

        @Schema(description = "Nombre completo del usuario obtenido de su perfil de Google. " +
                "Puede ser modificado posteriormente durante la compleción del perfil",
                example = "María González López",
                nullable = true)
        String name,

        @NotNull(message = "Birthdate is required")
        @Past(message = "Birthdate must be in the past")
        @Schema(description = "Fecha de nacimiento del paciente. Debe ser una fecha anterior a la actual. " +
                "Se utiliza para calcular la edad del paciente y validar restricciones de edad " +
                "para ciertos servicios médicos",
                example = "1985-03-15",
                requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDate birthdate
) {}