package com.example.leafdoc.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name= "pending_registration")
@Getter
@Setter
@NoArgsConstructor
public class VerificationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String verification_token_hash;
    @Column(name="created_at", nullable = false, updatable = false)
    private LocalDateTime created_at;
    @Column(name="expires_at", nullable = false, updatable = false)
    private LocalDateTime expires_at;

    @Column(nullable = false)
    private String serialisedData;

}
