package com.chronotrack.service;

import com.chronotrack.dto.ActivityResponse;
import com.chronotrack.dto.TimerResponse;
import com.chronotrack.dto.TimerStartRequest;
import com.chronotrack.entity.*;
import com.chronotrack.exception.BadRequestException;
import com.chronotrack.exception.ResourceNotFoundException;
import com.chronotrack.repository.ActivityRepository;
import com.chronotrack.repository.TimerSessionRepository;
import com.chronotrack.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * Service managing real-time persistent timer sessions.
 * Preserves timer state across restarts and browser refreshes.
 */
@Service
public class TimerService {

    private final TimerSessionRepository timerSessionRepository;
    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final CategoryService categoryService;
    private final ActivityService activityService;

    public TimerService(TimerSessionRepository timerSessionRepository,
                        ActivityRepository activityRepository,
                        UserRepository userRepository,
                        CategoryService categoryService,
                        ActivityService activityService) {
        this.timerSessionRepository = timerSessionRepository;
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
        this.categoryService = categoryService;
        this.activityService = activityService;
    }

    /**
     * Start a new activity timer session. If an existing timer exists, returns it or stops previous.
     */
    @Transactional
    public TimerResponse startTimer(Long userId, TimerStartRequest request) {
        Optional<TimerSession> existing = timerSessionRepository.findByUserId(userId);
        if (existing.isPresent()) {
            throw new BadRequestException("A timer session is already active ('" + existing.get().getTitle() + "'). Please stop or pause it first.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        Category category = categoryService.getCategoryEntity(request.getCategoryId(), userId);

        TimerSession session = new TimerSession(
                user,
                category,
                request.getTitle().trim(),
                request.getDescription() != null ? request.getDescription().trim() : null,
                request.getNotes() != null ? request.getNotes().trim() : null
        );

        TimerSession saved = timerSessionRepository.save(session);
        return mapToResponse(saved);
    }

    /**
     * Pause the active timer session and accumulate elapsed seconds.
     */
    @Transactional
    public TimerResponse pauseTimer(Long userId) {
        TimerSession session = timerSessionRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No active timer session found to pause."));

        if (session.getStatus() == TimerStatus.PAUSED) {
            return mapToResponse(session);
        }

        Instant now = Instant.now();
        long currentSegmentSeconds = Duration.between(session.getStartTime(), now).getSeconds();
        if (currentSegmentSeconds > 0) {
            session.setAccumulatedSeconds(session.getAccumulatedSeconds() + currentSegmentSeconds);
        }
        session.setPausedAt(now);
        session.setStatus(TimerStatus.PAUSED);

        TimerSession updated = timerSessionRepository.save(session);
        return mapToResponse(updated);
    }

    /**
     * Resume a paused timer session.
     */
    @Transactional
    public TimerResponse resumeTimer(Long userId) {
        TimerSession session = timerSessionRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No active timer session found to resume."));

        if (session.getStatus() == TimerStatus.RUNNING) {
            return mapToResponse(session);
        }

        session.setStartTime(Instant.now());
        session.setPausedAt(null);
        session.setStatus(TimerStatus.RUNNING);

        TimerSession updated = timerSessionRepository.save(session);
        return mapToResponse(updated);
    }

    /**
     * Stop the timer, validate timing and overlap, save completed Activity, and remove the timer session.
     */
    @Transactional
    public ActivityResponse stopTimer(Long userId) {
        TimerSession session = timerSessionRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No active timer session found to stop."));

        long totalSeconds = session.calculateCurrentElapsedSeconds();
        if (totalSeconds < 1) {
            totalSeconds = 1L;
        }

        Instant endTime = session.getStatus() == TimerStatus.PAUSED && session.getPausedAt() != null
                ? session.getPausedAt()
                : Instant.now();
        Instant startTime = endTime.minusSeconds(totalSeconds);

        // Verify no overlap with existing activities
        activityService.validateNoOverlap(userId, startTime, endTime, null);

        Activity activity = new Activity(
                session.getUser(),
                session.getCategory(),
                session.getTitle(),
                session.getDescription(),
                startTime,
                endTime,
                session.getNotes()
        );

        Activity saved = activityRepository.save(activity);
        timerSessionRepository.delete(session);

        return activityService.mapToResponse(saved);
    }

    /**
     * Fetch the currently running or paused timer for the authenticated user.
     */
    @Transactional(readOnly = true)
    public TimerResponse getCurrentTimer(Long userId) {
        return timerSessionRepository.findByUserId(userId)
                .map(this::mapToResponse)
                .orElse(null);
    }

    public TimerResponse mapToResponse(TimerSession session) {
        long elapsedSeconds = session.calculateCurrentElapsedSeconds();
        return new TimerResponse(
                session.getId(),
                session.getCategory().getId(),
                session.getCategory().getName(),
                session.getCategory().getColor(),
                session.getTitle(),
                session.getDescription(),
                session.getNotes(),
                session.getStartTime(),
                session.getPausedAt(),
                elapsedSeconds,
                session.getStatus().name()
        );
    }
}
