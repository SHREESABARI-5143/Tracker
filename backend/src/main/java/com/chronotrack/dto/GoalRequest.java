package com.chronotrack.dto;

import com.chronotrack.entity.GoalFrequency;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class GoalRequest {

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotBlank(message = "Goal title is required")
    @Size(min = 2, max = 150, message = "Goal title must be between 2 and 150 characters")
    private String title;

    @NotNull(message = "Target minutes is required")
    @Min(value = 1, message = "Target minutes must be at least 1 minute")
    private Integer targetMinutes;

    @NotNull(message = "Goal frequency is required (DAILY, WEEKLY, or MONTHLY)")
    private GoalFrequency frequency;

    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean active = true;

    public GoalRequest() {
    }

    public GoalRequest(Long categoryId, String title, Integer targetMinutes,
                       GoalFrequency frequency, LocalDate startDate, LocalDate endDate) {
        this.categoryId = categoryId;
        this.title = title;
        this.targetMinutes = targetMinutes;
        this.frequency = frequency;
        this.startDate = startDate;
        this.endDate = endDate;
        this.active = true;
    }

    // Getters and Setters
    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
