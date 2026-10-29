package com.example.leafdoc.DTO;

import com.example.leafdoc.entity.PlantImage;

import java.time.Instant;

public record ImageResponse(
        Long id,
        String imageUrl,
        Instant createdAt
) {
    public static ImageResponse from(PlantImage image) {
        return new ImageResponse(
                image.getId(),
                image.getObjectPath(), // Replace with URL generation if needed
                image.getCreatedAt()
        );
    }
}
