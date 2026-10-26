package com.example.leafdoc.DTO.plant;

import com.example.leafdoc.enums.CropType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

public record CreatePlantRequest(
        @NotBlank @Size(max = 20)
        String nickname,

        @NotNull
        CropType cropType
) {
}
