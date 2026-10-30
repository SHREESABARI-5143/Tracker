import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Attach JWT token to requests if present
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('chronotrack_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor to handle session expiry
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      // Avoid redirecting if already on login or register
      if (!window.location.pathname.includes('/login') && !window.location.pathname.includes('/register')) {
        localStorage.removeItem('chronotrack_token');
        localStorage.removeItem('chronotrack_user');
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export const authAPI = {
  login: (credentials) => api.post('/auth/login', credentials),
  register: (data) => api.post('/auth/register', data),
  getProfile: () => api.get('/auth/profile'),
  updateProfile: (data) => api.put('/auth/profile', data),
};

export const activityAPI = {
  getActivities: (params) => api.get('/activities', { params }),
  getActivity: (id) => api.get(`/activities/${id}`),
  createActivity: (data) => api.post('/activities', data),
  updateActivity: (id, data) => api.put(`/activities/${id}`, data),
  deleteActivity: (id) => api.delete(`/activities/${id}`),
};

export const timerAPI = {
  getCurrentTimer: () => api.get('/timer/current'),
  startTimer: (data) => api.post('/timer/start', data),
  pauseTimer: () => api.post('/timer/pause'),
  resumeTimer: () => api.post('/timer/resume'),
  stopTimer: () => api.post('/timer/stop'),
};

export const categoryAPI = {
  getCategories: () => api.get('/categories'),
  createCategory: (data) => api.post('/categories', data),
  updateCategory: (id, data) => api.put(`/categories/${id}`, data),
  deleteCategory: (id) => api.delete(`/categories/${id}`),
};

export const timelineAPI = {
  getDailyTimeline: (date) => api.get('/timeline/daily', { params: { date } }),
};

export const reportAPI = {
  getDailyReport: (date) => api.get('/reports/daily', { params: { date } }),
  getWeeklyReport: (startDate) => api.get('/reports/weekly', { params: { startDate } }),
  getMonthlyReport: (year, month) => api.get('/reports/monthly', { params: { year, month } }),
  getYearlyReport: (year) => api.get('/reports/yearly', { params: { year } }),
};

export const goalAPI = {
  getGoals: () => api.get('/goals'),
  getGoal: (id) => api.get(`/goals/${id}`),
  createGoal: (data) => api.post('/goals', data),
  updateGoal: (id, data) => api.put(`/goals/${id}`, data),
  deleteGoal: (id) => api.delete(`/goals/${id}`),
};

export const insightAPI = {
  getInsights: () => api.get('/insights'),
};

export const reminderAPI = {
  getReminders: () => api.get('/reminders'),
  createReminder: (data) => api.post('/reminders', data),
  updateReminder: (id, data) => api.put(`/reminders/${id}`, data),
  deleteReminder: (id) => api.delete(`/reminders/${id}`),
};

// Aliases with convenient method naming
export const reportApi = {
  ...reportAPI,
  getDailyReport: (date) => api.get('/reports/daily', { params: { date } }),
  getWeeklyReport: (startDate) => api.get('/reports/weekly', { params: { startDate } }),
  getMonthlyReport: (year, month) => api.get('/reports/monthly', { params: { year, month } }),
  getYearlyReport: (year) => api.get('/reports/yearly', { params: { year } }),
  exportCsv: (params) => api.get('/reports/export/csv', { params, responseType: 'blob' }),
  exportPdf: (params) => api.get('/reports/export/pdf', { params, responseType: 'blob' })
};

export const activityApi = {
  ...activityAPI,
  getAll: (params) => api.get('/activities', { params }),
  getRecent: (params) => api.get('/activities', { params: { page: 0, size: 5, ...params } }),
  create: (data) => api.post('/activities', data),
  update: (id, data) => api.put(`/activities/${id}`, data),
  delete: (id) => api.delete(`/activities/${id}`)
};

export const categoryApi = {
  ...categoryAPI,
  getAll: () => api.get('/categories'),
  create: (data) => api.post('/categories', data),
  update: (id, data) => api.put(`/categories/${id}`, data),
  delete: (id) => api.delete(`/categories/${id}`)
};

export const goalApi = {
  ...goalAPI,
  getAll: () => api.get('/goals'),
  create: (data) => api.post('/goals', data),
  update: (id, data) => api.put(`/goals/${id}`, data),
  delete: (id) => api.delete(`/goals/${id}`)
};

export const insightApi = {
  ...insightAPI,
  getAll: () => api.get('/insights'),
  getDailyInsights: (date) => api.get('/insights', { params: { date } })
};

export const reminderApi = {
  ...reminderAPI,
  getAll: () => api.get('/reminders'),
  create: (data) => api.post('/reminders', data),
  update: (id, data) => api.put(`/reminders/${id}`, data),
  delete: (id) => api.delete(`/reminders/${id}`)
};

export const timelineApi = {
  ...timelineAPI,
  getDailyTimeline: (date) => api.get('/timeline/daily', { params: { date } })
};

export default api;
