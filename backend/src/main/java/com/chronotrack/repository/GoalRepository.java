package com.chronotrack.repository;

import com.chronotrack.entity.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {

    List<Goal> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Goal> findByUserIdAndActiveTrue(Long userId);

    Optional<Goal> findByIdAndUserId(Long id, Long userId);

    List<Goal> findByUserIdAndCategoryId(Long userId, Long categoryId);

    @Query("SELECT g FROM Goal g JOIN FETCH g.category WHERE g.user.id = :userId AND g.active = true")
    List<Goal> findActiveGoalsWithCategory(@Param("userId") Long userId);
}
