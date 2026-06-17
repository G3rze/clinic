package com.terraplanistas.clinic.http.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

@Component
public class CustomJwtDecoder implements JwtDecoder {

    private final JwtProperties jwtProperties;

    public CustomJwtDecoder(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Instant issuedAt = claims.getIssuedAt() != null
                    ? claims.getIssuedAt().toInstant()
                    : null;
            Instant expiresAt = claims.getExpiration() != null
                    ? claims.getExpiration().toInstant()
                    : null;

            return new Jwt(
                    token,
                    issuedAt,
                    expiresAt,
                    Map.of("alg", "HS256", "typ", "JWT"),
                    claims
            );
        } catch (io.jsonwebtoken.JwtException e) {
            throw new JwtException("Failed to decode JWT: " + e.getMessage(), e);
        }
    }
}