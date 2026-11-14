package com.example.leafdoc.controllers;

import com.example.leafdoc.DTO.scan.ScanResponse;
import com.example.leafdoc.security.AuthenticatedUserPrincipal;
import com.example.leafdoc.service.ScanService;
import lombok.RequiredArgsConstructor;
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
            @RequestParam(required = false) Long plantId,
            @AuthenticationPrincipal AuthenticatedUserPrincipal user
    ) {

        ScanResponse response = scanService.createScan(
                image,
                plantId,
                user.userId()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
