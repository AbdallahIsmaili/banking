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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtFilter.class);

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final BlacklistedTokenRepository blacklistedTokenRepository;

    public JwtFilter(JwtUtil jwtUtil, UserRepository userRepository, BlacklistedTokenRepository blacklistedTokenRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.blacklistedTokenRepository = blacklistedTokenRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        // Skip filter for OAuth2 related endpoints
        String path = request.getRequestURI();
        if (path.startsWith("/oauth2") || path.startsWith("/login")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Skip if the user is already authenticated via OAuth2
        if (SecurityContextHolder.getContext().getAuthentication() instanceof OAuth2AuthenticationToken) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        String email = null;
        String jwt = null;

        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);

            // Check if token is blacklisted
            if (blacklistedTokenRepository.existsByToken(jwt)) {
                log.warn("Token is blacklisted");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token is blacklisted");
                return;
            }

            try {
                email = jwtUtil.extractEmail(jwt);
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
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            var userOptional = userRepository.findByEmail(email);

            if (userOptional.isPresent() && jwtUtil.validateToken(jwt, email)) {
                var user = userOptional.get();
                var authentication = new UsernamePasswordAuthenticationToken(
                        new User(user.getEmail(), user.getPassword(), Collections.emptyList()),
                        null,
                        Collections.emptyList()
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}