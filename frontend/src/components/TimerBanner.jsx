import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { timerAPI } from '../services/api';
import { Play, Pause, Square, Clock } from 'lucide-react';

const TimerBanner = ({ onTimerStopped }) => {
  const { activeTimer, setActiveTimer } = useAuth();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  if (!activeTimer) return null;

  const handlePause = async () => {
    try {
      setLoading(true);
      setError('');
      const res = await timerAPI.pauseTimer();
      setActiveTimer(res.data.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to pause timer.');
    } finally {
      setLoading(false);
    }
  };

  const handleResume = async () => {
    try {
      setLoading(true);
      setError('');
      const res = await timerAPI.resumeTimer();
      setActiveTimer(res.data.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to resume timer.');
    } finally {
      setLoading(false);
    }
  };

  const handleStop = async () => {
    if (!window.confirm('Stop timer and log this activity?')) return;
    try {
      setLoading(true);
      setError('');
      const res = await timerAPI.stopTimer();
      setActiveTimer(null);
      if (onTimerStopped) {
        onTimerStopped(res.data.data);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to stop timer.');
    } finally {
      setLoading(false);
    }
  };

  const isRunning = activeTimer.status === 'RUNNING';

  return (
    <div className="timer-banner">
      <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
        <div style={{
          width: '46px',
          height: '46px',
          borderRadius: 'var(--radius-full)',
          background: 'rgba(255, 255, 255, 0.2)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
        }}>
          <Clock size={24} />
        </div>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '4px' }}>
            <span style={{ fontSize: '1.1rem', fontWeight: 700 }}>
              {activeTimer.title}
            </span>
            <span style={{
              fontSize: '0.75rem',
              fontWeight: 600,
              background: activeTimer.categoryColor || 'rgba(255,255,255,0.3)',
              color: '#FFFFFF',
              padding: '2px 8px',
              borderRadius: 'var(--radius-full)',
            }}>
              {activeTimer.categoryName || 'General'}
            </span>
            <span style={{
              fontSize: '0.75rem',
              fontWeight: 600,
              background: isRunning ? 'rgba(16, 185, 129, 0.3)' : 'rgba(245, 158, 11, 0.3)',
              padding: '2px 8px',
              borderRadius: 'var(--radius-full)',
            }}>
              {activeTimer.status}
            </span>
          </div>
          <p style={{ fontSize: '0.825rem', opacity: 0.9 }}>
            {activeTimer.description || 'Active task in progress...'}
          </p>
          {error && <p style={{ fontSize: '0.8rem', color: '#FECACA', marginTop: '4px' }}>{error}</p>}
        </div>
      </div>

      <div style={{ display: 'flex', alignItems: 'center', gap: '20px' }}>
        <div style={{
          fontSize: '2rem',
          fontWeight: 800,
          letterSpacing: '1px',
          fontVariantNumeric: 'tabular-nums',
        }}>
          {activeTimer.elapsedFormatted || '00:00:00'}
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          {isRunning ? (
            <button
              onClick={handlePause}
              disabled={loading}
              className="btn"
              style={{ background: '#FFFFFF', color: 'var(--primary)' }}
              title="Pause Timer"
            >
              <Pause size={17} />
              <span>Pause</span>
            </button>
          ) : (
            <button
              onClick={handleResume}
              disabled={loading}
              className="btn"
              style={{ background: 'var(--accent)', color: '#FFFFFF' }}
              title="Resume Timer"
            >
              <Play size={17} />
              <span>Resume</span>
            </button>
          )}

          <button
            onClick={handleStop}
            disabled={loading}
            className="btn"
            style={{ background: '#EF4444', color: '#FFFFFF' }}
            title="Stop and Save Activity"
          >
            <Square size={17} />
            <span>Stop & Save</span>
          </button>
        </div>
      </div>
    </div>
  );
};

export default TimerBanner;
