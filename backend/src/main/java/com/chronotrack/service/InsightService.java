package com.chronotrack.service;

import com.chronotrack.dto.GoalResponse;
import com.chronotrack.dto.InsightResponse;
import com.chronotrack.dto.WeeklyReportResponse;
import com.chronotrack.entity.UserSettings;
import com.chronotrack.repository.ActivityRepository;
import com.chronotrack.repository.UserSettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Service generating rule-based, data-driven productivity insights and habit analytics
 * calculated directly from actual PostgreSQL database records.
 */
@Service
public class InsightService {

    private final ReportService reportService;
    private final GoalService goalService;
    private final TimelineService timelineService;
    private final ActivityRepository activityRepository;
    private final UserSettingsRepository userSettingsRepository;

    public InsightService(ReportService reportService,
                          GoalService goalService,
                          TimelineService timelineService,
                          ActivityRepository activityRepository,
                          UserSettingsRepository userSettingsRepository) {
        this.reportService = reportService;
        this.goalService = goalService;
        this.timelineService = timelineService;
        this.activityRepository = activityRepository;
        this.userSettingsRepository = userSettingsRepository;
    }

    /**
     * Calculates personalized productivity insights based on real database records.
     */
    @Transactional(readOnly = true)
    public List<InsightResponse> generateInsights(Long userId) {
        List<InsightResponse> insights = new ArrayList<>();
        ZoneId zoneId = getUserZoneId(userId);
        LocalDate today = LocalDate.now(zoneId);

        WeeklyReportResponse weekly = reportService.getWeeklyReport(userId, today);
        var dailyTimeline = timelineService.getDailyTimeline(userId, today);
        List<GoalResponse> goals = goalService.getGoals(userId);

        // Insight 1: Weekly Volume & Growth
        if (weekly.getTotalTrackedHours() > 0) {
            String title = "Weekly Productivity";
            String metric = weekly.getTotalTrackedHours() + " hrs";
            String message;

            if (weekly.getPercentageChange() != null && weekly.getPercentageChange() > 0) {
                message = String.format("You tracked %.1f hours this week — up by %.1f%% compared to last week!",
                        weekly.getTotalTrackedHours(), weekly.getPercentageChange());
                insights.add(new InsightResponse("insight-1", "SUCCESS", "PRODUCTIVITY", title, message, "+" + weekly.getPercentageChange() + "%", "TrendingUp"));
            } else if (weekly.getPercentageChange() != null && weekly.getPercentageChange() < 0) {
                message = String.format("You tracked %.1f hours this week (%.1f%% lower than last week). Keep building momentum!",
                        weekly.getTotalTrackedHours(), Math.abs(weekly.getPercentageChange()));
                insights.add(new InsightResponse("insight-1", "INFO", "PRODUCTIVITY", title, message, weekly.getPercentageChange() + "%", "TrendingDown"));
            } else {
                message = String.format("You logged %d activities totaling %.1f hours this week.",
                        weekly.getTotalActivities(), weekly.getTotalTrackedHours());
                insights.add(new InsightResponse("insight-1", "INFO", "PRODUCTIVITY", title, message, metric, "Clock"));
            }
        }

        // Insight 2: Peak Productive Day
        if (weekly.getMostActiveDay() != null && !weekly.getMostActiveDay().equals("None")) {
            insights.add(new InsightResponse(
                    "insight-2",
                    "INFO",
                    "HABIT",
                    "Peak Focus Day",
                    "Your most productive day this week was " + weekly.getMostActiveDay() + ".",
                    weekly.getMostActiveDay(),
                    "Zap"
            ));
        }

        // Insight 3: Goal Milestones
        for (GoalResponse goal : goals) {
            if (goal.isActive()) {
                if (goal.isCompleted()) {
                    insights.add(new InsightResponse(
                            "insight-goal-" + goal.getId(),
                            "ACHIEVEMENT",
                            "GOAL",
                            "Goal Accomplished!",
                            String.format("Congratulations! You achieved 100%% of your %s goal '%s' (%s).",
                                    goal.getFrequency().name().toLowerCase(), goal.getTitle(), goal.getProgressFormatted()),
                            "100%",
                            "Award"
                    ));
                    break;
                } else if (goal.getProgressPercentage() >= 50.0) {
                    insights.add(new InsightResponse(
                            "insight-goal-" + goal.getId(),
                            "SUCCESS",
                            "GOAL",
                            "Strong Goal Progress",
                            String.format("You have reached %.0f%% of your %s goal '%s' (%s).",
                                    goal.getProgressPercentage(), goal.getFrequency().name().toLowerCase(), goal.getTitle(), goal.getProgressFormatted()),
                            String.format("%.0f%%", goal.getProgressPercentage()),
                            "Target"
                    ));
                    break;
                }
            }
        }

        // Insight 4: Category Focus (e.g. Learning / Work / Exercise)
        if (!weekly.getCategoryDistribution().isEmpty()) {
            var topCategory = weekly.getCategoryDistribution().get(0);
            insights.add(new InsightResponse(
                    "insight-4",
                    "INFO",
                    "LEARNING",
                    "Top Focus Category",
                    String.format("'%s' is your leading category this week with %.1f hours (%.0f%% of tracked time).",
                            topCategory.getCategoryName(), topCategory.getHours(), topCategory.getPercentage()),
                    topCategory.getCategoryName(),
                    "PieChart"
            ));
        }

        // Insight 5: Untracked Time Awareness for Today
        if (dailyTimeline.getUntrackedHours() > 0) {
            insights.add(new InsightResponse(
                    "insight-5",
                    "INFO",
                    "HABIT",
                    "Untracked Time Today",
                    String.format("You have %.1f hours of untracked time today. Click on the 24-hour timeline to log your recent activities.",
                            dailyTimeline.getUntrackedHours()),
                    String.format("%.1f hrs", dailyTimeline.getUntrackedHours()),
                    "Clock"
            ));
        }

        return insights;
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
