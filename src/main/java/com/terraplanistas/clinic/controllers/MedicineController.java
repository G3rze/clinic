package com.terraplanistas.clinic.controllers;

import com.terraplanistas.clinic.domain.dto.request.MedicineRequest;
import com.terraplanistas.clinic.domain.dto.response.ApiResponse;
import com.terraplanistas.clinic.domain.dto.response.MedicineResponse;
import com.terraplanistas.clinic.services.MedicineService;
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
@RequestMapping("${app.base-uri}/medicines")
@Tag(name = "Catálogo de Medicamentos",
        description = "Endpoints para la gestión del catálogo farmacéutico del sistema. " +
                "Permite registrar, consultar, actualizar y eliminar medicamentos, " +
                "incluyendo información de laboratorios, composición y clasificación ATC.")
public class MedicineController {

    private final MedicineService medicineService;

    public MedicineController(MedicineService medicineService) {
        this.medicineService = medicineService;
    }

    @Operation(
            summary = "Buscar y listar medicamentos",
            description = "Obtiene un listado paginado de medicamentos del catálogo farmacéutico. " +
                    "Permite búsqueda por texto en nombre comercial, nombre genérico o código ATC."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Listado de medicamentos recuperado exitosamente",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<MedicineResponse>>> getAll(
            @Parameter(description = "Texto de búsqueda opcional. Busca coincidencias en nombre comercial, " +
                    "nombre genérico y código ATC",
                    example = "paracetamol")
            @RequestParam(required = false) String search,
            @Parameter(description = "Número de página para paginación (base 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Cantidad de registros por página", example = "50")
            @RequestParam(defaultValue = "50") int size) {
        List<MedicineResponse> medicines = medicineService.searchMedicines(search, page, size);
        return ResponseEntity.ok(ApiResponse.success(medicines));
    }

    @Operation(
            summary = "Consultar medicamento por ID",
            description = "Recupera la información detallada de un medicamento específico del catálogo farmacéutico."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Medicamento encontrado exitosamente",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Medicamento no encontrado", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MedicineResponse>> getById(
            @Parameter(description = "UUID del medicamento a consultar", example = "bb0e8400-e29b-41d4-a716-446655440011")
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(medicineService.getMedicineById(id)));
    }

    @Operation(
            summary = "Registrar nuevo medicamento",
            description = "Crea un nuevo registro de medicamento en el catálogo farmacéutico. " +
                    "Requiere rol de ADMINISTRADOR. Retorna la ubicación del recurso creado."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Medicamento creado exitosamente",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos inválidos o incompletos", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol ADMIN", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Laboratorio no encontrado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Ya existe un medicamento con el mismo código ATC", content = @Content)
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<MedicineResponse>> create(
            @Parameter(hidden = true) Authentication authentication,
            @Valid @RequestBody
            @Parameter(description = "Datos del medicamento a registrar", required = true)
            MedicineRequest request) {
        UUID currentUserId = UUID.fromString(authentication.getName());
        MedicineResponse body = medicineService.createMedicine(request, currentUserId);
        URI location = URI.create("/api/v1/medicines/" + body.id());
        return ResponseEntity.created(location).body(ApiResponse.created(body, location.toString()));
    }

    @Operation(
            summary = "Actualizar medicamento",
            description = "Modifica la información de un medicamento existente en el catálogo farmacéutico. " +
                    "Requiere rol de ADMINISTRADOR."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Medicamento actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Medicamento no encontrado", content = @Content)
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<MedicineResponse>> update(
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(description = "UUID del medicamento a actualizar", example = "bb0e8400-e29b-41d4-a716-446655440011")
            @PathVariable UUID id,
            @Valid @RequestBody
            @Parameter(description = "Nuevos datos del medicamento")
            MedicineRequest request) {
        UUID currentUserId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(
                medicineService.updateMedicine(id, request, currentUserId)));
    }

    @Operation(
            summary = "Eliminar medicamento",
            description = "Elimina permanentemente un medicamento del catálogo farmacéutico. " +
                    "Requiere rol de ADMINISTRADOR. Esta operación es irreversible."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Medicamento eliminado exitosamente", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Medicamento no encontrado", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "El medicamento está referenciado en recetas activas", content = @Content)
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> delete(
            @Parameter(description = "UUID del medicamento a eliminar", example = "bb0e8400-e29b-41d4-a716-446655440011")
            @PathVariable UUID id) {
        medicineService.deleteMedicine(id);
        return ResponseEntity.noContent().build();
    }
}