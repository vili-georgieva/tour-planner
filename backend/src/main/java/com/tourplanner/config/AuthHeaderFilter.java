package com.tourplanner.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class AuthHeaderFilter extends OncePerRequestFilter {

    // Request attribute key where the validated userId is stored for controllers.
    public static final String USER_ID_ATTR = "userId";

    private final JwtService jwtService;

    public AuthHeaderFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path == null) {
            return true;
        }

        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        if (!path.startsWith("/api/")) {
            return true;
        }

        return isAuthPath(path) || isSystemStatusPath(path);
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeUnauthorized(response, "Missing or invalid Authorization header.");
            return;
        }

        String token = authHeader.substring(7); // strip "Bearer "
        Long userId = jwtService.validateAndGetUserId(token);
        if (userId == null) {
            writeUnauthorized(response, "Invalid or expired token.");
            return;
        }

        // Store userId for controllers to read via request.getAttribute("userId")
        request.setAttribute(USER_ID_ATTR, userId);
        filterChain.doFilter(request, response);
    }

    private boolean isAuthPath(String path) {
        return path.equals("/api/auth") || path.startsWith("/api/auth/");
    }

    private boolean isSystemStatusPath(String path) {
        return path.equals("/api/system-status") || path.startsWith("/api/system-status/");
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }
}
