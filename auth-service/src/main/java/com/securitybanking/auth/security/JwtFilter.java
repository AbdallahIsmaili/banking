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
import org.springframework.security.core.userdetails.User;
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
            "/login",
            "/api/test/public",
            "/");

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final BlacklistedTokenRepository blacklistedTokenRepository;

    public JwtFilter(JwtUtil jwtUtil, UserRepository userRepository,
            BlacklistedTokenRepository blacklistedTokenRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.blacklistedTokenRepository = blacklistedTokenRepository;
    }

    // cette methode est change
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        log.debug("Request path: {}", path);

        // ✅ 1. Skip filter for public paths (always allow them)
        for (String publicPath : PUBLIC_PATHS) {
            if (path.startsWith(publicPath)) {
                log.debug("Path {} is public, skipping filter", path);
                return true;
            }
        }

        // ❌ 2. For all other paths, apply filter (even if no token)
        return false;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        try {
            // Skip if the user is already authenticated via OAuth2
            if (SecurityContextHolder.getContext().getAuthentication() instanceof OAuth2AuthenticationToken) {
                log.debug("User already authenticated via OAuth2");
                filterChain.doFilter(request, response);
                return;
            }

            final String authHeader = request.getHeader("Authorization");
            String email = null;
            String jwt = null;

            if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
                jwt = authHeader.substring(7);
                log.debug("Found JWT token in Authorization header");

                // Check if token is blacklisted
                if (blacklistedTokenRepository.existsByToken(jwt)) {
                    log.warn("Token is blacklisted");
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token is blacklisted");
                    return;
                }

                try {
                    email = jwtUtil.extractEmail(jwt);
                    log.debug("Extracted email from JWT: {}", email);
                } catch (ExpiredJwtException ex) {
                    log.warn("JWT token expired: {}", ex.getMessage());
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expired");
                    return;
                } catch (SignatureException ex) {
                    log.warn("Invalid JWT signature: {}", ex.getMessage());
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
                    return;
                } catch (JwtException | IllegalArgumentException ex) {
                    log.warn("JWT token error: {}", ex.getMessage());
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
                    return;
                }
            } else {
                log.debug("No JWT token found in request headers");
                filterChain.doFilter(request, response);
                return;
            }

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                var userOptional = userRepository.findByEmail(email);

                if (userOptional.isPresent() && jwtUtil.validateToken(jwt, email)) {
                    var user = userOptional.get();
                    log.debug("JWT token valid for user: {}", email);

                    // Create granted authorities from the user role
                    var authorities = Collections.singletonList(
                            new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

                    var authentication = new UsernamePasswordAuthenticationToken(
                            email, // Use email as principal instead of User object
                            null, // No credentials needed here
                            authorities);
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    log.debug("JWT token validation failed for email: {}", email);
                }
            }

            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            log.error("Exception occurred during JWT filter: ", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }
}