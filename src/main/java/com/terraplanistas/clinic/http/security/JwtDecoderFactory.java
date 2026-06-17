package com.terraplanistas.clinic.http.security;

import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtDecoderFactory {

    private final JwtProperties jwtProperties;

    public JwtDecoderFactory(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }
}