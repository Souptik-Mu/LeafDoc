package com.example.leafdoc.entity;

import com.example.leafdoc.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CurrentTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor()
//@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User {
    // get back uuid(auto generated)
    ///id (PK), name, email (unique), password_hash, role, created_at

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_name", nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name="password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @CurrentTimestamp
    @Column(name="created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    public User(String userName, String email, String passwordHash, Role role) {
        this.name = userName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role != null ? role : Role.GUEST;
    }

}
// have ROLE enum
