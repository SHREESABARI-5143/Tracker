package com.chronotrack.repository;

import com.chronotrack.entity.Activity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {

    Optional<Activity> findByIdAndUserId(Long id, Long userId);

    Page<Activity> findByUserIdOrderByStartTimeDesc(Long userId, Pageable pageable);

    List<Activity> findByUserIdOrderByStartTimeDesc(Long userId);

    /**
     * Finds any existing activities for this user that overlap with the interval [startTime, endTime).
     * An overlap occurs when: existing.startTime < endTime AND existing.endTime > startTime.
     */
    @Query("SELECT a FROM Activity a WHERE a.user.id = :userId " +
           "AND a.startTime < :endTime AND a.endTime > :startTime")
    List<Activity> findOverlappingActivities(
            @Param("userId") Long userId,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime
    );

    /**
     * Overlap check excluding a specific activity ID (used during updates).
     */
    @Query("SELECT a FROM Activity a WHERE a.user.id = :userId " +
           "AND a.id != :excludeId " +
           "AND a.startTime < :endTime AND a.endTime > :startTime")
    List<Activity> findOverlappingActivitiesExcludingId(
            @Param("userId") Long userId,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime,
            @Param("excludeId") Long excludeId
    );

    /**
     * Finds all activities for the user that intersect the specified time range, ordered by start time.
     * Used for daily timeline construction, reports, and productivity analytics.
     */
    @Query("SELECT a FROM Activity a JOIN FETCH a.category WHERE a.user.id = :userId " +
           "AND a.startTime < :rangeEnd AND a.endTime > :rangeStart " +
           "ORDER BY a.startTime ASC")
    List<Activity> findActivitiesInRange(
            @Param("userId") Long userId,
            @Param("rangeStart") Instant rangeStart,
            @Param("rangeEnd") Instant rangeEnd
    );

    /**
     * Filter activities by category, keyword search in title, and date range with pagination.
     */
    @Query("SELECT a FROM Activity a JOIN FETCH a.category WHERE a.user.id = :userId " +
           "AND (cast(:categoryId as Long) IS NULL OR a.category.id = :categoryId) " +
           "AND (cast(:search as String) IS NULL OR LOWER(a.title) LIKE CONCAT('%', LOWER(cast(:search as String)), '%')) " +
           "AND (cast(:startTime as java.time.Instant) IS NULL OR a.endTime >= :startTime) " +
           "AND (cast(:endTime as java.time.Instant) IS NULL OR a.startTime <= :endTime) " +
           "ORDER BY a.startTime DESC")
    Page<Activity> findFilteredActivities(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId,
            @Param("search") String search,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime,
            Pageable pageable
    );
}
