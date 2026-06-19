package com.terraplanistas.clinic.http.stripe.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "stripe")
@Getter
@Setter
public class StripeProperties {

    private String webhookSecret;

}