package com.terraplanistas.clinic.controllers;

import com.terraplanistas.clinic.domain.dto.response.PublicDoctorResponse;
import com.terraplanistas.clinic.services.EmployeeSpecialtyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("${app.base-uri}/doctors")
@Tag(name = "Directorio de Médicos (Público)",
        description = "Endpoints públicos para la consulta del directorio médico. " +
                "Permite a pacientes y visitantes buscar médicos por nombre o especialidad, " +
                "y consultar el perfil detallado de cada profesional.")
public class PublicDoctorController {

    private final EmployeeSpecialtyService employeeSpecialtyService;

    public PublicDoctorController(EmployeeSpecialtyService employeeSpecialtyService) {
        this.employeeSpecialtyService = employeeSpecialtyService;
    }

    @Operation(
            summary = "Buscar médicos disponibles",
            description = "Obtiene un listado paginado de médicos activos en el sistema. " +
                    "Permite filtrar por nombre del médico y/o especialidad médica. " +
                    "Retorna información pública del perfil profesional incluyendo las especialidades que ejerce."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Búsqueda de médicos completada exitosamente. Retorna página con resultados",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(responseCode = "400", description = "Parámetros de paginación inválidos", content = @Content)
    })
    @GetMapping
    public ResponseEntity<Page<PublicDoctorResponse>> getDoctors(
            @Parameter(description = "Filtro por nombre o apellido del médico. Búsqueda parcial, " +
                    "no distingue mayúsculas/minúsculas",
                    example = "Rodríguez")
            @RequestParam(required = false) String name,
            @Parameter(description = "Filtro por código o nombre de especialidad médica. " +
                    "Ejemplos: 'CARDIOLOGY', 'Cardiología'",
                    example = "CARDIOLOGY")
            @RequestParam(required = false) String specialty,
            @Parameter(description = "Número de página para paginación (base 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Cantidad de médicos por página", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        Page<PublicDoctorResponse> doctors = employeeSpecialtyService.getPublicDoctors(
                name, specialty, PageRequest.of(page, size));
        return ResponseEntity.ok(doctors);
    }

    @Operation(
            summary = "Consultar perfil de un médico",
            description = "Recupera el perfil público detallado de un médico específico, " +
                    "incluyendo su información personal y la lista de especialidades que ejerce " +
                    "con sus respectivas tarifas y horarios de atención."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Perfil del médico recuperado exitosamente",
                    content = @Content(schema = @Schema(implementation = PublicDoctorResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Médico no encontrado o inactivo", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<PublicDoctorResponse> getDoctorDetail(
            @Parameter(description = "UUID del médico a consultar",
                    example = "660e8400-e29b-41d4-a716-446655440001",
                    required = true)
            @PathVariable UUID id) {
        PublicDoctorResponse doctor = employeeSpecialtyService.getPublicDoctorDetail(id);
        return ResponseEntity.ok(doctor);
    }
}