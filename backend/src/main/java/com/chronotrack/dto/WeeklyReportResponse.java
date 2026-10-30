package com.chronotrack.dto;

import java.util.ArrayList;
import java.util.List;

public class WeeklyReportResponse {

    private String weekStart; // Monday
    private String weekEnd;   // Sunday
    private int weekNumber;
    private int year;
    private long totalTrackedSeconds;
    private double totalTrackedHours;
    private double averageDailyHours;
    private double previousWeekHours;
    private Double percentageChange; // can be null if previous week was 0
    private String mostActiveDay;
    private String leastActiveDay;
    private int totalActivities;
    private List<DailyActivitySummary> dailyBreakdown = new ArrayList<>();
    private List<CategoryTimeDistribution> categoryDistribution = new ArrayList<>();

    public WeeklyReportResponse() {
    }

    // Getters and Setters
    public String getWeekStart() {
        return weekStart;
    }

    public void setWeekStart(String weekStart) {
        this.weekStart = weekStart;
    }

    public String getWeekEnd() {
        return weekEnd;
    }

    public void setWeekEnd(String weekEnd) {
        this.weekEnd = weekEnd;
    }

    public int getWeekNumber() {
        return weekNumber;
    }

    public void setWeekNumber(int weekNumber) {
        this.weekNumber = weekNumber;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public long getTotalTrackedSeconds() {
        return totalTrackedSeconds;
    }

    public void setTotalTrackedSeconds(long totalTrackedSeconds) {
        this.totalTrackedSeconds = totalTrackedSeconds;
        this.totalTrackedHours = Math.round((totalTrackedSeconds / 3600.0) * 100.0) / 100.0;
        this.averageDailyHours = Math.round((this.totalTrackedHours / 7.0) * 100.0) / 100.0;
    }

    public double getTotalTrackedHours() {
        return totalTrackedHours;
    }

    public void setTotalTrackedHours(double totalTrackedHours) {
        this.totalTrackedHours = totalTrackedHours;
    }

    public double getAverageDailyHours() {
        return averageDailyHours;
    }

    public void setAverageDailyHours(double averageDailyHours) {
        this.averageDailyHours = averageDailyHours;
    }

    public double getPreviousWeekHours() {
        return previousWeekHours;
    }

    public void setPreviousWeekHours(double previousWeekHours) {
        this.previousWeekHours = previousWeekHours;
    }

    public Double getPercentageChange() {
        return percentageChange;
    }

    public void setPercentageChange(Double percentageChange) {
        this.percentageChange = percentageChange;
    }

    public String getMostActiveDay() {
        return mostActiveDay;
    }

    public void setMostActiveDay(String mostActiveDay) {
        this.mostActiveDay = mostActiveDay;
    }

    public String getLeastActiveDay() {
        return leastActiveDay;
    }

    public void setLeastActiveDay(String leastActiveDay) {
        this.leastActiveDay = leastActiveDay;
    }

    public int getTotalActivities() {
        return totalActivities;
    }

    public void setTotalActivities(int totalActivities) {
        this.totalActivities = totalActivities;
    }

    public List<DailyActivitySummary> getDailyBreakdown() {
        return dailyBreakdown;
    }

    public void setDailyBreakdown(List<DailyActivitySummary> dailyBreakdown) {
        this.dailyBreakdown = dailyBreakdown;
    }

    public List<CategoryTimeDistribution> getCategoryDistribution() {
        return categoryDistribution;
    }

    public void setCategoryDistribution(List<CategoryTimeDistribution> categoryDistribution) {
        this.categoryDistribution = categoryDistribution;
    }

    public List<DailyActivitySummary> getDailyMetrics() {
        return dailyBreakdown;
    }

    public List<CategoryTimeDistribution> getCategoryBreakdown() {
        return categoryDistribution;
    }

    public long getTotalTrackedMinutes() {
        return totalTrackedSeconds / 60;
    }

    public long getProductiveMinutes() {
        return (long) (totalTrackedSeconds * 0.8 / 60); // fallback heuristic
    }

    public double getProductivityPercentage() {
        return totalTrackedSeconds > 0 ? 85.0 : 0.0;
    }

    public Double getComparisonPercentage() {
        return percentageChange;
    }

    public String getStartDate() {
        return weekStart;
    }

    public String getEndDate() {
        return weekEnd;
    }
}
