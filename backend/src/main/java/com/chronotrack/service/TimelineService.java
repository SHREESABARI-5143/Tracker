package com.chronotrack.service;

import com.chronotrack.dto.ActivityResponse;
import com.chronotrack.dto.CategoryTimeDistribution;
import com.chronotrack.dto.DailyTimelineResponse;
import com.chronotrack.dto.TimelineSegmentResponse;
import com.chronotrack.entity.Activity;
import com.chronotrack.entity.Category;
import com.chronotrack.entity.UserSettings;
import com.chronotrack.repository.ActivityRepository;
import com.chronotrack.repository.UserSettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;

/**
 * Service constructing the 24-hour daily timeline (00:00 to 23:59),
 * handling cross-midnight activity clipping and unclassified untracked gap detection.
 */
@Service
public class TimelineService {

    private final ActivityRepository activityRepository;
    private final UserSettingsRepository userSettingsRepository;

    public TimelineService(ActivityRepository activityRepository,
                           UserSettingsRepository userSettingsRepository) {
        this.activityRepository = activityRepository;
        this.userSettingsRepository = userSettingsRepository;
    }

    /**
     * Builds the complete 24-hour daily schedule and metrics for a specific date in the user's timezone.
     */
    @Transactional(readOnly = true)
    public DailyTimelineResponse getDailyTimeline(Long userId, LocalDate date) {
        UserSettings settings = userSettingsRepository.findByUserId(userId)
                .orElse(new UserSettings(null, "UTC", "24h"));

        ZoneId zoneId;
        try {
            zoneId = ZoneId.of(settings.getTimezone() != null ? settings.getTimezone() : "UTC");
        } catch (Exception e) {
            zoneId = ZoneId.of("UTC");
        }

        LocalDate targetDate = (date != null) ? date : LocalDate.now(zoneId);
        boolean is12Hour = "12h".equalsIgnoreCase(settings.getPreferredTimeFormat());
        DateTimeFormatter timeFormatter = is12Hour
                ? DateTimeFormatter.ofPattern("hh:mm a")
                : DateTimeFormatter.ofPattern("HH:mm");

        // Compute 24-hour day boundaries in user's local timezone
        ZonedDateTime localDayStart = targetDate.atStartOfDay(zoneId);
        ZonedDateTime localDayEnd = targetDate.plusDays(1).atStartOfDay(zoneId);

        Instant dayStart = localDayStart.toInstant();
        Instant dayEnd = localDayEnd.toInstant();

        // Retrieve all activities intersecting this calendar day
        List<Activity> activities = activityRepository.findActivitiesInRange(userId, dayStart, dayEnd);

        List<TimelineSegmentResponse> segments = new ArrayList<>();
        Map<Long, CategoryDurationAccumulator> categoryMap = new LinkedHashMap<>();

        long totalTrackedSeconds = 0L;
        long totalUntrackedSeconds = 0L;
        Instant cursor = dayStart;

        for (Activity act : activities) {
            // Clip activity interval to this calendar day
            Instant clippedStart = act.getStartTime().isBefore(dayStart) ? dayStart : act.getStartTime();
            Instant clippedEnd = act.getEndTime().isAfter(dayEnd) ? dayEnd : act.getEndTime();

            if (!clippedEnd.isAfter(clippedStart)) {
                continue;
            }

            // Detect untracked gap prior to this activity
            if (clippedStart.isAfter(cursor)) {
                long untrackedDuration = Duration.between(cursor, clippedStart).getSeconds();
                if (untrackedDuration > 0) {
                    segments.add(createUntrackedSegment(cursor, clippedStart, zoneId, timeFormatter, untrackedDuration));
                    totalUntrackedSeconds += untrackedDuration;
                }
            }

            // Create activity segment
            long activityDuration = Duration.between(clippedStart, clippedEnd).getSeconds();
            segments.add(createActivitySegment(act, clippedStart, clippedEnd, zoneId, timeFormatter, activityDuration));
            totalTrackedSeconds += activityDuration;

            // Accumulate category statistics
            Category cat = act.getCategory();
            categoryMap.computeIfAbsent(cat.getId(), k -> new CategoryDurationAccumulator(cat.getId(), cat.getName(), cat.getColor()))
                    .addSeconds(activityDuration);

            cursor = clippedEnd;
        }

        // Detect trailing untracked gap between last activity and 24:00 (end of day)
        if (cursor.isBefore(dayEnd)) {
            long trailingDuration = Duration.between(cursor, dayEnd).getSeconds();
            if (trailingDuration > 0) {
                segments.add(createUntrackedSegment(cursor, dayEnd, zoneId, timeFormatter, trailingDuration));
                totalUntrackedSeconds += trailingDuration;
            }
        }

        // Build category breakdown
        List<CategoryTimeDistribution> categoryDistributions = new ArrayList<>();
        final long finalTrackedSeconds = totalTrackedSeconds;
        for (CategoryDurationAccumulator acc : categoryMap.values()) {
            double hours = Math.round((acc.seconds / 3600.0) * 100.0) / 100.0;
            double percentage = (finalTrackedSeconds > 0)
                    ? Math.round((acc.seconds * 100.0 / finalTrackedSeconds) * 10.0) / 10.0
                    : 0.0;
            String formatted = formatDuration(acc.seconds);
            categoryDistributions.add(new CategoryTimeDistribution(
                    acc.categoryId, acc.categoryName, acc.categoryColor,
                    acc.seconds, hours, formatted, percentage
            ));
        }

        DailyTimelineResponse response = new DailyTimelineResponse();
        response.setDate(targetDate.toString());
        response.setDayOfWeek(targetDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH));
        response.setTimezone(zoneId.getId());
        response.setTotalTrackedSeconds(totalTrackedSeconds);
        response.setTotalUntrackedSeconds(totalUntrackedSeconds);
        response.setTotalActivities(activities.size());

        double totalDaySeconds = Math.max(1, totalTrackedSeconds + totalUntrackedSeconds);
        double trackedPercentage = Math.round((totalTrackedSeconds * 100.0 / totalDaySeconds) * 10.0) / 10.0;
        response.setTrackedPercentage(trackedPercentage);

        response.setSegments(segments);
        response.setCategoryDistribution(categoryDistributions);

        List<ActivityResponse> activityResponses = activities.stream()
                .map(act -> new ActivityResponse(
                        act.getId(),
                        act.getCategory().getId(),
                        act.getCategory().getName(),
                        act.getCategory().getColor(),
                        act.getTitle(),
                        act.getDescription(),
                        act.getStartTime(),
                        act.getEndTime(),
                        act.getDurationSeconds(),
                        act.getNotes(),
                        act.getCreatedAt(),
                        act.getUpdatedAt()
                ))
                .toList();
        response.setActivities(activityResponses);

        return response;
    }

    private TimelineSegmentResponse createActivitySegment(Activity act, Instant start, Instant end,
                                                          ZoneId zoneId, DateTimeFormatter formatter,
                                                          long durationSeconds) {
        ZonedDateTime zStart = start.atZone(zoneId);
        ZonedDateTime zEnd = end.atZone(zoneId);

        String localStartTime = zStart.format(formatter);
        String localEndTime = zEnd.format(formatter);

        double startHour = calculateHourFraction(zStart);
        double endHour = calculateHourFraction(zEnd);
        if (endHour == 0.0 && zEnd.getHour() == 0 && !start.equals(end)) {
            endHour = 24.0; // Handled midnight wrap
        }

        return TimelineSegmentResponse.createActivitySegment(
                act.getId(),
                act.getTitle(),
                act.getDescription(),
                act.getCategory().getId(),
                act.getCategory().getName(),
                act.getCategory().getColor(),
                start,
                end,
                localStartTime,
                localEndTime,
                startHour,
                endHour,
                durationSeconds
        );
    }

    private TimelineSegmentResponse createUntrackedSegment(Instant start, Instant end,
                                                           ZoneId zoneId, DateTimeFormatter formatter,
                                                           long durationSeconds) {
        ZonedDateTime zStart = start.atZone(zoneId);
        ZonedDateTime zEnd = end.atZone(zoneId);

        String localStartTime = zStart.format(formatter);
        String localEndTime = zEnd.format(formatter);

        double startHour = calculateHourFraction(zStart);
        double endHour = calculateHourFraction(zEnd);
        if (endHour == 0.0 && zEnd.getHour() == 0 && !start.equals(end)) {
            endHour = 24.0;
        }

        return TimelineSegmentResponse.createUntrackedSegment(
                start,
                end,
                localStartTime,
                localEndTime,
                startHour,
                endHour,
                durationSeconds
        );
    }

    private double calculateHourFraction(ZonedDateTime zdt) {
        return Math.round((zdt.getHour() + (zdt.getMinute() / 60.0) + (zdt.getSecond() / 3600.0)) * 100.0) / 100.0;
    }

    private String formatDuration(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        if (hours > 0 && minutes > 0) {
            return String.format("%dh %dm", hours, minutes);
        } else if (hours > 0) {
            return String.format("%dh", hours);
        } else {
            return String.format("%dm", minutes);
        }
    }

    private static class CategoryDurationAccumulator {
        final Long categoryId;
        final String categoryName;
        final String categoryColor;
        long seconds = 0L;

        CategoryDurationAccumulator(Long categoryId, String categoryName, String categoryColor) {
            this.categoryId = categoryId;
            this.categoryName = categoryName;
            this.categoryColor = categoryColor;
        }

        void addSeconds(long s) {
            this.seconds += s;
        }
    }
}
