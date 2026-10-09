package com.example.leafdoc.util;

import com.example.leafdoc.enums.Role;

public record PendingRegistrationPayload(
        String name,
        String email,
        String passwordHash,
        Role role
) {
}
