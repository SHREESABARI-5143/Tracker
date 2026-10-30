import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { Clock, Plus, Play, User as UserIcon } from 'lucide-react';

const Navbar = ({ onAddActivity, onStartTimer }) => {
  const { user, activeTimer } = useAuth();
  const [currentTime, setCurrentTime] = useState(new Date());

  useEffect(() => {
    const timer = setInterval(() => setCurrentTime(new Date()), 1000);
    return () => clearInterval(timer);
  }, []);

  const formattedDate = currentTime.toLocaleDateString(undefined, {
    weekday: 'short',
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  });

  const formattedTime = currentTime.toLocaleTimeString(undefined, {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  });

  return (
    <header style={{
      height: '70px',
      background: '#FFFFFF',
      borderBottom: '1px solid var(--border-color)',
      padding: '0 36px',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      position: 'sticky',
      top: 0,
      zIndex: 30,
    }}>
      {/* Date & Time Display */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
        <div style={{
          display: 'flex',
          alignItems: 'center',
          gap: '8px',
          background: 'var(--bg-main)',
          padding: '6px 14px',
          borderRadius: 'var(--radius-full)',
          fontSize: '0.85rem',
          fontWeight: 600,
          color: 'var(--text-main)',
        }}>
          <Clock size={16} color="var(--primary)" />
          <span>{formattedDate}</span>
          <span style={{ color: 'var(--border-color)' }}>|</span>
          <span style={{ color: 'var(--primary)', fontVariantNumeric: 'tabular-nums' }}>{formattedTime}</span>
        </div>

        {activeTimer && (
          <div style={{
            display: 'flex',
            alignItems: 'center',
            gap: '8px',
            background: activeTimer.status === 'RUNNING' ? 'rgba(99, 91, 255, 0.1)' : 'rgba(245, 158, 11, 0.1)',
            color: activeTimer.status === 'RUNNING' ? 'var(--primary)' : 'var(--warning)',
            padding: '6px 12px',
            borderRadius: 'var(--radius-full)',
            fontSize: '0.8rem',
            fontWeight: 700,
          }}>
            <span style={{
              width: '8px',
              height: '8px',
              borderRadius: '50%',
              background: activeTimer.status === 'RUNNING' ? 'var(--primary)' : 'var(--warning)',
              animation: activeTimer.status === 'RUNNING' ? 'pulseGlow 1.5s infinite' : 'none',
            }} />
            <span>Timer: {activeTimer.elapsedFormatted || '00:00:00'}</span>
          </div>
        )}
      </div>

      {/* Action Buttons & User Profile */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
        {!activeTimer && (
          <button className="btn btn-secondary btn-sm" onClick={onStartTimer}>
            <Play size={15} color="var(--primary)" />
            <span>Start Timer</span>
          </button>
        )}

        <button className="btn btn-primary btn-sm" onClick={onAddActivity}>
          <Plus size={16} />
          <span>Add Activity</span>
        </button>

        <div style={{
          display: 'flex',
          alignItems: 'center',
          gap: '10px',
          marginLeft: '8px',
          paddingLeft: '16px',
          borderLeft: '1px solid var(--border-color)',
        }}>
          <div style={{
            width: '36px',
            height: '36px',
            borderRadius: '50%',
            background: 'var(--primary-light)',
            color: 'var(--primary)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontWeight: 700,
            fontSize: '0.9rem',
          }}>
            {user?.fullName ? user.fullName.charAt(0).toUpperCase() : <UserIcon size={18} />}
          </div>
          <div style={{ display: 'flex', flexDirection: 'column' }}>
            <span style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-main)' }}>
              {user?.fullName || 'User'}
            </span>
            <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
              {user?.timezone || 'UTC'}
            </span>
          </div>
        </div>
      </div>
    </header>
  );
};

export default Navbar;
