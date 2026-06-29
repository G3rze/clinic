package com.terraplanistas.clinic.domain.dto.response;

import com.terraplanistas.clinic.domain.enums.AppointmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "DTO de respuesta con la información completa y detallada de una cita médica, " +
        "incluyendo participantes, evento de Google Calendar, enlace de videollamada y datos de facturación")
public record AppointmentResponse(
        @Schema(description = "Identificador único universal de la cita médica",
                example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,

        @Schema(description = "Estado actual de la cita en su ciclo de vida. Valores posibles: " +
                "PENDING (pendiente de pago), " +
                "CONFIRMED (confirmada y paga), " +
                "CANCELLED (cancelada), " +
                "COMPLETED (atendida), " +
                "NO_SHOW (paciente no asistió)",
                example = "CONFIRMED")
        AppointmentStatus status,

        @Schema(description = "Tarifa final por hora de consulta después de aplicar descuentos, " +
                "impuestos y ajustes. Representa el valor facturado al paciente",
                example = "120.00")
        BigDecimal finalFeePerHour,

        @Schema(description = "Calificación otorgada por el paciente a la atención recibida. " +
                "Escala de 1 a 5, donde 5 es la máxima satisfacción. Nulo si no se ha calificado",
                example = "5",
                nullable = true)
        Integer score,

        @Schema(description = "Reseña o comentario del paciente sobre la consulta. " +
                "Texto libre que acompaña a la calificación numérica",
                example = "Excelente atención, el doctor fue muy profesional y resolvió todas mis dudas.",
                nullable = true)
        String review,

        @Schema(description = "Fecha y hora exacta en que se registró la cita en el sistema",
                example = "2026-06-15T10:30:00-05:00")
        OffsetDateTime registeredAt,

        @Schema(description = "Fecha y hora programada para la realización de la consulta médica",
                example = "2026-06-22T15:00:00-05:00")
        OffsetDateTime expectedAt,

        @Schema(description = "UUID del empleado (médico) asignado a la cita",
                example = "660e8400-e29b-41d4-a716-446655440001")
        UUID employeeId,

        @Schema(description = "UUID del paciente registrado en el sistema. Nulo si es un paciente nuevo o invitado",
                example = "770e8400-e29b-41d4-a716-446655440002",
                nullable = true)
        UUID patientId,

        @Schema(description = "UUID del usuario que realizó la llamada o agendamiento en nombre del paciente. " +
                "Aplica cuando un tercero agenda la cita (familiar, administrativo, etc.)",
                example = "880e8400-e29b-41d4-a716-446655440003",
                nullable = true)
        UUID patientCallerUserId,

        @Schema(description = "Información del evento sincronizado en Google Calendar. " +
                "Contiene el ID del evento y el enlace de Google Meet si está configurado",
                nullable = true)
        GoogleEventInfoResponse eventInfo,

        @Schema(description = "Lista de participantes registrados para la cita en el momento del agendamiento. " +
                "Incluye al paciente, médico y posibles acompañantes o intérpretes")
        List<AppointmentParticipantResponse> applicationParticipants,

        @Schema(description = "Lista de participantes que efectivamente se conectaron a la videollamada. " +
                "Registra la asistencia real a la consulta virtual")
        List<AppointmentParticipantResponse> meetParticipants,

        @Schema(description = "Nombre completo del médico asignado a la cita en formato 'Nombre Apellido'",
                example = "Dr. Carlos Rodríguez Mendoza")
        String doctorName,

        @Schema(description = "Nombre completo del paciente en formato 'Nombre Apellido'",
                example = "María González López")
        String patientName,

        @Schema(description = "Enlace de acceso a la videollamada de la consulta (Google Meet, Zoom, etc.). " +
                "Disponible solo para citas confirmadas y en modalidad virtual",
                example = "https://meet.google.com/abc-defg-hij",
                nullable = true)
        String meetingLink,

        @Schema(description = "Nombre legible del estado de la cita en español. " +
                "Valores: 'Pendiente', 'Confirmada', 'Cancelada', 'Completada', 'No asistió'",
                example = "Confirmada")
        String statusName
) {}