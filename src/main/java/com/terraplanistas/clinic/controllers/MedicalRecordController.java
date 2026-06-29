package com.terraplanistas.clinic.controllers;

import com.terraplanistas.clinic.domain.dto.request.MedicalRecordRequest;
import com.terraplanistas.clinic.domain.dto.response.ApiResponse;
import com.terraplanistas.clinic.domain.dto.response.MedicalRecordResponse;
import com.terraplanistas.clinic.domain.entities.Patient;
import com.terraplanistas.clinic.repositories.PatientRepository;
import com.terraplanistas.clinic.services.MedicalRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("${app.base-uri}/medical-records")
@RequiredArgsConstructor
@Tag(name = "Historias Clínicas",
        description = "Endpoints para la gestión de registros de historia clínica. " +
                "Permite documentar diagnósticos, notas clínicas, hallazgos de examen físico " +
                "y adjuntar archivos relevantes a cada consulta médica.")
@SecurityRequirement(name = "bearerAuth")
public class MedicalRecordController {

    private final MedicalRecordService service;
    private final PatientRepository patientRepository;

    @Operation(
            summary = "Registrar historia clínica",
            description = "Crea un nuevo registro en la historia clínica de un paciente asociado a una consulta. " +
                    "Documenta el diagnóstico, notas clínicas, hallazgos del examen físico y archivos adjuntos. " +
                    "El médico es identificado automáticamente desde el token de autenticación."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Historia clínica registrada exitosamente",
                    content = @Content(schema = @Schema(implementation = MedicalRecordResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos inválidos o campos requeridos ausentes", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "El usuario no es médico o no está autorizado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Paciente o cita no encontrada", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Ya existe un registro para esta cita", content = @Content)
    })
    @PostMapping
    public ResponseEntity<MedicalRecordResponse> registerRecord(
            @Parameter(hidden = true) Authentication authentication,
            @Valid @RequestBody
            @Parameter(description = "Datos completos del registro clínico", required = true)
            MedicalRecordRequest request) {
        UUID doctor = UUID.fromString(authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(
                service.registerRecord(doctor, request)
        );
    }

    @Operation(
            summary = "Consultar historia clínica por cita",
            description = "Recupera los registros de historia clínica asociados a una cita médica específica."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Registros de historia clínica recuperados exitosamente",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cita no encontrada", content = @Content)
    })
    @GetMapping("/appointment/{id}")
    public ResponseEntity<ApiResponse<List<MedicalRecordResponse>>> getFromAppointment(
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(description = "UUID de la cita médica", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id) {
        UUID requester = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(service.getFromAppointmentId(requester, id)));
    }

    @Operation(
            summary = "Consultar historial clínico del paciente autenticado",
            description = "Recupera el historial clínico completo del paciente actualmente autenticado. " +
                    "Permite filtrar por rango de fechas y/o médico específico. " +
                    "El paciente es identificado automáticamente desde el token de autenticación."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Historial clínico recuperado exitosamente",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No se encontró perfil de paciente para el usuario actual", content = @Content)
    })
    @GetMapping("/patient")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<MedicalRecordResponse>>> getByPatient(
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(description = "Fecha inicial del rango de búsqueda en formato ISO-8601. " +
                    "Filtra registros creados desde esta fecha",
                    example = "2026-01-01T00:00:00-05:00")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fromDate,
            @Parameter(description = "Fecha final del rango de búsqueda en formato ISO-8601. " +
                    "Filtra registros creados hasta esta fecha",
                    example = "2026-12-31T23:59:59-05:00")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime toDate,
            @Parameter(description = "UUID del médico para filtrar registros por profesional específico",
                    example = "660e8400-e29b-41d4-a716-446655440001")
            @RequestParam(required = false) UUID doctorId) {

        UUID userId = UUID.fromString(authentication.getName());
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new com.terraplanistas.clinic.exceptions.ResourceNotFoundException(
                        "Patient not found for current user"));

        List<MedicalRecordResponse> records;
        if (fromDate != null || toDate != null || doctorId != null) {
            records = service.getByPatientIdAndFilters(patient.getId(), fromDate, toDate, doctorId);
        } else {
            records = service.getByPatientId(patient.getId());
        }
        return ResponseEntity.ok(ApiResponse.success(records));
    }
}