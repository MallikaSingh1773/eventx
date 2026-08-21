import React, { useState, useEffect } from 'react';
import { useLocation, useNavigate, useParams, Link } from 'react-router-dom';
import { Check, Calendar, MapPin, Ticket, Loader2, AlertTriangle, ArrowRight, Download } from 'lucide-react';
import bookingService from '../../services/bookingService';
import ticketService from '../../services/ticketService';
import { formatCurrency, formatDate } from '../../utils/helpers';

const BookingSuccess = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { id: urlBookingId } = useParams();
  
  const bookingId = location.state?.bookingId || urlBookingId;
  
  const [booking, setBooking] = useState(null);
  const [tickets, setTickets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!bookingId) {
      navigate('/');
      return;
    }

    const fetchBookingDetails = async () => {
      try {
        setLoading(true);
        const [bookingRes, ticketsRes] = await Promise.all([
          bookingService.getBooking(bookingId),
          ticketService.getTicketsForBooking(bookingId)
        ]);

        if (bookingRes.success) setBooking(bookingRes.data);
        if (ticketsRes.success) setTickets(ticketsRes.data);
      } catch (err) {
        setError('Failed to load booking details.');
        console.error(err);
      } finally {
        setLoading(false);
      }
    };

    fetchBookingDetails();
  }, [bookingId, navigate]);

  if (loading) {
    return (
      <div style={styles.centerContainer}>
        <Loader2 className="spinner" size={48} color="var(--primary)" />
        <p style={{ marginTop: '1rem', color: 'var(--text-secondary)' }}>Loading booking details...</p>
        <style>{`.spinner { animation: spin 1s linear infinite; } @keyframes spin { 100% { transform: rotate(360deg); } }`}</style>
      </div>
    );
  }

  if (error || !booking) {
    return (
      <div style={styles.centerContainer}>
        <AlertTriangle size={48} color="var(--danger)" />
        <h2 style={{ marginTop: '1rem' }}>Oops!</h2>
        <p style={{ color: 'var(--text-secondary)', marginBottom: '1.5rem' }}>{error || 'Booking not found'}</p>
        <button style={styles.primaryBtn} onClick={() => navigate('/events')}>Return to Events</button>
      </div>
    );
  }

  return (
    <div style={styles.container}>
      <div style={styles.successHeader}>
        <div style={styles.successIconWrapper}>
          <Check size={48} color="white" />
        </div>
        <h1 style={styles.successTitle}>Booking Confirmed!</h1>
        <p style={styles.successSub}>Thank you for your purchase. Your tickets have been sent to your email.</p>
      </div>

      <div style={styles.contentGrid}>
        {/* Booking Summary */}
        <div style={styles.card}>
          <h2 style={styles.cardTitle}>Booking Summary</h2>
          
          <div style={styles.summaryItem}>
            <div style={styles.summaryLabel}>Booking ID</div>
            <div style={{ ...styles.summaryValue, fontFamily: 'monospace', color: 'var(--primary-light)' }}>{booking.id}</div>
          </div>
          
          <div style={styles.summaryItem}>
            <div style={styles.summaryLabel}>Event</div>
            <div style={styles.summaryValue}>{booking.event?.title || 'Event Name'}</div>
          </div>

          <div style={styles.summaryItem}>
            <div style={styles.summaryLabel}>Date</div>
            <div style={styles.summaryValue}>
              <Calendar size={16} style={{display: 'inline', marginRight: '5px', verticalAlign: 'text-bottom'}} /> 
              {formatDate(booking.event?.eventDate || new Date())}
            </div>
          </div>

          <div style={styles.summaryItem}>
            <div style={styles.summaryLabel}>Total Amount Paid</div>
            <div style={{ ...styles.summaryValue, fontWeight: 'bold', fontSize: '1.2rem' }}>
              {formatCurrency(booking.totalAmount)}
            </div>
          </div>
        </div>

        {/* Digital Tickets */}
        <div style={styles.ticketsSection}>
          <h2 style={styles.sectionTitle}><Ticket size={24} /> Your Digital Tickets</h2>
          
          <div style={styles.ticketsList}>
            {tickets.map((ticket, idx) => (
              <div key={ticket.id || idx} style={styles.ticketCard}>
                <div style={styles.ticketLeft}>
                  <div style={styles.ticketEventName}>{booking.event?.title || 'Event'}</div>
                  <div style={styles.ticketInfoRow}>
                    <MapPin size={16} color="var(--primary)" />
                    <span>{booking.event?.venue?.name || 'Venue'}</span>
                  </div>
                  <div style={styles.ticketDetailsGrid}>
                    <div>
                      <div style={styles.ticketLabel}>Category</div>
                      <div style={styles.ticketValue}>{ticket.ticketCategory?.name || 'Standard'}</div>
                    </div>
                    <div>
                      <div style={styles.ticketLabel}>Seat</div>
                      <div style={styles.ticketValue}>{ticket.seat?.seatNumber || 'N/A'}</div>
                    </div>
                  </div>
                  <div style={styles.ticketCode}>Code: {ticket.ticketCode}</div>
                </div>
                
                <div style={styles.ticketRight}>
                  {ticket.qrCodeData ? (
                    <img src={ticket.qrCodeData} alt="QR Code" style={styles.qrCode} />
                  ) : (
                    <div style={styles.qrPlaceholder}>QR Code</div>
                  )}
                  <button style={styles.downloadBtn}>
                    <Download size={14} /> Save
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      <div style={styles.actions}>
        <button style={styles.secondaryBtn} onClick={() => navigate('/profile/bookings')}>
          View My Bookings
        </button>
        <button style={styles.primaryBtn} onClick={() => navigate('/events')}>
          Browse More Events <ArrowRight size={18} />
        </button>
      </div>
    </div>
  );
};

const styles = {
  centerContainer: { display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', minHeight: '60vh', textAlign: 'center' },
  container: { maxWidth: '900px', margin: '0 auto', padding: '3rem 1rem' },
  successHeader: { display: 'flex', flexDirection: 'column', alignItems: 'center', textAlign: 'center', marginBottom: '3rem' },
  successIconWrapper: { width: '96px', height: '96px', background: 'var(--success)', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', marginBottom: '1.5rem', boxShadow: '0 0 30px rgba(16, 185, 129, 0.3)' },
  successTitle: { fontSize: '2.5rem', color: 'var(--text-primary)', margin: '0 0 0.5rem 0' },
  successSub: { color: 'var(--text-secondary)', fontSize: '1.1rem' },
  contentGrid: { display: 'grid', gridTemplateColumns: '1fr', gap: '2rem', marginBottom: '3rem', '@media (min-width: 768px)': { gridTemplateColumns: '1fr 2fr' } },
  card: { background: 'var(--bg-surface)', border: '1px solid var(--border)', borderRadius: '12px', padding: '2rem', height: 'fit-content' },
  cardTitle: { fontSize: '1.25rem', color: 'var(--text-primary)', borderBottom: '1px solid var(--border)', paddingBottom: '1rem', marginBottom: '1.5rem' },
  summaryItem: { display: 'flex', flexDirection: 'column', gap: '0.25rem', marginBottom: '1.25rem' },
  summaryLabel: { fontSize: '0.85rem', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '1px' },
  summaryValue: { fontSize: '1.1rem', color: 'var(--text-primary)' },
  ticketsSection: { display: 'flex', flexDirection: 'column', gap: '1.5rem' },
  sectionTitle: { display: 'flex', alignItems: 'center', gap: '0.75rem', fontSize: '1.5rem', color: 'var(--text-primary)', margin: 0 },
  ticketsList: { display: 'flex', flexDirection: 'column', gap: '1rem' },
  ticketCard: { display: 'flex', background: 'var(--bg-surface)', border: '1px dashed var(--primary-dark)', borderRadius: '12px', overflow: 'hidden' },
  ticketLeft: { flex: 1, padding: '1.5rem', borderRight: '2px dashed var(--bg-primary)' },
  ticketEventName: { fontSize: '1.25rem', fontWeight: 'bold', color: 'var(--text-primary)', marginBottom: '0.5rem' },
  ticketInfoRow: { display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--text-secondary)', marginBottom: '1.5rem', fontSize: '0.9rem' },
  ticketDetailsGrid: { display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginBottom: '1.5rem', background: 'var(--bg-secondary)', padding: '1rem', borderRadius: '8px' },
  ticketLabel: { fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '0.25rem' },
  ticketValue: { fontWeight: 'bold', color: 'var(--text-primary)' },
  ticketCode: { fontFamily: 'monospace', color: 'var(--text-muted)', fontSize: '0.9rem' },
  ticketRight: { display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', padding: '1.5rem', background: 'rgba(124, 58, 237, 0.05)', minWidth: '150px' },
  qrCode: { width: '100px', height: '100px', background: 'white', padding: '4px', borderRadius: '8px', marginBottom: '1rem' },
  qrPlaceholder: { width: '100px', height: '100px', background: 'var(--bg-secondary)', borderRadius: '8px', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--text-muted)', marginBottom: '1rem', border: '1px solid var(--border)' },
  downloadBtn: { display: 'flex', alignItems: 'center', gap: '0.5rem', background: 'var(--bg-secondary)', color: 'var(--text-primary)', border: '1px solid var(--border)', padding: '0.5rem 1rem', borderRadius: '4px', cursor: 'pointer', fontSize: '0.85rem', transition: 'background 0.2s' },
  actions: { display: 'flex', gap: '1rem', justifyContent: 'center', flexWrap: 'wrap' },
  primaryBtn: { display: 'flex', alignItems: 'center', gap: '0.5rem', background: 'var(--primary)', color: 'white', border: 'none', padding: '1rem 2rem', borderRadius: '8px', fontSize: '1.1rem', fontWeight: 'bold', cursor: 'pointer', transition: 'background 0.2s' },
  secondaryBtn: { display: 'flex', alignItems: 'center', gap: '0.5rem', background: 'var(--bg-surface)', color: 'var(--text-primary)', border: '1px solid var(--border)', padding: '1rem 2rem', borderRadius: '8px', fontSize: '1.1rem', fontWeight: 'bold', cursor: 'pointer', transition: 'background 0.2s' }
};

export default BookingSuccess;
