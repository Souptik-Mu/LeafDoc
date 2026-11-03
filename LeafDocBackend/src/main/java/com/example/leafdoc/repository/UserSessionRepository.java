package com.example.leafdoc.repository;

import com.example.leafdoc.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserSessionRepository
        extends JpaRepository<UserSession, Long> {

    // Count currently active sessions for a user
    @Query("""
        SELECT COUNT(us)
        FROM UserSession us
        WHERE us.user.id = :userId
          AND us.revokedAt IS NULL
          AND us.expiresAt > :now
    """)
    long countActiveSessions(
            @Param("userId") Long userId,
            @Param("now") Instant now
    );

    // Find one active session belonging to a user
    @Query("""
        SELECT us
        FROM UserSession us
        WHERE us.sessionId = :sessionId
          AND us.user.id = :userId
          AND us.revokedAt IS NULL
          AND us.expiresAt > :now
    """)
    Optional<UserSession> findActiveSession(
            @Param("sessionId") String sessionId,
            @Param("userId") Long userId,
            @Param("now") Instant now
    );

    // Get all active sessions belonging to a user
    @Query("""
        SELECT us
        FROM UserSession us
        WHERE us.user.id = :userId
          AND us.revokedAt IS NULL
          AND us.expiresAt > :now
        ORDER BY us.createdAt DESC
    """)
    List<UserSession> findAllActiveSessions(
            @Param("userId") Long userId,
            @Param("now") Instant now
    );
}
