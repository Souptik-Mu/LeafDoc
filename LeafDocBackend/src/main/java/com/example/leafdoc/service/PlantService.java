package com.example.leafdoc.service;

import com.example.leafdoc.DTO.ImageUploadResult;
import com.example.leafdoc.DTO.plant.CreatePlantRequest;
import com.example.leafdoc.DTO.plant.PlantResponse;
import com.example.leafdoc.client.ImageStorageClient;
import com.example.leafdoc.entity.Plant;
import com.example.leafdoc.entity.User;
import com.example.leafdoc.exceptions.ResourceNotFoundException;
import com.example.leafdoc.repository.PlantRepository;
import com.example.leafdoc.util.ImageFile;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlantService {

    private final EntityManager entityManager; // spring provides
    private final ImageStorageClient imageStorageClient;
    private final PlantRepository plantRepo;

    public void createPlant(
            Long userId,
            CreatePlantRequest request
    ){
        /// assumes the reference is valid. doesn't load the full object.
        User user = entityManager.getReference(User.class, userId);

        Plant newPlant = new Plant();
        newPlant.setUser(user);
        newPlant.setNickname(request.nickname());
        newPlant.setCropType(request.cropType());

        plantRepo.save(newPlant);
    }

    public List<PlantResponse> getMyPlants(
            Long userId
    ){
        List<Plant> plantList =  plantRepo.findAllByUserId(userId);

        return  plantList
                .stream()
                .map(PlantResponse::from)
                .toList();
    }

    public PlantResponse getMyPlant(
            Long userId,
            Long plantId
    ){
       return  PlantResponse.from(
                plantRepo.findByIdAndUserId(userId, plantId)
                        .orElseThrow(ResourceNotFoundException::new)
       );
    }

    @Transactional
    public PlantResponse updateMyPlant(
            Long userId,
            Long plantId,
            String newNickname
    ){
        Plant plant = plantRepo.findByIdAndUserId(userId, plantId)
                .orElseThrow(ResourceNotFoundException::new);

        plant.setNickname(newNickname);

        return PlantResponse.from(
                plantRepo.save(plant)
        );
    }

    public void deleteMyPlant(
            Long userId,
            Long plantId
    ){
        int c = plantRepo.deleteByPlantIdAndUserId(userId, plantId);
        if (c == 0)
            throw new ResourceNotFoundException();
    }

    /* * | Plant Image CRUD | * */



    public void upload(MultipartFile file)
    {
        ImageFile imageFile = new ImageFile(file);   /// auto validates

        // build an path, add db , after upload & store (user, image path, [contentType],  ...)

        ImageUploadResult res =  imageStorageClient.upload(imageFile, "path/name.jpeg");

    }
}
