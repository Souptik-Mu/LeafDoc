package com.example.leafdoc.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(
        name = "user_sessions",
        indexes = {
                @Index(name = "idx_session_user", columnList = "user_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_sessions_session_id",
                        columnNames = "session_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="session_id", nullable=false, length=36)
    private String sessionId;   // This included in the JWT as the jti claim.

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name="user_id",
            nullable=false,
            foreignKey = @ForeignKey(name = "fk_user_session_user")
    )
    private User user;

    @Column(name="device_name")
    private String deviceName;

    @CreationTimestamp
    @Column(name="created_at", nullable = false , updatable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt = null;

}
