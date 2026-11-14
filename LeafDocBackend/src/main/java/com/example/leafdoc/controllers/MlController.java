package com.example.leafdoc.controllers;

import com.example.leafdoc.DTO.prediction.MlPredictResponse;
import com.example.leafdoc.client.MlServiceClient;
import com.example.leafdoc.util.ImageFile;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/ml")
public class MlController {

    private final MlServiceClient mlServiceClient;

    @PostMapping(
            value = "/predict",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<MlPredictResponse> predict(
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.ok().body(mlServiceClient.predict(new ImageFile(file)));
    }
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> test() {
        return ResponseEntity.ok().body(mlServiceClient.check());
    }
}
