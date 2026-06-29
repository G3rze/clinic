package com.terraplanistas.clinic.controllers;

import com.terraplanistas.clinic.domain.dto.request.AppointmentTransactionRequest;
import com.terraplanistas.clinic.domain.dto.request.ConfirmPaymentRequest;
import com.terraplanistas.clinic.domain.dto.response.AppointmentResponse;
import com.terraplanistas.clinic.domain.dto.response.AppointmentTransactionResponse;
import com.terraplanistas.clinic.domain.dto.response.CheckoutSessionResponse;
import com.terraplanistas.clinic.domain.enums.AppointmentStatus;
import com.terraplanistas.clinic.http.config.AppStripeProperties;
import com.terraplanistas.clinic.http.stripe.StripePaymentService;
import com.terraplanistas.clinic.services.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("${app.base-uri}/appointments")
@Tag(name = "Gestión de Citas Médicas",
        description = "Endpoints para la gestión integral del ciclo de vida de citas médicas. " +
                "Incluye agendamiento, pago integrado con Stripe, confirmación, cancelación " +
                "y consulta de citas con información detallada de participantes y eventos de Google Calendar.")
@SecurityRequirement(name = "bearerAuth")
public class AppointmentController {

    private static final Logger log = LoggerFactory.getLogger(AppointmentController.class);

    private final AppointmentService appointmentService;
    private final StripePaymentService stripePaymentService;
    private final AppStripeProperties appStripeProperties;

    public AppointmentController(AppointmentService appointmentService, StripePaymentService stripePaymentService,
                                 AppStripeProperties appStripeProperties) {
        this.appointmentService = appointmentService;
        this.stripePaymentService = stripePaymentService;
        this.appStripeProperties = appStripeProperties;
    }

    @Operation(
            summary = "Consultar mis citas",
            description = "Obtiene el listado paginado de citas del usuario autenticado. " +
                    "Permite filtrar por mes (formato YYYY-MM) y por estado de la cita. " +
                    "Útil para que pacientes y doctores consulten su historial de citas programadas."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Listado de citas recuperado exitosamente. Retorna una página con los resultados.",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Formato de mes inválido. Debe usar el formato YYYY-MM (ejemplo: 2026-06)",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado. Se requiere token JWT válido",
                    content = @Content
            )
    })
    @GetMapping("/my-appointments")
    public ResponseEntity<?> getMyAppointments(
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(description = "Mes para filtrar citas en formato YYYY-MM (ejemplo: 2026-06). " +
                    "Si no se especifica, se retornan todas las citas sin filtro de fecha.",
                    example = "2026-06")
            @RequestParam(required = false) String month,
            @Parameter(description = "Estado de la cita para filtrar resultados. " +
                    "Valores disponibles: PENDING, CONFIRMED, CANCELLED, COMPLETED, NO_SHOW",
                    example = "CONFIRMED")
            @RequestParam(required = false) AppointmentStatus status) {

        UUID userId = UUID.fromString(authentication.getName());

        YearMonth yearMonth = null;
        if (month != null && !month.isBlank()) {
            try {
                yearMonth = YearMonth.parse(month);
            } catch (DateTimeParseException e) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Invalid month format. Use YYYY-MM (e.g., 2026-06)"
                ));
            }
        }

        Page<AppointmentResponse> appointments = appointmentService.getAppointmentsForUser(userId, yearMonth, status);

        return ResponseEntity.ok(appointments);
    }

    @Operation(
            summary = "Contar citas por fecha",
            description = "Retorna la cantidad de citas programadas para una fecha específica. " +
                    "Útil para verificar disponibilidad de agenda y control de carga laboral del personal médico."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Conteo realizado exitosamente. Retorna la fecha y el número de citas.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Formato de fecha inválido. Debe usar el formato YYYY-MM-DD (ejemplo: 2026-06-22)",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado. Se requiere token JWT válido",
                    content = @Content
            )
    })
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> countAppointments(
            @Parameter(description = "Fecha para consultar disponibilidad en formato YYYY-MM-DD (ejemplo: 2026-06-22)",
                    example = "2026-06-22",
                    required = true)
            @RequestParam String date) {
        try {
            LocalDate localDate = LocalDate.parse(date);
            long count = appointmentService.countAppointmentsForDate(localDate);
            return ResponseEntity.ok(Map.of("date", date, "count", count));
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid date format. Use YYYY-MM-DD (e.g., 2026-06-22)"
            ));
        }
    }

    @Operation(
            summary = "Crear cita con transacción de pago",
            description = "Inicia el proceso de agendamiento de una cita médica junto con la transacción de pago. " +
                    "Si se proporciona un ID de cita existente, se reintenta el pago de una cita previamente creada. " +
                    "La transacción incluye la creación del PaymentIntent en Stripe y la reserva del espacio en agenda."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Transacción de cita y pago creada exitosamente",
                    content = @Content(schema = @Schema(implementation = AppointmentTransactionResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos o incompletos en la solicitud",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado. Se requiere token JWT válido",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "402",
                    description = "Error en el procesamiento del pago. Fondos insuficientes o tarjeta rechazada",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflicto de horario. El espacio solicitado no está disponible",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Regla de negocio violada. El médico no está disponible en el horario solicitado",
                    content = @Content
            )
    })
    @PostMapping("/transactions")
    public ResponseEntity<AppointmentTransactionResponse> createAppointment(
            @Valid @RequestBody
            @Parameter(description = "Datos completos de la cita e información de pago", required = true)
            AppointmentTransactionRequest request,
            @Parameter(description = "ID de una cita existente para reintentar el pago. " +
                    "Si se proporciona, se ignora la información de cita nueva y se procesa solo el pago.")
            @RequestParam(required = false) UUID existingAppointmentId
    ) {
        if (existingAppointmentId != null) {
            return ResponseEntity.ok(
                    appointmentService.retryPaymentForExistingAppointment(existingAppointmentId)
            );
        }
        return ResponseEntity.ok(
                appointmentService.createAppointmentWithPayment(
                        request
                )
        );
    }

    @Operation(
            summary = "Confirmar pago de cita",
            description = "Confirma el pago de una cita específica utilizando el ID del PaymentIntent de Stripe. " +
                    "Una vez confirmado el pago, la cita pasa a estado CONFIRMED y se realizan las notificaciones correspondientes."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Pago confirmado y cita actualizada exitosamente",
                    content = @Content(schema = @Schema(implementation = AppointmentResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "ID de PaymentIntent inválido o no corresponde a la cita especificada",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado. Se requiere token JWT válido",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cita no encontrada con el ID proporcionado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "La cita ya se encuentra en un estado que no permite confirmación de pago",
                    content = @Content
            )
    })
    @PostMapping("/{appointmentId}/confirm-payment")
    public ResponseEntity<AppointmentResponse> confirmPayment(
            @PathVariable
            @Parameter(description = "UUID de la cita a confirmar pago",
                    example = "550e8400-e29b-41d4-a716-446655440000",
                    required = true)
            UUID appointmentId,
            @Valid @RequestBody
            @Parameter(description = "Datos de confirmación del PaymentIntent de Stripe", required = true)
            ConfirmPaymentRequest request
    ) {
        return ResponseEntity.ok(
                appointmentService.confirmPayment(appointmentId, request.paymentIntentId())
        );
    }

    @Operation(
            summary = "Cancelar cita",
            description = "Cancela una cita médica existente. Realiza las liberaciones de agenda correspondientes " +
                    "y procesa el reembolso del pago si aplica según las políticas de cancelación configuradas."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Cita cancelada exitosamente. Sin contenido en la respuesta.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado. Se requiere token JWT válido",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cita no encontrada con el ID proporcionado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "La cita no puede ser cancelada en su estado actual",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "No se puede procesar la cancelación por políticas de negocio",
                    content = @Content
            )
    })
    @PostMapping("/{appointmentId}/cancel")
    public ResponseEntity<Void> cancelAppointment(
            @PathVariable
            @Parameter(description = "UUID de la cita a cancelar",
                    example = "550e8400-e29b-41d4-a716-446655440000",
                    required = true)
            UUID appointmentId
    ) {
        appointmentService.cancelAppointment(appointmentId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Consultar detalle de cita",
            description = "Recupera la información completa de una cita específica, incluyendo participantes, " +
                    "datos del médico, paciente, evento de Google Calendar asociado y enlace de videollamada."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Información detallada de la cita recuperada exitosamente",
                    content = @Content(schema = @Schema(implementation = AppointmentResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado. Se requiere token JWT válido",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cita no encontrada con el ID proporcionado",
                    content = @Content
            )
    })
    @GetMapping("/{appointmentId}")
    public ResponseEntity<AppointmentResponse> getAppointment(
            @PathVariable
            @Parameter(description = "UUID de la cita a consultar",
                    example = "550e8400-e29b-41d4-a716-446655440000",
                    required = true)
            UUID appointmentId
    ) {
        return ResponseEntity.ok(
                appointmentService.getAppointment(appointmentId)
        );
    }

    @Operation(
            summary = "Crear sesión de pago Stripe Checkout",
            description = "Genera una sesión de checkout de Stripe para completar el pago de la cita. " +
                    "Retorna la URL de redirección al portal de pago de Stripe donde el usuario podrá " +
                    "ingresar sus datos de pago de forma segura. Se deben proporcionar las URLs de retorno " +
                    "para redireccionar al usuario después de completar o cancelar el pago."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Sesión de checkout creada exitosamente",
                    content = @Content(schema = @Schema(implementation = CheckoutSessionResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "URLs de retorno inválidas o mal formadas",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado. Se requiere token JWT válido",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cita no encontrada con el ID proporcionado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "La cita no está en estado PENDING y no puede ser pagada",
                    content = @Content
            )
    })
    @GetMapping("/{appointmentId}/checkout-session")
    public ResponseEntity<CheckoutSessionResponse> createCheckoutSession(
            @PathVariable
            @Parameter(description = "UUID de la cita para crear la sesión de pago",
                    example = "550e8400-e29b-41d4-a716-446655440000",
                    required = true)
            UUID appointmentId,
            @Parameter(description = "URL a la que Stripe redireccionará después de un pago exitoso",
                    example = "https://clinica.com/pago-exitoso",
                    required = true)
            @RequestParam String successUrl,
            @Parameter(description = "URL a la que Stripe redireccionará si el usuario cancela el pago",
                    example = "https://clinica.com/pago-cancelado",
                    required = true)
            @RequestParam String cancelUrl
    ) {
        return ResponseEntity.ok(
                appointmentService.createCheckoutSession(appointmentId, successUrl, cancelUrl)
        );
    }

    @Operation(
            summary = "Eliminar cita",
            description = "Elimina permanentemente una cita del sistema. Esta operación es irreversible y " +
                    "solo debe ejecutarse en casos administrativos excepcionales. No procesa reembolsos automáticos."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Cita eliminada permanentemente. Sin contenido en la respuesta.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado. Se requiere token JWT válido",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso denegado. Se requieren permisos administrativos",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cita no encontrada con el ID proporcionado",
                    content = @Content
            )
    })
    @DeleteMapping("/{appointmentId}")
    public ResponseEntity<Void> deleteAppointment(
            @PathVariable
            @Parameter(description = "UUID de la cita a eliminar permanentemente",
                    example = "550e8400-e29b-41d4-a716-446655440000",
                    required = true)
            UUID appointmentId
    ) {
        appointmentService.deleteAppointment(appointmentId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Confirmar sesión de checkout de Stripe",
            description = "Confirma la finalización de una sesión de checkout de Stripe y actualiza el estado de la cita. " +
                    "Este endpoint es llamado típicamente por el webhook de Stripe o por el frontend después de " +
                    "que el usuario completa el flujo de pago en el portal de Stripe."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Sesión de checkout confirmada y cita actualizada exitosamente",
                    content = @Content(schema = @Schema(implementation = AppointmentResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "ID de sesión inválido o sesión expirada",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado. Se requiere token JWT válido",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Sesión de checkout no encontrada",
                    content = @Content
            )
    })
    @PostMapping("/checkout-session/{sessionId}/confirm")
    public ResponseEntity<AppointmentResponse> confirmCheckoutSession(
            @PathVariable
            @Parameter(description = "ID de la sesión de checkout de Stripe a confirmar",
                    example = "cs_test_a1b2c3d4e5f6g7h8i9j0",
                    required = true)
            String sessionId
    ) {
        return ResponseEntity.ok(
                appointmentService.confirmCheckoutSession(sessionId)
        );
    }

    @Operation(
            summary = "Consultar información de sesión de checkout",
            description = "Recupera la información actualizada de una sesión de checkout de Stripe, " +
                    "incluyendo el estado de la sesión y el ID de la cita asociada. " +
                    "Útil para verificar el estado de un pago en proceso."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Información de la sesión recuperada exitosamente",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado. Se requiere token JWT válido",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Sesión de checkout no encontrada en Stripe",
                    content = @Content
            )
    })
    @GetMapping("/checkout-session/{sessionId}/info")
    public ResponseEntity<?> getCheckoutSessionInfo(
            @PathVariable
            @Parameter(description = "ID de la sesión de checkout de Stripe a consultar",
                    example = "cs_test_a1b2c3d4e5f6g7h8i9j0",
                    required = true)
            String sessionId) {
        StripePaymentService.CheckoutSessionInfo info = stripePaymentService.retrieveCheckoutSession(sessionId);
        return ResponseEntity.ok(Map.of(
                "sessionId", info.sessionId(),
                "appointmentId", info.appointmentId() != null ? info.appointmentId().toString() : null,
                "status", info.status()
        ));
    }

    @Operation(
            summary = "Consultar funcionalidades disponibles",
            description = "Retorna las funcionalidades habilitadas en el sistema. Actualmente indica si la integración " +
                    "con Stripe para pagos en línea está activa, lo que determina el flujo de pago a utilizar."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Funcionalidades consultadas exitosamente",
                    content = @Content
            )
    })
    @GetMapping("/features")
    public ResponseEntity<Map<String, Boolean>> getFeatures() {
        return ResponseEntity.ok(Map.of("stripeEnabled", appStripeProperties.isEnabled()));
    }

    @Operation(
            summary = "Simular confirmación de pago (Entorno de pruebas)",
            description = "Endpoint exclusivo para entornos de desarrollo y pruebas. " +
                    "Simula la confirmación de pago de una cita sin interactuar con Stripe, " +
                    "permitiendo probar el flujo completo de agendamiento sin realizar transacciones reales."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Pago simulado confirmado y cita actualizada exitosamente",
                    content = @Content(schema = @Schema(implementation = AppointmentResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado. Se requiere token JWT válido",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cita no encontrada con el ID proporcionado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "La cita no está en estado que permita simulación de pago",
                    content = @Content
            )
    })
    @PostMapping("/{appointmentId}/confirm-simulate")
    public ResponseEntity<AppointmentResponse> confirmSimulatePayment(
            @PathVariable
            @Parameter(description = "UUID de la cita para simular confirmación de pago",
                    example = "550e8400-e29b-41d4-a716-446655440000",
                    required = true)
            UUID appointmentId) {
        log.info("Simulation payment confirm requested for appointment {}", appointmentId);
        return ResponseEntity.ok(appointmentService.confirmSimulatePayment(appointmentId));
    }

}