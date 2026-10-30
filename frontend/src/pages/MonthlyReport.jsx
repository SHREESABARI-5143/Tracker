import React, { useState, useEffect } from 'react';
import { reportApi } from '../services/api';
import { CategoryPieChart } from '../components/Charts';
import {
  Calendar,
  ChevronLeft,
  ChevronRight,
  TrendingUp,
  TrendingDown,
  Clock,
  Zap,
  Download,
  CalendarDays
} from 'lucide-react';
import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer
} from 'recharts';

export default function MonthlyReport() {
  const [selectedMonth, setSelectedMonth] = useState(() => {
    const now = new Date();
    return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
  });
  const [report, setReport] = useState(null);
  const [loading, setLoading] = useState(true);

  const fetchReport = async (monthStr) => {
    try {
      setLoading(true);
      const [year, month] = monthStr.split('-');
      const res = await reportApi.getMonthlyReport(year, month);
      setReport(res.data?.data || res.data);
    } catch (err) {
      console.error('Failed to load monthly report:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchReport(selectedMonth);
  }, [selectedMonth]);

  const handlePrevMonth = () => {
    const [year, month] = selectedMonth.split('-').map(Number);
    const d = new Date(year, month - 2, 1);
    setSelectedMonth(`${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`);
  };

  const handleNextMonth = () => {
    const [year, month] = selectedMonth.split('-').map(Number);
    const d = new Date(year, month, 1);
    setSelectedMonth(`${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`);
  };

  const handleCurrentMonth = () => {
    const now = new Date();
    setSelectedMonth(`${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`);
  };

  const formatMinutes = (totalMinutes) => {
    if (!totalMinutes || totalMinutes <= 0) return '0h 0m';
    const hrs = Math.floor(totalMinutes / 60);
    const mins = totalMinutes % 60;
    return hrs > 0 ? `${hrs}h ${mins}m` : `${mins}m`;
  };

  // Prepare trend data for area chart
  const trendData = (report?.dailyMetrics || []).map((m) => {
    const dayNumber = m.date.split('-')[2];
    return {
      day: `Day ${parseInt(dayNumber, 10)}`,
      productiveHours: Number(((m.productiveMinutes || 0) / 60).toFixed(1)),
      totalHours: Number(((m.totalTrackedMinutes || 0) / 60).toFixed(1))
    };
  });

  const monthTitle = new Date(`${selectedMonth}-01T00:00:00`).toLocaleDateString('en-US', {
    month: 'long',
    year: 'numeric'
  });

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--spacing-xl)' }}>
      {/* Month Selector Bar */}
      <div className="card" style={{ padding: 'var(--spacing-md) var(--spacing-lg)' }}>
        <div style={{
          display: 'flex',
          flexWrap: 'wrap',
          alignItems: 'center',
          justifyContent: 'space-between',
          gap: 'var(--spacing-md)'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--spacing-sm)' }}>
            <button className="btn btn-secondary" onClick={handlePrevMonth} title="Previous Month">
              <ChevronLeft size={18} />
            </button>
            <button className="btn btn-secondary" onClick={handleCurrentMonth}>
              This Month
            </button>
            <button className="btn btn-secondary" onClick={handleNextMonth} title="Next Month">
              <ChevronRight size={18} />
            </button>
            <div style={{ marginLeft: 'var(--spacing-sm)', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <CalendarDays size={18} style={{ color: 'var(--color-primary-light)' }} />
              <span style={{ fontWeight: '700', fontSize: '1.1rem' }}>
                {monthTitle}
              </span>
            </div>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--spacing-sm)' }}>
            <input
              type="month"
              className="form-input"
              style={{ width: 'auto', height: '38px' }}
              value={selectedMonth}
              onChange={(e) => setSelectedMonth(e.target.value)}
            />
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
            <span style={{ fontSize: '0.825rem', fontWeight: '600' }}>MONTHLY TRACKED</span>
          </div>
          <div style={{ fontSize: '1.6rem', fontWeight: '800' }}>
            {formatMinutes(report?.totalTrackedMinutes || 0)}
          </div>
          <span style={{ fontSize: '0.85rem', color: 'var(--color-text-muted)' }}>
            Total activity logged
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
            <Calendar size={16} />
            <span style={{ fontSize: '0.825rem', fontWeight: '600' }}>ACTIVE DAYS</span>
          </div>
          <div style={{ fontSize: '1.6rem', fontWeight: '800', color: 'var(--color-accent)' }}>
            {report?.activeDaysCount || 0} days
          </div>
          <span style={{ fontSize: '0.85rem', color: 'var(--color-text-muted)' }}>
            Days with recorded activities
          </span>
        </div>

        <div className="card" style={{ padding: 'var(--spacing-lg)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: 'var(--color-warning)', marginBottom: '4px' }}>
            <TrendingUp size={16} />
            <span style={{ fontSize: '0.825rem', fontWeight: '600' }}>VS PREVIOUS MONTH</span>
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
            Month-over-month trend
          </span>
        </div>
      </div>

      {/* Daily Progression Area Chart */}
      <div className="card" style={{ padding: 'var(--spacing-lg)' }}>
        <h2 style={{ fontSize: '1.1rem', fontWeight: '700', marginBottom: 'var(--spacing-md)' }}>
          Daily Productive Hours Trend
        </h2>
        {trendData.length === 0 ? (
          <p style={{ textAlign: 'center', padding: 'var(--spacing-xl) 0', color: 'var(--color-text-muted)' }}>
            No activity logged this month yet.
          </p>
        ) : (
          <div style={{ width: '100%', height: 300 }}>
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={trendData} margin={{ top: 10, right: 20, left: -10, bottom: 0 }}>
                <defs>
                  <linearGradient id="monthProdGrad" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#10b981" stopOpacity={0.4} />
                    <stop offset="95%" stopColor="#10b981" stopOpacity={0.0} />
                  </linearGradient>
                  <linearGradient id="monthTotGrad" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#6366f1" stopOpacity={0.2} />
                    <stop offset="95%" stopColor="#6366f1" stopOpacity={0.0} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.08)" />
                <XAxis dataKey="day" stroke="#94a3b8" fontSize={11} tickLine={false} />
                <YAxis stroke="#94a3b8" fontSize={11} tickLine={false} unit="h" />
                <Tooltip
                  contentStyle={{
                    backgroundColor: '#1e293b',
                    borderColor: 'rgba(255,255,255,0.1)',
                    borderRadius: '8px',
                    color: '#f8fafc'
                  }}
                  formatter={(val) => [`${val} hrs`]}
                />
                <Area type="monotone" dataKey="totalHours" name="Total Tracked" stroke="#6366f1" strokeWidth={2} fillOpacity={1} fill="url(#monthTotGrad)" />
                <Area type="monotone" dataKey="productiveHours" name="Productive Time" stroke="#10b981" strokeWidth={2} fillOpacity={1} fill="url(#monthProdGrad)" />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        )}
      </div>

      {/* Category Breakdown Donut and Table */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(420px, 1fr))',
        gap: 'var(--spacing-lg)'
      }}>
        <div className="card" style={{ padding: 'var(--spacing-lg)' }}>
          <h2 style={{ fontSize: '1.1rem', fontWeight: '700', marginBottom: 'var(--spacing-md)' }}>
            Monthly Category Share
          </h2>
          <CategoryPieChart data={report?.categoryBreakdown || []} height={280} />
        </div>

        <div className="card" style={{ padding: 'var(--spacing-lg)' }}>
          <h2 style={{ fontSize: '1.1rem', fontWeight: '700', marginBottom: 'var(--spacing-md)' }}>
            Category Ranking
          </h2>
          {(!report?.categoryBreakdown || report.categoryBreakdown.length === 0) ? (
            <p style={{ color: 'var(--color-text-muted)', textAlign: 'center', padding: 'var(--spacing-lg) 0' }}>
              No categories recorded this month.
            </p>
          ) : (
            <div style={{ overflowX: 'auto' }}>
              <table className="table">
                <thead>
                  <tr>
                    <th>Category</th>
                    <th>Hours</th>
                    <th>Share</th>
                  </tr>
                </thead>
                <tbody>
                  {report.categoryBreakdown.map((c, idx) => {
                    const sharePct = report.totalTrackedMinutes > 0
                      ? Math.round((c.durationMinutes / report.totalTrackedMinutes) * 100)
                      : 0;

                    return (
                      <tr key={idx}>
                        <td style={{ display: 'flex', alignItems: 'center', gap: '8px', fontWeight: '600' }}>
                          <span style={{
                            width: '10px',
                            height: '10px',
                            borderRadius: '50%',
                            backgroundColor: c.categoryColor || '#6366f1'
                          }} />
                          <span>{c.categoryName}</span>
                        </td>
                        <td style={{ fontWeight: '600' }}>
                          {formatMinutes(c.durationMinutes)}
                        </td>
                        <td>{sharePct}%</td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
