package com.example.leafdoc.repository;

import com.example.leafdoc.entity.Scan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScanRepository extends JpaRepository<Scan, Long> {
    Page<Scan> findAllByUser_IdOrderByCreatedAtDesc(
            Long userId,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"disease.crop"})
    Optional<Scan> findByIdAndUser_Id(Long scanId, Long userId);
}
