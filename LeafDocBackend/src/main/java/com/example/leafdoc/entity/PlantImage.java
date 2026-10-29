package com.example.leafdoc.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "plant_images")
@Getter
@Setter
@RequiredArgsConstructor
public class PlantImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plant_id", nullable = false, updatable = false)
    private Plant plant;

    @Column(name = "object_path", updatable = false, nullable = false)
    private String objectPath;

    @Column(name = "content_type", updatable = false)
    private String contentType;

    @CreationTimestamp
    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

}
