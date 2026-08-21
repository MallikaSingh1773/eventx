import { useEffect, useState } from 'react';
import { ArrowRight, CalendarDays, MapPin, Search, ShieldCheck, Sparkles, Ticket } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import eventService from '../../services/eventService';
import EventCard from '../../components/common/EventCard';
import EventCardSkeleton from '../../components/common/EventCardSkeleton';
import { formatDate } from '../../utils/helpers';

const categories = ['Concerts', 'Conferences', 'Comedy', 'Food', 'Culture'];

export default function Home() {
  const navigate = useNavigate();
  const [query, setQuery] = useState('');
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    eventService.getUpcomingEvents({ page: 0, size: 7 })
      .then((response) => { if (response.success) setEvents(response.data.content || []); })
      .catch(() => setEvents([]))
      .finally(() => setLoading(false));
  }, []);

  const search = (event) => {
    event.preventDefault();
    navigate(query.trim() ? `/events?search=${encodeURIComponent(query.trim())}` : '/events');
  };

  const featured = events[0];
  return (
    <div className="discover-page">
      <section className="discover-hero">
        <div className="discover-orb orb-one" /><div className="discover-orb orb-two" />
        <div className="discover-copy">
          <p className="eyebrow"><span /> Your city, switched on</p>
          <h1>Make plans<br /><em>worth dressing</em><br />up for.</h1>
          <p className="hero-description">Concerts with your whole chest. Ideas that change your angle. Food worth taking the long way for.</p>
          <form className="hero-search" onSubmit={search}>
            <Search size={22} /><input value={query} onChange={(e) => setQuery(e.target.value)} placeholder="Search artists, ideas, cities..." aria-label="Search events" />
            <button type="submit">Search</button>
          </form>
          <div className="quick-picks">{categories.map((name) => <button key={name} onClick={() => navigate(`/events?category=${name === 'Concerts' ? 'CONCERT' : name.toUpperCase()}`)}>{name}</button>)}</div>
        </div>
      </section>
      <section className="shortlist-section">
        <div className="section-heading"><div><p className="eyebrow">The shortlist</p><h2>Good stuff, incoming.</h2></div><button className="text-action" onClick={() => navigate('/events')}>See all events <ArrowRight size={18} /></button></div>
        {loading ? <div className="event-grid">{Array.from({ length: 3 }).map((_, i) => <EventCardSkeleton key={i} />)}</div> : events.length ? <div className="event-grid">{events.slice(1, 4).map((event) => <EventCard event={event} key={event.id} />)}</div> : <div className="empty-discover"><Ticket size={28} /><p>Fresh events will appear here soon.</p><button onClick={() => navigate('/events')}>Explore events</button></div>}
      </section>
      {featured && <section className="feature-wrap"><article className="feature-event" style={{ backgroundImage: `linear-gradient(90deg, rgba(20, 19, 49, .9), rgba(20, 19, 49, .18)), url(${featured.imageUrl})` }}><div><p className="eyebrow">Featured this week</p><h2>{featured.title}</h2><p className="feature-meta"><CalendarDays size={17} /> {formatDate(featured.eventDate)} <span>•</span> <MapPin size={17} /> {featured.venueName || featured.city}</p><button onClick={() => navigate(`/events/${featured.id}`)}>View event <ArrowRight size={18} /></button></div></article></section>}
      <section className="trust-strip"><div><ShieldCheck /><span><strong>Secure checkout</strong>Payments verified by Razorpay</span></div><div><Sparkles /><span><strong>Real-time seats</strong>Your selection is held for you</span></div><div><Ticket /><span><strong>Tickets on your phone</strong>Scan and go at the venue</span></div></section>
    </div>
  );
}
