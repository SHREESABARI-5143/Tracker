import React, { useState, useEffect } from 'react';
import { timelineApi, activityApi } from '../services/api';
import Timeline from '../components/Timeline';
import ActivityForm from '../components/ActivityForm';
import {
  Calendar as CalendarIcon,
  ChevronLeft,
  ChevronRight,
  Clock,
  Plus,
  Zap,
  AlertCircle,
  CheckCircle2
} from 'lucide-react';

export default function DailyTimeline() {
  const [selectedDate, setSelectedDate] = useState(() => {
    return new Date().toISOString().split('T')[0];
  });
  const [timelineData, setTimelineData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingActivity, setEditingActivity] = useState(null);

  const fetchTimeline = async (date) => {
    try {
      setLoading(true);
      const res = await timelineApi.getDailyTimeline(date);
      setTimelineData(res.data?.data || res.data);
    } catch (err) {
      console.error('Failed to load timeline:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTimeline(selectedDate);
  }, [selectedDate]);

  const handlePrevDay = () => {
    const d = new Date(selectedDate);
    d.setDate(d.getDate() - 1);
    setSelectedDate(d.toISOString().split('T')[0]);
  };

  const handleNextDay = () => {
    const d = new Date(selectedDate);
    d.setDate(d.getDate() + 1);
    setSelectedDate(d.toISOString().split('T')[0]);
  };

  const handleToday = () => {
    setSelectedDate(new Date().toISOString().split('T')[0]);
  };

  const handleLogGap = (gap) => {
    setEditingActivity({
      startTime: gap.startTime,
      endTime: gap.endTime
    });
    setIsModalOpen(true);
  };

  const handleEditActivity = (act) => {
    setEditingActivity(act);
    setIsModalOpen(true);
  };

  const handleDeleteActivity = async (id) => {
    if (!window.confirm('Delete this activity?')) return;
    try {
      await activityApi.delete(id);
      fetchTimeline(selectedDate);
    } catch (err) {
      alert('Failed to delete activity: ' + (err.response?.data?.message || err.message));
    }
  };

  const formatMinutes = (totalMinutes) => {
    if (!totalMinutes || totalMinutes <= 0) return '0h 0m';
    const hrs = Math.floor(totalMinutes / 60);
    const mins = totalMinutes % 60;
    return hrs > 0 ? `${hrs}h ${mins}m` : `${mins}m`;
  };

  const isToday = selectedDate === new Date().toISOString().split('T')[0];
  const dateFormatted = new Date(selectedDate + 'T00:00:00').toLocaleDateString('en-US', {
    weekday: 'long',
    month: 'long',
    day: 'numeric',
    year: 'numeric'
  });

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--spacing-xl)' }}>
      {/* Date Navigation Bar */}
      <div className="card" style={{ padding: 'var(--spacing-md) var(--spacing-lg)' }}>
        <div style={{
          display: 'flex',
          flexWrap: 'wrap',
          alignItems: 'center',
          justifyContent: 'space-between',
          gap: 'var(--spacing-md)'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--spacing-sm)' }}>
            <button className="btn btn-secondary" onClick={handlePrevDay} title="Previous Day">
              <ChevronLeft size={18} />
            </button>
            <button
              className={`btn ${isToday ? 'btn-primary' : 'btn-secondary'}`}
              onClick={handleToday}
            >
              Today
            </button>
            <button className="btn btn-secondary" onClick={handleNextDay} title="Next Day">
              <ChevronRight size={18} />
            </button>
            <div style={{ position: 'relative', display: 'flex', alignItems: 'center' }}>
              <input
                type="date"
                className="form-input"
                style={{ paddingLeft: '36px', height: '38px', width: 'auto' }}
                value={selectedDate}
                onChange={(e) => setSelectedDate(e.target.value)}
              />
              <CalendarIcon size={16} style={{ position: 'absolute', left: '10px', color: 'var(--color-text-muted)' }} />
            </div>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--spacing-md)' }}>
            <span style={{ fontSize: '1.05rem', fontWeight: '700', color: 'var(--color-text)' }}>
              {dateFormatted}
            </span>
            <button
              className="btn btn-primary"
              onClick={() => {
                setEditingActivity(null);
                setIsModalOpen(true);
              }}
            >
              <Plus size={18} />
              <span>Log Activity</span>
            </button>
          </div>
        </div>
      </div>

      {/* Daily Metrics Row */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
        gap: 'var(--spacing-md)'
      }}>
        <div className="card" style={{ padding: 'var(--spacing-md)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: 'var(--color-primary-light)', marginBottom: '4px' }}>
            <Clock size={16} />
            <span style={{ fontSize: '0.825rem', fontWeight: '600' }}>TOTAL TRACKED</span>
          </div>
          <div style={{ fontSize: '1.4rem', fontWeight: '700' }}>
            {formatMinutes(timelineData?.totalTrackedMinutes || 0)}
          </div>
          <span style={{ fontSize: '0.8rem', color: 'var(--color-text-muted)' }}>
            {Math.round(((timelineData?.totalTrackedMinutes || 0) / 1440) * 100)}% of 24h
          </span>
        </div>

        <div className="card" style={{ padding: 'var(--spacing-md)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: 'var(--color-success)', marginBottom: '4px' }}>
            <Zap size={16} />
            <span style={{ fontSize: '0.825rem', fontWeight: '600' }}>PRODUCTIVE TIME</span>
          </div>
          <div style={{ fontSize: '1.4rem', fontWeight: '700', color: 'var(--color-success)' }}>
            {formatMinutes(timelineData?.productiveMinutes || 0)}
          </div>
          <span style={{ fontSize: '0.8rem', color: 'var(--color-text-muted)' }}>
            {Math.round(timelineData?.productivityPercentage || 0)}% of tracked
          </span>
        </div>

        <div className="card" style={{ padding: 'var(--spacing-md)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: 'var(--color-warning)', marginBottom: '4px' }}>
            <AlertCircle size={16} />
            <span style={{ fontSize: '0.825rem', fontWeight: '600' }}>UNTRACKED GAPS</span>
          </div>
          <div style={{ fontSize: '1.4rem', fontWeight: '700', color: 'var(--color-warning)' }}>
            {formatMinutes(timelineData?.untrackedMinutes != null ? timelineData.untrackedMinutes : (1440 - (timelineData?.totalTrackedMinutes || 0)))}
          </div>
          <span style={{ fontSize: '0.8rem', color: 'var(--color-text-muted)' }}>
            {timelineData?.untrackedGapCount || 0} gap(s) detected
          </span>
        </div>
      </div>

      {/* Full 24-Hour Timeline Bar Component */}
      <div className="card" style={{ padding: 'var(--spacing-xl)' }}>
        <h2 style={{ fontSize: '1.15rem', fontWeight: '700', marginBottom: 'var(--spacing-md)' }}>
          Visual 24-Hour Day Progression (00:00 - 23:59)
        </h2>
        <Timeline
          segments={timelineData?.segments || timelineData?.blocks || []}
          blocks={timelineData?.blocks || timelineData?.segments || []}
          onLogGap={handleLogGap}
          compact={false}
        />
      </div>

      {/* Activities Chronological Breakdown */}
      <div className="card" style={{ padding: 'var(--spacing-lg)' }}>
        <h2 style={{ fontSize: '1.15rem', fontWeight: '700', marginBottom: 'var(--spacing-md)' }}>
          Activities for {dateFormatted}
        </h2>

        {(() => {
          const acts = (timelineData?.activities && timelineData.activities.length > 0)
            ? timelineData.activities
            : (timelineData?.segments?.filter(s => s.type === 'ACTIVITY') || []);

          if (acts.length === 0) {
            return (
              <div style={{ textAlign: 'center', padding: 'var(--spacing-2xl) 0', color: 'var(--color-text-muted)' }}>
                <Clock size={40} style={{ margin: '0 auto var(--spacing-sm)', opacity: 0.3 }} />
                <p style={{ fontSize: '1rem', fontWeight: '500' }}>No activities logged on this date.</p>
                <button
                  className="btn btn-primary"
                  style={{ marginTop: 'var(--spacing-sm)' }}
                  onClick={() => {
                    setEditingActivity({
                      startTime: `${selectedDate}T09:00:00`,
                      endTime: `${selectedDate}T10:00:00`
                    });
                    setIsModalOpen(true);
                  }}
                >
                  <Plus size={16} />
                  <span>Log Activity for this Day</span>
                </button>
              </div>
            );
          }

          return (
            <div style={{ overflowX: 'auto' }}>
              <table className="table">
                <thead>
                  <tr>
                    <th>Time Interval</th>
                    <th>Duration</th>
                    <th>Title</th>
                    <th>Category</th>
                    <th>Productive?</th>
                    <th>Notes</th>
                    <th style={{ textAlign: 'right' }}>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {acts.map((act) => {
                    const sTime = act.localStartTime || (act.startTime ? new Date(act.startTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', hour12: false }) : '--:--');
                    const eTime = act.localEndTime || (act.endTime ? new Date(act.endTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', hour12: false }) : '--:--');
                    const durationMins = act.durationMinutes != null
                      ? act.durationMinutes
                      : (act.durationSeconds ? Math.floor(act.durationSeconds / 60) : 0);

                    return (
                      <tr key={act.id || act.activityId}>
                        <td style={{ fontWeight: '600', fontFamily: 'monospace', color: 'var(--color-text)' }}>
                          {sTime} – {eTime}
                        </td>
                        <td style={{ fontWeight: '600' }}>
                          {formatMinutes(durationMins)}
                        </td>
                        <td style={{ fontWeight: '500' }}>{act.title}</td>
                        <td>
                          <span
                            className="badge"
                            style={{
                              backgroundColor: `${act.categoryColor || '#6366f1'}20`,
                              color: act.categoryColor || '#6366f1',
                              border: `1px solid ${act.categoryColor || '#6366f1'}40`
                            }}
                          >
                            {act.categoryName}
                          </span>
                        </td>
                        <td>
                          {act.isProductive || act.productive ? (
                            <span style={{ display: 'inline-flex', alignItems: 'center', gap: '4px', color: 'var(--color-success)', fontSize: '0.85rem' }}>
                              <CheckCircle2 size={14} /> Yes
                            </span>
                          ) : (
                            <span style={{ color: 'var(--color-text-muted)', fontSize: '0.85rem' }}>No</span>
                          )}
                        </td>
                        <td style={{ color: 'var(--color-text-secondary)', fontSize: '0.85rem', maxWidth: '200px', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                          {act.description || act.notes || '-'}
                        </td>
                        <td style={{ textAlign: 'right' }}>
                          <button
                            className="btn btn-secondary"
                            style={{ padding: '4px 8px', fontSize: '0.8rem', marginRight: '6px' }}
                            onClick={() => handleEditActivity(act)}
                          >
                            Edit
                          </button>
                          <button
                            className="btn btn-danger"
                            style={{ padding: '4px 8px', fontSize: '0.8rem' }}
                            onClick={() => handleDeleteActivity(act.id || act.activityId)}
                          >
                            Delete
                          </button>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          );
        })()}
      </div>

      <ActivityForm
        isOpen={isModalOpen}
        onClose={() => {
          setIsModalOpen(false);
          setEditingActivity(null);
        }}
        activity={editingActivity}
        onSuccess={() => {
          setIsModalOpen(false);
          setEditingActivity(null);
          fetchTimeline(selectedDate);
        }}
      />
    </div>
  );
}
