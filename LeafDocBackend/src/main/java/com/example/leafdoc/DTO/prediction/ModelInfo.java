package com.example.leafdoc.DTO.prediction;

public record ModelInfo(
        String modelName,
        String modelVersion,
        Integer inputSize
) {}
