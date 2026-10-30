package com.chronotrack.dto;

import java.util.ArrayList;
import java.util.List;

public class DailyActivitySummary {

    private String date; // "YYYY-MM-DD"
    private String dayName; // "Mon", "Tue", etc.
    private long totalTrackedSeconds;
    private double totalTrackedHours;
    private int activityCount;
    private List<CategoryTimeDistribution> categoryBreakdown = new ArrayList<>();

    public DailyActivitySummary() {
    }

    public DailyActivitySummary(String date, String dayName, long totalTrackedSeconds, int activityCount) {
        this.date = date;
        this.dayName = dayName;
        this.totalTrackedSeconds = totalTrackedSeconds;
        this.totalTrackedHours = Math.round((totalTrackedSeconds / 3600.0) * 100.0) / 100.0;
        this.activityCount = activityCount;
    }

    // Getters and Setters
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDayName() {
        return dayName;
    }

    public void setDayName(String dayName) {
        this.dayName = dayName;
    }

    public long getTotalTrackedSeconds() {
        return totalTrackedSeconds;
    }

    public void setTotalTrackedSeconds(long totalTrackedSeconds) {
        this.totalTrackedSeconds = totalTrackedSeconds;
        this.totalTrackedHours = Math.round((totalTrackedSeconds / 3600.0) * 100.0) / 100.0;
    }

    public double getTotalTrackedHours() {
        return totalTrackedHours;
    }

    public void setTotalTrackedHours(double totalTrackedHours) {
        this.totalTrackedHours = totalTrackedHours;
    }

    public int getActivityCount() {
        return activityCount;
    }

    public void setActivityCount(int activityCount) {
        this.activityCount = activityCount;
    }

    public List<CategoryTimeDistribution> getCategoryBreakdown() {
        return categoryBreakdown;
    }

    public void setCategoryBreakdown(List<CategoryTimeDistribution> categoryBreakdown) {
        this.categoryBreakdown = categoryBreakdown;
    }

    public long getProductiveMinutes() {
        return (long) (totalTrackedSeconds * 0.85 / 60);
    }

    public long getTotalTrackedMinutes() {
        return totalTrackedSeconds / 60;
    }
}
