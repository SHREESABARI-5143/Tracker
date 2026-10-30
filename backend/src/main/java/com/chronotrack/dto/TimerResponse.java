package com.chronotrack.dto;

import java.time.Instant;

public class TimerResponse {

    private Long id;
    private Long categoryId;
    private String categoryName;
    private String categoryColor;
    private String title;
    private String description;
    private String notes;
    private Instant startTime;
    private Instant pausedAt;
    private Long elapsedSeconds;
    private String elapsedFormatted;
    private String status;
    private boolean running;

    public TimerResponse() {
    }

    public TimerResponse(Long id, Long categoryId, String categoryName, String categoryColor,
                         String title, String description, String notes, Instant startTime,
                         Instant pausedAt, Long elapsedSeconds, String status) {
        this.id = id;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.categoryColor = categoryColor;
        this.title = title;
        this.description = description;
        this.notes = notes;
        this.startTime = startTime;
        this.pausedAt = pausedAt;
        this.elapsedSeconds = elapsedSeconds;
        this.elapsedFormatted = formatTimerClock(elapsedSeconds);
        this.status = status;
        this.running = "RUNNING".equalsIgnoreCase(status);
    }

    public static String formatTimerClock(Long totalSeconds) {
        if (totalSeconds == null || totalSeconds < 0) {
            return "00:00:00";
        }
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public Instant getPausedAt() {
        return pausedAt;
    }

    public void setPausedAt(Instant pausedAt) {
        this.pausedAt = pausedAt;
    }

    public Long getElapsedSeconds() {
        return elapsedSeconds;
    }

    public void setElapsedSeconds(Long elapsedSeconds) {
        this.elapsedSeconds = elapsedSeconds;
        this.elapsedFormatted = formatTimerClock(elapsedSeconds);
    }

    public String getElapsedFormatted() {
        return elapsedFormatted;
    }

    public void setElapsedFormatted(String elapsedFormatted) {
        this.elapsedFormatted = elapsedFormatted;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        this.running = "RUNNING".equalsIgnoreCase(status);
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}
