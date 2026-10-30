# ChronoTrack – REST API Reference

All protected endpoints require the HTTP header:
`Authorization: Bearer <JWT_TOKEN>`

---

## 1. Authentication Endpoints

### Register User
- **Method:** `POST /api/auth/register`
- **Request Body:**
  ```json
  {
    "username": "alexmorgan",
    "email": "alex@chronotrack.io",
    "password": "Password123!",
    "fullName": "Alex Morgan"
  }
  ```
- **Response (200 OK):**
  ```json
  {
    "success": true,
    "message": "User registered successfully",
    "data": {
      "id": 1,
      "username": "alexmorgan",
      "email": "alex@chronotrack.io",
      "fullName": "Alex Morgan",
      "token": "eyJhbGciOiJIUzI1NiJ9..."
    }
  }
  ```

### User Login
- **Method:** `POST /api/auth/login`
- **Request Body:**
  ```json
  {
    "email": "alex@chronotrack.io",
    "password": "Password123!"
  }
  ```
- **Response (200 OK):** Returns JWT token and profile details.

---

## 2. Activity Endpoints

### List Activities
- **Method:** `GET /api/activities`
- **Query Parameters:** `page`, `size`, `search`, `categoryId`, `startDate`, `endDate`
- **Response (200 OK):** Paginated activity entries.

### Create Activity
- **Method:** `POST /api/activities`
- **Request Body:**
  ```json
  {
    "categoryId": 1,
    "title": "Core Engine Implementation",
    "description": "Implemented Spring Security and database models",
    "startTime": "2026-10-30T09:00:00Z",
    "endTime": "2026-10-30T11:30:00Z",
    "notes": "Finished 20 minutes early"
  }
  ```
- **Response (201 Created):** Returns created activity.

### Update Activity
- **Method:** `PUT /api/activities/{id}`

### Delete Activity
- **Method:** `DELETE /api/activities/{id}`

---

## 3. Persistent Timer Endpoints

### Get Current Active Timer
- **Method:** `GET /api/timer/current`
- **Response (200 OK):** Active running/paused session with calculated `elapsedSeconds` and `elapsedFormatted`.

### Start Timer
- **Method:** `POST /api/timer/start`
- **Request Body:**
  ```json
  {
    "categoryId": 1,
    "title": "Deep Work Sprint",
    "description": "Refactoring data pipeline"
  }
  ```

### Pause Timer
- **Method:** `POST /api/timer/pause`

### Resume Timer
- **Method:** `POST /api/timer/resume`

### Stop Timer
- **Method:** `POST /api/timer/stop`
- **Response (200 OK):** Converts the elapsed time into a persisted `Activity` record.

---

## 4. 24-Hour Timeline Endpoints

### Get Daily 24-Hour Timeline
- **Method:** `GET /api/timeline/daily?date=2026-10-30`
- **Response (200 OK):**
  ```json
  {
    "success": true,
    "data": {
      "date": "2026-10-30",
      "totalTrackedMinutes": 480,
      "productiveMinutes": 420,
      "productivityPercentage": 87.5,
      "untrackedMinutes": 960,
      "untrackedGapCount": 3,
      "blocks": [
        {
          "type": "ACTIVITY",
          "title": "Deep Work Sprint",
          "categoryName": "Work",
          "categoryColor": "#635BFF",
          "startTime": "2026-10-30T09:00:00",
          "endTime": "2026-10-30T11:30:00",
          "durationMinutes": 150,
          "startMinuteOfDay": 540,
          "endMinuteOfDay": 690,
          "leftPercentage": 37.5,
          "widthPercentage": 10.42
        },
        {
          "type": "GAP",
          "title": "Untracked Gap",
          "categoryName": "Untracked",
          "startTime": "2026-10-30T11:30:00",
          "endTime": "2026-10-30T13:00:00",
          "durationMinutes": 90,
          "leftPercentage": 47.92,
          "widthPercentage": 6.25
        }
      ]
    }
  }
  ```

---

## 5. Productivity Reports Endpoints

### Daily Report
- **Method:** `GET /api/reports/daily?date=YYYY-MM-DD`

### Weekly Report (Monday to Sunday)
- **Method:** `GET /api/reports/weekly?startDate=YYYY-MM-DD`
- **Response (200 OK):**
  - Monday–Sunday metrics
  - Productive vs leisure minutes per day
  - Week-over-week comparison percentage
  - Category percentage distribution

### Monthly Report
- **Method:** `GET /api/reports/monthly?year=2026&month=10`
- **Response (200 OK):**
  - Daily trend progression for each day of the month
  - Active days count
  - Month-over-month comparison

### Yearly Report
- **Method:** `GET /api/reports/yearly?year=2026`

### CSV Export
- **Method:** `GET /api/reports/export/csv?type=weekly&startDate=YYYY-MM-DD&endDate=YYYY-MM-DD`
- **Response:** Content-Type `text/csv; charset=UTF-8` with attachment header.

---

## 6. Goals, Insights & Reminders

### List Goals
- **Method:** `GET /api/goals`

### Create Goal
- **Method:** `POST /api/goals`
- **Request Body:**
  ```json
  {
    "categoryId": 1,
    "targetHours": 25.0,
    "period": "WEEKLY",
    "startDate": "2026-10-26",
    "endDate": "2026-11-01"
  }
  ```

### Get Productivity Insights
- **Method:** `GET /api/insights?date=YYYY-MM-DD`

### Reminders
- `GET /api/reminders`
- `POST /api/reminders`
- `DELETE /api/reminders/{id}`
