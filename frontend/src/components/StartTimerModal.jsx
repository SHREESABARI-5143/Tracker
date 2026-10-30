import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { categoryAPI, timerAPI } from '../services/api';
import { X, Play } from 'lucide-react';

const StartTimerModal = ({ isOpen, onClose, onTimerStarted }) => {
  if (isOpen === false) return null;

  const { setActiveTimer } = useAuth();
  const [categories, setCategories] = useState([]);
  const [formData, setFormData] = useState({
    categoryId: '',
    title: '',
    description: '',
    notes: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    loadCategories();
  }, []);

  const loadCategories = async () => {
    try {
      const res = await categoryAPI.getCategories();
      const list = res.data.data || [];
      setCategories(list);
      if (list.length > 0) {
        setFormData((prev) => ({ ...prev, categoryId: list[0].id }));
      }
    } catch (err) {
      console.error('Failed to load categories:', err);
    }
  };

  const handleStart = async (e) => {
    e.preventDefault();
    if (!formData.title.trim()) {
      setError('Please enter an activity title.');
      return;
    }

    try {
      setLoading(true);
      setError('');
      const payload = {
        categoryId: Number(formData.categoryId),
        title: formData.title.trim(),
        description: formData.description.trim() || null,
        notes: formData.notes.trim() || null,
      };
      const res = await timerAPI.startTimer(payload);
      setActiveTimer(res.data.data);
      if (onTimerStarted) onTimerStarted(res.data.data);
      onClose();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to start timer.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div style={{
          padding: '20px 24px',
          borderBottom: '1px solid var(--border-color)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
        }}>
          <h2 style={{ fontSize: '1.15rem', fontWeight: 700, color: 'var(--text-main)', display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Play size={18} color="var(--primary)" />
            <span>Start Activity Timer</span>
          </h2>
          <button onClick={onClose} style={{ border: 'none', background: 'transparent', cursor: 'pointer', color: 'var(--text-muted)' }}>
            <X size={20} />
          </button>
        </div>

        <form onSubmit={handleStart} style={{ padding: '24px' }}>
          {error && (
            <div style={{
              background: 'var(--danger-light)',
              color: 'var(--danger)',
              padding: '10px 14px',
              borderRadius: 'var(--radius-md)',
              fontSize: '0.875rem',
              marginBottom: '16px',
            }}>
              {error}
            </div>
          )}

          <div className="form-group">
            <label className="form-label">What are you working on? *</label>
            <input
              type="text"
              className="form-input"
              placeholder="e.g. Learning Spring Boot, Designing Dashboard"
              value={formData.title}
              onChange={(e) => setFormData({ ...formData, title: e.target.value })}
              autoFocus
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label">Category *</label>
            <select
              className="form-select"
              value={formData.categoryId}
              onChange={(e) => setFormData({ ...formData, categoryId: e.target.value })}
              required
            >
              {categories.map((cat) => (
                <option key={cat.id} value={cat.id}>
                  {cat.name}
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label className="form-label">Description (Optional)</label>
            <input
              type="text"
              className="form-input"
              placeholder="Specific task details..."
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
            />
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px', marginTop: '24px' }}>
            <button type="button" className="btn btn-secondary" onClick={onClose} disabled={loading}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              <Play size={16} />
              <span>{loading ? 'Starting...' : 'Start Timer Now'}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default StartTimerModal;
