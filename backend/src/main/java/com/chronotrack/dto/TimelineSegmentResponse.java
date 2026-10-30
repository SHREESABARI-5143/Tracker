package com.chronotrack.dto;

import java.time.Instant;

/**
 * Represents a discrete block of time within a 24-hour daily timeline.
 * Type is either "ACTIVITY" or "UNTRACKED".
 */
public class TimelineSegmentResponse {

    private String type; // "ACTIVITY" or "UNTRACKED"
    private Long activityId;
    private String title;
    private String description;
    private Long categoryId;
    private String categoryName;
    private String categoryColor;
    private Instant startTime;
    private Instant endTime;
    private String localStartTime; // e.g. "09:30"
    private String localEndTime;   // e.g. "11:00"
    private double startHour;      // e.g. 9.5 (for CSS/canvas percentage positioning)
    private double endHour;        // e.g. 11.0
    private long durationSeconds;
    private String durationFormatted;

    public TimelineSegmentResponse() {
    }

    public static TimelineSegmentResponse createActivitySegment(
            Long activityId, String title, String description,
            Long categoryId, String categoryName, String categoryColor,
            Instant startTime, Instant endTime,
            String localStartTime, String localEndTime,
            double startHour, double endHour, long durationSeconds) {
        TimelineSegmentResponse seg = new TimelineSegmentResponse();
        seg.type = "ACTIVITY";
        seg.activityId = activityId;
        seg.title = title;
        seg.description = description;
        seg.categoryId = categoryId;
        seg.categoryName = categoryName;
        seg.categoryColor = categoryColor;
        seg.startTime = startTime;
        seg.endTime = endTime;
        seg.localStartTime = localStartTime;
        seg.localEndTime = localEndTime;
        seg.startHour = startHour;
        seg.endHour = endHour;
        seg.durationSeconds = durationSeconds;
        seg.durationFormatted = ActivityResponse.formatDuration(durationSeconds);
        return seg;
    }

    public static TimelineSegmentResponse createUntrackedSegment(
            Instant startTime, Instant endTime,
            String localStartTime, String localEndTime,
            double startHour, double endHour, long durationSeconds) {
        TimelineSegmentResponse seg = new TimelineSegmentResponse();
        seg.type = "UNTRACKED";
        seg.activityId = null;
        seg.title = "Untracked Time";
        seg.description = "Unrecorded period";
        seg.categoryId = null;
        seg.categoryName = "Untracked";
        seg.categoryColor = "#E2E8F0"; // Subtle neutral slate/gray
        seg.startTime = startTime;
        seg.endTime = endTime;
        seg.localStartTime = localStartTime;
        seg.localEndTime = localEndTime;
        seg.startHour = startHour;
        seg.endHour = endHour;
        seg.durationSeconds = durationSeconds;
        seg.durationFormatted = ActivityResponse.formatDuration(durationSeconds);
        return seg;
    }

    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryColor() {
        return categoryColor;
    }

    public void setCategoryColor(String categoryColor) {
        this.categoryColor = categoryColor;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }

    public String getLocalStartTime() {
        return localStartTime;
    }

    public void setLocalStartTime(String localStartTime) {
        this.localStartTime = localStartTime;
    }

    public String getLocalEndTime() {
        return localEndTime;
    }

    public void setLocalEndTime(String localEndTime) {
        this.localEndTime = localEndTime;
    }

    public double getStartHour() {
        return startHour;
    }

    public void setStartHour(double startHour) {
        this.startHour = startHour;
    }

    public double getEndHour() {
        return endHour;
    }

    public void setEndHour(double endHour) {
        this.endHour = endHour;
    }

    public long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public String getDurationFormatted() {
        return durationFormatted;
    }

    public void setDurationFormatted(String durationFormatted) {
        this.durationFormatted = durationFormatted;
    }
}
