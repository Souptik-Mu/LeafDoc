package com.example.leafdoc.DTO;

import com.example.leafdoc.entity.PlantImage;

import java.time.Instant;

public record ImageResponse(
        Long id,
        String imageUrl,
        Instant createdAt
) {}
