package com.example.leafdoc.service;

import com.example.leafdoc.DTO.ImageResponse;
import com.example.leafdoc.DTO.plant.CreatePlantRequest;
import com.example.leafdoc.DTO.plant.PlantResponse;
import com.example.leafdoc.client.ImageStorageClient;
import com.example.leafdoc.entity.Plant;
import com.example.leafdoc.entity.PlantImage;
import com.example.leafdoc.entity.User;
import com.example.leafdoc.exceptions.ResourceNotFoundException;
import com.example.leafdoc.repository.PlantImageRepository;
import com.example.leafdoc.repository.PlantRepository;
import com.example.leafdoc.util.ImageFile;
import com.example.leafdoc.util.StoragePathGenarator;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlantService {

    private final EntityManager entityManager; // spring provides
    private final ImageStorageClient imageStorageClient;
    private final PlantRepository plantRepo;
    private final PlantImageRepository plantImageRepo;

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


    @Transactional(readOnly = true)
    public Page<PlantResponse> getMyPlants(
            Long userId,
            Pageable pageable
    ){
        Page<Plant> plants = plantRepo.findAllByUserId(userId, pageable);

        List<Long> plantIds = plants.getContent()
                .stream()
                .map(Plant::getId)
                .toList();

        Map<Long, PlantImage> latestImages = plantIds.isEmpty()
                            ? Map.of()
                            : plantImageRepo.findLatestImagesByPlantIds(plantIds)
                                            .stream()
                                            .collect(Collectors.toMap(
                                                    image -> image.getPlant().getId(),
                                                    Function.identity()
                                            ));
        return plants.map(plant -> {
            PlantImage image = latestImages.get(plant.getId());

            ImageResponse latest =
                    image == null
                    ? null
                    : new ImageResponse(
                                image.getId(),
                                imageStorageClient.getPublicImageUrl(image.getObjectPath()),
                                image.getCreatedAt()
                        );

            return PlantResponse.from(plant, latest);
        });
    }

    @Transactional(readOnly = true)
    public PlantResponse getMyPlant(
            Long userId,
            Long plantId
    ){
        Plant plant = plantRepo.findByIdAndUserId(userId, plantId)
                .orElseThrow(ResourceNotFoundException::new);

        ImageResponse latestImage = plantImageRepo
                .findLatestImageByPlantId(plantId)
                .map(image -> new ImageResponse(
                            image.getId(),
                            imageStorageClient.getPublicImageUrl(image.getObjectPath()),
                            image.getCreatedAt()
                ))
                .orElse(null);

       return  PlantResponse.from(plant, latestImage);
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

    @Transactional
    public void deleteMyPlant(
            Long userId,
            Long plantId
    ){
        Plant pl = plantRepo.findByIdAndUserId(userId, plantId)
                .orElseThrow(ResourceNotFoundException::new);

        List<PlantImage> images = plantImageRepo.findAllByPlantId(plantId);

        for (PlantImage image : images) {   /// Delete all images from Supabase
            imageStorageClient.delete(image.getObjectPath());
        }
        plantImageRepo.deleteAll(images);
        plantRepo.delete(pl);
    }

    /* * | Plant Image CRUD | * */

    @Transactional
    public ImageResponse uploadImage(
            Long userId,
            Long plantId,
            MultipartFile file
    ) {
        Plant plant = plantRepo.findByIdAndUserId(userId, plantId)
                .orElseThrow(ResourceNotFoundException::new);

        ImageFile imageFile = new ImageFile(file);   /// auto validates

        String objectPath = StoragePathGenarator.plantImage(userId, plantId, imageFile.getExtension());
        String publicUrl  =  imageStorageClient.upload(imageFile, objectPath);

        try {
            PlantImage plantImage = new PlantImage();
            plantImage.setObjectPath(objectPath);
            plantImage.setContentType(imageFile.getContentType());
            plantImage.setPlant(plant);

            PlantImage savedImage = plantImageRepo.saveAndFlush(plantImage);

            return new ImageResponse(
                    savedImage.getId(),
                    publicUrl,
                    savedImage.getCreatedAt()
            );
        } catch (RuntimeException ex) {
            try {   /// Compensate for successful storage upload if database persistence fails.
                imageStorageClient.delete(objectPath);
            } catch (RuntimeException cleanupEx) {
                ex.addSuppressed(cleanupEx);
            }
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public Page<ImageResponse> getRecentPlantImages(Long userId, Long plantId, Pageable pageable){
        plantRepo.findByIdAndUserId(userId, plantId)
                .orElseThrow(ResourceNotFoundException::new);

        return plantImageRepo.findRecentImagesByPlantId(plantId, pageable)
                .map(image -> new ImageResponse(
                                    image.getId(),
                                    imageStorageClient.getPublicImageUrl(image.getObjectPath()),
                                    image.getCreatedAt()
                ));
    }

    @Transactional
    public void deletePlantImage(Long userId, Long plantId, Long imageId){
        plantRepo.findByIdAndUserId(userId, plantId)
                .orElseThrow(ResourceNotFoundException::new);

        PlantImage image = plantImageRepo
                .findByImageIdAndPlantId(imageId, plantId)
                .orElseThrow(ResourceNotFoundException::new);

        imageStorageClient.delete(image.getObjectPath());
        plantImageRepo.delete(image);
    }
}
