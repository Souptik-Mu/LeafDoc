package com.example.leafdoc.repository;

import com.example.leafdoc.entity.Scan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScanRepository extends JpaRepository<Scan, Long> {
}
