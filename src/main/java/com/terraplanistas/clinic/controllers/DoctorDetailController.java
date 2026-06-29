package com.terraplanistas.clinic.controllers;

import com.terraplanistas.clinic.domain.dto.request.AddAvailabilityRequest;
import com.terraplanistas.clinic.domain.dto.request.UpdateEmployeeSpecialtyRequest;
import com.terraplanistas.clinic.domain.dto.response.AvailabilityResponse;
import com.terraplanistas.clinic.domain.dto.response.DoctorDetailResponse;
import com.terraplanistas.clinic.domain.dto.response.EmployeeSpecialtyResponse;
import com.terraplanistas.clinic.services.DoctorAvailabilityService;
import com.terraplanistas.clinic.services.EmployeeSpecialtyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("${app.base-uri}/admin/employees")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Gestión de Médicos - Detalle y Especialidades",
        description = "Endpoints administrativos para la gestión del perfil detallado de médicos, " +
                "sus especialidades y disponibilidad horaria. Acceso exclusivo para administradores.")
@SecurityRequirement(name = "bearerAuth")
public class DoctorDetailController {

    private final EmployeeSpecialtyService employeeSpecialtyService;
    private final DoctorAvailabilityService availabilityService;

    public DoctorDetailController(EmployeeSpecialtyService employeeSpecialtyService,
                                  DoctorAvailabilityService availabilityService) {
        this.employeeSpecialtyService = employeeSpecialtyService;
        this.availabilityService = availabilityService;
    }

    @Operation(
            summary = "Obtener detalle completo de un médico",
            description = "Recupera la información completa del perfil de un médico, incluyendo sus datos personales, " +
                    "todas las especialidades que ejerce, tarifas, licencias profesionales y disponibilidad horaria semanal."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Detalle del médico recuperado exitosamente",
                    content = @Content(schema = @Schema(implementation = DoctorDetailResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - Solo administradores", content = @Content),
            @ApiResponse(responseCode = "404", description = "Médico no encontrado", content = @Content)
    })
    @GetMapping("/{id}/detail")
    public ResponseEntity<DoctorDetailResponse> getDoctorDetail(
            @Parameter(description = "UUID del médico a consultar", example = "660e8400-e29b-41d4-a716-446655440001")
            @PathVariable UUID id) {
        DoctorDetailResponse detail = employeeSpecialtyService.getDoctorDetail(id);
        return ResponseEntity.ok(detail);
    }

    @Operation(
            summary = "Listar especialidades de un médico",
            description = "Obtiene la lista de especialidades médicas asignadas a un médico específico, " +
                    "junto con su configuración de tarifas, licencias y horarios."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de especialidades obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = EmployeeSpecialtyResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Médico no encontrado", content = @Content)
    })
    @GetMapping("/{id}/specialties")
    public ResponseEntity<List<EmployeeSpecialtyResponse>> getDoctorSpecialties(
            @Parameter(description = "UUID del médico", example = "660e8400-e29b-41d4-a716-446655440001")
            @PathVariable UUID id) {
        List<EmployeeSpecialtyResponse> specialties = employeeSpecialtyService.getSpecialtiesByEmployeeId(id);
        return ResponseEntity.ok(specialties);
    }

    @Operation(
            summary = "Asignar especialidad a un médico",
            description = "Asigna una nueva especialidad médica a un médico, registrando su número de licencia profesional. " +
                    "El médico podrá comenzar a atender consultas de esta especialidad una vez configurada la disponibilidad."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Especialidad asignada exitosamente",
                    content = @Content(schema = @Schema(implementation = EmployeeSpecialtyResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Médico o especialidad no encontrada", content = @Content),
            @ApiResponse(responseCode = "409", description = "El médico ya tiene asignada esta especialidad", content = @Content)
    })
    @PostMapping("/{id}/specialties")
    public ResponseEntity<EmployeeSpecialtyResponse> addSpecialty(
            @Parameter(description = "UUID del médico", example = "660e8400-e29b-41d4-a716-446655440001")
            @PathVariable UUID id,
            @RequestBody
            @Parameter(description = "Especialidad y licencia profesional a asignar")
            AddSpecialtyRequest request) {
        EmployeeSpecialtyResponse response = employeeSpecialtyService.addSpecialtyToEmployee(
                id, request.specialtyId(), request.professionalLicenseNumber());
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Actualizar configuración de especialidad",
            description = "Actualiza los parámetros de una especialidad asignada a un médico: " +
                    "licencia profesional, tarifa por hora y duración de la consulta."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Especialidad actualizada exitosamente",
                    content = @Content(schema = @Schema(implementation = EmployeeSpecialtyResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Médico o especialidad no encontrada", content = @Content)
    })
    @PutMapping("/{employeeId}/specialties/{specialtyId}")
    public ResponseEntity<EmployeeSpecialtyResponse> updateSpecialty(
            @Parameter(description = "UUID del médico", example = "660e8400-e29b-41d4-a716-446655440001")
            @PathVariable UUID employeeId,
            @Parameter(description = "UUID de la especialidad", example = "880e8400-e29b-41d4-a716-446655440003")
            @PathVariable UUID specialtyId,
            @Valid @RequestBody
            @Parameter(description = "Nueva configuración de la especialidad")
            UpdateEmployeeSpecialtyRequest request) {
        EmployeeSpecialtyResponse response = employeeSpecialtyService.updateEmployeeSpecialty(
                employeeId, specialtyId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Remover especialidad de un médico",
            description = "Elimina la asignación de una especialidad médica a un médico. " +
                    "Esta operación no afecta las citas ya agendadas, pero impide nuevas asignaciones."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Especialidad removida exitosamente", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Médico o especialidad no encontrada", content = @Content)
    })
    @DeleteMapping("/{employeeId}/specialties/{specialtyId}")
    public ResponseEntity<Void> removeSpecialty(
            @Parameter(description = "UUID del médico", example = "660e8400-e29b-41d4-a716-446655440001")
            @PathVariable UUID employeeId,
            @Parameter(description = "UUID de la especialidad a remover", example = "880e8400-e29b-41d4-a716-446655440003")
            @PathVariable UUID specialtyId) {
        employeeSpecialtyService.removeSpecialtyFromEmployee(employeeId, specialtyId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Agregar disponibilidad horaria a especialidad",
            description = "Crea una nueva franja horaria de disponibilidad para una especialidad específica del médico. " +
                    "Define un día de la semana y un rango horario en el que el médico estará disponible " +
                    "para atender consultas de esta especialidad."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Disponibilidad creada exitosamente",
                    content = @Content(schema = @Schema(implementation = AvailabilityResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o solapamiento de horarios", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Médico o especialidad no encontrada", content = @Content)
    })
    @PostMapping("/{employeeId}/specialties/{specialtyId}/availability")
    public ResponseEntity<AvailabilityResponse> addAvailability(
            @Parameter(description = "UUID del médico", example = "660e8400-e29b-41d4-a716-446655440001")
            @PathVariable UUID employeeId,
            @Parameter(description = "UUID de la especialidad", example = "880e8400-e29b-41d4-a716-446655440003")
            @PathVariable UUID specialtyId,
            @Valid @RequestBody
            @Parameter(description = "Día de la semana y rango horario para la disponibilidad")
            AddAvailabilityRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        UUID currentUserId = UUID.fromString(authentication.getName());
        AvailabilityResponse response = availabilityService.createAvailability(
                employeeId, specialtyId, request, currentUserId);
        return ResponseEntity.ok(response);
    }

    @Schema(description = "DTO para asignar una especialidad a un médico")
    public record AddSpecialtyRequest(
            @Schema(description = "UUID de la especialidad a asignar", example = "880e8400-e29b-41d4-a716-446655440003")
            UUID specialtyId,
            @Schema(description = "Número de licencia profesional para esta especialidad", example = "CAR-2020-12345")
            String professionalLicenseNumber
    ) {}
}