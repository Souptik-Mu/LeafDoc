package com.example.leafdoc.controllers;

import com.example.leafdoc.DTO.plant.CreatePlantRequest;
import com.example.leafdoc.DTO.plant.PlantResponse;
import com.example.leafdoc.security.AuthenticatedUserPrincipal;
import com.example.leafdoc.service.PlantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/plants")
@RequiredArgsConstructor
public class PlantController {

    private final PlantService plantService;

    @PostMapping     // add plant
    ResponseEntity<Void> createPlant(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @Valid @RequestBody CreatePlantRequest plantRequest
    ) {
        plantService.createPlant( principal.userId(), plantRequest );
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }

    @GetMapping     // get owned plants
    ResponseEntity<List<PlantResponse>> getMyPlants(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        plantService.getMyPlants( principal.userId() )
                );
    }

    @GetMapping("/{plantId}")     //get plant details (by id)
    ResponseEntity<PlantResponse> getMyPlant(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long plantId
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        plantService.getMyPlant( principal.userId(), plantId )
                );
    }


    @PatchMapping("/{plantId}")     //update plant (id) |parameter ?newNickname=""
    ResponseEntity<PlantResponse> renameMyPlant(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long plantId,
            @RequestParam String newNickname
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        plantService.updateMyPlant(
                                principal.userId(),
                                plantId,
                                newNickname
                        )
                );
    }

    @DeleteMapping("/{plantId}")     //delete plant (id)
    ResponseEntity<PlantResponse> deleteMyPlants(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long plantId
    ) {
        plantService.deleteMyPlant( principal.userId(), plantId );
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)    //204 (success with no response body)
                .build();
    }

    /* * | Plant Image CRUD | * */

    @PostMapping(
            value = "/{plantId}/image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PlantResponse> uploadImage(
            @PathVariable Long plantId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal
    ){



        return null;

    }

    /// IDK yet that a plant can hold multiple image or not, for now one plant one image
    //get {id}
    @GetMapping("/{plantId}/image")
    public ResponseEntity<PlantResponse> getPlantImage(
            @PathVariable Long plantId
    ){return null;}

    @PutMapping("/{plantId}/image")
    public ResponseEntity<PlantResponse> updatePlantImage(
            @PathVariable Long plantId
    ){return null;}

    //delete {id}
    @DeleteMapping("/{plantId}/image")
    public ResponseEntity<PlantResponse> deletePlantImage(
            @PathVariable Long plantId
    ){return null;}

}
