package com.example.leafdoc.entity;

import com.example.leafdoc.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
//@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User {
    // get back uuid(auto genarated)
    ///id (PK), name, email (unique), password_hash, role, created_at

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name="user_name", nullable = false)
    String userName;

    @Column(nullable = false,unique = true)
    String email;
    @Column(name="password_hash", nullable = false)
    String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name="created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    public User(String userName, String email, String passwordHash, Role role) {
        this.userName = userName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role != null ? role : Role.GUEST;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

}
// have ROLE enum
