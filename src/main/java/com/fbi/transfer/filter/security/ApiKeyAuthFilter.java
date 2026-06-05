package com.fbi.transfer.filter.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    @Value("${fbi.auth.api-key}")
    private String configuredApiKey;

    private static final String HEADER_NAME = "X-FIB-AUTH";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();

        // -----------------------------------
        // 1. EXCLUDE PUBLIC / SWAGGER ENDPOINTS
        // -----------------------------------
        if (isPublicEndpoint(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // -----------------------------------
        // 2. VALIDATE API KEY
        // -----------------------------------
        String apiKey = request.getHeader(HEADER_NAME);

        if (apiKey == null || apiKey.isBlank() || !apiKey.equals(configuredApiKey)) {
            writeUnauthorized(response, path);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicEndpoint(String path) {
        return path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-resources")
                || path.startsWith("/webjars");
    }

    private void writeUnauthorized(HttpServletResponse response, String path) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");

        response.getWriter().write("""
            {
              "timestamp": "%s",
              "status": 401,
              "error": "Unauthorized",
              "message": "Invalid or missing API key",
              "path": "%s"
            }
            """.formatted(Instant.now(), path));
    }
}