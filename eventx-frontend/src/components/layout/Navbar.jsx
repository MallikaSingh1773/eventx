import { useEffect, useRef, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { LogOut, Menu, Search, UserRound, X } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';

export default function Navbar() {
  const { user, logout, isAuthenticated, isOrganizer } = useAuth();
  const [open, setOpen] = useState(false); const [menu, setMenu] = useState(false);
  const ref = useRef(null); const navigate = useNavigate(); const location = useLocation();
  useEffect(() => { setMenu(false); setOpen(false); }, [location]);
  useEffect(() => { const close = (e) => !ref.current?.contains(e.target) && setOpen(false); document.addEventListener('mousedown', close); return () => document.removeEventListener('mousedown', close); }, []);
  const signedIn = isAuthenticated();
  return <header className="site-header"><nav className="site-nav container"><Link to="/" className="brand"><span className="brand-mark">X</span><span>event<span>x</span></span></Link><div className={`nav-center ${menu ? 'is-open' : ''}`}><Link to="/events">Explore</Link>{signedIn && <Link to="/my-bookings">My tickets</Link>}{signedIn && isOrganizer() ? <Link to="/organizer">Organizer studio</Link> : !signedIn && <Link to="/login?as=organizer">Organizer login</Link>}</div><div className="nav-tools"><button className="find-button" onClick={() => navigate('/events')}><Search size={18} /> <span>Find an event</span></button>{!signedIn && <Link className="host-button" to="/login?as=organizer">Organizer login</Link>}{signedIn ? <div className="account" ref={ref}><button className="profile-button" onClick={() => setOpen(!open)} aria-label="Account"><UserRound size={21} /></button>{open && <div className="account-menu"><strong>{user?.name}</strong>{isOrganizer() && <Link to="/organizer">Organizer studio</Link>}<Link to="/my-bookings">My bookings</Link><button onClick={() => { logout(); navigate('/'); }}><LogOut size={16} /> Sign out</button></div>}</div> : <Link className="profile-button" to="/login" aria-label="Login"><UserRound size={21} /></Link>}<button className="nav-mobile" onClick={() => setMenu(!menu)} aria-label="Menu">{menu ? <X /> : <Menu />}</button></div></nav></header>;
}
