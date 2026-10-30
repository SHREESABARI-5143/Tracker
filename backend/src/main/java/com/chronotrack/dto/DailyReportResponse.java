package com.chronotrack.dto;

import java.util.ArrayList;
import java.util.List;

public class DailyReportResponse {

    private String date;
    private String dayOfWeek;
    private String timezone;
    private long totalTrackedSeconds;
    private long totalUntrackedSeconds;
    private double totalTrackedHours;
    private double totalUntrackedHours;
    private int totalActivities;
    private double trackedPercentage;
    private List<CategoryTimeDistribution> categoryDistribution = new ArrayList<>();
    private List<ActivityResponse> activities = new ArrayList<>();

    public DailyReportResponse() {
    }

    // Getters and Setters
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public long getTotalTrackedSeconds() {
        return totalTrackedSeconds;
    }

    public void setTotalTrackedSeconds(long totalTrackedSeconds) {
        this.totalTrackedSeconds = totalTrackedSeconds;
        this.totalTrackedHours = Math.round((totalTrackedSeconds / 3600.0) * 100.0) / 100.0;
    }

    public long getTotalUntrackedSeconds() {
        return totalUntrackedSeconds;
    }

    public void setTotalUntrackedSeconds(long totalUntrackedSeconds) {
        this.totalUntrackedSeconds = totalUntrackedSeconds;
        this.totalUntrackedHours = Math.round((totalUntrackedSeconds / 3600.0) * 100.0) / 100.0;
    }

    public double getTotalTrackedHours() {
        return totalTrackedHours;
    }

    public void setTotalTrackedHours(double totalTrackedHours) {
        this.totalTrackedHours = totalTrackedHours;
    }

    public double getTotalUntrackedHours() {
        return totalUntrackedHours;
    }

    public void setTotalUntrackedHours(double totalUntrackedHours) {
        this.totalUntrackedHours = totalUntrackedHours;
    }

    public int getTotalActivities() {
        return totalActivities;
    }

    public void setTotalActivities(int totalActivities) {
        this.totalActivities = totalActivities;
    }

    public double getTrackedPercentage() {
        return trackedPercentage;
    }

    public void setTrackedPercentage(double trackedPercentage) {
        this.trackedPercentage = trackedPercentage;
    }

    public List<CategoryTimeDistribution> getCategoryDistribution() {
        return categoryDistribution;
    }

    public void setCategoryDistribution(List<CategoryTimeDistribution> categoryDistribution) {
        this.categoryDistribution = categoryDistribution;
    }

    public List<ActivityResponse> getActivities() {
        return activities;
    }

    public void setActivities(List<ActivityResponse> activities) {
        this.activities = activities;
    }

    public long getTotalTrackedMinutes() {
        return totalTrackedSeconds / 60;
    }

    public long getUntrackedMinutes() {
        return totalUntrackedSeconds / 60;
    }

    public long getProductiveMinutes() {
        return (long) ((totalTrackedSeconds / 60.0) * (trackedPercentage / 100.0));
    }

    public double getProductivityPercentage() {
        return trackedPercentage;
    }

    public List<CategoryTimeDistribution> getCategoryBreakdown() {
        return categoryDistribution;
    }
}
