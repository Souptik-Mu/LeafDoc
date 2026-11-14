package com.example.leafdoc.repository;

import com.example.leafdoc.entity.Disease;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DiseaseRepository extends JpaRepository<Disease, Long> {
    Optional<Disease> findByMlClassLabel(String mlClassLabel);

    boolean existsByMlClassLabel(String mlClassLabel);

    List<Disease> findAllByCrop_CropCode(String cropCode);
}
