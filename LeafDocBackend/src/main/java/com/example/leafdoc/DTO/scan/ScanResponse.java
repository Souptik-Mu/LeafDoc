package com.example.leafdoc.DTO.scan;

import com.example.leafdoc.entity.Disease;
import com.example.leafdoc.entity.Scan;
import com.example.leafdoc.enums.ScanStatus;

import java.time.Duration;
import java.time.Instant;

public record ScanResponse(
        String cropName,
        DiseaseInfo disease,
        ScanStatus scanStatus,
        boolean healthy,
        String imageUrl,
        Instant createdAt
) {
    public static ScanResponse from(Scan scan, String publicImageUrl) {
        Disease disease = scan.getDisease();
        return  new ScanResponse(
                disease == null ? null : disease.getCrop().getName(),
                DiseaseInfo.from(disease),
                scan.getStatus(),
                disease != null && disease.isHealthy(),
                publicImageUrl,
                scan.getScannedAt()
        );
    }
}
