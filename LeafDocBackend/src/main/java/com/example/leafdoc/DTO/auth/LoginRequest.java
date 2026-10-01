package com.example.leafdoc.DTO.auth;

public record LoginRequest(
        String email,
        String password
) {
}
