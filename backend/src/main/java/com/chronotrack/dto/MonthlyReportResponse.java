package com.chronotrack.dto;

import java.util.ArrayList;
import java.util.List;

public class MonthlyReportResponse {

    private int year;
    private int month;
    private String monthName;
    private long totalTrackedSeconds;
    private double totalTrackedHours;
    private double averageDailyHours;
    private double previousMonthHours;
    private Double percentageChange;
    private int daysInMonth;
    private int activeDaysCount;
    private String mostActiveDay;
    private int totalActivities;
    private List<DailyActivitySummary> dailyBreakdown = new ArrayList<>();
    private List<CategoryTimeDistribution> categoryDistribution = new ArrayList<>();

    public MonthlyReportResponse() {
    }

    // Getters and Setters
    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public String getMonthName() {
        return monthName;
    }

    public void setMonthName(String monthName) {
        this.monthName = monthName;
    }

    public long getTotalTrackedSeconds() {
        return totalTrackedSeconds;
    }

    public void setTotalTrackedSeconds(long totalTrackedSeconds) {
        this.totalTrackedSeconds = totalTrackedSeconds;
        this.totalTrackedHours = Math.round((totalTrackedSeconds / 3600.0) * 100.0) / 100.0;
        if (daysInMonth > 0) {
            this.averageDailyHours = Math.round((this.totalTrackedHours / daysInMonth) * 100.0) / 100.0;
        }
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

    public double getPreviousMonthHours() {
        return previousMonthHours;
    }

    public void setPreviousMonthHours(double previousMonthHours) {
        this.previousMonthHours = previousMonthHours;
    }

    public Double getPercentageChange() {
        return percentageChange;
    }

    public void setPercentageChange(Double percentageChange) {
        this.percentageChange = percentageChange;
    }

    public int getDaysInMonth() {
        return daysInMonth;
    }

    public void setDaysInMonth(int daysInMonth) {
        this.daysInMonth = daysInMonth;
        if (daysInMonth > 0 && totalTrackedHours > 0) {
            this.averageDailyHours = Math.round((this.totalTrackedHours / daysInMonth) * 100.0) / 100.0;
        }
    }

    public int getActiveDaysCount() {
        return activeDaysCount;
    }

    public void setActiveDaysCount(int activeDaysCount) {
        this.activeDaysCount = activeDaysCount;
    }

    public String getMostActiveDay() {
        return mostActiveDay;
    }

    public void setMostActiveDay(String mostActiveDay) {
        this.mostActiveDay = mostActiveDay;
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
}
