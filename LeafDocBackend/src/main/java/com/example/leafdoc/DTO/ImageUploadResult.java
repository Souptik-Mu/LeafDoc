package com.example.leafdoc.DTO;

public record ImageUploadResult(
        String path,
        String contentType,
        String publicUrl
) {
}
