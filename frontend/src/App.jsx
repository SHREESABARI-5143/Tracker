import React, { useState } from 'react';
import { Routes, Route, Navigate, useLocation } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import Navbar from './components/Navbar';
import Sidebar from './components/Sidebar';
import ActivityForm from './components/ActivityForm';
import StartTimerModal from './components/StartTimerModal';

// Pages
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import DailyTimeline from './pages/DailyTimeline';
import Activities from './pages/Activities';
import WeeklyReport from './pages/WeeklyReport';
import MonthlyReport from './pages/MonthlyReport';
import Goals from './pages/Goals';
import Settings from './pages/Settings';

// Protected Route Guard
const ProtectedRoute = ({ children }) => {
  const { token, loading } = useAuth();
  const location = useLocation();

  if (loading) {
    return (
      <div style={{
        height: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        background: 'var(--color-bg)',
        color: 'var(--color-primary-light)',
        fontSize: '1.1rem',
        fontWeight: '600'
      }}>
        Loading ChronoTrack...
      </div>
    );
  }

  if (!token) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  return children;
};

// Main App Layout for Authenticated Users
const AppLayout = ({ children }) => {
  const [isActivityModalOpen, setIsActivityModalOpen] = useState(false);
  const [isTimerModalOpen, setIsTimerModalOpen] = useState(false);
  const { refreshActiveTimer } = useAuth();

  return (
    <div style={{ display: 'flex', minHeight: '100vh', background: 'var(--color-bg)' }}>
      {/* Sidebar */}
      <Sidebar />

      {/* Main Content Area */}
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', minWidth: 0 }}>
        {/* Sticky Navbar */}
        <Navbar
          onAddActivity={() => setIsActivityModalOpen(true)}
          onStartTimer={() => setIsTimerModalOpen(true)}
        />

        {/* Dynamic Page Container */}
        <main style={{
          flex: 1,
          padding: 'var(--spacing-xl)',
          maxWidth: '1440px',
          width: '100%',
          margin: '0 auto'
        }}>
          {children}
        </main>
      </div>

      {/* Global Activity Modal */}
      <ActivityForm
        isOpen={isActivityModalOpen}
        onClose={() => setIsActivityModalOpen(false)}
        onSuccess={() => {
          setIsActivityModalOpen(false);
          // Optional page refresh or event
          window.dispatchEvent(new CustomEvent('chronotrack:activity_added'));
        }}
      />

      {/* Global Start Timer Modal */}
      <StartTimerModal
        isOpen={isTimerModalOpen}
        onClose={() => setIsTimerModalOpen(false)}
        onTimerStarted={() => {
          setIsTimerModalOpen(false);
          refreshActiveTimer();
        }}
      />
    </div>
  );
};

export default function App() {
  const { token } = useAuth();

  return (
    <Routes>
      {/* Public Authentication Routes */}
      <Route
        path="/login"
        element={token ? <Navigate to="/dashboard" replace /> : <Login />}
      />
      <Route
        path="/register"
        element={token ? <Navigate to="/dashboard" replace /> : <Register />}
      />

      {/* Protected Application Routes */}
      <Route
        path="/dashboard"
        element={
          <ProtectedRoute>
            <AppLayout>
              <Dashboard />
            </AppLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/timeline"
        element={
          <ProtectedRoute>
            <AppLayout>
              <DailyTimeline />
            </AppLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/activities"
        element={
          <ProtectedRoute>
            <AppLayout>
              <Activities />
            </AppLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/reports/weekly"
        element={
          <ProtectedRoute>
            <AppLayout>
              <WeeklyReport />
            </AppLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/reports/monthly"
        element={
          <ProtectedRoute>
            <AppLayout>
              <MonthlyReport />
            </AppLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/goals"
        element={
          <ProtectedRoute>
            <AppLayout>
              <Goals />
            </AppLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/settings"
        element={
          <ProtectedRoute>
            <AppLayout>
              <Settings />
            </AppLayout>
          </ProtectedRoute>
        }
      />

      {/* Fallback & Root Redirects */}
      <Route path="/" element={<Navigate to="/dashboard" replace />} />
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
}
