package com.example.leafdoc.DTO.catalog;

import java.util.List;

public record DiseaseCatalogDocument(
        String catalogVersion,
        List<CropCatalogEntry> crops,
        List<DiseaseCatalogEntry> diseases
) {
}
