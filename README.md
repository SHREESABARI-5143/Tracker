# ChronoTrack – 24-Hour Time Tracking & Productivity Analytics System

![Java 21](https://img.shields.io/badge/Java-21_LTS-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.2-6DB33F?logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18-4169E1?logo=postgresql&logoColor=white)
![React](https://img.shields.io/badge/React-18-61DAFB?logo=react&logoColor=black)
![Vite](https://img.shields.io/badge/Vite-6-646CFF?logo=vite&logoColor=white)
![Status](https://img.shields.io/badge/Build-Passing-10B981)

**ChronoTrack** is an enterprise-grade, full-stack personal time tracking and productivity analytics application. It empowers individuals and teams to track every minute of their 24-hour day (1,440 minutes), eliminate untracked time gaps, analyze historical productivity, set actionable goals, and optimize their daily routines.

---

## 🌟 Key Features

1. **Continuous 24-Hour Visual Timeline:**
   - Visualizes all 24 hours (00:00 to 23:59) for any selected day.
   - Automatically detects untracked time gaps and provides 1-click gap logging.
   - Accurately splits overnight activities (e.g., 10:00 PM to 6:00 AM) across day boundaries without double-counting.

2. **Persistent Database-Backed Timer:**
   - Start, pause, resume, and stop timers with live second-by-second UI ticking.
   - State persists directly in PostgreSQL—timer keeps running accurately even if the browser is closed or refreshed.
   - Automatically converts into a logged activity upon stop.

3. **Strict Business Invariant Engine:**
   - Multi-tenant isolation: Users only ever access their own activities and categories.
   - Strict non-overlapping interval validation: No two activities can overlap.
   - Positive duration constraints with instant friendly error messaging.

4. **Productivity Analytics & Monday–Sunday ISO Reporting:**
   - **Daily Report:** Tracked minutes, productive ratio (%), untracked minutes, category breakdown.
   - **Weekly Report:** Strictly adheres to Monday–Sunday ISO standard. Includes day-by-day Recharts bar graphs, donut charts, and percentage comparison against prior weeks.
   - **Monthly Report:** Daily trend area graphs, active days count, and month-over-month comparisons.
   - **CSV Export:** Download full weekly and custom date range productivity reports.

5. **Goals, Rule-Based Insights & Reminders:**
   - Set weekly or monthly targets (e.g. 25h of Work/Learning) with interactive progress bars.
   - Real-time productivity heuristics (sleep consistency, deep work streaks, gap warnings).
   - Configurable daily reminder prompts.

6. **Automatic Category Provisioning:**
   - 12 default productivity categories (*Work, Education, Learning, Exercise, Health, Sleep, Personal, Entertainment, Travel, Household, Social, Other*) auto-seeded upon registration.

---

## 🏗️ System Architecture

```
ChronoTrack System Architecture
│
├── frontend/                # React 18 Single Page Application
│   ├── src/
│   │   ├── components/      # Reusable UI (Navbar, Sidebar, TimerBanner, Timeline, Charts, Modals)
│   │   ├── context/         # AuthContext (JWT auth, persistent live timer ticker)
│   │   ├── pages/           # Dashboard, DailyTimeline, Activities, WeeklyReport, MonthlyReport, Goals, Settings
│   │   ├── services/        # Axios API client with JWT bearer interceptors
│   │   └── index.css        # Clean SaaS design system
│   ├── package.json
│   └── vite.config.js       # Proxy /api -> http://localhost:8080
│
├── backend/                 # Spring Boot 3.4.2 Java 21 Backend
│   ├── src/main/java/com/chronotrack/
│   │   ├── config/          # SecurityConfig (stateless JWT, CORS), AppConfig
│   │   ├── controller/      # REST API Controllers (Auth, Activity, Timer, Timeline, Report, Goal, Insight, Reminder)
│   │   ├── dto/             # Request & Response Data Transfer Objects
│   │   ├── entity/          # JPA Entities (User, UserSettings, Category, Activity, TimerSession, Goal, Reminder)
│   │   ├── exception/       # GlobalExceptionHandler with unified error format
│   │   ├── repository/      # Spring Data JPA Repositories
│   │   ├── security/        # JwtTokenProvider, JwtAuthenticationFilter, CustomUserDetailsService
│   │   └── service/         # Business Services (Overlap checking, Timeline, ISO Reports, Persistent Timer)
│   ├── src/test/            # 26 Comprehensive Integration Tests (100% Pass)
│   └── pom.xml
│
└── documentation/           # Comprehensive System Documentation
    ├── requirements.md
    ├── database-design.md
    ├── api-documentation.md
    ├── architecture.md
    └── installation-guide.md
```

---

## 🚀 Quick Start Guide

### Prerequisites
- JDK 21 LTS
- PostgreSQL 18 (or 15+) running on port `5432` with database `chronotrack_db`
- Node.js v18+ and npm

### 1. Database Setup
```sql
CREATE DATABASE chronotrack_db;
```

### 2. Run Backend
```powershell
cd backend
.\mvnw.cmd test               # Run all 26 integration tests (Verify build)
.\mvnw.cmd spring-boot:run    # Start Spring Boot server on http://localhost:8080
```

### 3. Run Frontend
```powershell
cd frontend
npm install                  # Install dependencies
npm run dev                  # Start Vite dev server on http://localhost:5173
```

### 4. Open in Browser
Open [http://localhost:5173](http://localhost:5173). Register an account or sign in to start tracking your 24-hour day!

---

## 🧪 Automated Test Suite
The project contains 26 full integration tests covering:
- Authentication & JWT token verification
- Category auto-provisioning
- Activity CRUD & strict non-overlapping validation
- Real-time persistent timer (Start, Pause, Resume, Stop)
- 24-Hour continuous visual timeline & midnight clipping
- Monday-to-Sunday weekly report generation & comparison metrics
- Goal progress tracking and rule-based productivity insights

To execute tests:
```powershell
cd backend
.\mvnw.cmd test
```
Result: **26 of 26 tests passed with 0 failures** (`BUILD SUCCESS`).

## 🌐 Netlify Deployment
For step-by-step instructions on deploying the frontend to Netlify and connecting with your backend:
- Read the [Netlify Deployment Guide](documentation/netlify-deployment-guide.md).

---

## 📄 License
This project is licensed under the MIT License.

