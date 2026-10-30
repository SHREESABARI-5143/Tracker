import React, { useState } from 'react';
import { Plus, Edit2, Clock } from 'lucide-react';

const Timeline = ({ segments, blocks, onSelectActivity, onSelectGap, onLogGap }) => {
  const [hoveredSegment, setHoveredSegment] = useState(null);
  const activeSegments = Array.isArray(segments) ? segments : (Array.isArray(blocks) ? blocks : []);

  // Hour labels: 00:00 to 24:00 in 2-hour increments
  const hours = [0, 2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22, 24];

  return (
    <div style={{ position: 'relative', width: '100%', userSelect: 'none' }}>
      {/* 24-Hour Axis Labels */}
      <div style={{
        display: 'flex',
        justifyContent: 'space-between',
        fontSize: '0.75rem',
        fontWeight: 600,
        color: 'var(--text-muted)',
        marginBottom: '10px',
        padding: '0 4px',
      }}>
        {hours.map((h) => (
          <span key={h}>{String(h).padStart(2, '0')}:00</span>
        ))}
      </div>

      {/* Timeline Track Container */}
      <div style={{
        position: 'relative',
        height: '68px',
        background: '#F1F5F9',
        borderRadius: 'var(--radius-md)',
        border: '1px solid var(--border-color)',
        overflow: 'hidden',
        boxShadow: 'inset 0 2px 4px rgba(0,0,0,0.04)',
      }}>
        {/* Background Hour Grid Lines */}
        {hours.map((h) => (
          <div
            key={h}
            style={{
              position: 'absolute',
              left: `${(h / 24) * 100}%`,
              top: 0,
              bottom: 0,
              borderLeft: '1px dashed #CBD5E1',
              pointerEvents: 'none',
              zIndex: 1,
            }}
          />
        ))}

        {/* Dynamic Activity and Untracked Segments */}
        {activeSegments.map((seg, idx) => {
          const leftPercent = seg.leftPercentage != null
            ? seg.leftPercentage
            : (seg.startHour / 24) * 100;
          const widthPercent = seg.widthPercentage != null
            ? seg.widthPercentage
            : ((seg.endHour - seg.startHour) / 24) * 100;
          const isActivity = seg.type === 'ACTIVITY';

          return (
            <div
              key={idx}
              onClick={() => {
                if (isActivity && onSelectActivity) {
                  onSelectActivity(seg.activityId);
                } else if (!isActivity) {
                  if (onSelectGap) onSelectGap(seg.startTime, seg.endTime);
                  if (onLogGap) onLogGap({ startTime: seg.startTime, endTime: seg.endTime });
                }
              }}
              onMouseEnter={() => setHoveredSegment(seg)}
              onMouseLeave={() => setHoveredSegment(null)}
              style={{
                position: 'absolute',
                left: `${leftPercent}%`,
                width: `${Math.max(0.5, widthPercent)}%`,
                top: '6px',
                bottom: '6px',
                borderRadius: 'var(--radius-sm)',
                background: isActivity
                  ? seg.categoryColor || 'var(--primary)'
                  : 'repeating-linear-gradient(45deg, #E2E8F0, #E2E8F0 6px, #F1F5F9 6px, #F1F5F9 12px)',
                color: isActivity ? '#FFFFFF' : 'var(--text-muted)',
                display: 'flex',
                alignItems: 'center',
                padding: '0 8px',
                fontSize: '0.75rem',
                fontWeight: 600,
                overflow: 'hidden',
                textOverflow: 'ellipsis',
                whiteSpace: 'nowrap',
                cursor: 'pointer',
                zIndex: isActivity ? 5 : 2,
                boxShadow: isActivity ? '0 2px 5px rgba(0,0,0,0.1)' : 'none',
                transition: 'transform 0.15s ease, filter 0.15s ease',
                border: isActivity ? '1px solid rgba(255,255,255,0.3)' : '1px solid #CBD5E1',
              }}
              title={`${seg.title} (${seg.localStartTime} - ${seg.localEndTime})`}
            >
              {widthPercent > 3 && (
                <span style={{ overflow: 'hidden', textOverflow: 'ellipsis' }}>
                  {seg.title}
                </span>
              )}
            </div>
          );
        })}
      </div>

      {/* Floating Info Tooltip */}
      {hoveredSegment && (
        <div style={{
          marginTop: '12px',
          padding: '10px 16px',
          background: '#FFFFFF',
          border: '1px solid var(--border-color)',
          borderRadius: 'var(--radius-md)',
          boxShadow: 'var(--shadow-md)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          fontSize: '0.85rem',
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <span style={{
              width: '12px',
              height: '12px',
              borderRadius: '3px',
              background: hoveredSegment.categoryColor || '#CBD5E1',
            }} />
            <strong style={{ color: 'var(--text-main)' }}>{hoveredSegment.title}</strong>
            <span style={{ color: 'var(--text-muted)' }}>
              {hoveredSegment.localStartTime} – {hoveredSegment.localEndTime}
            </span>
            <span style={{
              background: 'var(--bg-subtle)',
              padding: '2px 8px',
              borderRadius: 'var(--radius-full)',
              fontSize: '0.75rem',
              fontWeight: 600,
            }}>
              {hoveredSegment.durationFormatted}
            </span>
          </div>

          <span style={{ fontSize: '0.75rem', color: 'var(--primary)', fontWeight: 600 }}>
            {hoveredSegment.type === 'ACTIVITY' ? 'Click to edit' : 'Click to log this untracked gap'}
          </span>
        </div>
      )}
    </div>
  );
};

export default Timeline;
