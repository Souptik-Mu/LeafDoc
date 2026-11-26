package com.example.leafdoc.repository;

import com.example.leafdoc.entity.Disease;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DiseaseRepository extends JpaRepository<Disease, Long> {
    //* INHERIT save()

    //@EntityGraph(attributePaths = {"crop"})
    @Query("""
            SELECT d
            FROM Disease d
            JOIN FETCH d.crop
            WHERE d.mlClassLabel = :mlClassLabel
            """)
    Optional<Disease> findByMlClassLabel(
            @Param("mlClassLabel") String mlClassLabel
    );
}
