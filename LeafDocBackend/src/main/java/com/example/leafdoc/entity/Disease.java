package com.example.leafdoc.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Table(
        name = "diseases",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_disease_ml_class_label",
                        columnNames = "ml_class_label"
                )
        },
        indexes = {
                @Index(
                        name = "idx_disease_crop_id",
                        columnList = "crop_id"
                )
        }
)
@NoArgsConstructor
@Getter
@Setter
public class Disease {      ///READ ONLY | DATA LOADED

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "crop_id", nullable = false, foreignKey = @ForeignKey(name = "fk_disease_crop"))
    private Crop crop;

    @Column(name = "ml_class_label", nullable = false, unique = true, length = 150)
    private String mlClassLabel;    // must match with class identifier

    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSON")
    private List<String> symptoms;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSON")
    private List<String> causes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSON")
    private List<String> treatment;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSON")
    private List<String> prevention;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "care_tips", columnDefinition = "JSON")
    private List<String> careTips;

    @Column(name = "is_healthy", nullable = false)
    private boolean isHealthy = false;
}
