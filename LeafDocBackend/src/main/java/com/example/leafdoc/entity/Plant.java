package com.example.leafdoc.entity;

import com.example.leafdoc.enums.CropType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="plants",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_plant_user_nickname",
                        columnNames = {"user_id", "nickname"}
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

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Column(name = "crop_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private CropType cropType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false) //fk
    private User user;
}
