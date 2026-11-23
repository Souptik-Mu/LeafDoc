package com.example.leafdoc.DTO.auth.token;

import com.example.leafdoc.enums.Role;

public record PendingRegistrationPayload(
        String name,
        String email,
        String passwordHash,
        Role role
) implements TokenData {
}
