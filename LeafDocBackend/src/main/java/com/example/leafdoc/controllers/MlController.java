package com.example.leafdoc.controllers;

import com.example.leafdoc.service.MlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/ml")
public class MlController {

    private final MlService mlService;

//    public MlController(MlService mlService) {
//        this.mlService = mlService;
//    }

    @PostMapping(
            value = "/predict",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public String predict(
            @RequestParam("file") MultipartFile file
    ) throws IOException {

        return ""; //mlService.predict(file);
    }
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> test() {
        //mlService.check();
        return ResponseEntity.ok().body(mlService.check());
    }
}
