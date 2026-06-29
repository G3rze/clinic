package com.terraplanistas.clinic.http.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${app.base-uri:/api/v1}")
    private String baseUri;

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Clínica - Sistema de Gestión Médica Integral")
                        .version("1.0.0")
                        .description("""
                                API REST para la gestión integral de una clínica médica.
                                
                                **Módulos incluidos:**
                                -  Gestión de citas médicas con integración de pagos Stripe
                                - ‍️ Administración de médicos, especialidades y disponibilidad horaria
                                -  Registro y gestión de pacientes y dependientes familiares
                                -  Historias clínicas y notas post-consulta
                                -  Catálogo de medicamentos y recetas médicas digitales
                                -  Gestión de laboratorios farmacéuticos
                                -  Autenticación JWT y OAuth2 con Google
                                -  Sistema de calificación de consultas médicas
                                
                                **Autenticación:**
                                La mayoría de endpoints requieren token JWT en el header `Authorization: Bearer {token}`
                                """)
                        .contact(new Contact()
                                .name("Equipo de Desarrollo - Clínica Terraplanistas")
                                .email("desarrollo@terraplanistas.com")
                                .url("https://terraplanistas.com"))
                        .license(new License()
                                .name("Propietario - Uso Interno")
                                .url("https://terraplanistas.com/terminos")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort + baseUri)
                                .description(" Servidor de Desarrollo Local"),
                        new Server()
                                .url("https://api-dev.clinica.com" + baseUri)
                                .description(" Servidor de Desarrollo Remoto"),
                        new Server()
                                .url("https://api.clinica.com" + baseUri)
                                .description(" Servidor de Producción")
                ))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Ingrese el token JWT obtenido en el endpoint de autenticación.\n\n" +
                                                "**Formato:** `Bearer {token}`\n\n" +
                                                "**Obtener token:**\n" +
                                                "- Endpoint `/auth/me` con Google OAuth2\n" +
                                                "- Endpoint `/auth/refresh` para renovar token")));
    }
}