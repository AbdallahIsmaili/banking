package com.securitybanking.auth.controller;

import com.securitybanking.auth.dto.*;
import com.securitybanking.auth.entity.AppUser;
import com.securitybanking.auth.entity.BlacklistedToken;
import com.securitybanking.auth.repository.BlacklistedTokenRepository;
import com.securitybanking.auth.security.JwtUtil;
import com.securitybanking.auth.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final BlacklistedTokenRepository blacklistedTokenRepository;

    public AuthController(
            AuthService authService,
            JwtUtil jwtUtil,
            BlacklistedTokenRepository blacklistedTokenRepository
    ) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
        this.blacklistedTokenRepository = blacklistedTokenRepository;
    }

    @GetMapping("/")
    public Map<String, String> home() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "API is running");
        response.put("message", "Welcome to Security Banking Authentication Service");
        return response;
    }

    @PostMapping("/register")
    public ResponseEntity<AppUser> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            Date expiry = jwtUtil.extractExpiration(token);
            blacklistedTokenRepository.save(new BlacklistedToken(token, expiry));
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            authService.logout(authentication.getName());
            SecurityContextHolder.clearContext();
        }

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile(HttpServletRequest request) {
        // Use HttpServletRequest instead of relying on Spring's Authentication object
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                String email = jwtUtil.extractEmail(token);
                if (email != null && !jwtUtil.isTokenExpired(token) && !blacklistedTokenRepository.existsByToken(token)) {
                    return ResponseEntity.ok(authService.getUserProfile(email));
                }
            } catch (Exception e) {
                // Log the exception
                System.err.println("Error processing token: " + e.getMessage());
            }
        }

        // If reaching here, authentication failed
        return ResponseEntity.status(401).build();
    }

    @GetMapping("/oauth2/success")
    public ResponseEntity<String> oauthSuccess() {
        // This endpoint is just to confirm the OAuth2 flow completed successfully
        // Actual redirects are handled by OAuth2SuccessHandler
        return ResponseEntity.ok("OAuth2 authentication successful");
    }
}