package com.example.leafdoc.controllers;

import com.example.leafdoc.DTO.scan.ScanHistoryResponse;
import com.example.leafdoc.DTO.scan.ScanResponse;
import com.example.leafdoc.entity.User;
import com.example.leafdoc.security.AuthenticatedUserPrincipal;
import com.example.leafdoc.service.ScanService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/scan")
@RequiredArgsConstructor
public class ScanController {
    private final ScanService scanService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ScanResponse> createScan(
            @RequestPart("image") MultipartFile image,
            @AuthenticationPrincipal AuthenticatedUserPrincipal user
    ) {

        ScanResponse response = scanService.createScan(
                image,
                user.userId()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public Page<ScanHistoryResponse> getHistory(
            @AuthenticationPrincipal AuthenticatedUserPrincipal user,
            @PageableDefault(size = 10, sort = "scannedAt",
                    direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return scanService.getScanHistory(user.userId(), pageable);
    }

    @GetMapping("/{id}")
    public ScanResponse getDetails(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUserPrincipal user
    ) {
        return scanService.getScanDetails(id, user.userId());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUserPrincipal user
    ) {
        scanService.deleteScan(id, user.userId());
    }
}
