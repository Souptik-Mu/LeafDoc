package com.example.leafdoc.repository;

import com.example.leafdoc.entity.PendingRegistration;
import com.example.leafdoc.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PendingRegistrationRepo extends JpaRepository<PendingRegistration,Long> {
    //Optional<User> findByEmail(String email);
    Optional<PendingRegistration> findByVerificationTokenHash(
            String verificationTokenHash);
    Boolean existsByEmail(String email);
    long deleteByExpiresAtBefore(LocalDateTime now);
}