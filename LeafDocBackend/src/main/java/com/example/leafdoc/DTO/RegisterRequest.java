package com.example.leafdoc.DTO;

import com.example.leafdoc.enums.Role;
import jakarta.validation.constraints.*;

public record RegisterRequest(
        @NotBlank @Size(max = 100)
        String name,

        @NotNull @NotBlank @Email
        String email,

        @NotBlank @Size(min = 8)
        String password,

        @NotNull Role role
) {
}
