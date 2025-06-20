package com.securitybanking.auth.controller;

import com.securitybanking.auth.dto.*;
import com.securitybanking.auth.entity.AppUser;
import com.securitybanking.auth.entity.BlacklistedToken;
import com.securitybanking.auth.repository.BlacklistedTokenRepository;
import com.securitybanking.auth.security.JwtUtil;
import com.securitybanking.auth.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final BlacklistedTokenRepository blacklistedTokenRepository;

    public AuthController(
            AuthService authService,
            JwtUtil jwtUtil,
            BlacklistedTokenRepository blacklistedTokenRepository) {
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
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
        Map<String, String> response = new HashMap<>();

        try {
            String authHeader = request.getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String userEmail = null;

                try {
                    userEmail = jwtUtil.extractEmail(token);

                    // Only process token if it's valid and not expired
                    if (userEmail != null && !jwtUtil.isTokenExpired(token)) {
                        // Blacklist the token
                        Date expiry = jwtUtil.extractExpiration(token);
                        blacklistedTokenRepository.save(new BlacklistedToken(token, expiry));

                        // Clear refresh token from database
                        authService.logout(userEmail);
                    }
                } catch (Exception e) {
                    response.put("message", "Invalid token during logout: " + e.getMessage());
                }
            }

            // Clear security context in all cases
            SecurityContextHolder.clearContext();

            response.put("status", "success");
            response.put("message", "Logout completed");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error during logout");
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Principal principal, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();

        logger.debug("Profile endpoint called");
        logger.debug("Principal: {}", principal);

        // Get authentication from security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        logger.debug("Authentication: {}", authentication);
        logger.debug("Authentication class: {}", authentication != null ? authentication.getClass().getSimpleName() : "null");

        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("No authentication found or not authenticated");
            response.put("status", "error");
            response.put("authenticated", false);
            response.put("message", "No user is currently authenticated");
            return ResponseEntity.status(401).body(response);
        }

        try {
            String email = null;
            String authMethod = "UNKNOWN";

            // Handle different authentication types
            if (authentication instanceof OAuth2AuthenticationToken) {
                logger.debug("Processing OAuth2 authentication");
                OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;
                OAuth2User oauth2User = oauth2Token.getPrincipal();
                email = (String) oauth2User.getAttributes().get("email");
                authMethod = "OAUTH2";
                logger.debug("OAuth2 email: {}", email);
            } else {
                // For JWT authentication, the principal name should be the email
                email = authentication.getName();
                authMethod = "JWT";
                logger.debug("JWT email: {}", email);
            }

            if (email == null || email.isEmpty()) {
                logger.error("Could not extract email from authentication");
                response.put("status", "error");
                response.put("message", "Could not extract user email from authentication");
                return ResponseEntity.status(500).body(response);
            }

            UserProfileResponse userProfile = authService.getUserProfile(email);
            logger.debug("Retrieved user profile: {}", userProfile);

            response.put("status", "success");
            response.put("authenticated", true);
            response.put("user", userProfile);
            response.put("authMethod", authMethod);
            response.put("message", "Profile retrieved successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error retrieving profile", e);
            response.put("status", "error");
            response.put("message", "Error retrieving profile: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/oauth2/success")
    public ResponseEntity<Map<String, String>> oauthSuccess() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "OAuth2 authentication successful");
        return ResponseEntity.ok(response);
    }
}