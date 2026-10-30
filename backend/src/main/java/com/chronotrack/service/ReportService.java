package com.chronotrack.service;

import com.chronotrack.dto.*;
import com.chronotrack.entity.Activity;
import com.chronotrack.entity.Category;
import com.chronotrack.entity.UserSettings;
import com.chronotrack.repository.ActivityRepository;
import com.chronotrack.repository.UserSettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

/**
 * Service generating comprehensive daily, weekly (Monday-Sunday), monthly, and yearly reports.
 * Accurately divides overnight activities across dates and calculates productivity comparisons.
 */
@Service
public class ReportService {

    private final ActivityRepository activityRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final ActivityService activityService;

    public ReportService(ActivityRepository activityRepository,
                         UserSettingsRepository userSettingsRepository,
                         ActivityService activityService) {
        this.activityRepository = activityRepository;
        this.userSettingsRepository = userSettingsRepository;
        this.activityService = activityService;
    }

    /**
     * Generates detailed daily analytics for a chosen date.
     */
    @Transactional(readOnly = true)
    public DailyReportResponse getDailyReport(Long userId, LocalDate date) {
        ZoneId zoneId = getUserZoneId(userId);
        LocalDate targetDate = (date != null) ? date : LocalDate.now(zoneId);

        Instant dayStart = targetDate.atStartOfDay(zoneId).toInstant();
        Instant dayEnd = targetDate.plusDays(1).atStartOfDay(zoneId).toInstant();

        List<Activity> activities = activityRepository.findActivitiesInRange(userId, dayStart, dayEnd);

        long totalTrackedSeconds = 0L;
        Map<Long, CategoryAccumulator> categoryMap = new LinkedHashMap<>();
        List<ActivityResponse> activityResponses = new ArrayList<>();

        for (Activity act : activities) {
            Instant clippedStart = act.getStartTime().isBefore(dayStart) ? dayStart : act.getStartTime();
            Instant clippedEnd = act.getEndTime().isAfter(dayEnd) ? dayEnd : act.getEndTime();

            long duration = Duration.between(clippedStart, clippedEnd).getSeconds();
            if (duration > 0) {
                totalTrackedSeconds += duration;

                Category cat = act.getCategory();
                categoryMap.computeIfAbsent(cat.getId(), k -> new CategoryAccumulator(cat.getId(), cat.getName(), cat.getColor()))
                        .add(duration);

                activityResponses.add(activityService.mapToResponse(act));
            }
        }

        long totalUntrackedSeconds = Math.max(0, 86400L - totalTrackedSeconds);

        DailyReportResponse response = new DailyReportResponse();
        response.setDate(targetDate.toString());
        response.setDayOfWeek(targetDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH));
        response.setTimezone(zoneId.getId());
        response.setTotalTrackedSeconds(totalTrackedSeconds);
        response.setTotalUntrackedSeconds(totalUntrackedSeconds);
        response.setTotalActivities(activityResponses.size());

        double trackedPercentage = Math.round((totalTrackedSeconds / 864.0) * 10.0) / 10.0;
        response.setTrackedPercentage(trackedPercentage);

        response.setCategoryDistribution(buildCategoryDistributions(categoryMap, totalTrackedSeconds));
        response.setActivities(activityResponses);

        return response;
    }

    /**
     * Generates weekly report adhering strictly to Monday to Sunday definition.
     */
    @Transactional(readOnly = true)
    public WeeklyReportResponse getWeeklyReport(Long userId, LocalDate referenceDate) {
        ZoneId zoneId = getUserZoneId(userId);
        LocalDate target = (referenceDate != null) ? referenceDate : LocalDate.now(zoneId);

        LocalDate monday = target.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate sunday = monday.plusDays(6);

        Instant weekStart = monday.atStartOfDay(zoneId).toInstant();
        Instant weekEnd = sunday.plusDays(1).atStartOfDay(zoneId).toInstant();

        // Previous week boundaries for comparison
        LocalDate prevMonday = monday.minusWeeks(1);
        LocalDate prevSunday = monday.minusDays(1);
        Instant prevWeekStart = prevMonday.atStartOfDay(zoneId).toInstant();
        Instant prevWeekEnd = prevSunday.plusDays(1).atStartOfDay(zoneId).toInstant();

        List<Activity> currentWeekActivities = activityRepository.findActivitiesInRange(userId, weekStart, weekEnd);
        List<Activity> prevWeekActivities = activityRepository.findActivitiesInRange(userId, prevWeekStart, prevWeekEnd);

        // Calculate previous week tracked hours
        long prevTrackedSeconds = calculateTrackedSecondsInRange(prevWeekActivities, prevWeekStart, prevWeekEnd);
        double prevHours = Math.round((prevTrackedSeconds / 3600.0) * 100.0) / 100.0;

        // Daily breakdown for Monday through Sunday
        List<DailyActivitySummary> dailySummaries = new ArrayList<>();
        Map<Long, CategoryAccumulator> weekCategories = new LinkedHashMap<>();
        long totalWeekSeconds = 0L;
        int totalActivities = 0;

        DailyActivitySummary bestDay = null;
        DailyActivitySummary lowestDay = null;

        for (int i = 0; i < 7; i++) {
            LocalDate day = monday.plusDays(i);
            Instant dayStart = day.atStartOfDay(zoneId).toInstant();
            Instant dayEnd = day.plusDays(1).atStartOfDay(zoneId).toInstant();

            long dayTrackedSeconds = 0L;
            int dayActivityCount = 0;
            Map<Long, CategoryAccumulator> dayCategories = new LinkedHashMap<>();

            for (Activity act : currentWeekActivities) {
                Instant clippedStart = act.getStartTime().isBefore(dayStart) ? dayStart : act.getStartTime();
                Instant clippedEnd = act.getEndTime().isAfter(dayEnd) ? dayEnd : act.getEndTime();

                if (clippedEnd.isAfter(clippedStart)) {
                    long d = Duration.between(clippedStart, clippedEnd).getSeconds();
                    dayTrackedSeconds += d;
                    dayActivityCount++;

                    Category cat = act.getCategory();
                    dayCategories.computeIfAbsent(cat.getId(), k -> new CategoryAccumulator(cat.getId(), cat.getName(), cat.getColor()))
                            .add(d);
                    weekCategories.computeIfAbsent(cat.getId(), k -> new CategoryAccumulator(cat.getId(), cat.getName(), cat.getColor()))
                            .add(d);
                }
            }

            DailyActivitySummary summary = new DailyActivitySummary(
                    day.toString(),
                    day.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                    dayTrackedSeconds,
                    dayActivityCount
            );
            summary.setCategoryBreakdown(buildCategoryDistributions(dayCategories, dayTrackedSeconds));
            dailySummaries.add(summary);

            totalWeekSeconds += dayTrackedSeconds;
            totalActivities += dayActivityCount;

            if (bestDay == null || summary.getTotalTrackedSeconds() > bestDay.getTotalTrackedSeconds()) {
                bestDay = summary;
            }
            if (lowestDay == null || summary.getTotalTrackedSeconds() < lowestDay.getTotalTrackedSeconds()) {
                lowestDay = summary;
            }
        }

        WeeklyReportResponse response = new WeeklyReportResponse();
        response.setWeekStart(monday.toString());
        response.setWeekEnd(sunday.toString());
        response.setWeekNumber(monday.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR));
        response.setYear(monday.getYear());
        response.setTotalTrackedSeconds(totalWeekSeconds);
        response.setPreviousWeekHours(prevHours);
        response.setTotalActivities(totalActivities);

        if (prevTrackedSeconds > 0) {
            double change = ((double) (totalWeekSeconds - prevTrackedSeconds) / prevTrackedSeconds) * 100.0;
            response.setPercentageChange(Math.round(change * 10.0) / 10.0);
        } else if (totalWeekSeconds > 0) {
            response.setPercentageChange(100.0);
        } else {
            response.setPercentageChange(0.0);
        }

        if (bestDay != null && bestDay.getTotalTrackedHours() > 0) {
            response.setMostActiveDay(bestDay.getDayName() + " (" + bestDay.getTotalTrackedHours() + "h)");
        } else {
            response.setMostActiveDay("None");
        }

        if (lowestDay != null) {
            response.setLeastActiveDay(lowestDay.getDayName() + " (" + lowestDay.getTotalTrackedHours() + "h)");
        } else {
            response.setLeastActiveDay("None");
        }

        response.setDailyBreakdown(dailySummaries);
        response.setCategoryDistribution(buildCategoryDistributions(weekCategories, totalWeekSeconds));

        return response;
    }

    /**
     * Generates monthly productivity analytics and trends.
     */
    @Transactional(readOnly = true)
    public MonthlyReportResponse getMonthlyReport(Long userId, Integer year, Integer month) {
        ZoneId zoneId = getUserZoneId(userId);
        LocalDate now = LocalDate.now(zoneId);

        int targetYear = (year != null && year >= 2000) ? year : now.getYear();
        int targetMonth = (month != null && month >= 1 && month <= 12) ? month : now.getMonthValue();

        YearMonth currentYm = YearMonth.of(targetYear, targetMonth);
        YearMonth prevYm = currentYm.minusMonths(1);

        LocalDate startMonth = currentYm.atDay(1);
        LocalDate endMonth = currentYm.atEndOfMonth();

        Instant monthStart = startMonth.atStartOfDay(zoneId).toInstant();
        Instant monthEnd = endMonth.plusDays(1).atStartOfDay(zoneId).toInstant();

        Instant prevMonthStart = prevYm.atDay(1).atStartOfDay(zoneId).toInstant();
        Instant prevMonthEnd = prevYm.atEndOfMonth().plusDays(1).atStartOfDay(zoneId).toInstant();

        List<Activity> currentActivities = activityRepository.findActivitiesInRange(userId, monthStart, monthEnd);
        List<Activity> prevActivities = activityRepository.findActivitiesInRange(userId, prevMonthStart, prevMonthEnd);

        long prevTrackedSeconds = calculateTrackedSecondsInRange(prevActivities, prevMonthStart, prevMonthEnd);
        double prevHours = Math.round((prevTrackedSeconds / 3600.0) * 100.0) / 100.0;

        List<DailyActivitySummary> dailySummaries = new ArrayList<>();
        Map<Long, CategoryAccumulator> monthCategories = new LinkedHashMap<>();
        long totalMonthSeconds = 0L;
        int activeDays = 0;
        int totalActivities = 0;
        DailyActivitySummary bestDay = null;

        for (int dayNum = 1; dayNum <= currentYm.lengthOfMonth(); dayNum++) {
            LocalDate day = currentYm.atDay(dayNum);
            Instant dayStart = day.atStartOfDay(zoneId).toInstant();
            Instant dayEnd = day.plusDays(1).atStartOfDay(zoneId).toInstant();

            long dayTrackedSeconds = 0L;
            int dayActivityCount = 0;

            for (Activity act : currentActivities) {
                Instant clippedStart = act.getStartTime().isBefore(dayStart) ? dayStart : act.getStartTime();
                Instant clippedEnd = act.getEndTime().isAfter(dayEnd) ? dayEnd : act.getEndTime();

                if (clippedEnd.isAfter(clippedStart)) {
                    long d = Duration.between(clippedStart, clippedEnd).getSeconds();
                    dayTrackedSeconds += d;
                    dayActivityCount++;

                    Category cat = act.getCategory();
                    monthCategories.computeIfAbsent(cat.getId(), k -> new CategoryAccumulator(cat.getId(), cat.getName(), cat.getColor()))
                            .add(d);
                }
            }

            DailyActivitySummary summary = new DailyActivitySummary(
                    day.toString(),
                    day.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                    dayTrackedSeconds,
                    dayActivityCount
            );
            dailySummaries.add(summary);

            totalMonthSeconds += dayTrackedSeconds;
            totalActivities += dayActivityCount;
            if (dayTrackedSeconds > 0) {
                activeDays++;
            }

            if (bestDay == null || summary.getTotalTrackedSeconds() > bestDay.getTotalTrackedSeconds()) {
                bestDay = summary;
            }
        }

        MonthlyReportResponse response = new MonthlyReportResponse();
        response.setYear(targetYear);
        response.setMonth(targetMonth);
        response.setMonthName(currentYm.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH));
        response.setDaysInMonth(currentYm.lengthOfMonth());
        response.setTotalTrackedSeconds(totalMonthSeconds);
        response.setPreviousMonthHours(prevHours);
        response.setActiveDaysCount(activeDays);
        response.setTotalActivities(totalActivities);

        if (prevTrackedSeconds > 0) {
            double change = ((double) (totalMonthSeconds - prevTrackedSeconds) / prevTrackedSeconds) * 100.0;
            response.setPercentageChange(Math.round(change * 10.0) / 10.0);
        } else if (totalMonthSeconds > 0) {
            response.setPercentageChange(100.0);
        } else {
            response.setPercentageChange(0.0);
        }

        if (bestDay != null && bestDay.getTotalTrackedHours() > 0) {
            response.setMostActiveDay(bestDay.getDate() + " (" + bestDay.getTotalTrackedHours() + "h)");
        } else {
            response.setMostActiveDay("None");
        }

        response.setDailyBreakdown(dailySummaries);
        response.setCategoryDistribution(buildCategoryDistributions(monthCategories, totalMonthSeconds));

        return response;
    }

    /**
     * Generates yearly productivity summary.
     */
    @Transactional(readOnly = true)
    public YearlyReportResponse getYearlyReport(Long userId, Integer year) {
        ZoneId zoneId = getUserZoneId(userId);
        int targetYear = (year != null && year >= 2000) ? year : LocalDate.now(zoneId).getYear();

        LocalDate yearStartDate = LocalDate.of(targetYear, 1, 1);
        LocalDate yearEndDate = LocalDate.of(targetYear, 12, 31);

        Instant yearStart = yearStartDate.atStartOfDay(zoneId).toInstant();
        Instant yearEnd = yearEndDate.plusDays(1).atStartOfDay(zoneId).toInstant();

        List<Activity> yearActivities = activityRepository.findActivitiesInRange(userId, yearStart, yearEnd);

        List<YearlyReportResponse.MonthlySummaryItem> monthlySummaries = new ArrayList<>();
        Map<Long, CategoryAccumulator> yearCategories = new LinkedHashMap<>();
        long totalYearSeconds = 0L;
        int totalActivities = 0;
        YearlyReportResponse.MonthlySummaryItem bestMonth = null;

        for (int m = 1; m <= 12; m++) {
            YearMonth ym = YearMonth.of(targetYear, m);
            Instant mStart = ym.atDay(1).atStartOfDay(zoneId).toInstant();
            Instant mEnd = ym.atEndOfMonth().plusDays(1).atStartOfDay(zoneId).toInstant();

            long mSeconds = 0L;
            int mCount = 0;

            for (Activity act : yearActivities) {
                Instant clippedStart = act.getStartTime().isBefore(mStart) ? mStart : act.getStartTime();
                Instant clippedEnd = act.getEndTime().isAfter(mEnd) ? mEnd : act.getEndTime();

                if (clippedEnd.isAfter(clippedStart)) {
                    long d = Duration.between(clippedStart, clippedEnd).getSeconds();
                    mSeconds += d;
                    mCount++;

                    Category cat = act.getCategory();
                    yearCategories.computeIfAbsent(cat.getId(), k -> new CategoryAccumulator(cat.getId(), cat.getName(), cat.getColor()))
                            .add(d);
                }
            }

            YearlyReportResponse.MonthlySummaryItem item = new YearlyReportResponse.MonthlySummaryItem(
                    m, ym.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH), mSeconds, mCount
            );
            monthlySummaries.add(item);

            totalYearSeconds += mSeconds;
            totalActivities += mCount;

            if (bestMonth == null || item.getTotalTrackedSeconds() > bestMonth.getTotalTrackedSeconds()) {
                bestMonth = item;
            }
        }

        YearlyReportResponse response = new YearlyReportResponse();
        response.setYear(targetYear);
        response.setTotalTrackedSeconds(totalYearSeconds);
        response.setTotalActivities(totalActivities);
        response.setMostActiveMonth(bestMonth != null && bestMonth.getTotalTrackedHours() > 0
                ? bestMonth.getMonthName() + " (" + bestMonth.getTotalTrackedHours() + "h)"
                : "None");
        response.setMonthlyBreakdown(monthlySummaries);
        response.setCategoryDistribution(buildCategoryDistributions(yearCategories, totalYearSeconds));

        return response;
    }

    private long calculateTrackedSecondsInRange(List<Activity> activities, Instant rangeStart, Instant rangeEnd) {
        long seconds = 0L;
        for (Activity act : activities) {
            Instant s = act.getStartTime().isBefore(rangeStart) ? rangeStart : act.getStartTime();
            Instant e = act.getEndTime().isAfter(rangeEnd) ? rangeEnd : act.getEndTime();
            if (e.isAfter(s)) {
                seconds += Duration.between(s, e).getSeconds();
            }
        }
        return seconds;
    }

    private List<CategoryTimeDistribution> buildCategoryDistributions(Map<Long, CategoryAccumulator> categoryMap,
                                                                      long totalTrackedSeconds) {
        List<CategoryTimeDistribution> result = new ArrayList<>();
        for (CategoryAccumulator acc : categoryMap.values()) {
            double hours = Math.round((acc.seconds / 3600.0) * 100.0) / 100.0;
            double percentage = (totalTrackedSeconds > 0)
                    ? Math.round((acc.seconds * 100.0 / totalTrackedSeconds) * 10.0) / 10.0
                    : 0.0;
            String formatted = ActivityResponse.formatDuration(acc.seconds);
            result.add(new CategoryTimeDistribution(
                    acc.categoryId, acc.categoryName, acc.categoryColor,
                    acc.seconds, hours, formatted, percentage
            ));
        }
        return result;
    }

    /**
     * Exports activity and category productivity breakdown to CSV.
     */
    @Transactional(readOnly = true)
    public byte[] exportReportToCsv(Long userId, String type, LocalDate startDate, LocalDate endDate) {
        ZoneId zoneId = getUserZoneId(userId);
        LocalDate start = (startDate != null) ? startDate : LocalDate.now(zoneId).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate end = (endDate != null) ? endDate : start.plusDays(6);

        Instant rangeStart = start.atStartOfDay(zoneId).toInstant();
        Instant rangeEnd = end.plusDays(1).atStartOfDay(zoneId).toInstant();

        List<Activity> activities = activityRepository.findActivitiesInRange(userId, rangeStart, rangeEnd);
        WeeklyReportResponse weeklyReport = getWeeklyReport(userId, start);

        StringBuilder sb = new StringBuilder();
        sb.append("ChronoTrack Productivity Analytics Report\n");
        sb.append("Report Type,").append(type != null ? type.toUpperCase() : "WEEKLY").append("\n");
        sb.append("Date Range,").append(start).append(" to ").append(end).append("\n");
        sb.append("Total Tracked Time,").append(weeklyReport.getTotalTrackedFormatted()).append("\n");
        sb.append("Productive Time,").append(weeklyReport.getProductiveTimeFormatted()).append("\n");
        sb.append("Productivity Score,").append(weeklyReport.getProductivityPercentage()).append("%\n\n");

        sb.append("--- CATEGORY BREAKDOWN ---\n");
        sb.append("Category,Duration (Minutes),Duration (Hours),Share Percentage\n");
        for (CategoryTimeDistribution c : weeklyReport.getCategoryDistribution()) {
            sb.append(escapeCsv(c.categoryName())).append(",")
              .append(c.durationSeconds() / 60).append(",")
              .append(c.durationHours()).append(",")
              .append(c.percentage()).append("%\n");
        }
        sb.append("\n");

        sb.append("--- DETAILED ACTIVITIES ---\n");
        sb.append("Title,Category,Productive,Start Time,End Time,Duration (Minutes),Description\n");
        for (Activity act : activities) {
            sb.append(escapeCsv(act.getTitle())).append(",")
              .append(escapeCsv(act.getCategory() != null ? act.getCategory().getName() : "Uncategorized")).append(",")
              .append(act.isProductive() ? "Yes" : "No").append(",")
              .append(act.getStartTime().atZone(zoneId).toLocalDateTime()).append(",")
              .append(act.getEndTime().atZone(zoneId).toLocalDateTime()).append(",")
              .append(Duration.between(act.getStartTime(), act.getEndTime()).toMinutes()).append(",")
              .append(escapeCsv(act.getDescription() != null ? act.getDescription() : "")).append("\n");
        }

        return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private String escapeCsv(String text) {
        if (text == null) return "";
        if (text.contains(",") || text.contains("\"") || text.contains("\n")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
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

    private static class CategoryAccumulator {
        final Long categoryId;
        final String categoryName;
        final String categoryColor;
        long seconds = 0L;

        CategoryAccumulator(Long categoryId, String categoryName, String categoryColor) {
            this.categoryId = categoryId;
            this.categoryName = categoryName;
            this.categoryColor = categoryColor;
        }

        void add(long s) {
            this.seconds += s;
        }
    }
}
