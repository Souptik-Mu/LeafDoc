package com.example.leafdoc.controllers;

import com.example.leafdoc.DTO.plant.PlantResponse;
import com.example.leafdoc.security.AuthenticatedUserPrincipal;
import com.example.leafdoc.util.ImageFile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/plants/{plantId}/image")
public class PlantImageController {

    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<PlantResponse> uploadImage(
            @PathVariable Long plantId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal
    ){



        return null;

    }


}
