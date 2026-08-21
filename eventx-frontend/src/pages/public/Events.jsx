import React, { useState, useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { Search, Filter, AlertCircle, RefreshCw } from 'lucide-react';
import eventService from '../../services/eventService';
import EventCard from '../../components/common/EventCard';
import EventCardSkeleton from '../../components/common/EventCardSkeleton';

const ALL_CATEGORIES = [
  'CONCERT', 'CONFERENCE', 'SPORTS', 'THEATER', 
  'COMEDY', 'FESTIVAL', 'WORKSHOP', 'OTHER'
];

const Events = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const navigate = useNavigate();
  
  const initialQuery = searchParams.get('search') || '';
  const initialCategory = searchParams.get('category') || '';
  
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  
  // Filter states
  const [searchQuery, setSearchQuery] = useState(initialQuery);
  const [category, setCategory] = useState(initialCategory);
  const [city, setCity] = useState('');
  const [dateFrom, setDateFrom] = useState('');
  const [dateTo, setDateTo] = useState('');

  const fetchEvents = async (pageNumber = 0) => {
    try {
      setLoading(true);
      setError(null);
      
      const queryParams = {
        page: pageNumber,
        size: 9
      };
      
      const currentQuery = searchParams.get('search') || '';
      const currentCategory = searchParams.get('category') || category;
      
      if (currentQuery) queryParams.query = currentQuery;
      if (currentCategory) queryParams.category = currentCategory;
      if (city) queryParams.city = city;
      if (dateFrom) queryParams.dateFrom = dateFrom;
      if (dateTo) queryParams.dateTo = dateTo;
      
      let response;
      if (currentQuery || currentCategory || city || dateFrom || dateTo) {
        response = await eventService.searchEvents(queryParams);
      } else {
        response = await eventService.getEvents(queryParams);
      }
      
      if (response.success) {
        setEvents(response.data.content);
        setTotalPages(response.data.totalPages);
        setPage(response.data.number);
      } else {
        setError(response.message || 'Failed to fetch events');
      }
    } catch (err) {
      setError('An error occurred while fetching events');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    setSearchQuery(searchParams.get('search') || '');
    setCategory(searchParams.get('category') || '');
    fetchEvents(0);
  }, [searchParams, city, dateFrom, dateTo]);

  const handleFilterSubmit = (e) => {
    e.preventDefault();
    const newParams = new URLSearchParams();
    if (searchQuery) newParams.set('search', searchQuery);
    if (category) newParams.set('category', category);
    setSearchParams(newParams);
  };

  const handlePageChange = (newPage) => {
    if (newPage >= 0 && newPage < totalPages) {
      fetchEvents(newPage);
    }
  };

  return (
    <div className="events-page">
      <style>{`
        .events-page {
          min-height: 100vh;
          padding: 40px 20px;
          max-width: 1200px;
          margin: 0 auto;
        }
        .page-header {
          margin-bottom: 40px;
        }
        .page-title {
          font-size: 2.5rem;
          font-weight: 700;
          color: var(--text-primary);
          margin-bottom: 24px;
        }
        
        .filters-container {
          background: var(--bg-surface);
          border: 1px solid var(--border);
          border-radius: 12px;
          padding: 24px;
          margin-bottom: 40px;
        }
        .filters-form {
          display: grid;
          grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
          gap: 20px;
          align-items: flex-end;
        }
        .filter-group {
          display: flex;
          flex-direction: column;
          gap: 8px;
        }
        .filter-label {
          font-size: 0.875rem;
          color: var(--text-secondary);
          font-weight: 500;
        }
        .filter-input {
          background: var(--bg-primary);
          border: 1px solid var(--border);
          color: var(--text-primary);
          padding: 12px 16px;
          border-radius: 8px;
          font-size: 1rem;
          outline: none;
        }
        .filter-input:focus {
          border-color: var(--primary);
        }
        .filter-btn {
          background: var(--primary);
          color: white;
          border: none;
          padding: 12px;
          border-radius: 8px;
          font-weight: 600;
          cursor: pointer;
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 8px;
          transition: background 0.2s;
          height: 46px;
        }
        .filter-btn:hover {
          background: var(--primary-light);
        }
        
        .events-grid {
          display: grid;
          grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
          gap: 30px;
        }
        
        .empty-state {
          text-align: center;
          padding: 80px 20px;
          background: var(--bg-surface);
          border-radius: 16px;
          border: 1px dashed var(--border);
        }
        .empty-icon {
          color: var(--text-muted);
          margin-bottom: 16px;
        }
        
        .error-state {
          text-align: center;
          padding: 40px 20px;
          background: rgba(239, 68, 68, 0.1);
          border-radius: 12px;
          border: 1px solid rgba(239, 68, 68, 0.3);
          color: var(--danger);
        }
        
        .pagination {
          display: flex;
          justify-content: center;
          align-items: center;
          gap: 20px;
          margin-top: 50px;
        }
        .page-btn {
          background: var(--bg-surface);
          border: 1px solid var(--border);
          color: var(--text-primary);
          padding: 8px 16px;
          border-radius: 8px;
          cursor: pointer;
        }
        .page-btn:disabled {
          opacity: 0.5;
          cursor: not-allowed;
        }
        .page-btn:not(:disabled):hover {
          background: var(--bg-surface-hover);
        }
      `}</style>

      <div className="page-header">
        <h1 className="page-title">Explore Events</h1>
        
        <div className="filters-container">
          <form className="filters-form" onSubmit={handleFilterSubmit}>
            <div className="filter-group">
              <label className="filter-label">Search</label>
              <input 
                type="text" 
                className="filter-input" 
                placeholder="Search events..." 
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
            </div>
            
            <div className="filter-group">
              <label className="filter-label">Category</label>
              <select 
                className="filter-input" 
                value={category}
                onChange={(e) => setCategory(e.target.value)}
              >
                <option value="">All Categories</option>
                {ALL_CATEGORIES.map(cat => (
                  <option key={cat} value={cat}>{cat}</option>
                ))}
              </select>
            </div>
            
            <div className="filter-group">
              <label className="filter-label">City</label>
              <input 
                type="text" 
                className="filter-input" 
                placeholder="E.g. New York" 
                value={city}
                onChange={(e) => setCity(e.target.value)}
              />
            </div>
            
            <button type="submit" className="filter-btn">
              <Search size={20} />
              Search
            </button>
          </form>
        </div>
      </div>

      {error ? (
        <div className="error-state">
          <AlertCircle size={48} style={{ margin: '0 auto 16px auto' }} />
          <h3>Something went wrong</h3>
          <p>{error}</p>
          <button 
            className="filter-btn" 
            style={{ width: 'auto', margin: '20px auto 0 auto' }}
            onClick={() => fetchEvents(page)}
          >
            <RefreshCw size={16} /> Retry
          </button>
        </div>
      ) : loading ? (
        <div className="events-grid">
          {Array.from({ length: 6 }).map((_, idx) => <EventCardSkeleton key={idx} />)}
        </div>
      ) : events.length > 0 ? (
        <>
          <div className="events-grid">
            {events.map(event => <EventCard key={event.id} event={event} />)}
          </div>
          
          {totalPages > 1 && (
            <div className="pagination">
              <button 
                className="page-btn" 
                onClick={() => handlePageChange(page - 1)}
                disabled={page === 0}
              >
                Previous
              </button>
              <span style={{ color: 'var(--text-secondary)' }}>
                Page {page + 1} of {totalPages}
              </span>
              <button 
                className="page-btn" 
                onClick={() => handlePageChange(page + 1)}
                disabled={page >= totalPages - 1}
              >
                Next
              </button>
            </div>
          )}
        </>
      ) : (
        <div className="empty-state">
          <Search size={64} className="empty-icon" />
          <h3 style={{ fontSize: '1.5rem', color: 'var(--text-primary)', marginBottom: '8px' }}>No events found</h3>
          <p style={{ color: 'var(--text-secondary)' }}>We couldn't find any events matching your criteria. Try adjusting your filters.</p>
        </div>
      )}
    </div>
  );
};

export default Events;
