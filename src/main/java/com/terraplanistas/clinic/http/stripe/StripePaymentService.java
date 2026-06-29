package com.terraplanistas.clinic.http.stripe;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import com.terraplanistas.clinic.http.config.StripeApiProperties;
import com.terraplanistas.clinic.http.exceptions.ExternalApiException;
import com.terraplanistas.clinic.http.stripe.dto.PaymentIntentCreateRequest;
import com.terraplanistas.clinic.http.stripe.dto.PaymentIntentResponse;
import com.terraplanistas.clinic.http.stripe.dto.RefundRequest;
import com.terraplanistas.clinic.http.stripe.dto.RefundResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Service
public class StripePaymentService extends StripeApiService {

    public StripePaymentService(RestClient stripeRestClient, StripeApiProperties properties) {
        super(stripeRestClient, properties.getBaseUrl(), properties.getKey());
    }


    public PaymentIntentResponse createPaymentIntent(PaymentIntentCreateRequest request) {
        try {
            PaymentIntentCreateParams.Builder paramsBuilder = PaymentIntentCreateParams.builder()
                    .setAmount(centsFromDecimal(request.amount()))
                    .setCurrency(request.currency().toLowerCase());

            if (request.metadata() != null && !request.metadata().isEmpty()) {
                paramsBuilder.putAllMetadata(request.metadata());
            }

            PaymentIntent paymentIntent = PaymentIntent.create(paramsBuilder.build());

            return new PaymentIntentResponse(
                    paymentIntent.getId(),
                    paymentIntent.getStatus(),
                    paymentIntent.getClientSecret(),
                    decimalFromCents(paymentIntent.getAmount()),
                    paymentIntent.getCurrency(),
                    extractReceiptId(paymentIntent.getMetadata())
            );
        } catch (StripeException e) {
            throw new ExternalApiException("STRIPE", e.getStripeError() != null ? e.getStripeError().getMessage() : e.getMessage(),
                    e.getStatusCode(), e.getStripeError() != null ? e.getStripeError().getCode() : "UNKNOWN");
        }
    }

    public PaymentIntentResponse getPaymentIntent(String paymentIntentId) {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

            return new PaymentIntentResponse(
                    paymentIntent.getId(),
                    paymentIntent.getStatus(),
                    paymentIntent.getClientSecret(),
                    decimalFromCents(paymentIntent.getAmount()),
                    paymentIntent.getCurrency(),
                    extractReceiptId(paymentIntent.getMetadata())
            );
        } catch (StripeException e) {
            throw new ExternalApiException("STRIPE", e.getStripeError() != null ? e.getStripeError().getMessage() : e.getMessage(),
                    e.getStatusCode(), e.getStripeError() != null ? e.getStripeError().getCode() : "UNKNOWN");
        }
    }

    public RefundResponse refund(RefundRequest request) {
        try {
            RefundCreateParams.Builder paramsBuilder = RefundCreateParams.builder()
                    .setPaymentIntent(request.paymentIntentId());

            if (request.amount() != null) {
                paramsBuilder.setAmount(centsFromDecimal(request.amount()));
            }

            Refund refund = Refund.create(paramsBuilder.build());

            return new RefundResponse(
                    refund.getId(),
                    refund.getStatus(),
                    decimalFromCents(refund.getAmount()),
                    refund.getPaymentIntent()
            );
        } catch (StripeException e) {
            throw new ExternalApiException("STRIPE", e.getStripeError() != null ? e.getStripeError().getMessage() : e.getMessage(),
                    e.getStatusCode(), e.getStripeError() != null ? e.getStripeError().getCode() : "UNKNOWN");
        }
    }

    private Long centsFromDecimal(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100)).longValue();
    }

    private BigDecimal decimalFromCents(Long cents) {
        if (cents == null) return BigDecimal.ZERO;
        return BigDecimal.valueOf(cents).divide(BigDecimal.valueOf(100));
    }

    private String extractReceiptId(Map<String, String> metadata) {
        if (metadata == null) return null;
        return metadata.get("receipt_id");
    }

    public String createCheckoutSession(UUID appointmentId, BigDecimal amount, String currency, String successUrl, String cancelUrl) {
        try {
            SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl)
                    .setCancelUrl(cancelUrl)
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency(currency.toLowerCase())
                                                    .setUnitAmount(centsFromDecimal(amount))
                                                    .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                            .setName("Medical Appointment Consultation")
                                                            .build())
                                                    .build()
                                    )
                                    .build()
                    )
                    .putMetadata("appointment_id", appointmentId.toString());

            Session session = Session.create(paramsBuilder.build());
            return session.getUrl();
        } catch (StripeException e) {
            throw new ExternalApiException("STRIPE",
                    e.getStripeError() != null ? e.getStripeError().getMessage() : e.getMessage(),
                    e.getStatusCode(),
                    e.getStripeError() != null ? e.getStripeError().getCode() : "UNKNOWN");
        }
    }

    public record CheckoutSessionInfo(String sessionId, String paymentIntentId, String status, UUID appointmentId) {}

    public CheckoutSessionInfo retrieveCheckoutSession(String sessionId) {
        try {
            Session session = Session.retrieve(sessionId);
            String paymentIntentId = session.getPaymentIntent();
            String status = session.getStatus();
            UUID appointmentId = null;
            if (session.getMetadata() != null && session.getMetadata().containsKey("appointment_id")) {
                appointmentId = UUID.fromString(session.getMetadata().get("appointment_id"));
            }
            return new CheckoutSessionInfo(sessionId, paymentIntentId, status, appointmentId);
        } catch (StripeException e) {
            throw new ExternalApiException("STRIPE",
                    e.getStripeError() != null ? e.getStripeError().getMessage() : e.getMessage(),
                    e.getStatusCode(),
                    e.getStripeError() != null ? e.getStripeError().getCode() : "UNKNOWN");
        }
    }
}
