package com.chronotrack.dto;

import java.util.ArrayList;
import java.util.List;

public class YearlyReportResponse {

    private int year;
    private long totalTrackedSeconds;
    private double totalTrackedHours;
    private double averageDailyHours;
    private int totalActivities;
    private String mostActiveMonth;
    private List<MonthlySummaryItem> monthlyBreakdown = new ArrayList<>();
    private List<CategoryTimeDistribution> categoryDistribution = new ArrayList<>();

    public YearlyReportResponse() {
    }

    public static class MonthlySummaryItem {
        private int month;
        private String monthName;
        private long totalTrackedSeconds;
        private double totalTrackedHours;
        private int activityCount;

        public MonthlySummaryItem() {
        }

        public MonthlySummaryItem(int month, String monthName, long totalTrackedSeconds, int activityCount) {
            this.month = month;
            this.monthName = monthName;
            this.totalTrackedSeconds = totalTrackedSeconds;
            this.totalTrackedHours = Math.round((totalTrackedSeconds / 3600.0) * 100.0) / 100.0;
            this.activityCount = activityCount;
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
    }

    // Getters and Setters
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
        this.averageDailyHours = Math.round((this.totalTrackedHours / 365.0) * 100.0) / 100.0;
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

    public int getTotalActivities() {
        return totalActivities;
    }

    public void setTotalActivities(int totalActivities) {
        this.totalActivities = totalActivities;
    }

    public String getMostActiveMonth() {
        return mostActiveMonth;
    }

    public void setMostActiveMonth(String mostActiveMonth) {
        this.mostActiveMonth = mostActiveMonth;
    }

    public List<MonthlySummaryItem> getMonthlyBreakdown() {
        return monthlyBreakdown;
    }

    public void setMonthlyBreakdown(List<MonthlySummaryItem> monthlyBreakdown) {
        this.monthlyBreakdown = monthlyBreakdown;
    }

    public List<CategoryTimeDistribution> getCategoryDistribution() {
        return categoryDistribution;
    }

    public void setCategoryDistribution(List<CategoryTimeDistribution> categoryDistribution) {
        this.categoryDistribution = categoryDistribution;
    }
}
