package com.example.leafdoc.repository;

import com.example.leafdoc.entity.Crop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CropRepository extends JpaRepository<Crop, Long> {
    //* INHERITED save
    @Query("""
            SELECT c
            FROM Crop c
            WHERE c.cropCode = :code
            """)
    Optional<Crop> findByCropCode(@Param("code") String code);

    @Query("""
            SELECT c
            FROM Crop c
            WHERE c.name = :name
            """)
    Optional<Crop> findByName(@Param("name") String name);
}
