package com.example.leafdoc.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CurrentTimestamp;

import java.time.Instant;

@Entity
@Table(name="plants",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_plant_user_nickname",
                        columnNames = {"user_id", "nickname"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_plant_user_added_at",
                        columnList = "user_id, added_at"
                )
        }
)
@NoArgsConstructor
@Getter
@Setter
public class Plant {
    //user many to one - join on user id, crop type, nickname
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nickname", nullable = false, length = 20)
    private String nickname;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "crop_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_plant_crop")
    )
    private Crop crop;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_plant_user")
    )
    private User user;

    @CurrentTimestamp
    @Column(name="added_at", nullable = false, updatable = false)
    private Instant addedAt;
}
