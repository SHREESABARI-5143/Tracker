import React from 'react';
import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  PieChart,
  Pie,
  Cell,
  Legend,
  AreaChart,
  Area,
} from 'recharts';

export const WeeklyBarChart = ({ data, dailyMetrics, height = 260 }) => {
  const chartList = (Array.isArray(data) && data.length > 0) ? data : (Array.isArray(dailyMetrics) ? dailyMetrics : []);
  if (!chartList || chartList.length === 0) {
    return (
      <div style={{ height, display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--text-muted)' }}>
        No weekly data recorded
      </div>
    );
  }

  const chartData = chartList.map((d) => ({
    name: d.dayName || d.day || (d.date ? d.date.split('-')[2] : 'Day'),
    hours: d.totalTrackedHours != null
      ? d.totalTrackedHours
      : (d.totalTrackedSeconds ? Number((d.totalTrackedSeconds / 3600).toFixed(1)) : (d.totalHours || 0)),
    activities: d.activityCount || 0,
  }));

  return (
    <div style={{ width: '100%', height }}>
      <ResponsiveContainer>
        <BarChart data={chartData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
          <XAxis dataKey="name" stroke="#94A3B8" fontSize={12} tickLine={false} />
          <YAxis stroke="#94A3B8" fontSize={12} tickLine={false} unit="h" />
          <Tooltip
            formatter={(value) => [`${value} hrs`, 'Tracked Time']}
            contentStyle={{ background: '#FFFFFF', borderRadius: 8, border: '1px solid #E2E8F0', boxShadow: '0 4px 6px rgba(0,0,0,0.05)' }}
          />
          <Bar dataKey="hours" fill="#635BFF" radius={[6, 6, 0, 0]} />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
};

export const CategoryPieChart = ({ data, height = 260 }) => {
  if (!data || data.length === 0) {
    return (
      <div style={{ height, display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--text-muted)' }}>
        No category activity logged
      </div>
    );
  }

  const chartData = data.map((cat) => ({
    name: cat.categoryName,
    value: cat.hours != null
      ? cat.hours
      : (cat.totalSeconds ? Number((cat.totalSeconds / 3600).toFixed(2)) : (cat.durationSeconds ? Number((cat.durationSeconds / 3600).toFixed(2)) : (cat.durationMinutes ? Number((cat.durationMinutes / 60).toFixed(2)) : 0))),
    color: cat.categoryColor || '#635BFF',
    percentage: cat.percentage || 0,
  })).filter(item => item.value > 0);

  if (chartData.length === 0) {
    return (
      <div style={{ height, display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--text-muted)' }}>
        No category activity logged
      </div>
    );
  }

  return (
    <div style={{ width: '100%', height: 260 }}>
      <ResponsiveContainer>
        <PieChart>
          <Pie
            data={chartData}
            cx="50%"
            cy="50%"
            innerRadius={55}
            outerRadius={85}
            paddingAngle={3}
            dataKey="value"
          >
            {chartData.map((entry, index) => (
              <Cell key={`cell-${index}`} fill={entry.color} />
            ))}
          </Pie>
          <Tooltip
            formatter={(value, name, item) => [`${value} hrs (${item.payload.percentage}%)`, name]}
            contentStyle={{ background: '#FFFFFF', borderRadius: 8, border: '1px solid #E2E8F0', boxShadow: '0 4px 6px rgba(0,0,0,0.05)' }}
          />
          <Legend
            verticalAlign="bottom"
            iconType="circle"
            wrapperStyle={{ fontSize: '0.8rem', paddingTop: '10px' }}
          />
        </PieChart>
      </ResponsiveContainer>
    </div>
  );
};

export const MonthlyTrendChart = ({ data }) => {
  if (!data || data.length === 0) {
    return (
      <div style={{ height: 260, display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--text-muted)' }}>
        No monthly activity logged
      </div>
    );
  }

  const chartData = data.map((d) => ({
    date: d.date?.slice(8), // day of month e.g. "05"
    hours: d.totalTrackedHours,
  }));

  return (
    <div style={{ width: '100%', height: 260 }}>
      <ResponsiveContainer>
        <AreaChart data={chartData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
          <defs>
            <linearGradient id="colorHours" x1="0" y1="0" x2="0" y2="1">
              <stop offset="5%" stopColor="#635BFF" stopOpacity={0.3} />
              <stop offset="95%" stopColor="#635BFF" stopOpacity={0.0} />
            </linearGradient>
          </defs>
          <XAxis dataKey="date" stroke="#94A3B8" fontSize={11} tickLine={false} />
          <YAxis stroke="#94A3B8" fontSize={11} tickLine={false} unit="h" />
          <Tooltip
            formatter={(value) => [`${value} hrs`, 'Tracked Time']}
            contentStyle={{ background: '#FFFFFF', borderRadius: 8, border: '1px solid #E2E8F0' }}
          />
          <Area type="monotone" dataKey="hours" stroke="#635BFF" strokeWidth={2} fillOpacity={1} fill="url(#colorHours)" />
        </AreaChart>
      </ResponsiveContainer>
    </div>
  );
};
