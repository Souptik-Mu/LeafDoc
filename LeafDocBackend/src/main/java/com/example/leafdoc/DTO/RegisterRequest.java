package com.example.leafdoc.DTO;

import com.example.leafdoc.enums.Role;

public record RegisterRequest(
        String name,
        String email,
        String password,
        Role role
) {
}
