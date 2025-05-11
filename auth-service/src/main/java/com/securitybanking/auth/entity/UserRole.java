package com.securitybanking.auth.entity;

public enum UserRole {
    USER,       // Regular banking customer
    EMPLOYEE,   // Bank staff with limited privileges
    ADMIN;      // System administrator with full access

    public static UserRole fromString(String role) {
        try {
            return UserRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
    }
}