import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { categoryApi, reminderApi } from '../services/api';
import {
  User,
  Settings as SettingsIcon,
  Tag,
  Bell,
  Plus,
  Trash2,
  CheckCircle2,
  Save,
  Clock,
  Sparkles,
  Palette
} from 'lucide-react';

const PALETTE = [
  '#6366f1', '#10b981', '#06b6d4', '#f59e0b',
  '#ec4899', '#8b5cf6', '#ef4444', '#14b8a6',
  '#f97316', '#84cc16', '#64748b', '#3b82f6'
];

export default function Settings() {
  const { user, logout } = useAuth();
  const [categories, setCategories] = useState([]);
  const [reminders, setReminders] = useState([]);
  const [loading, setLoading] = useState(true);

  // New Category State
  const [newCatName, setNewCatName] = useState('');
  const [newCatColor, setNewCatColor] = useState(PALETTE[0]);
  const [newCatProductive, setNewCatProductive] = useState(true);

  // New Reminder State
  const [reminderTitle, setReminderTitle] = useState('');
  const [reminderTime, setReminderTime] = useState('21:00');
  const [reminderDays, setReminderDays] = useState('DAILY');

  // Preferences
  const [timeFormat, setTimeFormat] = useState('24h');
  const [timezone, setTimezone] = useState(Intl.DateTimeFormat().resolvedOptions().timeZone || 'UTC');

  const fetchData = async () => {
    try {
      setLoading(true);
      const [catsRes, remRes] = await Promise.allSettled([
        categoryApi.getAll(),
        reminderApi.getAll()
      ]);
      if (catsRes.status === 'fulfilled') {
        const c = catsRes.value.data?.data || catsRes.value.data || [];
        setCategories(Array.isArray(c) ? c : []);
      }
      if (remRes.status === 'fulfilled') {
        const r = remRes.value.data?.data || remRes.value.data || [];
        setReminders(Array.isArray(r) ? r : []);
      }
    } catch (err) {
      console.error('Failed to load settings data:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleAddCategory = async (e) => {
    e.preventDefault();
    if (!newCatName.trim()) return;

    try {
      await categoryApi.create({
        name: newCatName.trim(),
        color: newCatColor,
        isProductive: newCatProductive
      });
      setNewCatName('');
      fetchData();
    } catch (err) {
      alert('Failed to add category: ' + (err.response?.data?.message || err.message));
    }
  };

  const handleDeleteCategory = async (id) => {
    if (!window.confirm('Delete this category?')) return;
    try {
      await categoryApi.delete(id);
      fetchData();
    } catch (err) {
      alert('Failed to delete category: ' + (err.response?.data?.message || err.message));
    }
  };

  const handleAddReminder = async (e) => {
    e.preventDefault();
    if (!reminderTitle.trim()) return;

    try {
      await reminderApi.create({
        title: reminderTitle.trim(),
        reminderTime: reminderTime + ':00',
        frequency: reminderDays,
        isActive: true
      });
      setReminderTitle('');
      fetchData();
    } catch (err) {
      alert('Failed to add reminder: ' + (err.response?.data?.message || err.message));
    }
  };

  const handleDeleteReminder = async (id) => {
    try {
      await reminderApi.delete(id);
      fetchData();
    } catch (err) {
      alert('Failed to delete reminder: ' + (err.response?.data?.message || err.message));
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--spacing-xl)', maxWidth: '900px' }}>
      {/* Header */}
      <div>
        <h1 style={{ fontSize: '1.75rem', fontWeight: '800' }}>Account & Settings</h1>
        <p style={{ color: 'var(--color-text-secondary)', fontSize: '0.925rem' }}>
          Manage your personal profile, categories, and reminders
        </p>
      </div>

      {/* User Profile Card */}
      <div className="card" style={{ padding: 'var(--spacing-xl)' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--spacing-md)', marginBottom: 'var(--spacing-lg)' }}>
          <div style={{
            width: '56px',
            height: '56px',
            borderRadius: '50%',
            backgroundColor: 'var(--color-primary)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontSize: '1.5rem',
            fontWeight: '700',
            color: 'white'
          }}>
            {user?.fullName?.charAt(0) || user?.username?.charAt(0) || 'U'}
          </div>
          <div>
            <h2 style={{ fontSize: '1.3rem', fontWeight: '700', color: 'var(--color-text)' }}>
              {user?.fullName || user?.username}
            </h2>
            <p style={{ color: 'var(--color-text-secondary)', fontSize: '0.9rem' }}>
              {user?.email} • @{user?.username}
            </p>
          </div>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 'var(--spacing-md)' }}>
          <div>
            <label className="form-label">Time Format</label>
            <select
              className="form-input"
              value={timeFormat}
              onChange={(e) => setTimeFormat(e.target.value)}
            >
              <option value="24h">24-Hour (00:00 - 23:59)</option>
              <option value="12h">12-Hour (12:00 AM - 11:59 PM)</option>
            </select>
          </div>

          <div>
            <label className="form-label">System Timezone</label>
            <input
              type="text"
              className="form-input"
              value={timezone}
              disabled
            />
          </div>
        </div>
      </div>

      {/* Categories Management Card */}
      <div className="card" style={{ padding: 'var(--spacing-xl)' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: 'var(--spacing-md)' }}>
          <Tag size={20} style={{ color: 'var(--color-primary-light)' }} />
          <h2 style={{ fontSize: '1.25rem', fontWeight: '700' }}>Category Management</h2>
        </div>
        <p style={{ color: 'var(--color-text-secondary)', fontSize: '0.875rem', marginBottom: 'var(--spacing-lg)' }}>
          Categories allow you to tag activities as productive work or personal leisure for analytics.
        </p>

        {/* Existing categories list */}
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))',
          gap: 'var(--spacing-sm)',
          marginBottom: 'var(--spacing-xl)'
        }}>
          {categories.map((c) => (
            <div
              key={c.id}
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                padding: '8px 12px',
                borderRadius: 'var(--radius-md)',
                backgroundColor: 'var(--color-surface-hover)',
                border: '1px solid var(--color-border)'
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <span style={{
                  width: '12px',
                  height: '12px',
                  borderRadius: '50%',
                  backgroundColor: c.color || '#6366f1'
                }} />
                <span style={{ fontWeight: '500', fontSize: '0.9rem' }}>{c.name}</span>
                {c.isProductive && (
                  <span style={{ fontSize: '0.7rem', color: 'var(--color-success)', fontWeight: '700' }}>✓</span>
                )}
              </div>

              {!c.isDefault && (
                <button
                  style={{ background: 'transparent', border: 'none', color: 'var(--color-text-muted)', cursor: 'pointer' }}
                  onClick={() => handleDeleteCategory(c.id)}
                  title="Delete Category"
                >
                  <Trash2 size={14} />
                </button>
              )}
            </div>
          ))}
        </div>

        {/* Add custom category form */}
        <form onSubmit={handleAddCategory} style={{
          padding: 'var(--spacing-md)',
          backgroundColor: 'rgba(255, 255, 255, 0.02)',
          border: '1px solid var(--color-border)',
          borderRadius: 'var(--radius-md)'
        }}>
          <h3 style={{ fontSize: '0.95rem', fontWeight: '600', marginBottom: 'var(--spacing-md)' }}>
            Add Custom Category
          </h3>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 'var(--spacing-md)', alignItems: 'center' }}>
            <div style={{ flex: '1 1 200px' }}>
              <input
                type="text"
                className="form-input"
                placeholder="Category Name (e.g. Reading, Music)"
                value={newCatName}
                onChange={(e) => setNewCatName(e.target.value)}
                required
              />
            </div>

            <div style={{ display: 'flex', gap: '6px', alignItems: 'center' }}>
              {PALETTE.slice(0, 6).map((color) => (
                <button
                  key={color}
                  type="button"
                  style={{
                    width: '24px',
                    height: '24px',
                    borderRadius: '50%',
                    backgroundColor: color,
                    border: newCatColor === color ? '2px solid white' : 'none',
                    cursor: 'pointer'
                  }}
                  onClick={() => setNewCatColor(color)}
                />
              ))}
            </div>

            <label style={{ display: 'flex', alignItems: 'center', gap: '6px', cursor: 'pointer', fontSize: '0.875rem' }}>
              <input
                type="checkbox"
                checked={newCatProductive}
                onChange={(e) => setNewCatProductive(e.target.checked)}
              />
              <span>Counts as Productive</span>
            </label>

            <button type="submit" className="btn btn-primary" style={{ padding: '8px 14px' }}>
              <Plus size={16} />
              <span>Add</span>
            </button>
          </div>
        </form>
      </div>

      {/* Reminders Card */}
      <div className="card" style={{ padding: 'var(--spacing-xl)' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: 'var(--spacing-md)' }}>
          <Bell size={20} style={{ color: 'var(--color-warning)' }} />
          <h2 style={{ fontSize: '1.25rem', fontWeight: '700' }}>Reminders & Habit Prompts</h2>
        </div>

        {/* Existing Reminders */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--spacing-sm)', marginBottom: 'var(--spacing-lg)' }}>
          {reminders.length === 0 ? (
            <p style={{ color: 'var(--color-text-muted)', fontSize: '0.875rem' }}>
              No reminders set. Add daily reminders to log activities or review time.
            </p>
          ) : (
            reminders.map((rem) => (
              <div
                key={rem.id}
                style={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  padding: '10px 14px',
                  backgroundColor: 'var(--color-surface-hover)',
                  borderRadius: 'var(--radius-md)',
                  border: '1px solid var(--color-border)'
                }}
              >
                <div>
                  <div style={{ fontWeight: '600', fontSize: '0.925rem' }}>{rem.title}</div>
                  <div style={{ fontSize: '0.8rem', color: 'var(--color-text-muted)' }}>
                    {rem.reminderTime} • {rem.frequency}
                  </div>
                </div>

                <button
                  className="btn btn-danger"
                  style={{ padding: '6px' }}
                  onClick={() => handleDeleteReminder(rem.id)}
                  title="Delete Reminder"
                >
                  <Trash2 size={14} />
                </button>
              </div>
            ))
          )}
        </div>

        {/* Add Reminder Form */}
        <form onSubmit={handleAddReminder} style={{
          display: 'flex',
          flexWrap: 'wrap',
          gap: 'var(--spacing-md)',
          alignItems: 'flex-end',
          padding: 'var(--spacing-md)',
          backgroundColor: 'rgba(255, 255, 255, 0.02)',
          border: '1px solid var(--color-border)',
          borderRadius: 'var(--radius-md)'
        }}>
          <div style={{ flex: '1 1 200px' }}>
            <label className="form-label">Reminder Message</label>
            <input
              type="text"
              className="form-input"
              placeholder="e.g. End of day time audit"
              value={reminderTitle}
              onChange={(e) => setReminderTitle(e.target.value)}
              required
            />
          </div>

          <div style={{ width: '130px' }}>
            <label className="form-label">Time</label>
            <input
              type="time"
              className="form-input"
              value={reminderTime}
              onChange={(e) => setReminderTime(e.target.value)}
              required
            />
          </div>

          <button type="submit" className="btn btn-secondary">
            <Plus size={16} />
            <span>Set Reminder</span>
          </button>
        </form>
      </div>

      {/* Danger Zone / Logout */}
      <div className="card" style={{ padding: 'var(--spacing-xl)', borderColor: 'rgba(239, 68, 68, 0.2)' }}>
        <h3 style={{ fontSize: '1.1rem', fontWeight: '700', color: 'var(--color-danger)', marginBottom: 'var(--spacing-xs)' }}>
          Sign Out
        </h3>
        <p style={{ color: 'var(--color-text-secondary)', fontSize: '0.875rem', marginBottom: 'var(--spacing-md)' }}>
          Ready to wrap up? You can safely sign out of your session on this browser.
        </p>
        <button className="btn btn-danger" onClick={logout}>
          Log Out of ChronoTrack
        </button>
      </div>
    </div>
  );
}
