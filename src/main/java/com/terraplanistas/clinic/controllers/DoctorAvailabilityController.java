package com.terraplanistas.clinic.controllers;

import com.terraplanistas.clinic.domain.dto.request.AvailabilityRequest;
import com.terraplanistas.clinic.domain.dto.response.ApiResponse;
import com.terraplanistas.clinic.domain.dto.response.AvailabilityResponse;
import com.terraplanistas.clinic.services.DoctorAvailabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
@RequestMapping("${app.base-uri}/availability")
@Tag(name = "Disponibilidad Horaria",
        description = "Endpoints para la gestión de franjas horarias de disponibilidad de los médicos. " +
                "Permite crear, consultar, actualizar y eliminar horarios de atención.")
@SecurityRequirement(name = "bearerAuth")
public class DoctorAvailabilityController {

    private final DoctorAvailabilityService availabilityService;

    public DoctorAvailabilityController(DoctorAvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @Operation(
            summary = "Crear franja de disponibilidad",
            description = "Registra una nueva franja de disponibilidad horaria para un médico. " +
                    "Retorna la ubicación del recurso creado en el header Location."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Disponibilidad creada exitosamente",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos inválidos o solapamiento de horarios", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Médico o especialidad no encontrada", content = @Content)
    })
    @PostMapping
    public ResponseEntity<ApiResponse<AvailabilityResponse>> createAvailability(
            @Valid @RequestBody
            @Parameter(description = "Datos de la disponibilidad a crear", required = true)
            AvailabilityRequest request) {
        AvailabilityResponse response = availabilityService.createAvailability(request);
        URI location = URI.create("/api/v1/availability/" + response.id());
        return ResponseEntity.created(location).body(ApiResponse.created(response, location.toString()));
    }

    @Operation(
            summary = "Actualizar franja de disponibilidad",
            description = "Modifica una franja de disponibilidad horaria existente."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Disponibilidad actualizada exitosamente",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Disponibilidad no encontrada", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AvailabilityResponse>> updateAvailability(
            @Parameter(description = "UUID de la disponibilidad a actualizar", example = "990e8400-e29b-41d4-a716-446655440004")
            @PathVariable UUID id,
            @Valid @RequestBody
            @Parameter(description = "Nuevos datos de la disponibilidad")
            AvailabilityRequest request) {
        AvailabilityResponse response = availabilityService.updateAvailability(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
            summary = "Eliminar franja de disponibilidad",
            description = "Elimina permanentemente una franja de disponibilidad horaria."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Disponibilidad eliminada exitosamente", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Disponibilidad no encontrada", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAvailability(
            @Parameter(description = "UUID de la disponibilidad a eliminar", example = "990e8400-e29b-41d4-a716-446655440004")
            @PathVariable UUID id) {
        availabilityService.deleteAvailability(id);
        return ResponseEntity.ok(ApiResponse.success((Void) null));
    }

    @Operation(
            summary = "Consultar disponibilidad por ID",
            description = "Recupera la información de una franja de disponibilidad específica."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Disponibilidad encontrada",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Disponibilidad no encontrada", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AvailabilityResponse>> getAvailability(
            @Parameter(description = "UUID de la disponibilidad", example = "990e8400-e29b-41d4-a716-446655440004")
            @PathVariable UUID id) {
        AvailabilityResponse response = availabilityService.getAvailability(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
            summary = "Listar disponibilidades por médico",
            description = "Obtiene todas las franjas de disponibilidad de un médico. " +
                    "Puede filtrarse opcionalmente por especialidad."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Lista de disponibilidades recuperada exitosamente",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Médico no encontrado", content = @Content)
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<AvailabilityResponse>>> getAvailabilities(
            @Parameter(description = "UUID del médico", required = true, example = "660e8400-e29b-41d4-a716-446655440001")
            @RequestParam UUID employeeId,
            @Parameter(description = "UUID de la especialidad (opcional). Si se proporciona, filtra por especialidad",
                    example = "880e8400-e29b-41d4-a716-446655440003")
            @RequestParam(required = false) UUID specialtyId) {
        List<AvailabilityResponse> response = specialtyId != null
                ? availabilityService.getAvailabilitiesByEmployeeAndSpecialty(employeeId, specialtyId)
                : availabilityService.getAvailabilitiesByEmployee(employeeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}