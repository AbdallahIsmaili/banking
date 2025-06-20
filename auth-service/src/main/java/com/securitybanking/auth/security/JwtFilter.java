package com.securitybanking.auth.security;

import com.securitybanking.auth.repository.BlacklistedTokenRepository;
import com.securitybanking.auth.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtFilter.class);
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/auth/",
            "/oauth2",
            "/login/oauth2",
            "/api/auth/oauth2",
            "/api/test/public",
            "/",
            "/error",
            "/actuator/health",
            "/actuator/info");

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final BlacklistedTokenRepository blacklistedTokenRepository;

    public JwtFilter(JwtUtil jwtUtil, UserRepository userRepository,
                     BlacklistedTokenRepository blacklistedTokenRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.blacklistedTokenRepository = blacklistedTokenRepository;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        log.debug("Request path: {}", path);

        // Skip filter for public paths
        for (String publicPath : PUBLIC_PATHS) {
            if (path.startsWith(publicPath)) {
                log.debug("Path {} is public, skipping filter", path);
                return true;
            }
        }

        // Skip for OPTIONS requests (CORS preflight)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            log.debug("OPTIONS request, skipping filter");
            return true;
        }

        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            log.debug("Starting JWT filter processing for request: {}", request.getRequestURI());

            // Check if OAuth2 authentication is already present
            if (SecurityContextHolder.getContext().getAuthentication() instanceof OAuth2AuthenticationToken) {
                log.debug("OAuth2 authentication already present, skipping JWT processing");
                filterChain.doFilter(request, response);
                return;
            }

            final String authHeader = request.getHeader("Authorization");
            String email = null;
            String jwt = null;

            // Extract JWT token from Authorization header
            if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
                jwt = authHeader.substring(7);
                log.debug("JWT token found in Authorization header");

                try {
                    // Extract email from token
                    email = jwtUtil.extractEmail(jwt);
                    log.debug("Extracted email from JWT: {}", email);

                    // Check if token is blacklisted
                    if (blacklistedTokenRepository.existsByToken(jwt)) {
                        log.warn("Token is blacklisted for user: {}", email);
                        sendJsonErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                                "Token is blacklisted", "BLACKLISTED_TOKEN");
                        return;
                    }

                    // Validate token
                    if (!jwtUtil.validateToken(jwt, email)) {
                        log.warn("Token validation failed for user: {}", email);
                        sendJsonErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                                "Invalid or expired token", "INVALID_TOKEN");
                        return;
                    }

                } catch (ExpiredJwtException e) {
                    log.warn("JWT token is expired: {}", e.getMessage());
                    sendJsonErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                            "Token has expired", "EXPIRED_TOKEN");
                    return;
                } catch (SignatureException e) {
                    log.warn("JWT signature validation failed: {}", e.getMessage());
                    sendJsonErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                            "Invalid token signature", "INVALID_SIGNATURE");
                    return;
                } catch (JwtException e) {
                    log.warn("JWT processing error: {}", e.getMessage());
                    sendJsonErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                            "Invalid token format", "INVALID_TOKEN_FORMAT");
                    return;
                } catch (Exception e) {
                    log.error("Unexpected error during token validation", e);
                    sendJsonErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                            "Token validation error", "TOKEN_VALIDATION_ERROR");
                    return;
                }
            }

            // Set authentication if user is valid and not already authenticated
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                log.debug("Attempting to authenticate user: {}", email);

                try {
                    var userOptional = userRepository.findByEmail(email);
                    if (userOptional.isPresent()) {
                        var user = userOptional.get();
                        log.debug("User found in database: {}", email);

                        // Create authorities based on user role
                        var authorities = Collections.singletonList(
                                new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

                        // Create authentication token
                        var authentication = new UsernamePasswordAuthenticationToken(
                                email, null, authorities);
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        // Set authentication in security context
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.debug("Authentication set in security context for user: {}", email);
                    } else {
                        log.warn("User not found in database: {}", email);
                        sendJsonErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                                "User not found", "USER_NOT_FOUND");
                        return;
                    }
                } catch (Exception e) {
                    log.error("Error during user lookup", e);
                    sendJsonErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                            "Database error during authentication", "DATABASE_ERROR");
                    return;
                }
            }

            // Continue with the filter chain
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error("JWT filter error", e);
            // Don't send error response here to avoid double response writing
            // Let the exception handler deal with it
            throw new ServletException("JWT filter error", e);
        }
    }

    private void sendJsonErrorResponse(HttpServletResponse response, int status, String message, String errorCode) throws IOException {
        if (response.isCommitted()) {
            log.warn("Response already committed, cannot send error response");
            return;
        }

        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String jsonResponse = String.format("""
            {
                "timestamp": "%s",
                "status": %d,
                "error": "%s",
                "message": "%s",
                "errorCode": "%s"
            }
            """,
                java.time.LocalDateTime.now(),
                status,
                status == 401 ? "Unauthorized" : "Internal Server Error",
                message,
                errorCode
        );

        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
}