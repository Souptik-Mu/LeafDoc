package com.example.leafdoc.repository;

import com.example.leafdoc.entity.PendingRegistration;
import com.example.leafdoc.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PendingRegistrationRepo extends JpaRepository<PendingRegistration,Long> {
    Optional<User> findByEmail(String email);
    Boolean existsByEmail(String email);
}