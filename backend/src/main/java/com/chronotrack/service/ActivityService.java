package com.chronotrack.service;

import com.chronotrack.dto.ActivityRequest;
import com.chronotrack.dto.ActivityResponse;
import com.chronotrack.entity.Activity;
import com.chronotrack.entity.Category;
import com.chronotrack.entity.User;
import com.chronotrack.exception.BadRequestException;
import com.chronotrack.exception.ResourceNotFoundException;
import com.chronotrack.repository.ActivityRepository;
import com.chronotrack.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Service managing user activity lifecycle, overlap prevention, and duration validation.
 */
@Service
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final CategoryService categoryService;

    public ActivityService(ActivityRepository activityRepository,
                           UserRepository userRepository,
                           CategoryService categoryService) {
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
        this.categoryService = categoryService;
    }

    /**
     * Create a new activity manually after validating non-overlapping interval.
     */
    @Transactional
    public ActivityResponse createActivity(Long userId, ActivityRequest request) {
        validateActivityTiming(request.getStartTime(), request.getEndTime());
        validateNoOverlap(userId, request.getStartTime(), request.getEndTime(), null);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        Category category = categoryService.getCategoryEntity(request.getCategoryId(), userId);

        Activity activity = new Activity(
                user,
                category,
                request.getTitle().trim(),
                request.getDescription() != null ? request.getDescription().trim() : null,
                request.getStartTime(),
                request.getEndTime(),
                request.getNotes() != null ? request.getNotes().trim() : null
        );

        Activity saved = activityRepository.save(activity);
        return mapToResponse(saved);
    }

    /**
     * Retrieve a specific activity ensuring user ownership.
     */
    @Transactional(readOnly = true)
    public ActivityResponse getActivity(Long activityId, Long userId) {
        Activity activity = getActivityEntity(activityId, userId);
        return mapToResponse(activity);
    }

    /**
     * Retrieve all activities for a user with optional category, search, and date filters.
     */
    @Transactional(readOnly = true)
    public Page<ActivityResponse> getUserActivities(Long userId, Long categoryId, String search,
                                                    Instant startTime, Instant endTime, Pageable pageable) {
        String cleanSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        if (categoryId == null && cleanSearch == null && startTime == null && endTime == null) {
            return activityRepository.findByUserIdOrderByStartTimeDesc(userId, pageable)
                    .map(this::mapToResponse);
        }
        return activityRepository.findFilteredActivities(userId, categoryId, cleanSearch, startTime, endTime, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Update an existing activity after validating interval and excluding self from overlap check.
     */
    @Transactional
    public ActivityResponse updateActivity(Long activityId, Long userId, ActivityRequest request) {
        Activity activity = getActivityEntity(activityId, userId);
        validateActivityTiming(request.getStartTime(), request.getEndTime());
        validateNoOverlap(userId, request.getStartTime(), request.getEndTime(), activityId);

        Category category = categoryService.getCategoryEntity(request.getCategoryId(), userId);

        activity.setCategory(category);
        activity.setTitle(request.getTitle().trim());
        activity.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        activity.setStartTime(request.getStartTime());
        activity.setEndTime(request.getEndTime());
        activity.setNotes(request.getNotes() != null ? request.getNotes().trim() : null);

        Activity updated = activityRepository.save(activity);
        return mapToResponse(updated);
    }

    /**
     * Delete an activity by ID.
     */
    @Transactional
    public void deleteActivity(Long activityId, Long userId) {
        Activity activity = getActivityEntity(activityId, userId);
        activityRepository.delete(activity);
    }

    /**
     * Fetch entity ensuring user isolation.
     */
    @Transactional(readOnly = true)
    public Activity getActivityEntity(Long activityId, Long userId) {
        return activityRepository.findByIdAndUserId(activityId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found with id: " + activityId));
    }

    /**
     * Validates that end time is strictly after start time and duration is positive.
     */
    public void validateActivityTiming(Instant startTime, Instant endTime) {
        if (startTime == null || endTime == null) {
            throw new BadRequestException("Start time and end time cannot be null.");
        }
        if (!endTime.isAfter(startTime)) {
            throw new BadRequestException("End time must be strictly later than start time.");
        }
        if (Duration.between(startTime, endTime).getSeconds() < 1) {
            throw new BadRequestException("Activity duration must be at least 1 second.");
        }
    }

    /**
     * Strictly verifies that the new time interval does not conflict with existing activities of the same user.
     */
    public void validateNoOverlap(Long userId, Instant startTime, Instant endTime, Long excludeId) {
        List<Activity> overlapping = (excludeId == null)
                ? activityRepository.findOverlappingActivities(userId, startTime, endTime)
                : activityRepository.findOverlappingActivitiesExcludingId(userId, startTime, endTime, excludeId);

        if (!overlapping.isEmpty()) {
            Activity conflict = overlapping.get(0);
            throw new BadRequestException(String.format(
                    "Activity time conflicts with an existing activity: '%s' (%s to %s). Overlapping activities are not permitted.",
                    conflict.getTitle(), conflict.getStartTime(), conflict.getEndTime()
            ));
        }
    }

    public ActivityResponse mapToResponse(Activity activity) {
        return new ActivityResponse(
                activity.getId(),
                activity.getCategory().getId(),
                activity.getCategory().getName(),
                activity.getCategory().getColor(),
                activity.getTitle(),
                activity.getDescription(),
                activity.getStartTime(),
                activity.getEndTime(),
                activity.getDurationSeconds(),
                activity.getNotes(),
                activity.getCreatedAt(),
                activity.getUpdatedAt()
        );
    }
}
