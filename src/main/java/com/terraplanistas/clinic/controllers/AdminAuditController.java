package com.terraplanistas.clinic.controllers;

import com.terraplanistas.clinic.domain.dto.response.PermanentlyAnonymizedAuditResponse;
import com.terraplanistas.clinic.services.UserAnonymizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("${app.base-uri}/admin/audits")
@PreAuthorize("hasRole('ADMIN')")
@Tag(
        name = "Auditoría",
        description = "Endpoints para consultar el historial de auditoría de usuarios anonimizados permanentemente."
)
@SecurityRequirement(name = "bearerAuth")
public class AdminAuditController {

    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final UserAnonymizationService anonymizationService;

    public AdminAuditController(UserAnonymizationService anonymizationService) {
        this.anonymizationService = anonymizationService;
    }


    @Operation(
            summary = "Obtener usuarios anonimizados permanentemente",
            description = """
                    Devuelve un listado paginado de los usuarios cuyos datos fueron anonimizados de forma permanente.

                    Permite filtrar por:
                    - Fecha de eliminación lógica.
                    - Fecha de anonimización permanente.

                    Este endpoint únicamente puede ser utilizado por administradores.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Listado obtenido correctamente."
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado."
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No tiene permisos para acceder al recurso."
            )
    })
    @GetMapping("/permanently-anonymized")
    public ResponseEntity<Page<PermanentlyAnonymizedAuditResponse>> getPermanentlyAnonymized(
            @Parameter(description = "Mostrar usuarios eliminados a partir de esta fecha (ISO-8601).")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime deletedAtFrom,

            @Parameter(description = "Mostrar usuarios eliminados hasta esta fecha (ISO-8601).")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime deletedAtTo,

            @Parameter(description = "Mostrar usuarios anonimizados permanentemente a partir de esta fecha (ISO-8601).")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime permanentAtFrom,

            @Parameter(description = "Mostrar usuarios anonimizados permanentemente hasta esta fecha (ISO-8601).")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime permanentAtTo,

            @Parameter(description = "Número de página (comienza en 0).")
            @RequestParam(defaultValue = "0")
            int page,

            @Parameter(description = "Cantidad de registros por página. Máximo permitido: 100.")
            @RequestParam(defaultValue = "20")
            int size,
            @RequestParam(defaultValue = "anonymizationPermanentAt,desc") String sort) {

        int pageSize = Math.min(size > 0 ? size : DEFAULT_PAGE_SIZE, MAX_PAGE_SIZE);
        Pageable pageable = createPageable(page, pageSize, sort);

        Page<PermanentlyAnonymizedAuditResponse> result = anonymizationService.getPermanentlyAnonymized(
                pageable, deletedAtFrom, deletedAtTo, permanentAtFrom, permanentAtTo);

        return ResponseEntity.ok(result);
    }

    private Pageable createPageable(int page, int size, String sort) {
        String[] parts = sort.split(",");
        String field = parts[0];
        org.springframework.data.domain.Sort.Direction direction = parts.length > 1 && parts[1].equalsIgnoreCase("asc")
                ? org.springframework.data.domain.Sort.Direction.ASC
                : org.springframework.data.domain.Sort.Direction.DESC;
        return PageRequest.of(page, size, org.springframework.data.domain.Sort.by(direction, field));
    }
}
