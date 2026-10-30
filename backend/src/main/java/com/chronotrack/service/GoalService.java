package com.chronotrack.service;

import com.chronotrack.dto.GoalRequest;
import com.chronotrack.dto.GoalResponse;
import com.chronotrack.entity.*;
import com.chronotrack.exception.ResourceNotFoundException;
import com.chronotrack.repository.ActivityRepository;
import com.chronotrack.repository.GoalRepository;
import com.chronotrack.repository.UserRepository;
import com.chronotrack.repository.UserSettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

/**
 * Service managing user productivity goals and real-time progress calculations.
 */
@Service
public class GoalService {

    private final GoalRepository goalRepository;
    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final CategoryService categoryService;
    private final UserSettingsRepository userSettingsRepository;

    public GoalService(GoalRepository goalRepository,
                       ActivityRepository activityRepository,
                       UserRepository userRepository,
                       CategoryService categoryService,
                       UserSettingsRepository userSettingsRepository) {
        this.goalRepository = goalRepository;
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
        this.categoryService = categoryService;
        this.userSettingsRepository = userSettingsRepository;
    }

    /**
     * Create a new personal productivity goal.
     */
    @Transactional
    public GoalResponse createGoal(Long userId, GoalRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        Category category = categoryService.getCategoryEntity(request.getCategoryId(), userId);

        Goal goal = new Goal(
                user,
                category,
                request.getTitle().trim(),
                request.getTargetMinutes(),
                request.getFrequency(),
                request.getStartDate(),
                request.getEndDate()
        );
        if (request.getActive() != null) {
            goal.setActive(request.getActive());
        }

        Goal saved = goalRepository.save(goal);
        return mapToResponse(saved, userId);
    }

    /**
     * Retrieve all goals for a user with real-time calculated progress.
     */
    @Transactional(readOnly = true)
    public List<GoalResponse> getGoals(Long userId) {
        List<Goal> goals = goalRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<GoalResponse> responses = new ArrayList<>();
        for (Goal goal : goals) {
            responses.add(mapToResponse(goal, userId));
        }
        return responses;
    }

    /**
     * Retrieve a single goal by ID with calculated progress.
     */
    @Transactional(readOnly = true)
    public GoalResponse getGoal(Long goalId, Long userId) {
        Goal goal = getGoalEntity(goalId, userId);
        return mapToResponse(goal, userId);
    }

    /**
     * Update an existing goal.
     */
    @Transactional
    public GoalResponse updateGoal(Long goalId, Long userId, GoalRequest request) {
        Goal goal = getGoalEntity(goalId, userId);
        Category category = categoryService.getCategoryEntity(request.getCategoryId(), userId);

        goal.setCategory(category);
        goal.setTitle(request.getTitle().trim());
        goal.setTargetMinutes(request.getTargetMinutes());
        goal.setFrequency(request.getFrequency());
        if (request.getStartDate() != null) {
            goal.setStartDate(request.getStartDate());
        }
        goal.setEndDate(request.getEndDate());
        if (request.getActive() != null) {
            goal.setActive(request.getActive());
        }

        Goal updated = goalRepository.save(goal);
        return mapToResponse(updated, userId);
    }

    /**
     * Delete a goal by ID.
     */
    @Transactional
    public void deleteGoal(Long goalId, Long userId) {
        Goal goal = getGoalEntity(goalId, userId);
        goalRepository.delete(goal);
    }

    @Transactional(readOnly = true)
    public Goal getGoalEntity(Long goalId, Long userId) {
        return goalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found with id: " + goalId));
    }

    /**
     * Maps Goal entity to GoalResponse with real-time actual minutes for current frequency period.
     */
    public GoalResponse mapToResponse(Goal goal, Long userId) {
        ZoneId zoneId = getUserZoneId(userId);
        LocalDate today = LocalDate.now(zoneId);

        Instant periodStart;
        Instant periodEnd;

        if (goal.getFrequency() == GoalFrequency.DAILY) {
            periodStart = today.atStartOfDay(zoneId).toInstant();
            periodEnd = today.plusDays(1).atStartOfDay(zoneId).toInstant();
        } else if (goal.getFrequency() == GoalFrequency.WEEKLY) {
            LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            periodStart = monday.atStartOfDay(zoneId).toInstant();
            periodEnd = monday.plusDays(7).atStartOfDay(zoneId).toInstant();
        } else { // MONTHLY
            YearMonth ym = YearMonth.from(today);
            periodStart = ym.atDay(1).atStartOfDay(zoneId).toInstant();
            periodEnd = ym.atEndOfMonth().plusDays(1).atStartOfDay(zoneId).toInstant();
        }

        List<Activity> activities = activityRepository.findActivitiesInRange(userId, periodStart, periodEnd);
        long actualSeconds = 0L;
        Long targetCategoryId = goal.getCategory().getId();

        for (Activity act : activities) {
            if (act.getCategory().getId().equals(targetCategoryId)) {
                Instant s = act.getStartTime().isBefore(periodStart) ? periodStart : act.getStartTime();
                Instant e = act.getEndTime().isAfter(periodEnd) ? periodEnd : act.getEndTime();
                if (e.isAfter(s)) {
                    actualSeconds += Duration.between(s, e).getSeconds();
                }
            }
        }

        int actualMinutes = (int) (actualSeconds / 60);

        return new GoalResponse(
                goal.getId(),
                goal.getCategory().getId(),
                goal.getCategory().getName(),
                goal.getCategory().getColor(),
                goal.getTitle(),
                goal.getTargetMinutes(),
                actualMinutes,
                goal.getFrequency(),
                goal.getStartDate(),
                goal.getEndDate(),
                goal.isActive(),
                goal.getCreatedAt()
        );
    }

    private ZoneId getUserZoneId(Long userId) {
        UserSettings settings = userSettingsRepository.findByUserId(userId)
                .orElse(new UserSettings(null, "UTC", "24h"));
        try {
            return ZoneId.of(settings.getTimezone() != null ? settings.getTimezone() : "UTC");
        } catch (Exception e) {
            return ZoneId.of("UTC");
        }
    }
}
