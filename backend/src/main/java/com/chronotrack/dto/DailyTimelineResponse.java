package com.chronotrack.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Complete 24-hour daily timeline payload containing all continuous segments
 * (both activities and untracked gaps) totaling 24 hours.
 */
public class DailyTimelineResponse {

    private String date;
    private String dayOfWeek;
    private String timezone;
    private long totalTrackedSeconds;
    private long totalUntrackedSeconds;
    private double trackedHours;
    private double untrackedHours;
    private int totalActivities;
    private double trackedPercentage;
    private List<TimelineSegmentResponse> segments = new ArrayList<>();
    private List<CategoryTimeDistribution> categoryDistribution = new ArrayList<>();
    private List<ActivityResponse> activities = new ArrayList<>();

    public DailyTimelineResponse() {
    }

    public List<TimelineSegmentResponse> getBlocks() {
        return segments;
    }

    public List<ActivityResponse> getActivities() {
        return activities;
    }

    public void setActivities(List<ActivityResponse> activities) {
        this.activities = activities;
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
        this.trackedHours = Math.round((totalTrackedSeconds / 3600.0) * 100.0) / 100.0;
    }

    public long getTotalUntrackedSeconds() {
        return totalUntrackedSeconds;
    }

    public void setTotalUntrackedSeconds(long totalUntrackedSeconds) {
        this.totalUntrackedSeconds = totalUntrackedSeconds;
        this.untrackedHours = Math.round((totalUntrackedSeconds / 3600.0) * 100.0) / 100.0;
    }

    public double getTrackedHours() {
        return trackedHours;
    }

    public void setTrackedHours(double trackedHours) {
        this.trackedHours = trackedHours;
    }

    public double getUntrackedHours() {
        return untrackedHours;
    }

    public void setUntrackedHours(double untrackedHours) {
        this.untrackedHours = untrackedHours;
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

    public List<TimelineSegmentResponse> getSegments() {
        return segments;
    }

    public void setSegments(List<TimelineSegmentResponse> segments) {
        this.segments = segments;
    }

    public List<CategoryTimeDistribution> getCategoryDistribution() {
        return categoryDistribution;
    }

    public void setCategoryDistribution(List<CategoryTimeDistribution> categoryDistribution) {
        this.categoryDistribution = categoryDistribution;
    }
}
