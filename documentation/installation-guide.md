# ChronoTrack – Installation & Setup Guide

## 1. Prerequisites
- **Java Development Kit:** JDK 21 LTS (`jdk-21.0.11` or higher).
- **PostgreSQL:** Version 15+ (tested on PostgreSQL 18) running on port `5432`.
- **Node.js:** v18.0.0+ (tested on Node `v22.17.0`) and npm.
- **Git** (optional).

---

## 2. Database Preparation
1. Ensure PostgreSQL service is running on port `5432`.
2. Connect to PostgreSQL using `psql` or pgAdmin:
   ```bash
   psql -U postgres -h localhost -p 5432
   ```
3. Create the database:
   ```sql
   CREATE DATABASE chronotrack_db;
   ```
4. Default configuration uses username `postgres` with empty password or trust connection. If your PostgreSQL instance has a custom password, set the environment variable:
   ```powershell
   $env:DB_PASSWORD = "your_postgres_password"
   $env:DB_USERNAME = "postgres"
   ```

---

## 3. Backend Setup & Startup
1. Open PowerShell and navigate to the backend folder:
   ```powershell
   cd "d:\Desktop\java prjct\backend"
   ```
2. Verify test suite (all 26 integration tests):
   ```powershell
   .\mvnw.cmd test
   ```
3. Run the Spring Boot application:
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```
4. Verify backend health:
   - Tomcat will start on port `8080`.
   - API base URL: `http://localhost:8080/api`.

---

## 4. Frontend Setup & Startup
1. Open a new PowerShell terminal and navigate to the frontend folder:
   ```powershell
   cd "d:\Desktop\java prjct\frontend"
   ```
2. Install dependencies:
   ```powershell
   npm install
   ```
3. Run the Vite development server:
   ```powershell
   npm run dev
   ```
4. Open your browser and navigate to:
   ```
   http://localhost:5173
   ```

---

## 5. Verification & Demo Accounts
1. Navigate to `http://localhost:5173`.
2. Register a new user:
   - Name: `Alex Morgan`
   - Username: `alexmorgan`
   - Email: `alex@chronotrack.io`
   - Password: `Password123!`
3. Notice that 12 default productivity categories (*Work, Education, Learning, Exercise, Health, Sleep, Personal, etc.*) are immediately ready for use!
4. Click **Start Timer** to test real-time tracking, or **Log Activity** to record past blocks.
