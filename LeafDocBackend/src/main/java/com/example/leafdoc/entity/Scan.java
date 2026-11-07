package com.example.leafdoc.entity;

import com.example.leafdoc.enums.ScanStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "scans",
        indexes = {
                @Index(
                        name = "idx_scan_user_scanned_at",
                        columnList = "user_id, scanned_at"
                )
        },
        check = {
                @CheckConstraint(
                        name = "chk_scan_confidence_range",
                        constraint = "confidence >= 0 AND confidence <= 1"
                )
        }
)
@NoArgsConstructor
@Getter
@Setter
public class Scan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_scan_user")
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "plant_id",
            foreignKey = @ForeignKey(name = "fk_scan_plant")
    )
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Plant plant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "disease_id",
            foreignKey = @ForeignKey(name = "fk_scan_disease")
    )
    private Disease disease;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "predicted_crop_id",
            foreignKey = @ForeignKey(name = "fk_scan_predicted_crop")
    )
    private Crop predictedCrop; ///*

    @Column(
            name = "predicted_class_label",
            nullable = false,
            length = 150
    )
    private String predictedClassLabel;

    @DecimalMin("0.0000")
    @DecimalMax("1.0000")
    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal confidence;      /// value between 0 - 1

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ScanStatus status;

    @Column(name = "image_path", nullable = false, length = 500)
    private String imagePath;

    @Column(name = "model_version", length = 100)
    private String modelVersion;

    @CreationTimestamp
    @Column(name = "scanned_at", nullable = false, updatable = false)
    private Instant scannedAt;
}
