package com.chronotrack.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Entity representing user-specific preferences and regional settings.
 * Mapped to the "user_settings" table in PostgreSQL.
 */
@Entity
@Table(name = "user_settings")
public class UserSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, length = 50)
    private String timezone = "UTC";

    @Column(name = "preferred_time_format", nullable = false, length = 10)
    private String preferredTimeFormat = "24h";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public UserSettings() {
    }

    public UserSettings(User user, String timezone, String preferredTimeFormat) {
        this.user = user;
        this.timezone = timezone != null ? timezone : "UTC";
        this.preferredTimeFormat = preferredTimeFormat != null ? preferredTimeFormat : "24h";
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        if (this.timezone == null) {
            this.timezone = "UTC";
        }
        if (this.preferredTimeFormat == null) {
            this.preferredTimeFormat = "24h";
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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
