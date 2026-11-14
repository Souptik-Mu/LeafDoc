package com.example.leafdoc.repository;

import com.example.leafdoc.entity.Crop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CropRepository extends JpaRepository<Crop, Integer> {
    //* INHERITED save
    Optional<Crop> findByCropCode(String code);
    Optional<Crop> findByName(String name);
}
