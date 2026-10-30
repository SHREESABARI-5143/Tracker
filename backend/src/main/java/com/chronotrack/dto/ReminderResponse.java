package com.chronotrack.dto;

import java.time.Instant;

public class ReminderResponse {

    private Long id;
    private String title;
    private String reminderTime;
    private boolean enabled;
    private Instant createdAt;

    public ReminderResponse() {
    }

    public ReminderResponse(Long id, String title, String reminderTime, boolean enabled, Instant createdAt) {
        this.id = id;
        this.title = title;
        this.reminderTime = reminderTime;
        this.enabled = enabled;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
