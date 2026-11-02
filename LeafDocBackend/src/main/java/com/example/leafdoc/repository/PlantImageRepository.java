package com.example.leafdoc.repository;

import com.example.leafdoc.entity.PlantImage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlantImageRepository extends JpaRepository<PlantImage,Long> {

    //* INSERT : inherited saveAndFlush(plantImage)
    //* DELETE : inherited delete(plantImage) , deleteAll(List<PlantImage>)

    /**
     * Retrieves a plant's image history with pagination.
     * <hr/>
     * Results are ordered by creation time (newest first).
     * Image ID is used as a secondary sort to ensure deterministic ordering
     * when multiple images share the same creation timestamp.
     */
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

    /**
     * Retrieves the most recent image belonging to a plant.
     * <br/>
     * Reuses the paginated image query and limits the result to one record.
     * <br/>
     * @return the latest image, or Optional.empty() if the plant has no images
     */
    default Optional<PlantImage> findLatestImageByPlantId(Long plantId) {
        return findRecentImagesByPlantId(
                plantId,
                PageRequest.of(0, 1)
        ).stream().findFirst();
    }

    @Query("""
            SELECT pi
            FROM PlantImage pi
            WHERE pi.plant.id IN :plantIds
            AND NOT EXISTS (
                SELECT 1
                FROM PlantImage newer
                WHERE newer.plant.id = pi.plant.id
                AND (
                    newer.createdAt > pi.createdAt
                    OR (
                        newer.createdAt = pi.createdAt
                        AND newer.id > pi.id
                    )
                )
            )
        """)
    List<PlantImage> findLatestImagesByPlantIds(
            @Param("plantIds") List<Long> plantIds
    );

    /**
     * Retrieves a specific image belonging to a specific plant.
     * <br/>
     * Ensures the image ID and plant ID both match.
     * Useful for verifying image ownership before deletion or modification.
     * <br/>
     * @return the matching image, or Optional.empty() if not found
     */
    @Query("""
            SELECT pi
            FROM PlantImage pi
            WHERE pi.id = :imageId
            AND pi.plant.id = :plantId
        """)
    Optional<PlantImage> findByImageIdAndPlantId(
            @Param("imageId") Long imageId,
            @Param("plantId") Long plantId
    );

    @Query("""
            SELECT pi
            FROM PlantImage pi
            WHERE pi.plant.id = :plantId
        """)
    List<PlantImage> findAllByPlantId(
            @Param("plantId") Long plantId
    );
}
