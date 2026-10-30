import React from 'react';

const StatCard = ({ title, value, subtitle, icon: Icon, color = 'var(--primary)', trend }) => {
  return (
    <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <span style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-muted)' }}>
          {title}
        </span>
        {Icon && (
          <div style={{
            width: '38px',
            height: '38px',
            borderRadius: 'var(--radius-md)',
            background: `${color}15`,
            color: color,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
          }}>
            <Icon size={20} />
          </div>
        )}
      </div>

      <div style={{ display: 'flex', alignItems: 'baseline', gap: '8px' }}>
        <span style={{ fontSize: '1.85rem', fontWeight: 700, color: 'var(--text-main)', letterSpacing: '-0.5px' }}>
          {value}
        </span>
        {trend && (
          <span style={{
            fontSize: '0.8rem',
            fontWeight: 600,
            color: trend.startsWith('+') ? 'var(--accent)' : 'var(--danger)',
            background: trend.startsWith('+') ? 'var(--accent-light)' : 'var(--danger-light)',
            padding: '2px 8px',
            borderRadius: 'var(--radius-full)',
          }}>
            {trend}
          </span>
        )}
      </div>

      {subtitle && (
        <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
          {subtitle}
        </span>
      )}
    </div>
  );
};

export default StatCard;
