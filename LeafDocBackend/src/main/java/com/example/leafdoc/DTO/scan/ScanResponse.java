package com.example.leafdoc.DTO.scan;

import com.example.leafdoc.enums.ScanStatus;

public record ScanResponse(
        String cropName,
        DiseaseInfo disease,
        ScanStatus scanStatus,
        boolean healthy,
        String imageUrl
) {
}
