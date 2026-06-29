package com.terraplanistas.clinic.controllers;

import com.terraplanistas.clinic.domain.entities.Specialty;
import com.terraplanistas.clinic.services.SpecialtyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("${app.base-uri}/specialty")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Especialidades Médicas (Admin)",
        description = "Endpoints administrativos para la gestión del catálogo de especialidades médicas. " +
                "Permite crear, consultar y eliminar especialidades. Acceso exclusivo para administradores.")
@SecurityRequirement(name = "bearerAuth")
public class SpecialtyController {

    private final SpecialtyService specialtyService;

    public SpecialtyController(SpecialtyService specialtyService) {
        this.specialtyService = specialtyService;
    }

    @Operation(
            summary = "Listar todas las especialidades",
            description = "Obtiene el catálogo completo de especialidades médicas disponibles en el sistema."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de especialidades recuperada exitosamente",
                    content = @Content(schema = @Schema(implementation = Specialty.class))
            ),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - Solo administradores", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<Specialty>> getAllSpecialties() {
        return ResponseEntity.ok(specialtyService.getAllSpecialties());
    }

    @Operation(
            summary = "Consultar especialidad por ID",
            description = "Recupera la información de una especialidad médica específica."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Especialidad encontrada exitosamente",
                    content = @Content(schema = @Schema(implementation = Specialty.class))
            ),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Especialidad no encontrada", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<Specialty> getSpecialtyById(
            @Parameter(description = "UUID de la especialidad", example = "880e8400-e29b-41d4-a716-446655440003")
            @PathVariable UUID id) {
        return ResponseEntity.ok(specialtyService.getSpecialtyById(id));
    }

    @Operation(
            summary = "Crear nueva especialidad médica",
            description = "Registra una nueva especialidad en el catálogo del sistema. " +
                    "Requiere código único y nombre descriptivo de la especialidad."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Especialidad creada exitosamente",
                    content = @Content(schema = @Schema(implementation = Specialty.class))
            ),
            @ApiResponse(responseCode = "400", description = "Código o nombre vacío o inválido", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - Solo administradores", content = @Content),
            @ApiResponse(responseCode = "409", description = "Ya existe una especialidad con el mismo código", content = @Content)
    })
    @PostMapping
    public ResponseEntity<Specialty> createSpecialty(
            @Parameter(description = "Mapa con los campos 'code' (código único) y 'name' (nombre en español). " +
                    "Ejemplo: {\"code\": \"CARDIOLOGY\", \"name\": \"Cardiología\"}",
                    required = true)
            @RequestBody Map<String, String> request,
            @Parameter(hidden = true) Authentication authentication) {
        String code = request.get("code");
        String name = request.get("name");
        UUID currentUserId = UUID.fromString(authentication.getName());
        Specialty specialty = specialtyService.createSpecialty(code, name, currentUserId);
        return ResponseEntity.ok(specialty);
    }

    @Operation(
            summary = "Eliminar especialidad médica",
            description = "Elimina permanentemente una especialidad del catálogo. " +
                    "No se permite eliminar especialidades que tengan médicos asociados."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Especialidad eliminada exitosamente", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Especialidad no encontrada", content = @Content),
            @ApiResponse(responseCode = "409", description = "La especialidad tiene médicos asociados y no puede eliminarse", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSpecialty(
            @Parameter(description = "UUID de la especialidad a eliminar", example = "880e8400-e29b-41d4-a716-446655440003")
            @PathVariable UUID id) {
        specialtyService.deleteSpecialty(id);
        return ResponseEntity.noContent().build();
    }
}