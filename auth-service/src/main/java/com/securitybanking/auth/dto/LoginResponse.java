package com.securitybanking.auth.dto;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String role
) {}