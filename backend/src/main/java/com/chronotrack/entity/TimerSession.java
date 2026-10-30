package com.chronotrack.entity;

import jakarta.persistence.*;
import java.time.Duration;
import java.time.Instant;

/**
 * Entity representing an active persistent timer session for a user.
 * Ensures timer state survives application restarts and page refreshes.
 */
@Entity
@Table(name = "timer_sessions")
public class TimerSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 500)
    private String description;

    @Column(length = 1000)
    private String notes;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "paused_at")
    private Instant pausedAt;

    @Column(name = "accumulated_seconds", nullable = false)
    private Long accumulatedSeconds = 0L;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TimerStatus status = TimerStatus.RUNNING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public TimerSession() {
    }

    public TimerSession(User user, Category category, String title, String description, String notes) {
        this.user = user;
        this.category = category;
        this.title = title;
        this.description = description;
        this.notes = notes;
        this.startTime = Instant.now();
        this.accumulatedSeconds = 0L;
        this.status = TimerStatus.RUNNING;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        this.updatedAt = now;
        if (this.startTime == null) {
            this.startTime = now;
        }
        if (this.accumulatedSeconds == null) {
            this.accumulatedSeconds = 0L;
        }
        if (this.status == null) {
            this.status = TimerStatus.RUNNING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    /**
     * Calculates the real-time total elapsed seconds based on current instant.
     */
    public long calculateCurrentElapsedSeconds() {
        if (this.status == TimerStatus.PAUSED) {
            return this.accumulatedSeconds != null ? this.accumulatedSeconds : 0L;
        }
        long additional = 0L;
        if (this.startTime != null) {
            additional = Duration.between(this.startTime, Instant.now()).getSeconds();
            if (additional < 0) {
                additional = 0;
            }
        }
        return (this.accumulatedSeconds != null ? this.accumulatedSeconds : 0L) + additional;
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

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public Instant getPausedAt() {
        return pausedAt;
    }

    public void setPausedAt(Instant pausedAt) {
        this.pausedAt = pausedAt;
    }

    public Long getAccumulatedSeconds() {
        return accumulatedSeconds;
    }

    public void setAccumulatedSeconds(Long accumulatedSeconds) {
        this.accumulatedSeconds = accumulatedSeconds;
    }

    public TimerStatus getStatus() {
        return status;
    }

    public void setStatus(TimerStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
