package com.example.leafdoc.repository;

import com.example.leafdoc.entity.Scan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ScanRepository extends JpaRepository<Scan, Long> {

    @Query("""
            SELECT s
            FROM Scan s
            WHERE s.user.id = :userId
            ORDER BY s.scannedAt DESC
            """)
    Page<Scan> findScanHistory(
            @Param("userId") Long userId,
            Pageable pageable
    );

    @Query("""
            SELECT s
            FROM Scan s
            WHERE s.id = :scanId
            AND s.user.id = :userId
            """)
    Optional<Scan> findScanDetails(
            @Param("scanId") Long scanId,
            @Param("userId") Long userId
    );
}
