package com.chronotrack.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ReminderRequest {

    @NotBlank(message = "Reminder title is required")
    @Size(min = 2, max = 150, message = "Reminder title must be between 2 and 150 characters")
    private String title;

    @NotBlank(message = "Reminder time is required")
    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Reminder time must be in HH:mm format (e.g. 09:00, 21:30)")
    private String reminderTime;

    private Boolean enabled = true;

    public ReminderRequest() {
    }

    public ReminderRequest(String title, String reminderTime, Boolean enabled) {
        this.title = title;
        this.reminderTime = reminderTime;
        this.enabled = enabled != null ? enabled : true;
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(String reminderTime) {
        this.reminderTime = reminderTime;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
