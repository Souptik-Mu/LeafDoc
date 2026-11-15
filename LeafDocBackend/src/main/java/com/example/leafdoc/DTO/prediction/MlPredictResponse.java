package com.example.leafdoc.DTO.prediction;

import java.util.List;

public record MlPredictResponse(
        //Dictionary<String, Double> prediction, //mlClassLabel, score
        String predictionStatus,
        List<PredictionItem> prediction,
        ModelInfo modelInfo
) {
}

