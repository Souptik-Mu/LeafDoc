package com.example.leafdoc.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
        name= "tokens",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_tokens_verification_token_hash",
                        columnNames = "verification_token_hash"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class VerificationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "verification_token_hash", nullable = false)
    private String verificationTokenHash;
    @Column(name="created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name="expires_at", nullable = false, updatable = false)
    private Instant expiresAt;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String serialisedData;  //Stores the registrationPayload after serialization.

}
