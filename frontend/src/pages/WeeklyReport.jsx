import React, { useState, useEffect } from 'react';
import { reportApi } from '../services/api';
import { WeeklyBarChart, CategoryPieChart } from '../components/Charts';
import {
  Calendar,
  ChevronLeft,
  ChevronRight,
  TrendingUp,
  TrendingDown,
  Clock,
  Zap,
  Download,
  AlertCircle
} from 'lucide-react';

export default function WeeklyReport() {
  const [currentDate, setCurrentDate] = useState(() => {
    return new Date().toISOString().split('T')[0];
  });
  const [report, setReport] = useState(null);
  const [loading, setLoading] = useState(true);

  const fetchReport = async (date) => {
    try {
      setLoading(true);
      const res = await reportApi.getWeeklyReport(date);
      setReport(res.data?.data || res.data);
    } catch (err) {
      console.error('Failed to load weekly report:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchReport(currentDate);
  }, [currentDate]);

  const handlePrevWeek = () => {
    const d = new Date(currentDate);
    d.setDate(d.getDate() - 7);
    setCurrentDate(d.toISOString().split('T')[0]);
  };

  const handleNextWeek = () => {
    const d = new Date(currentDate);
    d.setDate(d.getDate() + 7);
    setCurrentDate(d.toISOString().split('T')[0]);
  };

  const handleCurrentWeek = () => {
    setCurrentDate(new Date().toISOString().split('T')[0]);
  };

  const formatMinutes = (totalMinutes) => {
    if (!totalMinutes || totalMinutes <= 0) return '0h 0m';
    const hrs = Math.floor(totalMinutes / 60);
    const mins = totalMinutes % 60;
    return hrs > 0 ? `${hrs}h ${mins}m` : `${mins}m`;
  };

  const handleExportCsv = async () => {
    try {
      const response = await reportApi.exportCsv({
        type: 'weekly',
        startDate: report?.startDate,
        endDate: report?.endDate
      });
      // Create download blob
      const blob = new Blob([response.data], { type: 'text/csv' });
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `ChronoTrack_Weekly_${report?.startDate || 'export'}.csv`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
    } catch (err) {
      // Fallback CSV generation on client side if endpoint isn't wired yet
      const rows = [
        ['Category', 'Duration (Minutes)', 'Duration (Hours)', 'Productive'],
        ...(report?.categoryBreakdown || []).map(c => [
          c.categoryName,
          c.durationMinutes,
          (c.durationMinutes / 60).toFixed(2),
          c.productive ? 'Yes' : 'No'
        ])
      ];
      const csvContent = 'data:text/csv;charset=utf-8,' + rows.map(e => e.join(',')).join('\n');
      const encodedUri = encodeURI(csvContent);
      const link = document.createElement('a');
      link.setAttribute('href', encodedUri);
      link.setAttribute('download', `ChronoTrack_Weekly_${report?.startDate || 'export'}.csv`);
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--spacing-xl)' }}>
      {/* Header & Week Selector Bar */}
      <div className="card" style={{ padding: 'var(--spacing-md) var(--spacing-lg)' }}>
        <div style={{
          display: 'flex',
          flexWrap: 'wrap',
          alignItems: 'center',
          justifyContent: 'space-between',
          gap: 'var(--spacing-md)'
        }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--spacing-sm)' }}>
              <button className="btn btn-secondary" onClick={handlePrevWeek} title="Previous Week">
                <ChevronLeft size={18} />
              </button>
              <button className="btn btn-secondary" onClick={handleCurrentWeek}>
                This Week
              </button>
              <button className="btn btn-secondary" onClick={handleNextWeek} title="Next Week">
                <ChevronRight size={18} />
              </button>
              <div style={{ marginLeft: 'var(--spacing-sm)', display: 'flex', alignItems: 'center', gap: '6px' }}>
                <Calendar size={18} style={{ color: 'var(--color-primary-light)' }} />
                <span style={{ fontWeight: '700', fontSize: '1.05rem' }}>
                  {report?.startDate} to {report?.endDate} (Mon – Sun)
                </span>
              </div>
            </div>
          </div>

          <div style={{ display: 'flex', gap: 'var(--spacing-sm)' }}>
            <button className="btn btn-secondary" onClick={handleExportCsv}>
              <Download size={16} />
              <span>Export CSV</span>
            </button>
          </div>
        </div>
      </div>

      {/* KPI Cards */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))',
        gap: 'var(--spacing-md)'
      }}>
        <div className="card" style={{ padding: 'var(--spacing-lg)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: 'var(--color-primary-light)', marginBottom: '4px' }}>
            <Clock size={16} />
            <span style={{ fontSize: '0.825rem', fontWeight: '600' }}>TOTAL TRACKED</span>
          </div>
          <div style={{ fontSize: '1.6rem', fontWeight: '800' }}>
            {formatMinutes(report?.totalTrackedMinutes || 0)}
          </div>
          <span style={{ fontSize: '0.85rem', color: 'var(--color-text-muted)' }}>
            Across 7 days ({Math.round(((report?.totalTrackedMinutes || 0) / (7 * 1440)) * 100)}% of 168h)
          </span>
        </div>

        <div className="card" style={{ padding: 'var(--spacing-lg)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: 'var(--color-success)', marginBottom: '4px' }}>
            <Zap size={16} />
            <span style={{ fontSize: '0.825rem', fontWeight: '600' }}>PRODUCTIVE TIME</span>
          </div>
          <div style={{ fontSize: '1.6rem', fontWeight: '800', color: 'var(--color-success)' }}>
            {formatMinutes(report?.productiveMinutes || 0)}
          </div>
          <span style={{ fontSize: '0.85rem', color: 'var(--color-text-muted)' }}>
            {Math.round(report?.productivityPercentage || 0)}% productivity score
          </span>
        </div>

        <div className="card" style={{ padding: 'var(--spacing-lg)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: 'var(--color-accent)', marginBottom: '4px' }}>
            <TrendingUp size={16} />
            <span style={{ fontSize: '0.825rem', fontWeight: '600' }}>VS PREVIOUS WEEK</span>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <span style={{
              fontSize: '1.6rem',
              fontWeight: '800',
              color: (report?.comparisonPercentage || 0) >= 0 ? 'var(--color-success)' : 'var(--color-danger)'
            }}>
              {(report?.comparisonPercentage || 0) > 0 ? '+' : ''}
              {report?.comparisonPercentage != null ? report.comparisonPercentage.toFixed(1) : 0}%
            </span>
            {(report?.comparisonPercentage || 0) >= 0 ? (
              <TrendingUp size={22} style={{ color: 'var(--color-success)' }} />
            ) : (
              <TrendingDown size={22} style={{ color: 'var(--color-danger)' }} />
            )}
          </div>
          <span style={{ fontSize: '0.85rem', color: 'var(--color-text-muted)' }}>
            Productive hours change
          </span>
        </div>

        <div className="card" style={{ padding: 'var(--spacing-lg)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: 'var(--color-warning)', marginBottom: '4px' }}>
            <AlertCircle size={16} />
            <span style={{ fontSize: '0.825rem', fontWeight: '600' }}>UNTRACKED GAPS</span>
          </div>
          <div style={{ fontSize: '1.6rem', fontWeight: '800', color: 'var(--color-warning)' }}>
            {formatMinutes(report?.untrackedMinutes != null ? report.untrackedMinutes : ((7 * 1440) - (report?.totalTrackedMinutes || 0)))}
          </div>
          <span style={{ fontSize: '0.85rem', color: 'var(--color-text-muted)' }}>
            Weekly unlogged period
          </span>
        </div>
      </div>

      {/* Visual Charts Row */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(420px, 1fr))',
        gap: 'var(--spacing-lg)'
      }}>
        {/* Day-by-Day Bar Chart */}
        <div className="card" style={{ padding: 'var(--spacing-lg)' }}>
          <h2 style={{ fontSize: '1.1rem', fontWeight: '700', marginBottom: 'var(--spacing-md)' }}>
            Daily Breakdown (Monday – Sunday)
          </h2>
          <WeeklyBarChart
            data={report?.dailyBreakdown || report?.dailyMetrics || []}
            dailyMetrics={report?.dailyBreakdown || report?.dailyMetrics || []}
            height={280}
          />
        </div>

        {/* Category Breakdown Donut */}
        <div className="card" style={{ padding: 'var(--spacing-lg)' }}>
          <h2 style={{ fontSize: '1.1rem', fontWeight: '700', marginBottom: 'var(--spacing-md)' }}>
            Category Distribution
          </h2>
          <CategoryPieChart
            data={report?.categoryDistribution || report?.categoryBreakdown || []}
            height={280}
          />
        </div>
      </div>

      {/* Category Breakdown Ranking Table */}
      <div className="card" style={{ padding: 'var(--spacing-lg)' }}>
        <h2 style={{ fontSize: '1.1rem', fontWeight: '700', marginBottom: 'var(--spacing-md)' }}>
          Category Summary Table
        </h2>

        {(() => {
          const catList = report?.categoryDistribution || report?.categoryBreakdown || [];
          if (catList.length === 0) {
            return (
              <p style={{ color: 'var(--color-text-muted)', textAlign: 'center', padding: 'var(--spacing-lg) 0' }}>
                No activity data recorded for this week.
              </p>
            );
          }

          return (
            <div style={{ overflowX: 'auto' }}>
              <table className="table">
                <thead>
                  <tr>
                    <th>Category</th>
                    <th>Productive?</th>
                    <th>Total Time</th>
                    <th>% of Tracked</th>
                    <th>Visual Share</th>
                  </tr>
                </thead>
                <tbody>
                  {catList.map((c, idx) => {
                    const durationMins = c.durationMinutes != null
                      ? c.durationMinutes
                      : (c.totalSeconds ? Math.floor(c.totalSeconds / 60) : (c.durationSeconds ? Math.floor(c.durationSeconds / 60) : 0));
                    const totalMins = report.totalTrackedMinutes || (report.totalTrackedSeconds ? Math.floor(report.totalTrackedSeconds / 60) : 0);
                    const sharePct = totalMins > 0 ? Math.round((durationMins / totalMins) * 100) : (c.percentage || 0);

                    return (
                      <tr key={idx}>
                        <td style={{ fontWeight: '600', display: 'flex', alignItems: 'center', gap: '8px' }}>
                          <span style={{
                            width: '12px',
                            height: '12px',
                            borderRadius: '50%',
                            backgroundColor: c.categoryColor || '#6366f1'
                          }} />
                          <span>{c.categoryName}</span>
                        </td>
                        <td>
                          {c.productive || c.isProductive ? (
                            <span style={{ color: 'var(--color-success)', fontWeight: '500', fontSize: '0.85rem' }}>
                              Productive
                            </span>
                          ) : (
                            <span style={{ color: 'var(--color-text-muted)', fontSize: '0.85rem' }}>
                              Leisure / Routine
                            </span>
                          )}
                        </td>
                        <td style={{ fontWeight: '600' }}>
                          {formatMinutes(durationMins)}
                        </td>
                        <td>{sharePct}%</td>
                        <td style={{ width: '200px' }}>
                          <div style={{
                            width: '100%',
                            height: '6px',
                            background: 'var(--color-surface)',
                            borderRadius: '3px',
                            overflow: 'hidden'
                          }}>
                            <div style={{
                              width: `${sharePct}%`,
                              height: '100%',
                              backgroundColor: c.categoryColor || 'var(--color-primary)'
                            }} />
                          </div>
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
    </div>
  );
}
