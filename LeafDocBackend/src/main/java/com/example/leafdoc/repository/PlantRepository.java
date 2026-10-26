package com.example.leafdoc.repository;

import com.example.leafdoc.entity.Plant;
import com.example.leafdoc.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlantRepository extends JpaRepository<Plant,Long> {

    //* INSERT & UPDATE: inherited save(plant)

    //get plants of user (u_id)
    @Query("""
        SELECT p
        FROM Plant P
        WHERE p.user.id = :user_id
    """)
    List<Plant> findAllByUserId(@Param("user_id") Long userId);

    @Query("""
        SELECT p
        FROM Plant p
        WHERE p.id = :id
        AND p.user.id = :user_id
        """)
    Optional<Plant> findByIdAndUserId(@Param("user_id") Long userId, @Param("id") Long plantId);

    @Modifying
    @Query("""
        DELETE FROM Plant p
        WHERE p.id = :plantId
        AND p.user.id = :userId
    """)
    int deleteByPlantIdAndUserId(
            @Param("plantId") Long plantId,
            @Param("userId") Long userId
    );
}
