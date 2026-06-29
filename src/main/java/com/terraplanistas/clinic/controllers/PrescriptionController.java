package com.terraplanistas.clinic.controllers;

import com.terraplanistas.clinic.domain.dto.request.PrescriptionRequest;
import com.terraplanistas.clinic.domain.dto.response.ApiResponse;
import com.terraplanistas.clinic.domain.dto.response.PrescriptionResponse;
import com.terraplanistas.clinic.services.PrescriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("${app.base-uri}/prescriptions")
@Tag(name = "Recetas Médicas Digitales",
        description = "Endpoints para la gestión del ciclo de vida de recetas médicas digitales. " +
                "Permite a los médicos emitir prescripciones con firma digital, a los pacientes " +
                "consultar sus recetas, y a las farmacias registrar dispensaciones controladas.")
@SecurityRequirement(name = "bearerAuth")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    @Operation(
            summary = "Emitir receta médica digital",
            description = "Crea una nueva receta médica digital firmada electrónicamente por el médico tratante. " +
                    "La receta incluye el medicamento prescrito, instrucciones de dosificación detalladas, " +
                    "y un límite máximo de dispensaciones en farmacia (1 a 3). " +
                    "El médico es identificado mediante el header X-User-Id.\n\n" +
                    "**Control de dispensaciones:**\n" +
                    "- maxUsages: define cuántas veces el paciente puede reclamar el medicamento\n" +
                    "- usageCount: se incrementa automáticamente en cada dispensación\n" +
                    "- Cuando usageCount alcanza maxUsages, la receta se marca como agotada"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Receta médica emitida exitosamente. Retorna la receta con su UUID y ubicación",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos inválidos. Instrucciones de dosificación vacías, medicamento no especificado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado o header X-User-Id ausente", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cita médica o medicamento no encontrado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "La cita no está en estado que permita emitir recetas", content = @Content)
    })
    @PostMapping
    public ResponseEntity<ApiResponse<PrescriptionResponse>> create(
            @Valid @RequestBody
            @Parameter(description = "Datos completos de la receta: medicamento, instrucciones, " +
                    "firma digital y límite de dispensaciones", required = true)
            PrescriptionRequest request,
            @Parameter(description = "UUID del médico que emite la receta. Se envía en el header X-User-Id",
                    required = true, example = "660e8400-e29b-41d4-a716-446655440001")
            @RequestHeader("X-User-Id") UUID doctorUserId) {

        PrescriptionResponse body = prescriptionService.createPrescription(request, doctorUserId);
        URI location = URI.create("/api/v1/prescriptions/" + body.id());
        return ResponseEntity.created(location).body(ApiResponse.created(body, location.toString()));
    }

    @Operation(
            summary = "Dispensar medicamento (farmacia)",
            description = "Registra la dispensación de un medicamento asociado a una receta médica. " +
                    "Incrementa el contador de uso (usageCount) de la receta. " +
                    "Cuando el contador alcanza el máximo permitido (maxUsages), " +
                    "la receta se marca automáticamente como agotada y no permite más dispensaciones."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Dispensación registrada exitosamente. Retorna la receta con contador actualizado",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Receta no encontrada", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "La receta ya alcanzó el máximo de dispensaciones permitidas", content = @Content)
    })
    @PatchMapping("/{id}/dispense")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> dispense(
            @Parameter(description = "UUID de la receta a dispensar",
                    example = "ee0e8400-e29b-41d4-a716-446655440014",
                    required = true)
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(prescriptionService.dispensePrescription(id)));
    }

    @Operation(
            summary = "Consultar recetas de un paciente",
            description = "Recupera todas las recetas médicas asociadas a un paciente específico. " +
                    "Útil para que el paciente consulte su historial de medicamentos prescritos " +
                    "y los farmacéuticos verifiquen recetas pendientes de dispensación."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Lista de recetas del paciente recuperada exitosamente",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Paciente no encontrado", content = @Content)
    })
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<ApiResponse<List<PrescriptionResponse>>> getByPatient(
            @Parameter(description = "UUID del paciente cuyas recetas se desean consultar",
                    example = "770e8400-e29b-41d4-a716-446655440002",
                    required = true)
            @PathVariable UUID patientId) {
        return ResponseEntity.ok(ApiResponse.success(prescriptionService.getPrescriptionsByPatient(patientId)));
    }

    @Operation(
            summary = "Consultar receta individual",
            description = "Recupera la información detallada de una receta médica específica, " +
                    "incluyendo estado de dispensaciones, instrucciones de dosificación y firma digital."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Receta encontrada exitosamente",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Receta no encontrada", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> getById(
            @Parameter(description = "UUID de la receta a consultar",
                    example = "ee0e8400-e29b-41d4-a716-446655440014",
                    required = true)
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(prescriptionService.getPrescriptionById(id)));
    }

    @Operation(
            summary = "Consultar recetas de una cita",
            description = "Recupera todas las recetas médicas emitidas durante una cita médica específica. " +
                    "Una misma consulta puede generar múltiples recetas para diferentes medicamentos."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Lista de recetas de la cita recuperada exitosamente",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cita no encontrada", content = @Content)
    })
    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<ApiResponse<List<PrescriptionResponse>>> getByAppointment(
            @Parameter(description = "UUID de la cita médica",
                    example = "550e8400-e29b-41d4-a716-446655440000",
                    required = true)
            @PathVariable UUID appointmentId) {
        return ResponseEntity.ok(ApiResponse.success(prescriptionService.getPrescriptionsByAppointment(appointmentId)));
    }
}