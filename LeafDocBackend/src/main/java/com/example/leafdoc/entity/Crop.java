package com.example.leafdoc.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "crops",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_crops_crop_name",
                        columnNames = "crop_name"
                ),
                @UniqueConstraint(
                        name = "uk_crops_code",
                        columnNames = "code"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Crop {     ///READ ONLY | DATA LOADED

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="crop_name", length = 100, nullable = false)
    private String name;

    @Column(name="code", length = 50, nullable = false, updatable = false)
    private String cropCode;
}
