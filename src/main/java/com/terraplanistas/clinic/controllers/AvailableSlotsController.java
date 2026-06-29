package com.terraplanistas.clinic.controllers;

import com.terraplanistas.clinic.domain.dto.request.AvailableSlotsQuery;
import com.terraplanistas.clinic.domain.dto.response.ApiResponse;
import com.terraplanistas.clinic.domain.dto.response.AvailableSlotResponse;
import com.terraplanistas.clinic.services.AvailableSlotsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("${app.base-uri}/slots")
@Tag(name = "Disponibilidad de Citas",
        description = "Endpoints para consultar las franjas horarias disponibles de los médicos. " +
                "Permite buscar slots libres por médico, especialidad, rango de fechas y duración de consulta.")
public class AvailableSlotsController {

    private final AvailableSlotsService availableSlotsService;

    public AvailableSlotsController(AvailableSlotsService availableSlotsService) {
        this.availableSlotsService = availableSlotsService;
    }

    @Operation(
            summary = "Consultar franjas horarias disponibles",
            description = "Busca y retorna las franjas horarias disponibles para agendar una cita médica " +
                    "según los criterios especificados. El sistema calcula automáticamente los slots libres " +
                    "considerando la disponibilidad del médico, citas ya agendadas, días feriados " +
                    "y la duración requerida para la consulta.\n\n" +
                    "**Criterios de búsqueda:**\n" +
                    "- Médico específico (obligatorio)\n" +
                    "- Especialidad médica (obligatorio)\n" +
                    "- Rango de fechas para la búsqueda (obligatorio)\n" +
                    "- Duración de la consulta en minutos (obligatorio)\n" +
                    "- Zona horaria para el cálculo de horarios (opcional)"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Búsqueda completada exitosamente. Retorna lista de franjas horarias disponibles",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Parámetros de búsqueda inválidos. Fechas en el pasado, rango de fechas inconsistente, " +
                            "o duración de consulta menor a 5 minutos",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "No se encontró el médico o la especialidad especificada",
                    content = @Content
            )
    })
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<AvailableSlotResponse>>> findAvailableSlots(
            @Parameter(description = "UUID del médico a consultar", required = true,
                    example = "660e8400-e29b-41d4-a716-446655440001")
            @RequestParam UUID doctorId,

            @Parameter(description = "Código de la especialidad médica (ej: CARDIOLOGY, PEDIATRICS)", required = true,
                    example = "CARDIOLOGY")
            @RequestParam String specialtyCode,

            @Parameter(description = "Fecha inicial del rango de búsqueda en formato YYYY-MM-DD. Debe ser futura", required = true,
                    example = "2026-07-01")
            @RequestParam LocalDate startDate,

            @Parameter(description = "Fecha final del rango de búsqueda en formato YYYY-MM-DD. Debe ser futura y >= startDate", required = true,
                    example = "2026-07-07")
            @RequestParam LocalDate endDate,

            @Parameter(description = "Duración de la consulta en minutos. Mínimo 5 minutos", required = true,
                    example = "30")
            @RequestParam Integer consultDurationMinutes,

            @Parameter(description = "Zona horaria para el cálculo (opcional). Ej: America/Bogota",
                    example = "America/Bogota")
            @RequestParam(required = false) ZoneId timezone) {
        AvailableSlotsQuery query = new AvailableSlotsQuery(
                doctorId,
                specialtyCode,
                startDate,
                endDate,
                timezone,
                consultDurationMinutes
        );
        List<AvailableSlotResponse> slots = availableSlotsService.findAvailableSlots(query);
        return ResponseEntity.ok(ApiResponse.success(slots));
    }
}