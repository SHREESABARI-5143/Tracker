import React, { useState, useEffect } from 'react';
import { goalApi, categoryApi } from '../services/api';
import {
  Target,
  Plus,
  Clock,
  CheckCircle2,
  AlertCircle,
  Trash2,
  Sparkles,
  Calendar,
  X
} from 'lucide-react';

export default function Goals() {
  const [goals, setGoals] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [formError, setFormError] = useState('');

  const [formData, setFormData] = useState({
    categoryId: '',
    targetHours: 20,
    period: 'WEEKLY',
    startDate: '',
    endDate: ''
  });

  const fetchData = async () => {
    try {
      setLoading(true);
      const [goalsRes, catsRes] = await Promise.all([
        goalApi.getAll(),
        categoryApi.getAll()
      ]);
      const gData = goalsRes.data?.data || goalsRes.data || [];
      const cData = catsRes.data?.data || catsRes.data || [];
      setGoals(Array.isArray(gData) ? gData : []);
      setCategories(Array.isArray(cData) ? cData : []);
    } catch (err) {
      console.error('Failed to load goals data:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleOpenModal = () => {
    // Default start/end date for weekly
    const now = new Date();
    const sDate = now.toISOString().split('T')[0];
    const e = new Date(now);
    e.setDate(e.getDate() + 7);
    const eDate = e.toISOString().split('T')[0];

    setFormData({
      categoryId: '',
      targetHours: 20,
      period: 'WEEKLY',
      startDate: sDate,
      endDate: eDate
    });
    setFormError('');
    setIsModalOpen(true);
  };

  const handlePeriodChange = (newPeriod) => {
    const now = new Date();
    const sDate = now.toISOString().split('T')[0];
    const e = new Date(now);
    if (newPeriod === 'WEEKLY') {
      e.setDate(e.getDate() + 7);
    } else {
      e.setDate(e.getDate() + 30);
    }
    setFormData({
      ...formData,
      period: newPeriod,
      startDate: sDate,
      endDate: e.toISOString().split('T')[0]
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setFormError('');

    if (!formData.targetHours || formData.targetHours <= 0) {
      setFormError('Target hours must be greater than 0.');
      return;
    }

    try {
      await goalApi.create({
        categoryId: formData.categoryId ? Number(formData.categoryId) : null,
        targetHours: Number(formData.targetHours),
        period: formData.period,
        startDate: formData.startDate,
        endDate: formData.endDate
      });
      setIsModalOpen(false);
      fetchData();
    } catch (err) {
      setFormError(err.response?.data?.message || err.message || 'Failed to create goal.');
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this goal?')) return;
    try {
      await goalApi.delete(id);
      fetchData();
    } catch (err) {
      alert('Failed to delete goal: ' + (err.response?.data?.message || err.message));
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--spacing-xl)' }}>
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 'var(--spacing-md)' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: '800' }}>Productivity Goals</h1>
          <p style={{ color: 'var(--color-text-secondary)', fontSize: '0.925rem' }}>
            Set weekly or monthly targets for productive work, study, or habits
          </p>
        </div>
        <button className="btn btn-primary" onClick={handleOpenModal}>
          <Plus size={18} />
          <span>New Goal</span>
        </button>
      </div>

      {/* Goals Grid */}
      {goals.length === 0 ? (
        <div className="card" style={{ textAlign: 'center', padding: 'var(--spacing-2xl)', color: 'var(--color-text-muted)' }}>
          <Target size={48} style={{ margin: '0 auto var(--spacing-md)', opacity: 0.3 }} />
          <h3 style={{ fontSize: '1.2rem', fontWeight: '700', color: 'var(--color-text)', marginBottom: 'var(--spacing-xs)' }}>
            No Goals Defined Yet
          </h3>
          <p style={{ maxWidth: '420px', margin: '0 auto var(--spacing-lg)', fontSize: '0.925rem' }}>
            Setting weekly or monthly productivity targets helps you maintain deep focus and stay accountable.
          </p>
          <button className="btn btn-primary" onClick={handleOpenModal}>
            <Plus size={18} />
            <span>Create Your First Goal</span>
          </button>
        </div>
      ) : (
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))',
          gap: 'var(--spacing-lg)'
        }}>
          {goals.map((goal) => {
            const pct = Math.min(100, Math.round(goal.progressPercentage || 0));
            const isCompleted = pct >= 100;

            return (
              <div key={goal.id} className="card" style={{ padding: 'var(--spacing-lg)', display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
                <div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 'var(--spacing-md)' }}>
                    <div>
                      <span className="badge" style={{
                        backgroundColor: goal.period === 'WEEKLY' ? 'rgba(99, 102, 241, 0.15)' : 'rgba(236, 72, 153, 0.15)',
                        color: goal.period === 'WEEKLY' ? 'var(--color-primary-light)' : 'var(--color-accent)',
                        marginBottom: '6px'
                      }}>
                        {goal.period} GOAL
                      </span>
                      <h3 style={{ fontSize: '1.2rem', fontWeight: '700', color: 'var(--color-text)' }}>
                        {goal.categoryName || 'Total Productive Time'}
                      </h3>
                    </div>

                    <button
                      className="btn btn-danger"
                      style={{ padding: '6px', borderRadius: 'var(--radius-md)' }}
                      onClick={() => handleDelete(goal.id)}
                      title="Delete Goal"
                    >
                      <Trash2 size={16} />
                    </button>
                  </div>

                  {/* Hours & Percentage */}
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline', marginBottom: 'var(--spacing-sm)' }}>
                    <div style={{ fontSize: '1.5rem', fontWeight: '800', color: 'var(--color-text)' }}>
                      {goal.achievedHours?.toFixed(1) || 0}{' '}
                      <span style={{ fontSize: '0.95rem', fontWeight: '400', color: 'var(--color-text-muted)' }}>
                        / {goal.targetHours} hrs
                      </span>
                    </div>
                    <span style={{
                      fontWeight: '700',
                      fontSize: '1.1rem',
                      color: isCompleted ? 'var(--color-success)' : pct >= 50 ? 'var(--color-primary-light)' : 'var(--color-warning)'
                    }}>
                      {pct}%
                    </span>
                  </div>

                  {/* Progress Bar */}
                  <div style={{
                    width: '100%',
                    height: '10px',
                    background: 'var(--color-surface)',
                    borderRadius: '5px',
                    overflow: 'hidden',
                    marginBottom: 'var(--spacing-md)'
                  }}>
                    <div style={{
                      width: `${pct}%`,
                      height: '100%',
                      background: isCompleted
                        ? 'var(--color-success)'
                        : pct >= 50
                          ? 'linear-gradient(90deg, var(--color-primary), var(--color-primary-light))'
                          : 'var(--color-warning)',
                      transition: 'width 0.4s ease'
                    }} />
                  </div>
                </div>

                <div style={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  paddingTop: 'var(--spacing-sm)',
                  borderTop: '1px solid var(--color-border)',
                  fontSize: '0.8rem',
                  color: 'var(--color-text-muted)'
                }}>
                  <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                    <Calendar size={14} />
                    {goal.startDate} to {goal.endDate}
                  </span>
                  {isCompleted && (
                    <span style={{ color: 'var(--color-success)', fontWeight: '600', display: 'flex', alignItems: 'center', gap: '4px' }}>
                      <CheckCircle2 size={14} /> Completed!
                    </span>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Goal Creation Modal */}
      {isModalOpen && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(0, 0, 0, 0.7)',
          backdropFilter: 'blur(4px)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 1000,
          padding: 'var(--spacing-md)'
        }}>
          <div style={{
            background: 'var(--color-surface)',
            border: '1px solid var(--color-border)',
            borderRadius: 'var(--radius-xl)',
            width: '100%',
            maxWidth: '460px',
            padding: 'var(--spacing-xl)',
            boxShadow: 'var(--shadow-xl)',
            position: 'relative'
          }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--spacing-lg)' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <Target size={22} style={{ color: 'var(--color-primary-light)' }} />
                <h2 style={{ fontSize: '1.25rem', fontWeight: '700' }}>Create New Goal</h2>
              </div>
              <button
                className="btn btn-secondary"
                style={{ padding: '6px' }}
                onClick={() => setIsModalOpen(false)}
              >
                <X size={18} />
              </button>
            </div>

            {formError && (
              <div style={{
                padding: 'var(--spacing-sm) var(--spacing-md)',
                backgroundColor: 'rgba(239, 68, 68, 0.15)',
                color: 'var(--color-danger)',
                borderRadius: 'var(--radius-md)',
                marginBottom: 'var(--spacing-md)',
                fontSize: '0.85rem'
              }}>
                {formError}
              </div>
            )}

            <form onSubmit={handleSubmit}>
              {/* Category */}
              <div className="form-group">
                <label className="form-label">Category</label>
                <select
                  className="form-input"
                  value={formData.categoryId}
                  onChange={(e) => setFormData({ ...formData, categoryId: e.target.value })}
                >
                  <option value="">All Productive Time (Overall)</option>
                  {categories.map((c) => (
                    <option key={c.id} value={c.id}>
                      {c.name} {c.isProductive ? '(Productive)' : ''}
                    </option>
                  ))}
                </select>
                <span style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)', marginTop: '4px', display: 'block' }}>
                  Leave on "All Productive Time" to track total work & learning combined.
                </span>
              </div>

              {/* Period */}
              <div className="form-group">
                <label className="form-label">Period</label>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 'var(--spacing-sm)' }}>
                  <button
                    type="button"
                    className={`btn ${formData.period === 'WEEKLY' ? 'btn-primary' : 'btn-secondary'}`}
                    onClick={() => handlePeriodChange('WEEKLY')}
                  >
                    Weekly
                  </button>
                  <button
                    type="button"
                    className={`btn ${formData.period === 'MONTHLY' ? 'btn-primary' : 'btn-secondary'}`}
                    onClick={() => handlePeriodChange('MONTHLY')}
                  >
                    Monthly
                  </button>
                </div>
              </div>

              {/* Target Hours */}
              <div className="form-group">
                <label className="form-label">Target Hours</label>
                <input
                  type="number"
                  step="0.5"
                  min="0.5"
                  className="form-input"
                  value={formData.targetHours}
                  onChange={(e) => setFormData({ ...formData, targetHours: e.target.value })}
                  required
                />
              </div>

              {/* Date range */}
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 'var(--spacing-sm)', marginBottom: 'var(--spacing-lg)' }}>
                <div className="form-group">
                  <label className="form-label">Start Date</label>
                  <input
                    type="date"
                    className="form-input"
                    value={formData.startDate}
                    onChange={(e) => setFormData({ ...formData, startDate: e.target.value })}
                    required
                  />
                </div>
                <div className="form-group">
                  <label className="form-label">End Date</label>
                  <input
                    type="date"
                    className="form-input"
                    value={formData.endDate}
                    onChange={(e) => setFormData({ ...formData, endDate: e.target.value })}
                    required
                  />
                </div>
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 'var(--spacing-sm)' }}>
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={() => setIsModalOpen(false)}
                >
                  Cancel
                </button>
                <button type="submit" className="btn btn-primary">
                  Save Goal
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
