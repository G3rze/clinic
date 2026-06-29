package com.terraplanistas.clinic.http.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        Map<String, Object> body;
        if (Boolean.TRUE.equals(request.getAttribute("token_expired"))) {
            body = Map.of(
                    "error", "Token Expired",
                    "message", "Your session has expired. Please login again.",
                    "path", request.getRequestURI(),
                    "status", 401,
                    "expired", true
            );
        } else {
            body = Map.of(
                    "error", "Unauthorized",
                    "message", "Authentication required to access this resource",
                    "path", request.getRequestURI(),
                    "status", 401
            );
        }

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}