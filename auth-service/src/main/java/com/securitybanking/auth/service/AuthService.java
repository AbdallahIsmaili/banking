package com.securitybanking.auth.service;

import com.securitybanking.auth.dto.*;
import com.securitybanking.auth.entity.AppUser;
import com.securitybanking.auth.entity.UserRole;
import com.securitybanking.auth.exception.AuthException;
import com.securitybanking.auth.repository.UserRepository;
import com.securitybanking.auth.security.JwtUtil;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AppUser register(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new AuthException("Email already in use");
        }

        AppUser user = new AppUser();
        user.setFullname(request.fullname());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.fromString(request.role()));
        user.setEnabled(true);
        user.setLocked(false);

        return userRepository.save(user);
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        AppUser user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        if (!user.isEnabled()) {
            throw new AuthException("Account is disabled");
        }

        if (user.isLocked()) {
            throw new AuthException("Account is locked");
        }

        String accessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        user.setLastLogin(LocalDateTime.now());
        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        return new LoginResponse(accessToken, refreshToken, user.getRole().name());
    }

    @Transactional
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        String email = jwtUtil.extractEmail(request.refreshToken());

        if (!jwtUtil.validateToken(request.refreshToken(), email)) {
            throw new AuthException("Invalid refresh token");
        }

        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (user.getRefreshToken() == null || !user.getRefreshToken().equals(request.refreshToken())) {
            throw new AuthException("Refresh token doesn't match");
        }

        String newAccessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getRole());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        user.setRefreshToken(newRefreshToken);
        userRepository.save(user);

        return new LoginResponse(newAccessToken, newRefreshToken, user.getRole().name());
    }

    @Transactional
    public void logout(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setRefreshToken(null);
            userRepository.save(user);
        });
    }

    public UserProfileResponse getUserProfile(String email) {
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new UserProfileResponse(
                user.getEmail(),
                user.getFullname(),
                user.getRole().name(),
                user.getLastLogin()
        );
    }
}