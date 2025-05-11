package com.securitybanking.auth.dto;

import java.time.LocalDateTime;

public record UserProfileResponse(
        String email,
        String fullname,
        String role,
        LocalDateTime lastLogin
) {}
