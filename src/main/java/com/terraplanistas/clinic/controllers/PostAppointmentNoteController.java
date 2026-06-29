package com.terraplanistas.clinic.controllers;

import com.terraplanistas.clinic.domain.dto.request.PostAppointmentNoteRequest;
import com.terraplanistas.clinic.domain.dto.response.PostAppointmentNoteResponse;
import com.terraplanistas.clinic.services.PostAppointmentNoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("${app.base-uri}/post-appointment-note")
@RequiredArgsConstructor
@Tag(name = "Notas Post-Consulta",
        description = "Endpoints para la gestión de notas médicas complementarias posteriores a la consulta. " +
                "Permite a los médicos registrar observaciones adicionales, recomendaciones de seguimiento " +
                "e indicaciones especiales que complementan la historia clínica principal.")
@SecurityRequirement(name = "bearerAuth")
public class PostAppointmentNoteController {

    private final PostAppointmentNoteService service;

    @Operation(
            summary = "Registrar nota post-consulta",
            description = "Crea una nueva nota post-consulta asociada a una cita médica ya finalizada. " +
                    "El médico es identificado automáticamente desde el token de autenticación JWT. " +
                    "Esta funcionalidad permite documentar información clínica relevante que surge " +
                    "después de la consulta o que complementa el registro clínico principal.\n\n" +
                    "**Casos de uso típicos:**\n" +
                    "- Resultados de exámenes recibidos después de la consulta\n" +
                    "- Ajustes al tratamiento basados en nueva información\n" +
                    "- Recomendaciones adicionales para el paciente\n" +
                    "- Notas de seguimiento y recordatorios"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Nota post-consulta creada exitosamente. Retorna la nota con su UUID asignado",
                    content = @Content(schema = @Schema(implementation = PostAppointmentNoteResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos. Causas frecuentes: contenido vacío, " +
                            "ID de cita nulo o con formato UUID inválido",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado. Se requiere token JWT válido en el header Authorization",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso denegado. El usuario autenticado no es el médico asignado a la cita " +
                            "o no tiene permisos para registrar notas",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cita médica no encontrada con el UUID proporcionado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Regla de negocio violada. La cita no está en estado COMPLETED " +
                            "y no permite agregar notas post-consulta",
                    content = @Content
            )
    })
    @PostMapping
    public ResponseEntity<PostAppointmentNoteResponse> createNote(
            @Parameter(hidden = true) Authentication authentication,
            @Valid @RequestBody
            @Parameter(description = "Datos de la nota post-consulta: ID de la cita y contenido textual de la nota",
                    required = true)
            PostAppointmentNoteRequest note) {
        UUID doctor = UUID.fromString(authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(
                service.createNote(doctor, note)
        );
    }

    @Operation(
            summary = "Consultar notas de una cita",
            description = "Recupera todas las notas post-consulta asociadas a una cita médica específica. " +
                    "El usuario autenticado debe ser el médico que atendió la cita, el paciente titular, " +
                    "o un administrador del sistema para poder acceder a esta información."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de notas post-consulta recuperada exitosamente. " +
                            "Puede ser una lista vacía si la cita no tiene notas registradas",
                    content = @Content(schema = @Schema(implementation = PostAppointmentNoteResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado. Se requiere token JWT válido",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso denegado. El usuario no tiene permisos para consultar las notas de esta cita. " +
                            "Solo el médico tratante, el paciente o un administrador pueden acceder",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cita médica no encontrada con el UUID proporcionado",
                    content = @Content
            )
    })
    @GetMapping("/{appointmentId}")
    public ResponseEntity<List<PostAppointmentNoteResponse>> getNoteFromAppointment(
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(description = "UUID de la cita médica de la cual se desean consultar las notas post-consulta",
                    example = "550e8400-e29b-41d4-a716-446655440000",
                    required = true)
            @PathVariable UUID appointmentId) {
        UUID requester = UUID.fromString(authentication.getName());
        return ResponseEntity.ok().body(
                service.getNoteFromAppointment(requester, appointmentId)
        );
    }
}