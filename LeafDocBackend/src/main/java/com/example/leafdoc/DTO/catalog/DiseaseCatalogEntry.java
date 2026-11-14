package com.example.leafdoc.DTO.catalog;

import java.util.List;

public record DiseaseCatalogEntry(
        String cropCode,
        String mlClassLabel,
        String name,
        String description,
        List<String> symptoms,
        List<String> causes,
        List<String> treatment,
        List<String> prevention,
        List<String> careTips,
        boolean isHealthy
) {
}
