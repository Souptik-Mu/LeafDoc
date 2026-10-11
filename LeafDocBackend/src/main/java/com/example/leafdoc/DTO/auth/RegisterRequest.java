package com.example.leafdoc.DTO.auth;

import com.example.leafdoc.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record RegisterRequest(
        @NotBlank
        String name,
        @Email String email,
        String password,
        Role role
) {
}
