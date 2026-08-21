import React from 'react';
import { Link } from 'react-router-dom';
import { Ticket, Globe2, MessageCircle, Camera, Play, Mail, MapPin, Phone } from 'lucide-react';

const Footer = () => {
  return (
    <footer className="footer">
      <div className="container">
        <div className="footer-grid">
          
          {/* Brand Column */}
          <div className="footer-col">
            <div className="navbar-logo" style={{ marginBottom: '1rem' }}>
              <Ticket className="icon" size={28} />
              <span className="text-gradient">EventX</span>
            </div>
            <p className="text-secondary" style={{ marginBottom: '1.5rem', lineHeight: '1.6' }}>
              Your premium destination for discovering and booking the best events, concerts, and experiences worldwide. We make memories accessible.
            </p>
            <div className="social-links">
              <a href="#" className="social-link" aria-label="Website"><Globe2 size={18} /></a>
              <a href="#" className="social-link" aria-label="Community"><MessageCircle size={18} /></a>
              <a href="#" className="social-link" aria-label="Instagram"><Camera size={18} /></a>
              <a href="#" className="social-link" aria-label="Video"><Play size={18} /></a>
            </div>
          </div>

          {/* Quick Links Column */}
          <div className="footer-col">
            <h4>Quick Links</h4>
            <div className="footer-links">
              <Link to="/">Home</Link>
              <Link to="/events">Discover Events</Link>
              <Link to="/bookings">My Bookings</Link>
              <Link to="/about">About Us</Link>
            </div>
          </div>

          {/* Support Column */}
          <div className="footer-col">
            <h4>Support</h4>
            <div className="footer-links">
              <Link to="/help">Help Center</Link>
              <Link to="/contact">Contact Us</Link>
              <Link to="/privacy">Privacy Policy</Link>
              <Link to="/terms">Terms of Service</Link>
            </div>
          </div>

          {/* Contact Column */}
          <div className="footer-col">
            <h4>Contact Info</h4>
            <div className="footer-links">
              <div style={{ display: 'flex', gap: '0.5rem', color: 'var(--text-secondary)' }}>
                <MapPin size={18} style={{ color: 'var(--primary)', flexShrink: 0 }} />
                <span>123 EventX Towers, Tech Park, Bangalore 560001</span>
              </div>
              <div style={{ display: 'flex', gap: '0.5rem', color: 'var(--text-secondary)', marginTop: '0.5rem' }}>
                <Phone size={18} style={{ color: 'var(--primary)', flexShrink: 0 }} />
                <span>+91 98765 43210</span>
              </div>
              <div style={{ display: 'flex', gap: '0.5rem', color: 'var(--text-secondary)', marginTop: '0.5rem' }}>
                <Mail size={18} style={{ color: 'var(--primary)', flexShrink: 0 }} />
                <span>support@eventx.com</span>
              </div>
            </div>
          </div>

        </div>

        <div className="footer-bottom">
          <p>© 2026 EventX. All rights reserved. | Made with <span style={{color: 'var(--danger)'}}>❤️</span> in India</p>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
