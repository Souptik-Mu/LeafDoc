package com.example.leafdoc.repository;

import com.example.leafdoc.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken,Long> {
    //Optional<User> findByEmail(String email);
    Optional<VerificationToken> findByVerificationTokenHash(
            String verificationTokenHash);
    Boolean existsByEmail(String email);
    void deleteByExpiresAtBefore(LocalDateTime now);
}