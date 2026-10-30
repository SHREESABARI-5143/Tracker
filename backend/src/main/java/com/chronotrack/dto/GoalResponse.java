package com.chronotrack.dto;

import com.chronotrack.entity.GoalFrequency;

import java.time.Instant;
import java.time.LocalDate;

public class GoalResponse {

    private Long id;
    private Long categoryId;
    private String categoryName;
    private String categoryColor;
    private String title;
    private Integer targetMinutes;
    private int actualMinutes;
    private double progressPercentage;
    private String progressFormatted;
    private boolean completed;
    private GoalFrequency frequency;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean active;
    private Instant createdAt;

    public GoalResponse() {
    }

    public GoalResponse(Long id, Long categoryId, String categoryName, String categoryColor,
                        String title, Integer targetMinutes, int actualMinutes,
                        GoalFrequency frequency, LocalDate startDate, LocalDate endDate,
                        boolean active, Instant createdAt) {
        this.id = id;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.categoryColor = categoryColor;
        this.title = title;
        this.targetMinutes = targetMinutes;
        this.actualMinutes = actualMinutes;
        this.frequency = frequency;
        this.startDate = startDate;
        this.endDate = endDate;
        this.active = active;
        this.createdAt = createdAt;

        double target = (targetMinutes != null && targetMinutes > 0) ? targetMinutes : 1;
        this.progressPercentage = Math.round((actualMinutes * 100.0 / target) * 10.0) / 10.0;
        this.completed = actualMinutes >= target;

        this.progressFormatted = String.format("%s / %s",
                ActivityResponse.formatDuration((long) actualMinutes * 60),
                ActivityResponse.formatDuration((long) target * 60)
        );
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

    public Integer getTargetMinutes() {
        return targetMinutes;
    }

    public void setTargetMinutes(Integer targetMinutes) {
        this.targetMinutes = targetMinutes;
    }

    public int getActualMinutes() {
        return actualMinutes;
    }

    public void setActualMinutes(int actualMinutes) {
        this.actualMinutes = actualMinutes;
    }

    public double getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(double progressPercentage) {
        this.progressPercentage = progressPercentage;
    }

    public String getProgressFormatted() {
        return progressFormatted;
    }

    public void setProgressFormatted(String progressFormatted) {
        this.progressFormatted = progressFormatted;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public GoalFrequency getFrequency() {
        return frequency;
    }

    public void setFrequency(GoalFrequency frequency) {
        this.frequency = frequency;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
