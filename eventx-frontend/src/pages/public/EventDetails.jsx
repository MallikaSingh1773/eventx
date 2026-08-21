import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Calendar, Clock, MapPin, Info, ChevronLeft, AlertCircle, Loader2, ArrowRight, UserRound } from 'lucide-react';
import { toast } from 'react-toastify';
import eventService from '../../services/eventService';
import bookingService from '../../services/bookingService';
import { formatCurrency, formatDate, formatTime } from '../../utils/helpers';
import { useAuth } from '../../context/AuthContext';

const selectionStorageKey = (eventId) => `eventx:selectedSeats:${eventId}`;

const matchTicketCategory = (event, seat) => {
  const categories = event?.ticketCategories || [];
  return categories.find((c) => c.seatCategory === seat.category)
    || categories.find((c) => c.name === seat.category || c.name === seat.section)
    || categories.find((c) => c.seatCategory === seat.section)
    || categories[0];
};

const EventDetails = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  
  const [event, setEvent] = useState(null);
  const [seats, setSeats] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedSeats, setSelectedSeats] = useState(() => {
    try {
      const saved = JSON.parse(sessionStorage.getItem(selectionStorageKey(id)) || '[]');
      return Array.isArray(saved) ? saved : [];
    } catch {
      return [];
    }
  });
  const [bookingLoading, setBookingLoading] = useState(false);

  useEffect(() => {
    const fetchEventDetails = async () => {
      try {
        setLoading(true);
        const [eventRes, seatsRes] = await Promise.all([
          eventService.getEvent(id),
          eventService.getSeats(id)
        ]);
        
        if (eventRes.success) setEvent(eventRes.data);
        if (seatsRes.success) setSeats(seatsRes.data);
      } catch (err) {
        setError('Failed to load event details. Please try again later.');
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    
    fetchEventDetails();
    try {
      const saved = JSON.parse(sessionStorage.getItem(selectionStorageKey(id)) || '[]');
      setSelectedSeats(Array.isArray(saved) ? saved : []);
    } catch {
      setSelectedSeats([]);
    }
  }, [id]);

  useEffect(() => {
    if (!id) return;
    sessionStorage.setItem(selectionStorageKey(id), JSON.stringify(selectedSeats));
  }, [selectedSeats]);

  const handleSeatClick = (seat) => {
    if (!seat.available) {
      toast.info('That seat is already booked.');
      return;
    }
    
    setSelectedSeats(prev => {
      const isSelected = prev.some(s => String(s.id) === String(seat.id));
      if (isSelected) {
        return prev.filter(s => String(s.id) !== String(seat.id));
      }

      const category = matchTicketCategory(event, seat);
      if (!category) {
        toast.error('No ticket category is available for this seat.');
        return prev;
      }
        
      return [...prev, { ...seat, price: Number(category.price), ticketCategoryId: category.id }];
    });
  };

  const handleLockSeats = async () => {
    if (selectedSeats.length === 0) {
      toast.info('Select at least one seat from the map first.');
      return;
    }

    if (!user) {
      navigate('/login', { state: { from: `/events/${id}` } });
      return;
    }

    try {
      setBookingLoading(true);
      const payload = {
        eventId: Number(id),
        seats: selectedSeats.map(s => ({
          seatId: Number(s.id),
          ticketCategoryId: Number(s.ticketCategoryId)
        }))
      };

      const res = await bookingService.lockSeats(payload);
      if (res.success) {
        const data = res.data;
        sessionStorage.removeItem(selectionStorageKey(id));
        navigate('/checkout', {
          state: {
            bookingId: data.bookingId,
            lockedUntil: data.lockedUntil,
            ttlSeconds: data.ttlSeconds,
            totalAmount: data.totalAmount,
            eventTitle: event.title,
            eventDate: event.eventDate,
            items: data.items || selectedSeats
          }
        });
      } else {
        toast.error(res.message || 'Failed to lock seats.');
      }
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to lock seats. They might have been taken.');
      const seatsRes = await eventService.getSeats(id);
      if (seatsRes.success) setSeats(seatsRes.data);
    } finally {
      setBookingLoading(false);
    }
  };

  if (loading) {
    return (
      <div style={styles.loadingContainer}>
        <Loader2 className="spinner" size={48} color="var(--primary)" />
        <p style={{ marginTop: '1rem', color: 'var(--text-secondary)' }}>Loading event details...</p>
      </div>
    );
  }

  if (error || !event) {
    return (
      <div style={styles.errorContainer}>
        <AlertCircle size={48} color="var(--danger)" />
        <h2 style={{ marginTop: '1rem', color: 'var(--text-primary)' }}>Oops!</h2>
        <p style={{ color: 'var(--text-secondary)', marginBottom: '1.5rem' }}>{error || 'Event not found'}</p>
        <button style={styles.primaryBtn} onClick={() => navigate('/events')}>Back to Events</button>
      </div>
    );
  }

  // Group seats by section and row
  const groupedSeats = seats.reduce((acc, seat) => {
    if (!acc[seat.section]) acc[seat.section] = {};
    if (!acc[seat.section][seat.row]) acc[seat.section][seat.row] = [];
    acc[seat.section][seat.row].push(seat);
    return acc;
  }, {});

  const totalSelectedPrice = selectedSeats.reduce((sum, seat) => sum + seat.price, 0);

  return (
    <>
      <style>{`
        .spinner { animation: spin 1s linear infinite; }
        @keyframes spin { 100% { transform: rotate(360deg); } }
        .seat { transition: all 0.2s; cursor: pointer; display: flex; align-items: center; justify-content: center; font-size: 0.7rem; font-weight: bold; border-radius: 4px; }
        .seat:hover:not(.unavailable) { transform: scale(1.1); box-shadow: 0 0 8px var(--primary); }
        .seat.available { background: var(--bg-surface-hover); border: 1px solid var(--border); color: var(--text-primary); }
        .seat.selected { background: var(--primary); color: white; border: 1px solid var(--primary-light); }
        .seat.unavailable { background: #333; color: #555; cursor: not-allowed; border: 1px solid #222; }
        .category-row { display: grid; grid-template-columns: 2fr 1fr 1fr 1fr; gap: 1rem; padding: 1rem; border-bottom: 1px solid var(--border); align-items: center; }
        .event-layout { display: grid; grid-template-columns: 1fr; gap: 2rem; }
        @media (min-width: 992px) {
          .event-layout { grid-template-columns: 2fr 1fr; align-items: start; }
        }
      `}</style>
      
      <div style={styles.container}>
        <div style={styles.header}>
          <button style={styles.backBtn} onClick={() => navigate('/events')}>
            <ChevronLeft size={20} /> Back to Events
          </button>
        </div>

        <div style={styles.banner}>
          {event.imageUrl ? (
            <img src={event.imageUrl} alt={event.title} style={styles.bannerImg} />
          ) : (
            <div style={styles.bannerPlaceholder}>
              <h1>{event.title}</h1>
            </div>
          )}
        </div>

        <div className="event-layout">
          {/* Main Content */}
          <div style={styles.mainContent}>
            <div style={styles.card}>
              <div style={styles.badge}>{event.category}</div>
              <h1 style={styles.title}>{event.title}</h1>
              <p style={styles.description}>{event.description}</p>
            </div>

            <div style={styles.card}>
              <h2 style={styles.sectionTitle}>Ticket Categories</h2>
              <div style={{ background: 'var(--bg-secondary)', borderRadius: '8px', overflow: 'hidden' }}>
                <div style={{ ...styles.categoryHeader, display: 'grid', gridTemplateColumns: '2fr 1fr 1fr 1fr', gap: '1rem', padding: '1rem', fontWeight: 'bold' }}>
                  <span>Category</span>
                  <span>Price</span>
                  <span>Available</span>
                  <span>Zone</span>
                </div>
                {event.ticketCategories.map(cat => (
                  <div key={cat.id} className="category-row">
                    <div>{cat.name}</div>
                    <div style={{ color: 'var(--primary-light)', fontWeight: 'bold' }}>{formatCurrency(cat.price)}</div>
                    <div>{cat.availableSeats} / {cat.totalSeats}</div>
                    <div style={{ color: 'var(--text-muted)' }}>{cat.seatCategory}</div>
                  </div>
                ))}
              </div>
            </div>

            <div style={styles.card}>
              <h2 style={styles.sectionTitle}>Select Seats</h2>
              
              <div style={styles.legend}>
                <div style={styles.legendItem}><div className="seat available" style={styles.legendBox}></div> Available</div>
                <div style={styles.legendItem}><div className="seat selected" style={styles.legendBox}></div> Selected</div>
                <div style={styles.legendItem}><div className="seat unavailable" style={styles.legendBox}></div> Booked</div>
              </div>

              <div style={styles.theatre}>
                <div style={styles.stage}>STAGE</div>
                
                {Object.entries(groupedSeats).map(([section, rows]) => (
                  <div key={section} style={styles.section}>
                    <h3 style={styles.sectionLabel}>{section}</h3>
                    {Object.entries(rows).map(([row, rowSeats]) => (
                      <div key={row} style={styles.row}>
                        <div style={styles.rowLabel}>{row}</div>
                        <div style={styles.seatsRow}>
                          {rowSeats.sort((a,b) => a.seatNumber.localeCompare(b.seatNumber)).map(seat => {
                            const isSelected = selectedSeats.some(s => String(s.id) === String(seat.id));
                            return (
                              <div
                                key={seat.id}
                                className={`seat ${isSelected ? 'selected' : seat.available ? 'available' : 'unavailable'}`}
                                style={styles.seatStyle}
                                onClick={() => handleSeatClick(seat)}
                                title={`${seat.section} - Row ${seat.row} - Seat ${seat.seatNumber} (${seat.category})`}
                              >
                                {seat.seatNumber}
                              </div>
                            );
                          })}
                        </div>
                      </div>
                    ))}
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* Sidebar */}
          <div style={styles.sidebar}>
            <div style={styles.card}>
              <h3 style={styles.sidebarTitle}>Event Info</h3>
              
              <div style={styles.infoRow}>
                <Calendar size={20} color="var(--primary-light)" style={styles.infoIcon} />
                <div>
                  <div style={styles.infoLabel}>Date</div>
                  <div style={styles.infoValue}>{formatDate(event.eventDate)}</div>
                </div>
              </div>
              
              <div style={styles.infoRow}>
                <Clock size={20} color="var(--primary-light)" style={styles.infoIcon} />
                <div>
                  <div style={styles.infoLabel}>Time</div>
                  <div style={styles.infoValue}>{formatTime(event.startTime)} - {formatTime(event.endTime)}</div>
                </div>
              </div>

              <div style={styles.infoRow}>
                <MapPin size={20} color="var(--primary-light)" style={styles.infoIcon} />
                <div>
                  <div style={styles.infoLabel}>Venue</div>
                  <div style={styles.infoValue}>{event.venue.name}</div>
                  <div style={styles.infoSubValue}>{event.venue.address}, {event.venue.city}</div>
                </div>
              </div>

              {event.organizerName && (
                <div style={styles.infoRow}>
                  <UserRound size={20} color="var(--primary-light)" style={styles.infoIcon} />
                  <div>
                    <div style={styles.infoLabel}>Organizer</div>
                    <div style={styles.infoValue}>{event.organizerName}</div>
                  </div>
                </div>
              )}
            </div>

            <div style={{...styles.card, position: 'sticky', top: '2rem'}}>
              <h3 style={styles.sidebarTitle}>Your Selection</h3>
              
              {selectedSeats.length === 0 ? (
                <div style={styles.emptySelection}>
                  <Info size={24} color="var(--text-muted)" style={{marginBottom: '0.5rem'}} />
                  <p>Select seats from the map to proceed</p>
                </div>
              ) : (
                <div style={styles.selectionList}>
                  {selectedSeats.map(seat => (
                    <div key={seat.id} style={styles.selectionItem}>
                      <div>
                        <div style={{fontWeight: 'bold', color: 'var(--text-primary)'}}>
                          {seat.section} - {seat.row}{seat.seatNumber}
                        </div>
                        <div style={{fontSize: '0.8rem', color: 'var(--text-muted)'}}>{seat.category}</div>
                      </div>
                      <div style={{fontWeight: 'bold'}}>{formatCurrency(seat.price)}</div>
                    </div>
                  ))}
                  
                  <div style={styles.totalRow}>
                    <span>Total ({selectedSeats.length} seats)</span>
                    <span style={{color: 'var(--primary-light)', fontSize: '1.2rem'}}>{formatCurrency(totalSelectedPrice)}</span>
                  </div>
                </div>
              )}

              {!user && (
                <div style={styles.loginPrompt}>
                  You will be asked to login before booking.
                </div>
              )}

              <button 
                style={{
                  ...styles.primaryBtn,
                  opacity: selectedSeats.length === 0 ? 0.6 : 1,
                  cursor: selectedSeats.length === 0 || bookingLoading ? 'not-allowed' : 'pointer'
                }} 
                onClick={handleLockSeats}
                disabled={bookingLoading}
              >
                {bookingLoading ? (
                  <><Loader2 className="spinner" size={20} /> Locking...</>
                ) : (
                  <>Lock Seats & Proceed <ArrowRight size={20} /></>
                )}
              </button>
            </div>
          </div>
        </div>
      </div>
    </>
  );
};

const styles = {
  loadingContainer: { display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', minHeight: '60vh' },
  errorContainer: { display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', minHeight: '60vh', textAlign: 'center', padding: '2rem' },
  container: { maxWidth: '1200px', margin: '0 auto', padding: '2rem 1rem' },
  header: { marginBottom: '1.5rem' },
  backBtn: { display: 'flex', alignItems: 'center', gap: '0.5rem', background: 'none', border: 'none', color: 'var(--text-secondary)', cursor: 'pointer', fontSize: '1rem', padding: 0 },
  banner: { width: '100%', height: '300px', borderRadius: '16px', overflow: 'hidden', marginBottom: '2rem', background: 'var(--bg-surface)' },
  bannerImg: { width: '100%', height: '100%', objectFit: 'cover' },
  bannerPlaceholder: { width: '100%', height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', background: 'linear-gradient(135deg, var(--bg-surface) 0%, var(--primary-dark) 100%)', color: 'var(--text-primary)' },
  mainContent: { display: 'flex', flexDirection: 'column', gap: '2rem' },
  sidebar: { display: 'flex', flexDirection: 'column', gap: '2rem' },
  card: { background: 'var(--bg-surface)', borderRadius: '12px', padding: '1.5rem', border: '1px solid var(--border)' },
  badge: { display: 'inline-block', padding: '0.25rem 0.75rem', background: 'rgba(124, 58, 237, 0.2)', color: 'var(--primary-light)', borderRadius: '99px', fontSize: '0.85rem', fontWeight: 'bold', marginBottom: '1rem' },
  title: { fontSize: '2rem', margin: '0 0 1rem 0', color: 'var(--text-primary)' },
  description: { color: 'var(--text-secondary)', lineHeight: 1.6 },
  sectionTitle: { fontSize: '1.25rem', margin: '0 0 1.5rem 0', color: 'var(--text-primary)', borderBottom: '1px solid var(--border)', paddingBottom: '0.5rem' },
  categoryHeader: { background: 'var(--bg-secondary)', color: 'var(--text-secondary)' },
  legend: { display: 'flex', gap: '1.5rem', justifyContent: 'center', marginBottom: '2rem', padding: '1rem', background: 'var(--bg-secondary)', borderRadius: '8px' },
  legendItem: { display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--text-secondary)', fontSize: '0.9rem' },
  legendBox: { width: '24px', height: '24px' },
  theatre: { background: 'var(--bg-secondary)', padding: '2rem', borderRadius: '12px', overflowX: 'auto', display: 'flex', flexDirection: 'column', alignItems: 'center' },
  stage: { width: '80%', height: '40px', background: 'linear-gradient(to bottom, var(--primary-dark), transparent)', borderTopLeftRadius: '50%', borderTopRightRadius: '50%', borderTop: '2px solid var(--primary)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--primary-light)', fontWeight: 'bold', letterSpacing: '4px', marginBottom: '3rem' },
  section: { width: '100%', marginBottom: '2rem', display: 'flex', flexDirection: 'column', alignItems: 'center' },
  sectionLabel: { color: 'var(--text-muted)', letterSpacing: '2px', marginBottom: '1rem', fontSize: '0.8rem', textTransform: 'uppercase' },
  row: { display: 'flex', alignItems: 'center', marginBottom: '0.5rem', gap: '1rem' },
  rowLabel: { width: '30px', color: 'var(--text-muted)', fontWeight: 'bold', textAlign: 'right' },
  seatsRow: { display: 'flex', gap: '0.5rem' },
  seatStyle: { width: '30px', height: '30px', userSelect: 'none' },
  sidebarTitle: { fontSize: '1.1rem', margin: '0 0 1.5rem 0', color: 'var(--text-primary)' },
  infoRow: { display: 'flex', gap: '1rem', marginBottom: '1.5rem', alignItems: 'flex-start' },
  infoIcon: { marginTop: '2px' },
  infoLabel: { fontSize: '0.85rem', color: 'var(--text-muted)', marginBottom: '0.2rem' },
  infoValue: { color: 'var(--text-primary)', fontWeight: '500' },
  infoSubValue: { fontSize: '0.85rem', color: 'var(--text-secondary)', marginTop: '0.2rem' },
  emptySelection: { textAlign: 'center', padding: '2rem 1rem', background: 'var(--bg-secondary)', borderRadius: '8px', color: 'var(--text-secondary)' },
  selectionList: { display: 'flex', flexDirection: 'column', gap: '1rem' },
  selectionItem: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '1rem', background: 'var(--bg-secondary)', borderRadius: '8px' },
  totalRow: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '1rem 0', borderTop: '1px solid var(--border)', borderBottom: '1px solid var(--border)', fontWeight: 'bold', color: 'var(--text-primary)', marginTop: '0.5rem' },
  loginPrompt: { fontSize: '0.85rem', color: 'var(--warning)', display: 'flex', alignItems: 'center', gap: '0.5rem', marginTop: '0.5rem' },
  primaryBtn: { width: '100%', padding: '1rem', background: 'var(--primary)', color: 'white', border: 'none', borderRadius: '8px', fontSize: '1rem', fontWeight: 'bold', cursor: 'pointer', display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '0.5rem', marginTop: '1rem', transition: 'background 0.2s' }
};

export default EventDetails;
