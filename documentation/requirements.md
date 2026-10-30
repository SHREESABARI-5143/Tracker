# ChronoTrack – Requirements & System Specification

## 1. Project Overview
**ChronoTrack** is a production-grade personal time-tracking and productivity analytics application. It allows knowledge workers, engineers, students, and freelancers to continuously track, review, and optimize every minute of their 24-hour day.

---

## 2. Functional Requirements

### 2.1 User Management & Authentication
- **User Registration:** Users can sign up with full name, unique username, valid email, and secure password (minimum 6 characters).
- **Password Security:** Passwords must be hashed using BCrypt before storing in PostgreSQL.
- **Stateless Authentication:** JSON Web Token (JWT) based authentication with HS256/HS512 signatures and Bearer token headers.
- **Default Seeding:** Upon new user registration, the system automatically provisions 12 standard productivity categories:
  - *Work* (Productive)
  - *Education* (Productive)
  - *Learning* (Productive)
  - *Exercise* (Productive)
  - *Health* (Productive)
  - *Sleep* (Routine)
  - *Personal* (Routine)
  - *Entertainment* (Leisure)
  - *Travel* (Routine)
  - *Household* (Routine)
  - *Social* (Leisure)
  - *Other* (Leisure)

### 2.2 Activity Tracking (CRUD & Business Invariants)
- **Time Block Logging:** Activities require title, category, start timestamp, and end timestamp.
- **Interval Validation:**
  - `startTime` must strictly precede `endTime`.
  - Negative or zero durations are rejected.
  - Multi-tenant isolation: Users can never read, modify, or overlap another user's activity.
- **No Overlapping Intervals:** New or updated activities must not overlap with any existing activity for that user.
- **Overnight Activities:** Activities spanning midnight (e.g. 10:00 PM to 06:00 AM) are supported and dynamically split in daily reporting.

### 2.3 Real-Time Persistent Timer
- **State Persistence:** Timer sessions (`RUNNING`, `PAUSED`, `STOPPED`) are saved directly in the PostgreSQL database.
- **Resilience:** If the browser tab is closed, refreshed, or opened on another device, the active timer session continues accurately without data loss.
- **Controls:**
  - `POST /api/timer/start`: Starts a new timer for a category and task title.
  - `POST /api/timer/pause`: Pauses accumulation.
  - `POST /api/timer/resume`: Resumes active counting.
  - `POST /api/timer/stop`: Computes exact start and end times and automatically persists the completed activity block.

### 2.4 24-Hour Visual Timeline
- Continuous visual display representing 00:00 to 23:59 (1440 minutes) for any chosen date.
- Colored activity segments matching category hex codes.
- **Untracked Gap Detection:** Automatically identifies unrecorded gaps between logged activities and provides a 1-click "Log this gap" action.

### 2.5 Productivity Analytics & Reporting
- **Daily Report:** Total tracked minutes, productive minutes, productivity score (%), untracked minutes, category distribution breakdown.
- **Weekly Report:** Strictly adheres to Monday–Sunday ISO week. Includes day-by-day comparison, day-of-week bar chart, category breakdown donut, and week-over-week productivity change percentage.
- **Monthly Report:** Daily productivity area trend chart, active days count, monthly total hours, and month-over-month comparison.
- **Yearly Report:** Annual aggregate metrics and monthly productivity comparisons.

### 2.6 Goals, Insights & Reminders
- **Target Goals:** Set weekly or monthly targets (e.g. 25 hours of Work/Learning) with real-time visual progress bars.
- **Productivity Insights:** Rule-based heuristics analyzing sleep patterns, deep work streaks, untracked time warnings, and goal achievements.
- **Reminders:** Configurable daily and custom reminder prompts for logging time and reviewing habits.

### 2.7 Export Functionality
- Full CSV export of weekly/custom report summaries and chronological activity rows.

---

## 3. Non-Functional Requirements
- **Performance:** Sub-100ms API response times for timeline and report generation via indexed PostgreSQL queries.
- **Security:** CSRF disabled for stateless JWT, CORS restricted to frontend origin, Spring Security 6 authorization filters.
- **Reliability:** AC-ID transactional integrity on activity commits.
- **Responsive UI:** Adaptive interface tailored for both desktop workstations and tablet/mobile screens.
