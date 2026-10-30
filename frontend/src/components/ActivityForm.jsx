import React, { useState, useEffect } from 'react';
import { categoryAPI, activityAPI } from '../services/api';
import { X, Calendar, Clock, Tag, FileText } from 'lucide-react';

const ActivityForm = ({
  isOpen,
  activity,
  initialData,
  initialStartTime,
  initialEndTime,
  onClose,
  onSaved,
  onSuccess
}) => {
  if (isOpen === false) return null;

  const currentData = activity || initialData;
  const [categories, setCategories] = useState([]);
  const [formData, setFormData] = useState({
    categoryId: currentData?.categoryId || '',
    title: currentData?.title || '',
    description: currentData?.description || '',
    startTime: '',
    endTime: '',
    notes: currentData?.notes || '',
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState({});

  useEffect(() => {
    loadCategories();
    initializeTimes();
  }, [currentData, isOpen]);

  const loadCategories = async () => {
    try {
      const res = await categoryAPI.getCategories();
      const list = res.data?.data || res.data || [];
      const catArray = Array.isArray(list) ? list : [];
      setCategories(catArray);
      if (!formData.categoryId && catArray.length > 0) {
        setFormData((prev) => ({ ...prev, categoryId: catArray[0].id }));
      }
    } catch (err) {
      console.error('Failed to load categories:', err);
    }
  };

  const formatToLocalInput = (isoString) => {
    if (!isoString) return '';
    const date = new Date(isoString);
    const offset = date.getTimezoneOffset() * 60000;
    return new Date(date.getTime() - offset).toISOString().slice(0, 16);
  };

  const initializeTimes = () => {
    if (currentData) {
      setFormData((prev) => ({
        ...prev,
        categoryId: currentData.categoryId,
        title: currentData.title || '',
        description: currentData.description || '',
        notes: currentData.notes || '',
        startTime: formatToLocalInput(currentData.startTime),
        endTime: formatToLocalInput(currentData.endTime),
      }));
    } else {
      const now = new Date();
      const oneHourAgo = new Date(now.getTime() - 60 * 60 * 1000);
      setFormData((prev) => ({
        ...prev,
        startTime: initialStartTime ? formatToLocalInput(initialStartTime) : formatToLocalInput(oneHourAgo.toISOString()),
        endTime: initialEndTime ? formatToLocalInput(initialEndTime) : formatToLocalInput(now.toISOString()),
      }));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setFieldErrors({});

    const start = new Date(formData.startTime);
    const end = new Date(formData.endTime);

    if (end <= start) {
      setError('End time must be later than start time.');
      return;
    }

    try {
      setLoading(true);
      const payload = {
        categoryId: Number(formData.categoryId),
        title: formData.title.trim(),
        description: formData.description.trim() || null,
        startTime: start.toISOString(),
        endTime: end.toISOString(),
        notes: formData.notes.trim() || null,
      };

      let res;
      if (currentData?.id) {
        res = await activityAPI.updateActivity(currentData.id, payload);
      } else {
        res = await activityAPI.createActivity(payload);
      }

      const savedData = res.data?.data || res.data;
      if (onSuccess) {
        onSuccess(savedData);
      }
      if (onSaved) {
        onSaved(savedData);
      }
      onClose();
    } catch (err) {
      const resp = err.response?.data;
      if (resp?.errors) {
        setFieldErrors(resp.errors);
      } else {
        setError(resp?.message || 'Failed to save activity. Please check input.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        {/* Header */}
        <div style={{
          padding: '20px 24px',
          borderBottom: '1px solid var(--border-color)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
        }}>
          <h2 style={{ fontSize: '1.15rem', fontWeight: 700, color: 'var(--text-main)' }}>
            {initialData?.id ? 'Edit Activity' : 'Add New Activity'}
          </h2>
          <button
            onClick={onClose}
            style={{ border: 'none', background: 'transparent', cursor: 'pointer', color: 'var(--text-muted)' }}
          >
            <X size={20} />
          </button>
        </div>

        {/* Form Body */}
        <form onSubmit={handleSubmit} style={{ padding: '24px' }}>
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

          {/* Title */}
          <div className="form-group">
            <label className="form-label">Activity Title *</label>
            <input
              type="text"
              className="form-input"
              placeholder="e.g. Code refactoring, Gym workout"
              value={formData.title}
              onChange={(e) => setFormData({ ...formData, title: e.target.value })}
              required
            />
            {fieldErrors.title && <p className="form-error">{fieldErrors.title}</p>}
          </div>

          {/* Category */}
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

          {/* Time Range */}
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
            <div className="form-group">
              <label className="form-label">Start Time *</label>
              <input
                type="datetime-local"
                className="form-input"
                value={formData.startTime}
                onChange={(e) => setFormData({ ...formData, startTime: e.target.value })}
                required
              />
              {fieldErrors.startTime && <p className="form-error">{fieldErrors.startTime}</p>}
            </div>

            <div className="form-group">
              <label className="form-label">End Time *</label>
              <input
                type="datetime-local"
                className="form-input"
                value={formData.endTime}
                onChange={(e) => setFormData({ ...formData, endTime: e.target.value })}
                required
              />
              {fieldErrors.endTime && <p className="form-error">{fieldErrors.endTime}</p>}
            </div>
          </div>

          {/* Description */}
          <div className="form-group">
            <label className="form-label">Description (Optional)</label>
            <input
              type="text"
              className="form-input"
              placeholder="Brief details..."
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
            />
          </div>

          {/* Notes */}
          <div className="form-group">
            <label className="form-label">Notes (Optional)</label>
            <textarea
              className="form-textarea"
              rows={3}
              placeholder="Reflections, goals, or notes..."
              value={formData.notes}
              onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
            />
          </div>

          {/* Footer Actions */}
          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px', marginTop: '24px' }}>
            <button type="button" className="btn btn-secondary" onClick={onClose} disabled={loading}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Saving...' : initialData?.id ? 'Update Activity' : 'Save Activity'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default ActivityForm;
