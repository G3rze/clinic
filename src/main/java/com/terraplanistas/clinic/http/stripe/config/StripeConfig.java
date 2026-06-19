package com.terraplanistas.clinic.http.stripe.config;

import com.terraplanistas.clinic.http.stripe.properties.StripeProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(
        StripeProperties.class
)
public class StripeConfig {
}
