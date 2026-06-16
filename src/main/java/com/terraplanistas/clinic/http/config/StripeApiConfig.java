package com.terraplanistas.clinic.http.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class StripeApiConfig {

    private final StripeApiProperties properties;

    public StripeApiConfig(StripeApiProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void init() {
        Stripe.apiKey = properties.getKey();
    }

    @Bean
    public RestClient stripeRestClient() {
        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeaders(headers -> headers.set("Authorization", "Bearer " + properties.getKey()))
                .build();
    }
}
