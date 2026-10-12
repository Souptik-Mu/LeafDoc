package com.example.leafdoc.repository;

import com.example.leafdoc.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken,Long> {

    //* INSERT: inherited save(token)

    @Query("""
        SELECT t
        FROM VerificationToken t
        WHERE t.verificationTokenHash = :tokenHash
    """)
    Optional<VerificationToken> findByVerificationTokenHash(
            @Param("tokenHash") String tokenHash
    );

    @Query("""
        SELECT COUNT(t) > 0
        FROM VerificationToken t
        WHERE t.verificationTokenHash = :tokenHash
    """)
    boolean existsByTokenHash(
            @Param("tokenHash") String tokenHash
    );

    @Modifying
    @Query("""
        DELETE FROM VerificationToken t
        WHERE t.expiresAt <= :now
    """)
    void deleteExpiredTokens(
            @Param("now") LocalDateTime now
    );

    //* DELETE: inherited delete(token)
}