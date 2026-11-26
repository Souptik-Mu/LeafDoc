package com.example.leafdoc.DTO.auth.token;

public record PasswordResetPayload(
        String email
) implements TokenData {
}
