import React, { createContext, useContext, useState, useEffect } from 'react';
import { authAPI, timerAPI } from '../services/api';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('chronotrack_user');
    return saved ? JSON.parse(saved) : null;
  });

  const [token, setToken] = useState(() => localStorage.getItem('chronotrack_token'));
  const [activeTimer, setActiveTimer] = useState(null);
  const [loading, setLoading] = useState(true);

  // Poll or fetch active timer on login / mount
  const refreshActiveTimer = async () => {
    if (!token) return;
    try {
      const res = await timerAPI.getCurrentTimer();
      setActiveTimer(res.data?.data || null);
    } catch (err) {
      console.error('Failed to load active timer:', err);
    }
  };

  useEffect(() => {
    if (token) {
      refreshActiveTimer();
    }
    setLoading(false);
  }, [token]);

  // Live second ticker for running timer
  useEffect(() => {
    if (!activeTimer || activeTimer.status !== 'RUNNING') return;

    const interval = setInterval(() => {
      setActiveTimer((prev) => {
        if (!prev || prev.status !== 'RUNNING') return prev;
        const newSeconds = (prev.elapsedSeconds || 0) + 1;
        const h = String(Math.floor(newSeconds / 3600)).padStart(2, '0');
        const m = String(Math.floor((newSeconds % 3600) / 60)).padStart(2, '0');
        const s = String(newSeconds % 60).padStart(2, '0');
        return {
          ...prev,
          elapsedSeconds: newSeconds,
          elapsedFormatted: `${h}:${m}:${s}`,
        };
      });
    }, 1000);

    return () => clearInterval(interval);
  }, [activeTimer?.status]);

  const login = async (email, password) => {
    const res = await authAPI.login({ email, password });
    const data = res.data.data;
    localStorage.setItem('chronotrack_token', data.token);
    localStorage.setItem(
      'chronotrack_user',
      JSON.stringify({
        id: data.id,
        fullName: data.fullName,
        email: data.email,
        timezone: data.timezone,
        preferredTimeFormat: data.preferredTimeFormat,
      })
    );
    setToken(data.token);
    setUser(data);
    refreshActiveTimer();
    return data;
  };

  const register = async (formData) => {
    const res = await authAPI.register(formData);
    const data = res.data.data;
    localStorage.setItem('chronotrack_token', data.token);
    localStorage.setItem(
      'chronotrack_user',
      JSON.stringify({
        id: data.id,
        fullName: data.fullName,
        email: data.email,
        timezone: data.timezone,
        preferredTimeFormat: data.preferredTimeFormat,
      })
    );
    setToken(data.token);
    setUser(data);
    refreshActiveTimer();
    return data;
  };

  const logout = () => {
    localStorage.removeItem('chronotrack_token');
    localStorage.removeItem('chronotrack_user');
    setToken(null);
    setUser(null);
    setActiveTimer(null);
  };

  const updateUserProfile = (updatedProfile) => {
    setUser((prev) => ({ ...prev, ...updatedProfile }));
    localStorage.setItem(
      'chronotrack_user',
      JSON.stringify({
        ...user,
        ...updatedProfile,
      })
    );
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        loading,
        activeTimer,
        setActiveTimer,
        refreshActiveTimer,
        login,
        register,
        logout,
        updateUserProfile,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
