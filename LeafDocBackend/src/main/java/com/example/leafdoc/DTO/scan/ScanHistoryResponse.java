package com.example.leafdoc.DTO.scan;

import com.example.leafdoc.entity.Scan;
import com.example.leafdoc.enums.ScanStatus;

import java.time.Instant;

public record ScanHistoryResponse(
        Long id,
        String title,
        ScanStatus status,
        Instant createdAt
) {
    public static ScanHistoryResponse from(Scan scan) {
        return new ScanHistoryResponse(
                scan.getId(),
                scan.getTitle(),
                scan.getStatus(),
                scan.getScannedAt()
        );
    }
}
