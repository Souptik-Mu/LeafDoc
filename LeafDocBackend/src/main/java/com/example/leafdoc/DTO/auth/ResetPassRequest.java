package com.example.leafdoc.DTO.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPassRequest(
        @NotBlank String token,
        @NotBlank @Size(min = 8) String newPassword
) {
}
