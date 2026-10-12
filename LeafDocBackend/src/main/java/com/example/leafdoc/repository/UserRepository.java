package com.example.leafdoc.repository;

import com.example.leafdoc.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {

    //* INSERT: inherited save(token)

    @Query("""
        SELECT u
        FROM User u
        WHERE u.email = :email
    """)
    Optional<User> findByEmail(
            @Param("email") String email
    );

    Boolean existsByEmail(String email);

    @Modifying
    @Query("""
        UPDATE User u
        SET u.passwordHash = :passwordHash
        WHERE u.email = :email
    """)
    int updatePasswordHash(
            @Param("email") String email,
            @Param("passwordHash") String passwordHash
    );
}
