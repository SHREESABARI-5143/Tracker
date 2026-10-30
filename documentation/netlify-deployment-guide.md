# ChronoTrack – Netlify Deployment Guide

This guide outlines how to deploy **ChronoTrack** to **Netlify** and connect the React frontend with the Spring Boot & PostgreSQL backend.

---

## 1. Architecture Overview

ChronoTrack consists of two decoupled components:
- **Frontend (React 18 + Vite):** Hosted on **Netlify** (Global CDN, automated SSL/TLS, edge caching).
- **Backend (Java 21 Spring Boot + PostgreSQL 18):** Hosted on a cloud application platform such as **Render**, **Railway**, or **Fly.io** (since Netlify is a frontend/serverless platform and does not run continuous Java JVM processes or PostgreSQL databases).

---

## 2. Configuration Files Included

The following configurations have been pre-configured in the repository:
1. [netlify.toml](file:///d:/Desktop/java%20prjct/netlify.toml) *(Repository root)*:
   ```toml
   [build]
     base = "frontend"
     publish = "dist"
     command = "npm run build"

   [build.environment]
     NODE_VERSION = "20"

   [[redirects]]
     from = "/*"
     to = "/index.html"
     status = 200
   ```
2. [frontend/public/_redirects](file:///d:/Desktop/java%20prjct/frontend/public/_redirects):
   ```text
   /*    /index.html   200
   ```
   *Ensures client-side routing (`react-router-dom`) works on direct URL access or page reload without 404 errors.*
3. [frontend/src/services/api.js](file:///d:/Desktop/java%20prjct/frontend/src/services/api.js):
   ```javascript
   const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';
   ```
4. [SecurityConfig.java](file:///d:/Desktop/java%20prjct/backend/src/main/java/com/chronotrack/config/SecurityConfig.java):
   Updated CORS origins to permit `https://*.netlify.app`.

---

## 3. Deployment Options

### Option A: Netlify Drop (Fastest – No Git Required)
1. Run the production build locally:
   ```bash
   cd frontend
   npm run build
   ```
2. Open your browser and visit: **[https://app.netlify.com/drop](https://app.netlify.com/drop)** (log in or create a free Netlify account).
3. Drag and drop the `frontend/dist` folder into the upload box:
   - Path: `d:\Desktop\java prjct\frontend\dist`
4. Netlify will immediately deploy the frontend and generate a live URL (e.g., `https://chronotrack-analytics.netlify.app`).

---

### Option B: Netlify Git Continuous Deployment (Recommended)
1. Push your repository to GitHub, GitLab, or Bitbucket:
   ```bash
   git add .
   git commit -m "Configure Netlify deployment"
   git push origin main
   ```
2. Log in to [Netlify Dashboard](https://app.netlify.com).
3. Click **"Add new site"** → **"Import an existing project"**.
4. Authorize your Git provider and select the **chronotrack** repository.
5. The build settings are auto-detected from `netlify.toml`:
   - **Base directory:** `frontend`
   - **Build command:** `npm run build`
   - **Publish directory:** `dist`
6. Click **"Environment variables"** and add:
   - Key: `VITE_API_BASE_URL`
   - Value: `https://your-backend-service.onrender.com/api` *(or your deployed backend URL)*
7. Click **"Deploy chronotrack"**.

---

### Option C: Netlify CLI
1. Open PowerShell in `d:\Desktop\java prjct\frontend`:
   ```powershell
   npm run build
   npx netlify deploy --prod --dir=dist
   ```
2. Follow the interactive prompt to link or create a new site on your Netlify account.

---

## 4. Deploying the Backend & PostgreSQL (Free / Low Cost)

To make data persist in the cloud:
1. **Render.com / Railway.app:**
   - Create a free PostgreSQL instance on Render or Railway.
   - Create a Web Service pointing to the `backend/` directory with Java 21 LTS:
     - Build Command: `./mvnw clean package -DskipTests`
     - Start Command: `java -jar target/chronotrack-backend-1.0.0.jar`
   - Configure environment variables:
     - `DB_URL`: `jdbc:postgresql://<host>:5432/<db_name>`
     - `DB_USERNAME`: `<db_user>`
     - `DB_PASSWORD`: `<db_password>`
     - `JWT_SECRET`: *(your 64-character secret)*
2. In Netlify Site Settings → **Environment Variables**, set:
   - `VITE_API_BASE_URL` = `https://<your-render-app>.onrender.com/api`
