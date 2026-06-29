package com.terraplanistas.clinic.controllers;

import com.terraplanistas.clinic.domain.dto.request.LaboratoryRequest;
import com.terraplanistas.clinic.domain.dto.response.ApiResponse;
import com.terraplanistas.clinic.domain.dto.response.LaboratoryResponse;
import com.terraplanistas.clinic.services.LaboratoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("${app.base-uri}/laboratories")
@Tag(name = "Laboratorios Farmacéuticos",
        description = "Endpoints para la gestión del catálogo de laboratorios farmacéuticos. " +
                "Permite registrar, consultar, actualizar y eliminar laboratorios fabricantes de medicamentos.")
public class LaboratoryController {

    private final LaboratoryService laboratoryService;

    public LaboratoryController(LaboratoryService laboratoryService) {
        this.laboratoryService = laboratoryService;
    }

    @Operation(
            summary = "Listar todos los laboratorios",
            description = "Obtiene el listado completo de laboratorios farmacéuticos registrados en el sistema."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Listado de laboratorios recuperado exitosamente",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<LaboratoryResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(laboratoryService.getAllLaboratories()));
    }

    @Operation(
            summary = "Consultar laboratorio por ID",
            description = "Recupera la información de un laboratorio farmacéutico específico."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Laboratorio encontrado",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Laboratorio no encontrado", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LaboratoryResponse>> getById(
            @Parameter(description = "UUID del laboratorio", example = "aa0e8400-e29b-41d4-a716-446655440010")
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(laboratoryService.getLaboratoryById(id)));
    }

    @Operation(
            summary = "Registrar nuevo laboratorio",
            description = "Crea un nuevo laboratorio farmacéutico en el catálogo. Requiere rol de ADMINISTRADOR."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Laboratorio creado exitosamente",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Nombre inválido o vacío", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol ADMIN", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Ya existe un laboratorio con el mismo nombre", content = @Content)
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<LaboratoryResponse>> create(
            @Parameter(hidden = true) Authentication authentication,
            @Valid @RequestBody LaboratoryRequest request) {
        UUID currentUserId = UUID.fromString(authentication.getName());
        LaboratoryResponse body = laboratoryService.createLaboratory(request.name(), currentUserId);
        URI location = URI.create("/api/v1/laboratories/" + body.id());
        return ResponseEntity.created(location).body(ApiResponse.created(body, location.toString()));
    }

    @Operation(
            summary = "Actualizar laboratorio",
            description = "Modifica el nombre de un laboratorio farmacéutico existente. Requiere rol de ADMINISTRADOR."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Laboratorio actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Laboratorio no encontrado", content = @Content)
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<LaboratoryResponse>> update(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable UUID id,
            @Valid @RequestBody LaboratoryRequest request) {
        UUID currentUserId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(
                laboratoryService.updateLaboratory(id, request.name(), currentUserId)));
    }

    @Operation(
            summary = "Eliminar laboratorio",
            description = "Elimina permanentemente un laboratorio farmacéutico del sistema. Requiere rol de ADMINISTRADOR."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Laboratorio eliminado exitosamente", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Laboratorio no encontrado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "El laboratorio tiene medicamentos asociados", content = @Content)
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> delete(
            @Parameter(description = "UUID del laboratorio a eliminar", example = "aa0e8400-e29b-41d4-a716-446655440010")
            @PathVariable UUID id) {
        laboratoryService.deleteLaboratory(id);
        return ResponseEntity.noContent().build();
    }
}