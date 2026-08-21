import { useEffect, useState } from 'react';
import { CalendarPlus, Loader2, MapPin, Megaphone, Plus } from 'lucide-react';
import { toast } from 'react-toastify';
import organizerService from '../../services/organizerService';
import { formatCurrency, formatDate } from '../../utils/helpers';

const categories = ['CONCERT', 'CONFERENCE', 'SPORTS', 'THEATER', 'COMEDY', 'FESTIVAL', 'WORKSHOP', 'OTHER'];

const emptyForm = () => ({
  title: '',
  description: '',
  category: 'CONCERT',
  imageUrl: '',
  venueId: '',
  eventDate: '',
  startTime: '19:00',
  endTime: '22:00',
  publish: true,
  ticketCategories: [
    { name: 'VIP', price: 3000, totalSeats: 20, seatCategory: 'VIP' },
    { name: 'Premium', price: 1800, totalSeats: 40, seatCategory: 'PREMIUM' },
    { name: 'Regular', price: 800, totalSeats: 80, seatCategory: 'REGULAR' },
  ],
});

const emptyVenue = { name: '', city: '', address: '', state: '', capacity: 200 };

export default function OrganizerStudio() {
  const [events, setEvents] = useState([]);
  const [venues, setVenues] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [venueForm, setVenueForm] = useState(emptyVenue);
  const [showVenue, setShowVenue] = useState(false);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  const load = async () => {
    try {
      const [eventsRes, venuesRes] = await Promise.all([
        organizerService.getMyEvents({ size: 50, sort: 'eventDate,desc' }),
        organizerService.getVenues(),
      ]);
      if (eventsRes.success) setEvents(eventsRes.data.content || eventsRes.data || []);
      if (venuesRes.success) setVenues(venuesRes.data || []);
    } catch (err) {
      toast.error(err.response?.data?.message || 'Could not load organizer data');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const updateTicket = (index, field, value) => {
    setForm((prev) => {
      const next = [...prev.ticketCategories];
      next[index] = { ...next[index], [field]: value };
      return { ...prev, ticketCategories: next };
    });
  };

  const handleCreateVenue = async (e) => {
    e.preventDefault();
    try {
      const res = await organizerService.createVenue({
        ...venueForm,
        capacity: Number(venueForm.capacity),
      });
      toast.success('Venue added with a default seat map');
      setShowVenue(false);
      setVenueForm(emptyVenue);
      await load();
      if (res.data?.id) setForm((prev) => ({ ...prev, venueId: String(res.data.id) }));
    } catch (err) {
      toast.error(err.response?.data?.message || 'Could not create venue');
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.venueId) {
      toast.info('Pick a venue, or add a new one.');
      return;
    }
    try {
      setSaving(true);
      await organizerService.createEvent({
        title: form.title,
        description: form.description,
        category: form.category,
        imageUrl: form.imageUrl || undefined,
        venueId: Number(form.venueId),
        eventDate: form.eventDate,
        startTime: form.startTime + ':00',
        endTime: form.endTime + ':00',
        publish: form.publish,
        ticketCategories: form.ticketCategories.map((row) => ({
          name: row.name,
          price: Number(row.price),
          totalSeats: Number(row.totalSeats),
          seatCategory: row.seatCategory,
        })),
      });
      toast.success(form.publish ? 'Event is live on EventX' : 'Draft saved');
      setForm(emptyForm());
      await load();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Could not post event');
    } finally {
      setSaving(false);
    }
  };

  const cancelEvent = async (id) => {
    try {
      await organizerService.cancelEvent(id);
      toast.success('Event cancelled');
      await load();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Could not cancel event');
    }
  };

  const publishEvent = async (id) => {
    try {
      await organizerService.publishEvent(id);
      toast.success('Event published');
      await load();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Could not publish event');
    }
  };

  if (loading) {
    return (
      <div className="container" style={{ padding: '4rem 1rem', textAlign: 'center' }}>
        <Loader2 className="spinner" />
        <p>Loading your studio...</p>
      </div>
    );
  }

  return (
    <div className="container" style={{ padding: '2rem 1rem 4rem', maxWidth: '1100px' }}>
      <style>{`
        .studio-grid { display: grid; gap: 1.5rem; }
        @media (min-width: 960px) { .studio-grid { grid-template-columns: 1.1fr 0.9fr; align-items: start; } }
        .studio-card { background: var(--bg-surface); border: 1px solid var(--border); border-radius: 16px; padding: 1.4rem; }
        .studio-form input, .studio-form select, .studio-form textarea {
          width: 100%; padding: 0.7rem 0.85rem; border-radius: 8px; border: 1px solid var(--border);
          background: var(--bg-secondary); color: var(--text-primary); margin-top: 0.35rem;
        }
        .studio-form label { display: block; margin-bottom: 0.9rem; color: var(--text-secondary); font-size: 0.9rem; }
        .ticket-grid { display: grid; grid-template-columns: 1.2fr 1fr 1fr; gap: 0.5rem; margin-bottom: 0.5rem; }
      `}</style>

      <p className="eyebrow" style={{ color: 'var(--primary-light)', marginBottom: '0.4rem' }}>Organizer studio</p>
      <h1 style={{ marginBottom: '0.4rem' }}>Post events. Own the listing.</h1>
      <p style={{ color: 'var(--text-secondary)', marginBottom: '1.75rem' }}>
        Attendees browse and book. You create the show, pick a venue, set ticket prices, and publish.
      </p>

      <div className="studio-grid">
        <form className="studio-card studio-form" onSubmit={handleSubmit}>
          <h2 style={{ marginBottom: '1rem', display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
            <CalendarPlus size={20} /> New event
          </h2>
          <label>Title
            <input required value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} placeholder="Arijit Singh Live" />
          </label>
          <label>Description
            <textarea required rows={4} value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
          </label>
          <label>Category
            <select value={form.category} onChange={(e) => setForm({ ...form, category: e.target.value })}>
              {categories.map((c) => <option key={c} value={c}>{c}</option>)}
            </select>
          </label>
          <label>Image URL
            <input value={form.imageUrl} onChange={(e) => setForm({ ...form, imageUrl: e.target.value })} placeholder="https://..." />
          </label>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '0.75rem' }}>
            <label>Date
              <input required type="date" value={form.eventDate} onChange={(e) => setForm({ ...form, eventDate: e.target.value })} />
            </label>
            <label>Start
              <input required type="time" value={form.startTime} onChange={(e) => setForm({ ...form, startTime: e.target.value })} />
            </label>
            <label>End
              <input required type="time" value={form.endTime} onChange={(e) => setForm({ ...form, endTime: e.target.value })} />
            </label>
          </div>
          <label>Venue
            <select required value={form.venueId} onChange={(e) => setForm({ ...form, venueId: e.target.value })}>
              <option value="">Select a venue</option>
              {venues.map((v) => <option key={v.id} value={v.id}>{v.name} — {v.city}</option>)}
            </select>
          </label>
          <button type="button" onClick={() => setShowVenue(!showVenue)} style={{ background: 'none', border: 'none', color: 'var(--primary-light)', cursor: 'pointer', marginBottom: '1rem' }}>
            <Plus size={16} /> {showVenue ? 'Hide venue form' : 'Add a new venue'}
          </button>

          {showVenue && (
            <div style={{ padding: '1rem', background: 'var(--bg-secondary)', borderRadius: '12px', marginBottom: '1rem' }}>
              <strong>New venue</strong>
              <label>Name<input value={venueForm.name} onChange={(e) => setVenueForm({ ...venueForm, name: e.target.value })} /></label>
              <label>City<input value={venueForm.city} onChange={(e) => setVenueForm({ ...venueForm, city: e.target.value })} /></label>
              <label>Address<input value={venueForm.address} onChange={(e) => setVenueForm({ ...venueForm, address: e.target.value })} /></label>
              <label>State<input value={venueForm.state} onChange={(e) => setVenueForm({ ...venueForm, state: e.target.value })} /></label>
              <label>Capacity<input type="number" min="20" value={venueForm.capacity} onChange={(e) => setVenueForm({ ...venueForm, capacity: e.target.value })} /></label>
              <button type="button" className="auth-button" onClick={handleCreateVenue}>Save venue</button>
            </div>
          )}

          <p style={{ margin: '0.5rem 0', fontWeight: 600 }}>Ticket categories</p>
          {form.ticketCategories.map((row, index) => (
            <div className="ticket-grid" key={row.seatCategory}>
              <input value={row.name} onChange={(e) => updateTicket(index, 'name', e.target.value)} />
              <input type="number" min="1" value={row.price} onChange={(e) => updateTicket(index, 'price', e.target.value)} />
              <input type="number" min="1" value={row.totalSeats} onChange={(e) => updateTicket(index, 'totalSeats', e.target.value)} />
            </div>
          ))}

          <label style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', marginTop: '0.75rem' }}>
            <input type="checkbox" checked={form.publish} onChange={(e) => setForm({ ...form, publish: e.target.checked })} />
            Publish now (visible to attendees)
          </label>

          <button className="auth-button" disabled={saving} type="submit" style={{ marginTop: '0.75rem' }}>
            {saving ? <Loader2 className="spinner" size={18} /> : <Megaphone size={18} />}
            {saving ? 'Posting...' : 'Post event'}
          </button>
        </form>

        <div className="studio-card">
          <h2 style={{ marginBottom: '1rem' }}>Your events</h2>
          {events.length === 0 && <p style={{ color: 'var(--text-secondary)' }}>Nothing posted yet. Use the form to go live.</p>}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.85rem' }}>
            {events.map((event) => (
              <article key={event.id} style={{ padding: '1rem', background: 'var(--bg-secondary)', borderRadius: '12px' }}>
                <strong>{event.title}</strong>
                <div style={{ color: 'var(--text-muted)', fontSize: '0.85rem', margin: '0.35rem 0' }}>
                  <MapPin size={14} /> {event.venueName} · {formatDate(event.eventDate)} · {event.status}
                </div>
                <div>From {formatCurrency(event.startingPrice || 0)}</div>
                <div style={{ display: 'flex', gap: '0.5rem', marginTop: '0.6rem' }}>
                  {event.status === 'DRAFT' && (
                    <button type="button" onClick={() => publishEvent(event.id)} style={{ cursor: 'pointer' }}>Publish</button>
                  )}
                  {event.status !== 'CANCELLED' && (
                    <button type="button" onClick={() => cancelEvent(event.id)} style={{ cursor: 'pointer', color: 'var(--danger)' }}>Cancel</button>
                  )}
                </div>
              </article>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
