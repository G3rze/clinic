package com.terraplanistas.clinic.controllers;

import com.terraplanistas.clinic.domain.dto.request.RateAppointmentRequest;
import com.terraplanistas.clinic.domain.dto.response.ApiResponse;
import com.terraplanistas.clinic.domain.dto.response.AppointmentResponse;
import com.terraplanistas.clinic.services.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("${app.base-uri}/appointments")
@Tag(name = "Calificación de Citas Médicas",
        description = "Endpoints para la evaluación de la calidad de las consultas médicas por parte de los pacientes. " +
                "Permite registrar calificaciones numéricas y reseñas detalladas sobre la experiencia " +
                "de atención recibida, contribuyendo al sistema de reputación de los profesionales de la salud.")
@SecurityRequirement(name = "bearerAuth")
public class AppointmentRatingController {

    private final AppointmentService appointmentService;

    public AppointmentRatingController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @Operation(
            summary = "Calificar y reseñar una consulta médica",
            description = "Registra la evaluación de un paciente sobre una cita médica ya finalizada. " +
                    "Este endpoint permite asignar una puntuación numérica en escala de 1 a 5 estrellas " +
                    "y, opcionalmente, escribir una reseña con observaciones detalladas sobre la experiencia."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Calificación y reseña registradas correctamente",
                    content = @Content(schema = @Schema(implementation = AppointmentResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "No autenticado"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Acceso denegado"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Cita no encontrada"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Conflicto - La cita ya fue calificada o no está COMPLETED"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "422",
                    description = "Regla de negocio violada"
            )
    })
    @PatchMapping("/{appointmentId}/rating")
    public ResponseEntity<ApiResponse<AppointmentResponse>> rateAppointment(
            @PathVariable
            @Parameter(description = "UUID de la cita médica a calificar",
                    example = "550e8400-e29b-41d4-a716-446655440000")
            UUID appointmentId,
            @Valid @RequestBody
            @Parameter(description = "Puntuación (1-5) y reseña opcional")
            RateAppointmentRequest request) {
        AppointmentResponse response = appointmentService.rateAppointment(appointmentId, request.patientId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}