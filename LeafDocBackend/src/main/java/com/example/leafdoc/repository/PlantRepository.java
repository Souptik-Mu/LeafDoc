package com.example.leafdoc.repository;

import com.example.leafdoc.entity.Plant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlantRepository extends JpaRepository<Plant,Integer> {
}
