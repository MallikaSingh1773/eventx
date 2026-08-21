import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import bookingService from '../../services/bookingService';
import { Calendar, MapPin, Ticket, XCircle, Eye, AlertCircle } from 'lucide-react';
import './MyBookings.css';

const MyBookings = () => {
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [filter, setFilter] = useState('ALL'); // ALL, UPCOMING, COMPLETED, CANCELLED
  
  const navigate = useNavigate();

  const fetchBookings = async () => {
    try {
      setLoading(true);
      // Assuming getMyBookings returns { data: { content: [...] } } via ApiResponse
      const response = await bookingService.getMyBookings({ size: 50 });
      setBookings(response.data?.data?.content || []);
    } catch (err) {
      setError('Failed to load bookings. Please try again later.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchBookings();
  }, []);

  const handleCancel = async (bookingId) => {
    if (!window.confirm('Are you sure you want to cancel this booking? This action cannot be undone.')) {
      return;
    }
    
    try {
      await bookingService.cancelBooking(bookingId);
      fetchBookings(); // Refresh list
    } catch (err) {
      alert('Failed to cancel booking: ' + (err.response?.data?.message || err.message));
    }
  };

  const getFilteredBookings = () => {
    if (filter === 'ALL') return bookings;
    
    const now = new Date();
    return bookings.filter(booking => {
      const eventDate = new Date(booking.eventDate);
      if (filter === 'CANCELLED') return booking.status === 'CANCELLED';
      if (filter === 'UPCOMING') return eventDate > now && booking.status !== 'CANCELLED';
      if (filter === 'COMPLETED') return eventDate <= now && booking.status !== 'CANCELLED';
      return true;
    });
  };

  const filteredBookings = getFilteredBookings();

  return (
    <div className="bookings-container">
      <div className="bookings-header">
        <h1>My Bookings</h1>
        <div className="bookings-tabs">
          {['ALL', 'UPCOMING', 'COMPLETED', 'CANCELLED'].map(tab => (
            <button 
              key={tab}
              className={`tab-btn ${filter === tab ? 'active' : ''}`}
              onClick={() => setFilter(tab)}
            >
              {tab.charAt(0) + tab.slice(1).toLowerCase()}
            </button>
          ))}
        </div>
      </div>

      {error && <div className="auth-error">{error}</div>}

      <div className="bookings-list">
        {loading ? (
          <>
            <div className="loading-skeleton"></div>
            <div className="loading-skeleton"></div>
            <div className="loading-skeleton"></div>
          </>
        ) : filteredBookings.length === 0 ? (
          <div className="empty-state">
            <Ticket size={48} />
            <h3>No bookings found</h3>
            <p>You have no {filter !== 'ALL' ? filter.toLowerCase() : ''} bookings yet. Start exploring events!</p>
          </div>
        ) : (
          filteredBookings.map(booking => {
            const eventDate = new Date(booking.eventDate);
            const isUpcoming = eventDate > new Date();
            const canCancel = (booking.status === 'PENDING' || booking.status === 'CONFIRMED') && 
                              (eventDate.getTime() - new Date().getTime() > 24 * 60 * 60 * 1000);

            return (
              <div key={booking.id} className="booking-card">
                <div className="booking-info">
                  <h3 className="booking-title">{booking.eventTitle}</h3>
                  <div className="booking-details">
                    <span className="detail-item">
                      <Calendar size={16} />
                      {eventDate.toLocaleDateString()} {eventDate.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                    </span>
                    <span className="detail-item">
                      <MapPin size={16} />
                      {booking.venueName}
                    </span>
                  </div>
                  <div className="booking-seats">
                    <Ticket size={14} style={{ display: 'inline', marginRight: '4px', verticalAlign: 'middle' }}/>
                    {booking.items?.length || 0} Tickets • {booking.items?.map(item => item.seatCategory).join(', ')}
                  </div>
                </div>

                <div className="booking-actions">
                  <div className={`booking-status status-${booking.status}`}>
                    {booking.status}
                  </div>
                  <div className="price-tag">
                    ₹{booking.totalAmount.toFixed(2)}
                  </div>
                  <div className="action-buttons">
                    {booking.status === 'CONFIRMED' && (
                      <button 
                        className="btn-view"
                        onClick={() => navigate(`/tickets/${booking.id}`)}
                      >
                        <Eye size={16} /> View Tickets
                      </button>
                    )}
                    {canCancel && (
                      <button 
                        className="btn-cancel"
                        onClick={() => handleCancel(booking.id)}
                      >
                        <XCircle size={16} /> Cancel
                      </button>
                    )}
                  </div>
                </div>
              </div>
            );
          })
        )}
      </div>
    </div>
  );
};

export default MyBookings;
