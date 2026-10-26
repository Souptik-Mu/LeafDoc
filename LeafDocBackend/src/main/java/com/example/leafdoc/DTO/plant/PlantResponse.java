package com.example.leafdoc.DTO.plant;

import com.example.leafdoc.entity.Plant;
import com.example.leafdoc.enums.CropType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PlantResponse (
        @NotNull  Long id,
        @NotBlank String nickname,
        @NotNull  CropType cropType
) {
        public static PlantResponse from(Plant plant) {
                return new  PlantResponse(plant.getId(), plant.getNickname(), plant.getCropType());
        }
}
