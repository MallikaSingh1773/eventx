import React, { useState } from 'react';
import { useNavigate, Link, useSearchParams } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { User, Mail, Lock, Phone, Eye, EyeOff, Loader } from 'lucide-react';
import './Auth.css';

const Register = () => {
  const [searchParams] = useSearchParams();
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    password: '',
    confirmPassword: '',
    phone: '',
    accountType: searchParams.get('as') === 'organizer' ? 'ORGANIZER' : 'USER',
  });
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  const { register } = useAuth();
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const getPasswordStrength = (pass) => {
    if (!pass) return 0;
    let strength = 0;
    if (pass.length >= 6) strength += 1;
    if (pass.match(/[A-Z]/)) strength += 1;
    if (pass.match(/[0-9]/) || pass.match(/[^A-Za-z0-9]/)) strength += 1;
    return strength;
  };

  const strength = getPasswordStrength(formData.password);
  const strengthClass = strength === 1 ? 'weak' : strength === 2 ? 'medium' : strength === 3 ? 'strong' : '';

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);

    if (formData.password !== formData.confirmPassword) {
      setError("Passwords do not match.");
      return;
    }
    
    if (formData.password.length < 6) {
      setError("Password must be at least 6 characters.");
      return;
    }

    setLoading(true);

    try {
      const userData = await register({
        name: formData.name,
        email: formData.email,
        password: formData.password,
        phone: formData.phone,
        accountType: formData.accountType,
      });
      navigate(userData?.role === 'ROLE_ORGANIZER' ? '/organizer' : '/');
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to create account.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        <div className="auth-header">
          <h1>{formData.accountType === 'ORGANIZER' ? 'Create organizer account' : 'Create Account'}</h1>
          <p>Book tickets, or post your own events</p>
        </div>

        {error && <div className="auth-error">{error}</div>}

        <form onSubmit={handleSubmit} className="auth-form">
          <div className="role-toggle" role="group" aria-label="Account type">
            <button type="button" className={formData.accountType === 'USER' ? 'active' : ''} onClick={() => setFormData({ ...formData, accountType: 'USER' })}>
              Attendee
            </button>
            <button type="button" className={formData.accountType === 'ORGANIZER' ? 'active' : ''} onClick={() => setFormData({ ...formData, accountType: 'ORGANIZER' })}>
              Organizer
            </button>
          </div>
          <p className="role-hint">
            {formData.accountType === 'ORGANIZER'
              ? 'Organizers can post events, set prices, and go live. Attendees still book seats as usual.'
              : 'Attendees explore events and book seats. Switch to Organizer if you want to host.'}
          </p>
          <div className="input-group">
            <label htmlFor="name">Full Name</label>
            <div className="input-wrapper">
              <User className="input-icon" />
              <input
                id="name"
                name="name"
                type="text"
                className="auth-input"
                placeholder="Enter your full name"
                value={formData.name}
                onChange={handleChange}
                required
              />
            </div>
          </div>

          <div className="input-group">
            <label htmlFor="email">Email</label>
            <div className="input-wrapper">
              <Mail className="input-icon" />
              <input
                id="email"
                name="email"
                type="email"
                className="auth-input"
                placeholder="Enter your email"
                value={formData.email}
                onChange={handleChange}
                required
              />
            </div>
          </div>

          <div className="input-group">
            <label htmlFor="phone">Phone (Optional)</label>
            <div className="input-wrapper">
              <Phone className="input-icon" />
              <input
                id="phone"
                name="phone"
                type="tel"
                className="auth-input"
                placeholder="Enter your phone number"
                value={formData.phone}
                onChange={handleChange}
              />
            </div>
          </div>

          <div className="input-group">
            <label htmlFor="password">Password</label>
            <div className="input-wrapper">
              <Lock className="input-icon" />
              <input
                id="password"
                name="password"
                type={showPassword ? 'text' : 'password'}
                className="auth-input"
                placeholder="Create a password"
                value={formData.password}
                onChange={handleChange}
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
            
            {formData.password && (
              <div className="password-strength-container">
                <div className="password-strength">
                  <div className={`strength-bar ${strength >= 1 ? 'active ' + strengthClass : ''}`}></div>
                  <div className={`strength-bar ${strength >= 2 ? 'active ' + strengthClass : ''}`}></div>
                  <div className={`strength-bar ${strength >= 3 ? 'active ' + strengthClass : ''}`}></div>
                </div>
                <div className="strength-text">
                  {strength === 1 && 'Weak'}
                  {strength === 2 && 'Medium'}
                  {strength === 3 && 'Strong'}
                </div>
              </div>
            )}
          </div>

          <div className="input-group">
            <label htmlFor="confirmPassword">Confirm Password</label>
            <div className="input-wrapper">
              <Lock className="input-icon" />
              <input
                id="confirmPassword"
                name="confirmPassword"
                type={showPassword ? 'text' : 'password'}
                className="auth-input"
                placeholder="Confirm your password"
                value={formData.confirmPassword}
                onChange={handleChange}
                required
              />
            </div>
          </div>

          <button type="submit" className="auth-button" disabled={loading}>
            {loading ? <Loader size={20} className="spinner" /> : <User size={20} />}
            {loading ? 'Creating Account...' : 'Register'}
          </button>
        </form>

        <div className="auth-footer">
          <p>
            Already have an account?{' '}
            <Link to={formData.accountType === 'ORGANIZER' ? '/login?as=organizer' : '/login'} className="auth-link">
              Login
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
};

export default Register;
