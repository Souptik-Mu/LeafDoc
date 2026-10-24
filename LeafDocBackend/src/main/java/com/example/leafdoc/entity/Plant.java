package com.example.leafdoc.entity;

import com.example.leafdoc.enums.CropType;
import jakarta.persistence.*;

@Entity
@Table(name="plants")
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
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false) //fk
    private User user;
}
