import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import {
  reportApi,
  timelineApi,
  activityApi,
  goalApi,
  insightApi,
  categoryApi
} from '../services/api';
import StatCard from '../components/StatCard';
import TimerBanner from '../components/TimerBanner';
import Timeline from '../components/Timeline';
import { CategoryPieChart, WeeklyBarChart } from '../components/Charts';
import ActivityForm from '../components/ActivityForm';
import StartTimerModal from '../components/StartTimerModal';
import {
  Clock,
  Zap,
  AlertTriangle,
  Award,
  Plus,
  Play,
  Calendar,
  Sparkles,
  TrendingUp,
  Target,
  ChevronRight,
  ArrowRight
} from 'lucide-react';
import { Link } from 'react-router-dom';

export default function Dashboard() {
  const { user } = useAuth();
  const [loading, setLoading] = useState(true);
  const [dailyData, setDailyData] = useState(null);
  const [weeklyData, setWeeklyData] = useState(null);
  const [timelineData, setTimelineData] = useState(null);
  const [recentActivities, setRecentActivities] = useState([]);
  const [goals, setGoals] = useState([]);
  const [insights, setInsights] = useState([]);
  const [categories, setCategories] = useState([]);

  // Modals
  const [isActivityModalOpen, setIsActivityModalOpen] = useState(false);
  const [isTimerModalOpen, setIsTimerModalOpen] = useState(false);
  const [editingActivity, setEditingActivity] = useState(null);

  const todayStr = new Date().toISOString().split('T')[0];

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      const [
        dailyRes,
        weeklyRes,
        timelineRes,
        activitiesRes,
        goalsRes,
        insightsRes,
        catsRes
      ] = await Promise.allSettled([
        reportApi.getDailyReport(todayStr),
        reportApi.getWeeklyReport(todayStr),
        timelineApi.getDailyTimeline(todayStr),
        activityApi.getAll({ page: 0, size: 5 }),
        goalApi.getAll(),
        insightApi.getDailyInsights(todayStr),
        categoryApi.getAll()
      ]);

      const unwrap = (val) => {
        if (!val) return null;
        if (val.data !== undefined) return val.data;
        return val;
      };

      if (dailyRes.status === 'fulfilled') setDailyData(unwrap(dailyRes.value.data));
      if (weeklyRes.status === 'fulfilled') setWeeklyData(unwrap(weeklyRes.value.data));
      if (timelineRes.status === 'fulfilled') setTimelineData(unwrap(timelineRes.value.data));
      if (activitiesRes.status === 'fulfilled') {
        const d = unwrap(activitiesRes.value.data);
        const content = d?.content || d;
        setRecentActivities(Array.isArray(content) ? content : []);
      }
      if (goalsRes.status === 'fulfilled') {
        const d = unwrap(goalsRes.value.data);
        setGoals(Array.isArray(d) ? d : []);
      }
      if (insightsRes.status === 'fulfilled') {
        const d = unwrap(insightsRes.value.data);
        setInsights(Array.isArray(d) ? d : []);
      }
      if (catsRes.status === 'fulfilled') {
        const d = unwrap(catsRes.value.data);
        setCategories(Array.isArray(d) ? d : []);
      }
    } catch (err) {
      console.error('Failed to fetch dashboard data:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const handleActivitySaved = () => {
    setIsActivityModalOpen(false);
    setEditingActivity(null);
    fetchDashboardData();
  };

  const handleEditActivity = (act) => {
    setEditingActivity(act);
    setIsActivityModalOpen(true);
  };

  const handleDeleteActivity = async (id) => {
    if (!window.confirm('Are you sure you want to delete this activity?')) return;
    try {
      await activityApi.delete(id);
      fetchDashboardData();
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

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--spacing-xl)' }}>
      {/* Timer Banner for Active/Paused Sessions */}
      <TimerBanner onSessionStopped={fetchDashboardData} />

      {/* Header & Quick Action Buttons */}
      <div style={{
        display: 'flex',
        flexWrap: 'wrap',
        alignItems: 'center',
        justifyContent: 'space-between',
        gap: 'var(--spacing-md)'
      }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: '800', color: 'var(--color-text)', display: 'flex', alignItems: 'center', gap: '8px' }}>
            <span>Welcome back, {user?.fullName || user?.username || 'Explorer'}!</span>
          </h1>
          <p style={{ color: 'var(--color-text-secondary)', fontSize: '0.925rem' }}>
            Today is {new Date().toLocaleDateString('en-US', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}
          </p>
        </div>

        <div style={{ display: 'flex', gap: 'var(--spacing-sm)' }}>
          <button
            className="btn btn-secondary"
            onClick={() => setIsTimerModalOpen(true)}
          >
            <Play size={16} style={{ color: 'var(--color-success)' }} />
            <span>Start Timer</span>
          </button>
          <button
            className="btn btn-primary"
            onClick={() => {
              setEditingActivity(null);
              setIsActivityModalOpen(true);
            }}
          >
            <Plus size={18} />
            <span>Log Activity</span>
          </button>
        </div>
      </div>

      {/* 4 Summary Stat Cards */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))',
        gap: 'var(--spacing-lg)'
      }}>
        <StatCard
          title="Tracked Today"
          value={formatMinutes(
            dailyData?.totalTrackedMinutes != null
              ? dailyData.totalTrackedMinutes
              : (dailyData?.totalTrackedSeconds ? Math.floor(dailyData.totalTrackedSeconds / 60) : 0)
          )}
          subtitle={`${Math.round((((dailyData?.totalTrackedMinutes != null ? dailyData.totalTrackedMinutes : (dailyData?.totalTrackedSeconds ? dailyData.totalTrackedSeconds / 60 : 0))) / 1440) * 100)}% of 24h day`}
          icon={Clock}
          color="primary"
        />
        <StatCard
          title="Productive Time"
          value={formatMinutes(
            dailyData?.productiveMinutes != null
              ? dailyData.productiveMinutes
              : (dailyData?.totalTrackedSeconds ? Math.floor(dailyData.totalTrackedSeconds / 60) : 0)
          )}
          subtitle={`${Math.round(dailyData?.productivityPercentage || dailyData?.trackedPercentage || 0)}% of tracked time`}
          icon={Zap}
          color="success"
        />
        <StatCard
          title="Untracked Gaps"
          value={formatMinutes(
            dailyData?.untrackedMinutes != null
              ? dailyData.untrackedMinutes
              : (dailyData?.totalUntrackedSeconds ? Math.floor(dailyData.totalUntrackedSeconds / 60) : 1440)
          )}
          subtitle="Unlogged 24-hr period"
          icon={AlertTriangle}
          color="warning"
        />
        <StatCard
          title="Top Category"
          value={
            (dailyData?.categoryDistribution && dailyData.categoryDistribution.length > 0)
              ? dailyData.categoryDistribution[0].categoryName
              : ((dailyData?.categoryBreakdown && dailyData.categoryBreakdown.length > 0)
                  ? dailyData.categoryBreakdown[0].categoryName
                  : 'None Yet')
          }
          subtitle={
            (dailyData?.categoryDistribution && dailyData.categoryDistribution.length > 0)
              ? formatMinutes(dailyData.categoryDistribution[0].durationMinutes || Math.floor((dailyData.categoryDistribution[0].totalSeconds || 0) / 60))
              : 'Log activities to see stats'
          }
          icon={Award}
          color="accent"
        />
      </div>

      {/* 24-Hour Visual Timeline Widget */}
      <div className="card" style={{ padding: 'var(--spacing-lg)' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--spacing-md)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Calendar size={18} style={{ color: 'var(--color-primary-light)' }} />
            <h2 style={{ fontSize: '1.1rem', fontWeight: '700' }}>Today's 24-Hour Timeline</h2>
          </div>
          <Link
            to="/timeline"
            style={{ display: 'flex', alignItems: 'center', gap: '4px', fontSize: '0.875rem', color: 'var(--color-primary-light)', textDecoration: 'none', fontWeight: '500' }}
          >
            <span>Full Timeline Details</span>
            <ChevronRight size={16} />
          </Link>
        </div>

        <Timeline
          segments={timelineData?.segments || timelineData?.blocks || []}
          blocks={timelineData?.blocks || timelineData?.segments || []}
          onLogGap={(gap) => {
            setEditingActivity({
              startTime: gap.startTime,
              endTime: gap.endTime
            });
            setIsActivityModalOpen(true);
          }}
          compact={true}
        />
      </div>

      {/* Charts & Analytics Row */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(420px, 1fr))',
        gap: 'var(--spacing-lg)'
      }}>
        {/* Weekly Trend Chart */}
        <div className="card" style={{ padding: 'var(--spacing-lg)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--spacing-md)' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <TrendingUp size={18} style={{ color: 'var(--color-success)' }} />
              <h2 style={{ fontSize: '1.1rem', fontWeight: '700' }}>Weekly Productivity (Mon - Sun)</h2>
            </div>
            <Link to="/reports/weekly" style={{ fontSize: '0.85rem', color: 'var(--color-primary-light)', textDecoration: 'none' }}>
              View Report
            </Link>
          </div>
          <WeeklyBarChart
            data={weeklyData?.dailyBreakdown || weeklyData?.dailyMetrics || []}
            dailyMetrics={weeklyData?.dailyBreakdown || weeklyData?.dailyMetrics || []}
            height={250}
          />
        </div>

        {/* Today's Category Breakdown Donut */}
        <div className="card" style={{ padding: 'var(--spacing-lg)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--spacing-md)' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Sparkles size={18} style={{ color: 'var(--color-accent)' }} />
              <h2 style={{ fontSize: '1.1rem', fontWeight: '700' }}>Today's Category Distribution</h2>
            </div>
          </div>
          <CategoryPieChart
            data={dailyData?.categoryDistribution || dailyData?.categoryBreakdown || []}
            height={250}
          />
        </div>
      </div>

      {/* Active Goals & Productivity Insights */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(420px, 1fr))',
        gap: 'var(--spacing-lg)'
      }}>
        {/* Goals Progress */}
        <div className="card" style={{ padding: 'var(--spacing-lg)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--spacing-md)' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Target size={18} style={{ color: 'var(--color-primary-light)' }} />
              <h2 style={{ fontSize: '1.1rem', fontWeight: '700' }}>Active Goals</h2>
            </div>
            <Link to="/goals" style={{ fontSize: '0.85rem', color: 'var(--color-primary-light)', textDecoration: 'none' }}>
              Manage Goals
            </Link>
          </div>

          {goals.length === 0 ? (
            <div style={{ textAlign: 'center', padding: 'var(--spacing-xl) 0', color: 'var(--color-text-muted)' }}>
              <p>No active goals configured yet.</p>
              <Link to="/goals" className="btn btn-secondary" style={{ marginTop: 'var(--spacing-sm)', display: 'inline-flex' }}>
                Set Your First Goal
              </Link>
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--spacing-md)' }}>
              {goals.slice(0, 3).map((goal) => {
                const pct = Math.min(100, Math.round(goal.progressPercentage || 0));
                return (
                  <div key={goal.id} style={{
                    padding: 'var(--spacing-md)',
                    background: 'var(--color-surface-hover)',
                    borderRadius: 'var(--radius-md)',
                    border: '1px solid var(--color-border)'
                  }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '6px' }}>
                      <span style={{ fontWeight: '600', fontSize: '0.925rem' }}>
                        {goal.categoryName || 'Productive Hours'} ({goal.period})
                      </span>
                      <span style={{ fontSize: '0.875rem', color: pct >= 100 ? 'var(--color-success)' : 'var(--color-text-secondary)' }}>
                        {goal.achievedHours?.toFixed(1) || 0} / {goal.targetHours}h ({pct}%)
                      </span>
                    </div>
                    <div style={{
                      width: '100%',
                      height: '8px',
                      background: 'var(--color-surface)',
                      borderRadius: '4px',
                      overflow: 'hidden'
                    }}>
                      <div style={{
                        width: `${pct}%`,
                        height: '100%',
                        background: pct >= 100
                          ? 'var(--color-success)'
                          : pct >= 50
                            ? 'var(--color-primary)'
                            : 'var(--color-warning)',
                        transition: 'width 0.4s ease'
                      }} />
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>

        {/* Productivity Insights */}
        <div className="card" style={{ padding: 'var(--spacing-lg)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--spacing-md)' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Sparkles size={18} style={{ color: 'var(--color-warning)' }} />
              <h2 style={{ fontSize: '1.1rem', fontWeight: '700' }}>Productivity Insights</h2>
            </div>
          </div>

          {insights.length === 0 ? (
            <div style={{ textAlign: 'center', padding: 'var(--spacing-xl) 0', color: 'var(--color-text-muted)' }}>
              <p>Keep logging activities today to unlock personalized productivity insights!</p>
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--spacing-sm)' }}>
              {insights.slice(0, 3).map((insight, idx) => (
                <div
                  key={idx}
                  style={{
                    display: 'flex',
                    alignItems: 'flex-start',
                    gap: 'var(--spacing-sm)',
                    padding: 'var(--spacing-md)',
                    borderRadius: 'var(--radius-md)',
                    background: insight.type === 'SUCCESS' || insight.type === 'ACHIEVEMENT'
                      ? 'rgba(16, 185, 129, 0.08)'
                      : insight.type === 'WARNING'
                        ? 'rgba(245, 158, 11, 0.08)'
                        : 'rgba(99, 102, 241, 0.08)',
                    border: `1px solid ${
                      insight.type === 'SUCCESS' || insight.type === 'ACHIEVEMENT'
                        ? 'rgba(16, 185, 129, 0.25)'
                        : insight.type === 'WARNING'
                          ? 'rgba(245, 158, 11, 0.25)'
                          : 'rgba(99, 102, 241, 0.25)'
                    }`
                  }}
                >
                  <Sparkles size={18} style={{
                    color: insight.type === 'SUCCESS' || insight.type === 'ACHIEVEMENT'
                      ? 'var(--color-success)'
                      : insight.type === 'WARNING'
                        ? 'var(--color-warning)'
                        : 'var(--color-primary-light)',
                    marginTop: '2px',
                    flexShrink: 0
                  }} />
                  <div>
                    <h4 style={{ fontSize: '0.9rem', fontWeight: '600', marginBottom: '2px', color: 'var(--color-text)' }}>
                      {insight.title}
                    </h4>
                    <p style={{ fontSize: '0.825rem', color: 'var(--color-text-secondary)', margin: 0 }}>
                      {insight.message}
                    </p>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Recent Activities Section */}
      <div className="card" style={{ padding: 'var(--spacing-lg)' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--spacing-md)' }}>
          <h2 style={{ fontSize: '1.1rem', fontWeight: '700' }}>Recent Logged Activities</h2>
          <Link
            to="/activities"
            style={{ display: 'flex', alignItems: 'center', gap: '4px', fontSize: '0.875rem', color: 'var(--color-primary-light)', textDecoration: 'none', fontWeight: '500' }}
          >
            <span>View All Activities</span>
            <ArrowRight size={16} />
          </Link>
        </div>

        {recentActivities.length === 0 ? (
          <div style={{ textAlign: 'center', padding: 'var(--spacing-2xl) 0', color: 'var(--color-text-muted)' }}>
            <Clock size={40} style={{ margin: '0 auto var(--spacing-sm)', opacity: 0.4 }} />
            <p style={{ fontSize: '1rem', fontWeight: '500' }}>No activities logged yet.</p>
            <p style={{ fontSize: '0.875rem' }}>Click "Log Activity" or "Start Timer" above to begin tracking your 24h day.</p>
          </div>
        ) : (
          <div style={{ overflowX: 'auto' }}>
            <table className="table">
              <thead>
                <tr>
                  <th>Title</th>
                  <th>Category</th>
                  <th>Start Time</th>
                  <th>End Time</th>
                  <th>Duration</th>
                  <th style={{ textAlign: 'right' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {recentActivities.map((act) => (
                  <tr key={act.id}>
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
                    <td style={{ color: 'var(--color-text-secondary)', fontSize: '0.875rem' }}>
                      {new Date(act.startTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', hour12: false })}
                    </td>
                    <td style={{ color: 'var(--color-text-secondary)', fontSize: '0.875rem' }}>
                      {new Date(act.endTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', hour12: false })}
                    </td>
                    <td style={{ fontWeight: '600', color: 'var(--color-text)' }}>
                      {formatMinutes(act.durationMinutes)}
                    </td>
                    <td style={{ textAlign: 'right' }}>
                      <button
                        className="btn btn-secondary"
                        style={{ padding: '4px 10px', fontSize: '0.8rem', marginRight: '6px' }}
                        onClick={() => handleEditActivity(act)}
                      >
                        Edit
                      </button>
                      <button
                        className="btn btn-danger"
                        style={{ padding: '4px 10px', fontSize: '0.8rem' }}
                        onClick={() => handleDeleteActivity(act.id)}
                      >
                        Delete
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Activity Modal */}
      <ActivityForm
        isOpen={isActivityModalOpen}
        onClose={() => {
          setIsActivityModalOpen(false);
          setEditingActivity(null);
        }}
        activity={editingActivity}
        onSuccess={handleActivitySaved}
      />

      {/* Start Timer Modal */}
      <StartTimerModal
        isOpen={isTimerModalOpen}
        onClose={() => setIsTimerModalOpen(false)}
        onTimerStarted={fetchDashboardData}
      />
    </div>
  );
}
