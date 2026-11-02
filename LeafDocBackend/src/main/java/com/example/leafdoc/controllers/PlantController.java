package com.example.leafdoc.controllers;

import com.example.leafdoc.DTO.plant.CreatePlantRequest;
import com.example.leafdoc.DTO.ImageResponse;
import com.example.leafdoc.DTO.plant.PlantResponse;
import com.example.leafdoc.security.AuthenticatedUserPrincipal;
import com.example.leafdoc.service.PlantService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

    @GetMapping     // get owned plants | GET /plants?page=0&size=20&sort=id,desc
    ResponseEntity<Page<PlantResponse>> getMyPlants(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PageableDefault(size = 20)
            Pageable pageable
    ) {
        Pageable limited = PageRequest.of(
                pageable.getPageNumber(),
                Math.min(pageable.getPageSize(), 100),
                Sort.by(Sort.Direction.DESC, "id")
        );
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        plantService.getMyPlants( principal.userId(), limited )
                );
    }

    @GetMapping("/{plantId}")     //get plant details (by id) + latest Image
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

    @PatchMapping("/{plantId}")     //update plant (id)
    ResponseEntity<PlantResponse> renameMyPlant(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long plantId,
            @NotBlank @Size(max=20) @RequestBody String newNickname
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
    ResponseEntity<Void> deleteMyPlant(
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
    public ResponseEntity<ImageResponse> uploadImage(
            @PathVariable Long plantId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal
    ){
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        plantService.uploadImage(
                                principal.userId(),
                                plantId,
                                file
                        )
                );
    }

    @GetMapping("/{plantId}/image")     //GET /plants/1/image?page=0&size=20
    public ResponseEntity<Page<ImageResponse>> getRecentPlantImages(
            @PathVariable Long plantId,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal
    ){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        plantService.getRecentPlantImages(
                                principal.userId(),
                                plantId,
                                pageable
                        )
                );
    }

    @DeleteMapping("/{plantId}/image/{imageId})")
    public ResponseEntity<Void> deletePlantImage(
            @PathVariable Long plantId,
            @PathVariable Long imageId,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal
    ){
        plantService.deletePlantImage(principal.userId(), plantId, imageId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
