package com.chronotrack.dto;

public class CategoryTimeDistribution {

    private Long categoryId;
    private String categoryName;
    private String categoryColor;
    private long totalSeconds;
    private double hours;
    private String durationFormatted;
    private double percentage;

    public CategoryTimeDistribution() {
    }

    public CategoryTimeDistribution(Long categoryId, String categoryName, String categoryColor,
                                    long totalSeconds, double hours, String durationFormatted, double percentage) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.categoryColor = categoryColor;
        this.totalSeconds = totalSeconds;
        this.hours = hours;
        this.durationFormatted = durationFormatted;
        this.percentage = percentage;
    }

    // Getters and Setters
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

    public long getTotalSeconds() {
        return totalSeconds;
    }

    public void setTotalSeconds(long totalSeconds) {
        this.totalSeconds = totalSeconds;
    }

    public double getHours() {
        return hours;
    }

    public void setHours(double hours) {
        this.hours = hours;
    }

    public String getDurationFormatted() {
        return durationFormatted;
    }

    public void setDurationFormatted(String durationFormatted) {
        this.durationFormatted = durationFormatted;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public long getDurationMinutes() {
        return totalSeconds / 60;
    }

    public long getDurationSeconds() {
        return totalSeconds;
    }
}
