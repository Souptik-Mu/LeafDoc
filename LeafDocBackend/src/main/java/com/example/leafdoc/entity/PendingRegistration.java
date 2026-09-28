package com.example.leafdoc.entity;

import com.example.leafdoc.enums.Role;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name= "pending_registration")
public class PendingRegistration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(name="password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private String verification_token_hash;
    @Column(name="created_at", nullable = false, updatable = false)
    private LocalDateTime created_at;
    @Column(name="expires_at", nullable = false, updatable = false)
    private LocalDateTime expires_at;

}
