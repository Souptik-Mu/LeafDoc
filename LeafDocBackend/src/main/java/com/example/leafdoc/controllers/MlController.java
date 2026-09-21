package com.example.leafdoc.controllers;

import com.example.leafdoc.service.MlService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/ml")
public class MlController {

    private final MlService mlService;

    public MlController(MlService mlService) {
        this.mlService = mlService;
    }

    @PostMapping(
            value = "/predict",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public String predict(
            @RequestParam("file") MultipartFile file
    ) throws IOException {

        return ""; //mlService.predict(file);
    }
}
