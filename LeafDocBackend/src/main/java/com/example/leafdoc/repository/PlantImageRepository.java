package com.example.leafdoc.repository;

import com.example.leafdoc.entity.PlantImage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlantImageRepository extends JpaRepository<PlantImage,Long> {

   //Optional<PlantImage> findFirstByPlant_IdOrderByCreatedAtDescIdDesc(Long plantId);

    // Fetch images for a plant, with pagination.
    @Query("""
        SELECT pi
        FROM PlantImage pi
        WHERE pi.plant.id = :plantId
        ORDER BY pi.createdAt DESC, pi.id DESC
        """)
    Page<PlantImage> findRecentImagesByPlantId(
            @Param("plantId") Long plantId,
            Pageable pageable
    );

    // Fetch only the latest image for a plant.
    default Optional<PlantImage> findLatestImageByPlantId(Long plantId) {
        return findRecentImagesByPlantId(
                plantId,
                PageRequest.of(0, 1)
        ).stream().findFirst();
    }

    @Query("""
            DELETE FROM PlantImage pi
            WHERE pi.plant.id = :plantId
        """)
    void deleteByPlantId( @Param("plantId") Long plantId);
}
