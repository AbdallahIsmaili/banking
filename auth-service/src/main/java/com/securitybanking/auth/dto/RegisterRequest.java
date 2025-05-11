package com.securitybanking.auth.dto;

public record RegisterRequest(
        String fullname,
        String email,
        String password,
        String role
) {}