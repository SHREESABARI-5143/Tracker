import React, { useState, useEffect } from 'react';
import { activityApi, categoryApi } from '../services/api';
import ActivityForm from '../components/ActivityForm';
import {
  Search,
  Filter,
  Plus,
  Calendar,
  Clock,
  CheckCircle2,
  Trash2,
  Edit2,
  X,
  ChevronLeft,
  ChevronRight
} from 'lucide-react';

export default function Activities() {
  const [activities, setActivities] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);

  // Filters
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');

  // Pagination
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);

  // Modals
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingActivity, setEditingActivity] = useState(null);

  const fetchCategories = async () => {
    try {
      const res = await categoryApi.getAll();
      const list = res.data?.data || res.data || [];
      setCategories(Array.isArray(list) ? list : []);
    } catch (err) {
      console.error('Failed to load categories:', err);
    }
  };

  const fetchActivities = async () => {
    try {
      setLoading(true);
      const params = {
        page,
        size: 15,
        search: searchTerm || undefined,
        categoryId: selectedCategory || undefined,
        startDate: startDate || undefined,
        endDate: endDate || undefined
      };

      const res = await activityApi.getAll(params);
      const d = res.data?.data || res.data;
      if (d?.content) {
        setActivities(d.content);
        setTotalPages(d.totalPages || 1);
        setTotalElements(d.totalElements || 0);
      } else if (Array.isArray(d)) {
        setActivities(d);
        setTotalPages(1);
        setTotalElements(d.length);
      } else {
        setActivities([]);
      }
    } catch (err) {
      console.error('Failed to fetch activities:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCategories();
  }, []);

  useEffect(() => {
    fetchActivities();
  }, [page, selectedCategory, startDate, endDate]);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    setPage(0);
    fetchActivities();
  };

  const handleClearFilters = () => {
    setSearchTerm('');
    setSelectedCategory('');
    setStartDate('');
    setEndDate('');
    setPage(0);
  };

  const handleEdit = (act) => {
    setEditingActivity(act);
    setIsModalOpen(true);
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this activity?')) return;
    try {
      await activityApi.delete(id);
      fetchActivities();
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
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 'var(--spacing-md)' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: '800' }}>Activities</h1>
          <p style={{ color: 'var(--color-text-secondary)', fontSize: '0.925rem' }}>
            Browse, filter, and manage all your tracked time entries
          </p>
        </div>
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

      {/* Filter Toolbar Card */}
      <div className="card" style={{ padding: 'var(--spacing-lg)' }}>
        <form onSubmit={handleSearchSubmit} style={{ display: 'flex', flexWrap: 'wrap', gap: 'var(--spacing-md)', alignItems: 'flex-end' }}>
          {/* Search text */}
          <div style={{ flex: '1 1 200px' }}>
            <label className="form-label">Search Activity</label>
            <div style={{ position: 'relative' }}>
              <input
                type="text"
                className="form-input"
                style={{ paddingLeft: '36px' }}
                placeholder="Title, description, notes..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
              <Search size={16} style={{ position: 'absolute', left: '10px', top: '50%', transform: 'translateY(-50%)', color: 'var(--color-text-muted)' }} />
            </div>
          </div>

          {/* Category filter */}
          <div style={{ flex: '1 1 180px' }}>
            <label className="form-label">Category</label>
            <select
              className="form-input"
              value={selectedCategory}
              onChange={(e) => {
                setSelectedCategory(e.target.value);
                setPage(0);
              }}
            >
              <option value="">All Categories</option>
              {categories.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </select>
          </div>

          {/* Date range start */}
          <div style={{ flex: '1 1 150px' }}>
            <label className="form-label">From Date</label>
            <input
              type="date"
              className="form-input"
              value={startDate}
              onChange={(e) => {
                setStartDate(e.target.value);
                setPage(0);
              }}
            />
          </div>

          {/* Date range end */}
          <div style={{ flex: '1 1 150px' }}>
            <label className="form-label">To Date</label>
            <input
              type="date"
              className="form-input"
              value={endDate}
              onChange={(e) => {
                setEndDate(e.target.value);
                setPage(0);
              }}
            />
          </div>

          <div style={{ display: 'flex', gap: 'var(--spacing-xs)' }}>
            <button type="submit" className="btn btn-secondary">
              <Search size={16} />
              <span>Filter</span>
            </button>
            {(searchTerm || selectedCategory || startDate || endDate) && (
              <button
                type="button"
                className="btn btn-secondary"
                style={{ color: 'var(--color-danger)' }}
                onClick={handleClearFilters}
                title="Clear all filters"
              >
                <X size={16} />
              </button>
            )}
          </div>
        </form>
      </div>

      {/* Activities Table Card */}
      <div className="card" style={{ padding: 'var(--spacing-lg)' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--spacing-md)' }}>
          <span style={{ fontSize: '0.9rem', color: 'var(--color-text-secondary)' }}>
            Showing <strong>{activities.length}</strong> of <strong>{totalElements}</strong> entries
          </span>
        </div>

        {activities.length === 0 ? (
          <div style={{ textAlign: 'center', padding: 'var(--spacing-2xl) 0', color: 'var(--color-text-muted)' }}>
            <Clock size={40} style={{ margin: '0 auto var(--spacing-sm)', opacity: 0.3 }} />
            <p style={{ fontSize: '1rem', fontWeight: '500' }}>No matching activities found.</p>
            <p style={{ fontSize: '0.875rem' }}>Try modifying your search criteria or add a new activity.</p>
          </div>
        ) : (
          <div style={{ overflowX: 'auto' }}>
            <table className="table">
              <thead>
                <tr>
                  <th>Date</th>
                  <th>Interval</th>
                  <th>Duration</th>
                  <th>Activity Title</th>
                  <th>Category</th>
                  <th>Productive?</th>
                  <th>Description</th>
                  <th style={{ textAlign: 'right' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {activities.map((act) => {
                  const sDate = new Date(act.startTime).toLocaleDateString();
                  const sTime = new Date(act.startTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', hour12: false });
                  const eTime = new Date(act.endTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', hour12: false });

                  return (
                    <tr key={act.id}>
                      <td style={{ color: 'var(--color-text-secondary)', fontSize: '0.85rem' }}>
                        {sDate}
                      </td>
                      <td style={{ fontWeight: '600', fontFamily: 'monospace' }}>
                        {sTime} – {eTime}
                      </td>
                      <td style={{ fontWeight: '600', color: 'var(--color-text)' }}>
                        {formatMinutes(act.durationMinutes)}
                      </td>
                      <td style={{ fontWeight: '600' }}>{act.title}</td>
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
                        {act.isProductive ? (
                          <span style={{ display: 'inline-flex', alignItems: 'center', gap: '4px', color: 'var(--color-success)', fontSize: '0.85rem' }}>
                            <CheckCircle2 size={14} /> Yes
                          </span>
                        ) : (
                          <span style={{ color: 'var(--color-text-muted)', fontSize: '0.85rem' }}>No</span>
                        )}
                      </td>
                      <td style={{ color: 'var(--color-text-secondary)', fontSize: '0.85rem', maxWidth: '220px', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                        {act.description || '-'}
                      </td>
                      <td style={{ textAlign: 'right' }}>
                        <button
                          className="btn btn-secondary"
                          style={{ padding: '6px 10px', marginRight: '6px' }}
                          onClick={() => handleEdit(act)}
                          title="Edit Activity"
                        >
                          <Edit2 size={14} />
                        </button>
                        <button
                          className="btn btn-danger"
                          style={{ padding: '6px 10px' }}
                          onClick={() => handleDelete(act.id)}
                          title="Delete Activity"
                        >
                          <Trash2 size={14} />
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}

        {/* Pagination Controls */}
        {totalPages > 1 && (
          <div style={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            marginTop: 'var(--spacing-lg)',
            paddingTop: 'var(--spacing-md)',
            borderTop: '1px solid var(--color-border)'
          }}>
            <button
              className="btn btn-secondary"
              disabled={page <= 0}
              onClick={() => setPage((p) => Math.max(0, p - 1))}
            >
              <ChevronLeft size={16} />
              <span>Previous</span>
            </button>
            <span style={{ fontSize: '0.875rem', color: 'var(--color-text-secondary)' }}>
              Page {page + 1} of {totalPages}
            </span>
            <button
              className="btn btn-secondary"
              disabled={page >= totalPages - 1}
              onClick={() => setPage((p) => p + 1)}
            >
              <span>Next</span>
              <ChevronRight size={16} />
            </button>
          </div>
        )}
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
          fetchActivities();
        }}
      />
    </div>
  );
}
