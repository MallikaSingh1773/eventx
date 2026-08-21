import React, { useEffect, useState } from 'react';
import { useNavigate, useLocation, Link, useSearchParams } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { Mail, Lock, Eye, EyeOff, LogIn, Loader } from 'lucide-react';
import './Auth.css';

const ORGANIZER_DEMO = { email: 'organizer@eventx.com', password: 'Organizer@123' };

const Login = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const [mode, setMode] = useState(searchParams.get('as') === 'organizer' ? 'ORGANIZER' : 'ATTENDEE');
  const [email, setEmail] = useState(mode === 'ORGANIZER' ? ORGANIZER_DEMO.email : '');
  const [password, setPassword] = useState(mode === 'ORGANIZER' ? ORGANIZER_DEMO.password : '');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const from = typeof location.state?.from === 'string'
    ? location.state.from
    : location.state?.from?.pathname || '/';

  useEffect(() => {
    const next = searchParams.get('as') === 'organizer' ? 'ORGANIZER' : 'ATTENDEE';
    setMode(next);
  }, [searchParams]);

  const switchMode = (next) => {
    setError(null);
    setMode(next);
    if (next === 'ORGANIZER') {
      setSearchParams({ as: 'organizer' });
      setEmail(ORGANIZER_DEMO.email);
      setPassword(ORGANIZER_DEMO.password);
    } else {
      setSearchParams({});
      setEmail('');
      setPassword('');
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      const userData = await login({ email, password });
      const isOrganizer = userData?.role === 'ROLE_ORGANIZER' || userData?.role === 'ROLE_ADMIN';

      if (from && from !== '/') {
        navigate(from, { replace: true });
      } else if (isOrganizer) {
        navigate('/organizer', { replace: true });
      } else {
        navigate('/', { replace: true });
      }
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to login. Please check your credentials.');
    } finally {
      setLoading(false);
    }
  };

  const isOrganizerMode = mode === 'ORGANIZER';

  return (
    <div className="auth-container">
      <div className="auth-card">
        <div className="auth-header">
          <h1>{isOrganizerMode ? 'Organizer login' : 'Welcome Back'}</h1>
          <p>{isOrganizerMode ? 'Post events, set prices, and go live' : 'Sign in to book seats and tickets'}</p>
        </div>

        {error && <div className="auth-error">{error}</div>}

        <form onSubmit={handleSubmit} className="auth-form">
          <div className="role-toggle" role="group" aria-label="Login type">
            <button type="button" className={!isOrganizerMode ? 'active' : ''} onClick={() => switchMode('ATTENDEE')}>
              Attendee
            </button>
            <button type="button" className={isOrganizerMode ? 'active' : ''} onClick={() => switchMode('ORGANIZER')}>
              Organizer
            </button>
          </div>
          <p className="role-hint">
            {isOrganizerMode
              ? 'Demo organizer is filled in. Sign in, then you will land in Organizer studio.'
              : 'Booking tickets? Stay on Attendee. Hosting an event? Switch to Organizer.'}
          </p>

          <div className="input-group">
            <label htmlFor="email">Email</label>
            <div className="input-wrapper">
              <Mail className="input-icon" />
              <input
                id="email"
                type="email"
                className="auth-input"
                placeholder="Enter your email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>
          </div>

          <div className="input-group">
            <label htmlFor="password">Password</label>
            <div className="input-wrapper">
              <Lock className="input-icon" />
              <input
                id="password"
                type={showPassword ? 'text' : 'password'}
                className="auth-input"
                placeholder="Enter your password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
              <button
                type="button"
                className="toggle-password"
                onClick={() => setShowPassword(!showPassword)}
              >
                {showPassword ? <EyeOff size={20} /> : <Eye size={20} />}
              </button>
            </div>
          </div>

          <button type="submit" className="auth-button" disabled={loading}>
            {loading ? <Loader size={20} className="spinner" /> : <LogIn size={20} />}
            {loading ? 'Signing in...' : isOrganizerMode ? 'Sign in as organizer' : 'Sign In'}
          </button>
        </form>

        <div className="auth-footer">
          <p>
            Don't have an account?{' '}
            <Link to={isOrganizerMode ? '/register?as=organizer' : '/register'} className="auth-link">
              {isOrganizerMode ? 'Register as organizer' : 'Register'}
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
};

export default Login;
