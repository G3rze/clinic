package com.terraplanistas.clinic.http.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jspecify.annotations.NullMarked;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@NullMarked
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;
    private final CookieService cookieService;

    public JwtAuthenticationFilter(JwtTokenService jwtTokenService, CookieService cookieService) {
        this.jwtTokenService = jwtTokenService;
        this.cookieService = cookieService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        // Si es una ruta pública, continuar sin autenticación
        if (isPublicPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = null;

        // Intentar obtener token del header Authorization
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        } else {
            // Si no hay header, intentar obtener de las cookies
            token = cookieService.getAccessTokenFromRequest(request);
        }

        // Si no hay token, continuar (la seguridad se manejará después)
        if (token == null || token.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String tokenType = jwtTokenService.getTokenType(token);

            // Validar que no sea un refresh token
            if ("refresh".equals(tokenType)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Refresh token cannot be used for API access\"}");
                return;
            }

            // Validar expiración del token
            if (jwtTokenService.isTokenExpired(token)) {
                request.setAttribute("token_expired", true);
                filterChain.doFilter(request, response);
                return;
            }

            // Extraer información del token
            var userId = jwtTokenService.getUserIdFromToken(token);
            List<String> roles = jwtTokenService.getRoles(token);

            // Convertir roles a autoridades de Spring Security
            List<GrantedAuthority> authorities = roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(java.util.stream.Collectors.toList());

            // Crear autenticación
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, authorities);

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // Establecer autenticación en el contexto de seguridad
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (JwtException e) {
            // Si el token es inválido, limpiar el contexto
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Verifica si la ruta solicitada es pública y no requiere autenticación JWT.
     * Incluye rutas de Swagger/OpenAPI para permitir acceso a la documentación.
     *
     * @param path URI de la solicitud
     * @return true si la ruta es pública
     */
    private boolean isPublicPath(String path) {
        return path.equals("/") ||
                path.startsWith("/health") ||
                path.startsWith("/error") ||
                path.startsWith("/oauth2/") ||
                path.startsWith("/login/oauth2/") ||
                path.startsWith("/api/public/") ||
                path.equals("/favicon.ico") ||
                // =============================================
                // SWAGGER / OPENAPI - RUTAS PÚBLICAS
                // =============================================
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-resources") ||
                path.startsWith("/webjars");
    }
}