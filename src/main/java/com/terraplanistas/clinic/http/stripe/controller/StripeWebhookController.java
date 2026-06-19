package com.terraplanistas.clinic.http.stripe.controller;


import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;

import com.terraplanistas.clinic.http.stripe.properties.StripeProperties;
import com.terraplanistas.clinic.http.stripe.services.StripeWebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/stripe/webhooks")
@RequiredArgsConstructor
public class StripeWebhookController {

    private final StripeWebhookService stripeWebhookService;
    private final StripeProperties stripeProperties;

    @PostMapping
    public ResponseEntity<Void> webhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature
    ) {

        Event event;

        try {

            event = Webhook.constructEvent(
                    payload,
                    signature,
                    stripeProperties.getWebhookSecret()
            );

        } catch (Exception ex) {

            return ResponseEntity.badRequest().build();
        }

        switch (event.getType()) {

            case "payment_intent.succeeded" -> {

                PaymentIntent paymentIntent =
                        (PaymentIntent) event
                                .getDataObjectDeserializer()
                                .getObject()
                                .orElseThrow();

                stripeWebhookService.handlePaymentSucceeded(
                        paymentIntent.getId()
                );
            }

            case "payment_intent.payment_failed" -> {

                PaymentIntent paymentIntent =
                        (PaymentIntent) event
                                .getDataObjectDeserializer()
                                .getObject()
                                .orElseThrow();

                stripeWebhookService.handlePaymentFailed(
                        paymentIntent.getId()
                );
            }

            default -> {
            }
        }

        return ResponseEntity.ok().build();
    }
}