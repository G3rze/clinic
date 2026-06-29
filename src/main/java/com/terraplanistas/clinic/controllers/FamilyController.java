package com.terraplanistas.clinic.controllers;

import com.terraplanistas.clinic.domain.dto.request.AddDependentRequest;
import com.terraplanistas.clinic.domain.dto.response.PatientResponse;
import com.terraplanistas.clinic.domain.entities.Patient;
import com.terraplanistas.clinic.services.FamilyService;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${app.base-uri}/auth/family")
@Tag(name = "Gestión Familiar - Dependientes",
        description = "Endpoints para la administración de pacientes dependientes vinculados al usuario autenticado. " +
                "Permite a un paciente titular gestionar los perfiles de sus familiares dependientes " +
                "(hijos, cónyuges, adultos mayores a cargo) para agendar y administrar citas médicas en su nombre.")
@SecurityRequirement(name = "bearerAuth")
public class FamilyController {

    private final FamilyService familyService;

    public FamilyController(FamilyService familyService) {
        this.familyService = familyService;
    }

    @Operation(
            summary = "Registrar un nuevo dependiente",
            description = "Agrega un paciente dependiente al perfil del usuario autenticado. " +
                    "El dependiente queda vinculado al titular, quien podrá gestionar sus citas médicas, " +
                    "consultar su historial clínico y administrar sus datos según las políticas de privacidad.\n\n" +
                    "**Tipos de relación soportados:**\n" +
                    "- CHILD: Hijo/a\n" +
                    "- SPOUSE: Cónyuge\n" +
                    "- PARENT: Padre/Madre\n" +
                    "- SIBLING: Hermano/a\n" +
                    "- OTHER: Otro familiar o persona a cargo"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Dependiente registrado exitosamente. Retorna los datos del nuevo paciente",
                    content = @Content(schema = @Schema(implementation = PatientResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos. Documento con formato incorrecto o fecha de nacimiento futura", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado. Token JWT ausente o inválido", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflicto. Ya existe un dependiente registrado con el mismo número de documento", content = @Content)
    })
    @PostMapping("/dependents")
    public ResponseEntity<PatientResponse> addDependent(
            @Parameter(hidden = true) Authentication authentication,
            @Valid @RequestBody
            @Parameter(description = "Datos del dependiente a registrar: nombres, apellidos, documento de identidad, " +
                    "fecha de nacimiento y tipo de relación con el titular", required = true)
            AddDependentRequest request) {
        UUID userId = UUID.fromString(authentication.getName());
        Patient dependent = familyService.addDependent(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PatientResponse.from(dependent));
    }

    @Operation(
            summary = "Listar todos los dependientes",
            description = "Obtiene la lista completa de pacientes dependientes asociados al usuario autenticado, " +
                    "incluyendo sus datos básicos de identificación y contacto."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de dependientes recuperada exitosamente",
                    content = @Content(schema = @Schema(implementation = PatientResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    })
    @GetMapping("/dependents")
    public ResponseEntity<List<PatientResponse>> getDependents(
            @Parameter(hidden = true) Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        List<Patient> dependents = familyService.getDependents(userId);
        List<PatientResponse> response = dependents.stream()
                .map(PatientResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Consultar dependiente específico",
            description = "Recupera la información detallada de un dependiente por su UUID. " +
                    "Solo el representante legal autorizado puede acceder a los datos del dependiente."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Información del dependiente recuperada exitosamente",
                    content = @Content(schema = @Schema(implementation = PatientResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado. El usuario no es representante de este dependiente", content = @Content),
            @ApiResponse(responseCode = "404", description = "Dependiente no encontrado con el UUID proporcionado", content = @Content)
    })
    @GetMapping("/dependents/{id}")
    public ResponseEntity<PatientResponse> getDependentById(
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(description = "UUID del paciente dependiente a consultar",
                    example = "770e8400-e29b-41d4-a716-446655440002")
            @PathVariable UUID id) {
        UUID userId = UUID.fromString(authentication.getName());
        if (!familyService.isUserRepresentativeOf(userId, id)) {
            return ResponseEntity.notFound().build();
        }
        Patient dependent = familyService.getDependentById(id);
        return ResponseEntity.ok(PatientResponse.from(dependent));
    }

    @Operation(
            summary = "Desvincular dependiente",
            description = "Elimina la relación de dependencia entre el usuario autenticado y el paciente. " +
                    "El dependiente no se elimina del sistema, solo se desvincula del representante. " +
                    "Solo el representante legal autorizado puede ejecutar esta acción."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Dependiente desvinculado exitosamente. Sin contenido en la respuesta", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado. El usuario no es representante de este dependiente", content = @Content),
            @ApiResponse(responseCode = "404", description = "Dependiente no encontrado", content = @Content)
    })
    @DeleteMapping("/dependents/{id}")
    public ResponseEntity<Void> removeDependent(
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(description = "UUID del paciente dependiente a desvincular",
                    example = "770e8400-e29b-41d4-a716-446655440002")
            @PathVariable UUID id) {
        UUID userId = UUID.fromString(authentication.getName());
        if (!familyService.isUserRepresentativeOf(userId, id)) {
            return ResponseEntity.notFound().build();
        }
        familyService.removeDependent(id, userId);
        return ResponseEntity.noContent().build();
    }
}