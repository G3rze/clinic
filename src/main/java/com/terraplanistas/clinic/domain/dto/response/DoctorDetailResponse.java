package com.terraplanistas.clinic.domain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.OffsetTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "DTO con la información completa de un médico, incluyendo sus especialidades " +
        "y disponibilidad horaria. Utilizado para el perfil detallado del profesional")
public record DoctorDetailResponse(
        @Schema(description = "UUID del empleado (médico)",
                example = "660e8400-e29b-41d4-a716-446655440001")
        UUID id,

        @Schema(description = "UUID del usuario asociado al médico en el sistema de autenticación",
                example = "550e8400-e29b-41d4-a716-446655440000")
        UUID userId,

        @Schema(description = "Nombres del médico",
                example = "Carlos Alberto")
        String firstName,

        @Schema(description = "Apellidos del médico",
                example = "Rodríguez Mendoza")
        String lastName,

        @Schema(description = "Correo electrónico institucional del médico",
                example = "carlos.rodriguez@clinica.com")
        String email,

        @Schema(description = "Código del rol asignado en el sistema",
                example = "DOCTOR")
        String roleCode,

        @Schema(description = "Estado del acceso del médico al sistema: 'active' o 'revoked'",
                example = "active")
        String status,

        @Schema(description = "Números de teléfono de contacto del médico",
                example = "+573001234567, +576017654321")
        String phones,

        @Schema(description = "Dirección de residencia o consultorio del médico",
                example = "Carrera 15 #98-45, Consultorio 302, Bogotá")
        String address,

        @Schema(description = "Lista de especialidades médicas que ejerce el médico con sus respectivas " +
                "configuraciones y disponibilidad horaria")
        List<SpecialtyDetail> specialties
) {
    @Schema(description = "Información detallada de una especialidad ejercida por el médico")
    public record SpecialtyDetail(
            @Schema(description = "UUID de la especialidad médica",
                    example = "880e8400-e29b-41d4-a716-446655440003")
            UUID specialtyId,

            @Schema(description = "Código único de la especialidad",
                    example = "CARDIOLOGY")
            String code,

            @Schema(description = "Nombre de la especialidad en español",
                    example = "Cardiología")
            String name,

            @Schema(description = "Número de licencia profesional del médico para esta especialidad",
                    example = "CAR-2020-12345")
            String professionalLicenseNumber,

            @Schema(description = "Tarifa por hora de consulta para esta especialidad",
                    example = "200.00")
            BigDecimal feePerHour,

            @Schema(description = "Duración estándar de consulta en minutos para esta especialidad",
                    example = "45")
            Integer consultDurationMinutes,

            @Schema(description = "Franjas horarias semanales en las que el médico atiende esta especialidad")
            List<AvailabilitySlot> availabilities
    ) {}

    @Schema(description = "Franja horaria de disponibilidad semanal del médico")
    public record AvailabilitySlot(
            @Schema(description = "UUID de la franja de disponibilidad",
                    example = "990e8400-e29b-41d4-a716-446655440004")
            UUID id,

            @Schema(description = "Día de la semana de la franja horaria",
                    example = "MONDAY")
            DayOfWeek dayOfWeek,

            @Schema(description = "Hora de inicio de la franja",
                    example = "08:00:00-05:00")
            OffsetTime startTime,

            @Schema(description = "Hora de finalización de la franja",
                    example = "12:00:00-05:00")
            OffsetTime endTime
    ) {}
}