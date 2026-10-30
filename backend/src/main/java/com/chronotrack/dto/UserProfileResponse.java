package com.chronotrack.dto;

import java.time.Instant;

public class UserProfileResponse {

    private Long id;
    private String fullName;
    private String email;
    private String timezone;
    private String preferredTimeFormat;
    private Instant createdAt;

    public UserProfileResponse() {
    }

    public UserProfileResponse(Long id, String fullName, String email, String timezone, String preferredTimeFormat, Instant createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.timezone = timezone;
        this.preferredTimeFormat = preferredTimeFormat;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getPreferredTimeFormat() {
        return preferredTimeFormat;
    }

    public void setPreferredTimeFormat(String preferredTimeFormat) {
        this.preferredTimeFormat = preferredTimeFormat;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
