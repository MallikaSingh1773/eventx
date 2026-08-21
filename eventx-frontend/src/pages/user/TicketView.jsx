import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import ticketService from '../../services/ticketService';
import { Printer, ArrowLeft, Loader } from 'lucide-react';
import './TicketView.css';

const TicketView = () => {
  const { bookingId } = useParams();
  const navigate = useNavigate();
  
  const [tickets, setTickets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchTickets = async () => {
      try {
        setLoading(true);
        const response = await ticketService.getTicketsForBooking(bookingId);
        setTickets(response.data?.data || []);
      } catch (err) {
        setError('Failed to load tickets. They may not be available yet.');
        console.error(err);
      } finally {
        setLoading(false);
      }
    };

    if (bookingId) {
      fetchTickets();
    }
  }, [bookingId]);

  const handlePrint = () => {
    window.print();
  };

  if (loading) {
    return (
      <div className="ticket-view-container loading-state">
        <Loader size={48} className="spinner" />
        <p>Loading your tickets...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="ticket-view-container error-state">
        <p className="auth-error">{error}</p>
        <button className="btn-back" onClick={() => navigate('/my-bookings')}>
          <ArrowLeft size={16} /> Back to Bookings
        </button>
      </div>
    );
  }

  return (
    <div className="ticket-view-container">
      <div className="ticket-header">
        <button className="btn-back" onClick={() => navigate('/my-bookings')}>
          <ArrowLeft size={20} /> Back to Bookings
        </button>
        <button className="btn-print" onClick={handlePrint}>
          <Printer size={20} /> Print Tickets
        </button>
      </div>

      <div className="tickets-list">
        {tickets.map(ticket => (
          <div key={ticket.id} className="ticket-wrapper">
            <div className="ticket-left">
              <div className="ticket-status">{ticket.status}</div>
              
              <h2 className="event-title">{ticket.eventTitle}</h2>
              
              <div className="event-details-grid">
                <div className="detail-block">
                  <span className="detail-label">Date & Time</span>
                  <span className="detail-value">
                    {new Date(ticket.eventDate).toLocaleString([], {
                      weekday: 'short',
                      year: 'numeric',
                      month: 'short',
                      day: 'numeric',
                      hour: '2-digit',
                      minute: '2-digit'
                    })}
                  </span>
                </div>
                
                <div className="detail-block">
                  <span className="detail-label">Venue</span>
                  <span className="detail-value">{ticket.venueName}</span>
                </div>

                <div className="detail-block">
                  <span className="detail-label">Category</span>
                  <span className="detail-value">{ticket.ticketCategoryName || ticket.seatCategory}</span>
                </div>

                <div className="detail-block">
                  <span className="detail-label">Booking ID</span>
                  <span className="detail-value">#{ticket.bookingId}</span>
                </div>
              </div>

              <div className="ticket-footer">
                <div className="attendee-info">
                  <span className="detail-label">Attendee</span>
                  <span className="detail-value">{ticket.attendeeName}</span>
                  <span className="detail-label" style={{textTransform: 'none', marginTop: '4px'}}>{ticket.attendeeEmail}</span>
                </div>
                <div className="ticket-price">
                  ₹{ticket.price.toFixed(2)}
                </div>
              </div>
            </div>

            <div className="ticket-right">
              <div className="detail-block">
                <span className="detail-label">Row {ticket.row || '-'} • Sec {ticket.section || '-'}</span>
                <div className="seat-main">{ticket.seatNumber}</div>
                <span className="detail-label">Seat</span>
              </div>

              {ticket.qrCodeData && (
                <div className="qr-code-container">
                  <img src={ticket.qrCodeData} alt="Ticket QR Code" className="qr-code-img" />
                </div>
              )}
              
              <div className="ticket-code">{ticket.ticketCode}</div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default TicketView;
