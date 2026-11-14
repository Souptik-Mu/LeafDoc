package com.example.leafdoc.DTO.prediction;

import java.math.BigDecimal;

public record PredictionItem(
        String mlClassLabel,
        BigDecimal confidenceScore
) {}
