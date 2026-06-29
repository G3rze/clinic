package com.terraplanistas.clinic.controllers;

import com.terraplanistas.clinic.domain.dto.request.CreateEmployeeRequest;
import com.terraplanistas.clinic.domain.dto.response.AdminEmployeeResponse;
import com.terraplanistas.clinic.domain.entities.Employee;
import com.terraplanistas.clinic.domain.entities.User;
import com.terraplanistas.clinic.services.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${app.base-uri}/admin/employees")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Administración de Empleados",
        description = "Endpoints para la gestión integral de empleados del sistema. " +
                "Permite crear, consultar, actualizar, revocar y reactivar accesos de empleados. " +
                "Acceso exclusivo para usuarios con rol ADMIN.")
@SecurityRequirement(name = "bearerAuth")
public class AdminEmployeeController {

    private final EmployeeService employeeService;

    public AdminEmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Operation(
            summary = "Crear un nuevo empleado",
            description = "Registra un nuevo empleado en el sistema junto con su usuario de acceso. " +
                    "El empleado será creado con el rol especificado y se le asignará un correo electrónico institucional. " +
                    "Requiere autenticación de administrador."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Empleado creado exitosamente",
                    content = @Content(schema = @Schema(implementation = AdminEmployeeResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos o incompletos",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado. Se requiere un token JWT válido",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso denegado. Se requiere rol de ADMIN",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflicto. El correo electrónico o número de documento ya existe en el sistema",
                    content = @Content
            )
    })
    @PostMapping
    public ResponseEntity<AdminEmployeeResponse> createEmployee(
            @Valid @RequestBody
            @Parameter(description = "Datos del empleado a crear", required = true)
            CreateEmployeeRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        UUID currentAdminUserId = UUID.fromString(authentication.getName());
        User user = employeeService.createEmployee(request, currentAdminUserId);
        Employee employee = employeeService.getEmployeeByUserId(user.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AdminEmployeeResponse.from(employee));
    }

    @Operation(
            summary = "Listar todos los empleados",
            description = "Obtiene una lista completa de todos los empleados registrados en el sistema, " +
                    "incluyendo su información personal, estado de acceso y rol asignado."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de empleados obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = AdminEmployeeResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado. Se requiere un token JWT válido",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso denegado. Se requiere rol de ADMIN",
                    content = @Content
            )
    })
    @GetMapping
    public ResponseEntity<List<AdminEmployeeResponse>> getAllEmployees() {
        List<Employee> employees = employeeService.getAllEmployees();
        List<AdminEmployeeResponse> response = employees.stream()
                .map(AdminEmployeeResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Obtener empleado por ID",
            description = "Recupera la información detallada de un empleado específico mediante su identificador único universal (UUID)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Información del empleado recuperada exitosamente",
                    content = @Content(schema = @Schema(implementation = AdminEmployeeResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado. Se requiere un token JWT válido",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso denegado. Se requiere rol de ADMIN",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Empleado no encontrado con el ID proporcionado",
                    content = @Content
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<AdminEmployeeResponse> getEmployeeById(
            @PathVariable
            @Parameter(description = "UUID del empleado a consultar",
                    example = "550e8400-e29b-41d4-a716-446655440000",
                    required = true)
            UUID id) {
        Employee employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(AdminEmployeeResponse.from(employee));
    }

    @Operation(
            summary = "Actualizar datos del empleado",
            description = "Actualiza la información personal y de contacto de un empleado existente. " +
                    "También permite modificar el rol asignado al empleado en el sistema."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Datos del empleado actualizados exitosamente",
                    content = @Content(schema = @Schema(implementation = AdminEmployeeResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos o incompletos",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado. Se requiere un token JWT válido",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso denegado. Se requiere rol de ADMIN",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Empleado no encontrado con el ID proporcionado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflicto. El correo electrónico o número de documento ya existe en el sistema",
                    content = @Content
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<AdminEmployeeResponse> updateEmployee(
            @PathVariable
            @Parameter(description = "UUID del empleado a actualizar",
                    example = "550e8400-e29b-41d4-a716-446655440000",
                    required = true)
            UUID id,
            @Valid @RequestBody
            @Parameter(description = "Nuevos datos del empleado", required = true)
            CreateEmployeeRequest request) {
        Employee employee = employeeService.updateEmployee(id, request);
        return ResponseEntity.ok(AdminEmployeeResponse.from(employee));
    }

    @Operation(
            summary = "Revocar acceso de empleado",
            description = "Revoca el acceso al sistema del empleado especificado mediante una eliminación lógica. " +
                    "El empleado no podrá autenticarse en el sistema, pero su información permanece almacenada " +
                    "para fines de auditoría y trazabilidad. Registra el administrador que ejecuta la acción."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Acceso del empleado revocado exitosamente. Sin contenido en la respuesta.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado. Se requiere un token JWT válido",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso denegado. Se requiere rol de ADMIN",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Empleado no encontrado con el ID proporcionado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflicto. El empleado ya tiene el acceso revocado previamente",
                    content = @Content
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> revokeEmployeeAccess(
            @PathVariable
            @Parameter(description = "UUID del empleado cuyo acceso será revocado",
                    example = "550e8400-e29b-41d4-a716-446655440000",
                    required = true)
            UUID id,
            @Parameter(hidden = true) Authentication authentication) {
        UUID currentAdminUserId = UUID.fromString(authentication.getName());
        employeeService.revokeEmployeeAccess(id, currentAdminUserId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Reactivar acceso de empleado",
            description = "Reactiva el acceso al sistema de un empleado previamente revocado. " +
                    "El empleado podrá volver a autenticarse y utilizar el sistema según el rol que tenga asignado. " +
                    "Solo aplica para empleados cuyo acceso fue revocado mediante eliminación lógica."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Acceso del empleado reactivado exitosamente",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado. Se requiere un token JWT válido",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso denegado. Se requiere rol de ADMIN",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Empleado no encontrado con el ID proporcionado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflicto. El empleado ya tiene el acceso activo",
                    content = @Content
            )
    })
    @PostMapping("/{id}/reactivate")
    public ResponseEntity<Void> reactivateEmployee(
            @PathVariable
            @Parameter(description = "UUID del empleado cuyo acceso será reactivado",
                    example = "550e8400-e29b-41d4-a716-446655440000",
                    required = true)
            UUID id) {
        employeeService.reactivateEmployee(id);
        return ResponseEntity.ok().build();
    }
}