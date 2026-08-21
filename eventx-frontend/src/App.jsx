import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from './context/AuthContext';

import Navbar from './components/layout/Navbar';
import Footer from './components/layout/Footer';

import Home from './pages/public/Home';
import Events from './pages/public/Events';
import EventDetails from './pages/public/EventDetails';
import Login from './pages/auth/Login';
import Register from './pages/auth/Register';
import Checkout from './pages/user/Checkout';
import BookingSuccess from './pages/user/BookingSuccess';
import MyBookings from './pages/user/MyBookings';
import TicketView from './pages/user/TicketView';
import Dashboard from './pages/admin/Dashboard';
import OrganizerStudio from './pages/organizer/OrganizerStudio';

// Private Route Wrapper
const PrivateRoute = ({ children }) => {
  const { isAuthenticated, loading } = useAuth();
  
  if (loading) return <div style={{ height: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>Loading...</div>;
  
  return isAuthenticated() ? children : <Navigate to="/login" replace state={{ from: window.location.pathname }} />;
};

// Admin Route Wrapper
const OrganizerRoute = ({ children }) => {
  const { isAuthenticated, isOrganizer, loading } = useAuth();
  if (loading) return <div style={{ height: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>Loading...</div>;
  return isAuthenticated() && isOrganizer() ? children : <Navigate to="/login?as=organizer" replace state={{ from: '/organizer' }} />;
};

const AdminRoute = ({ children }) => {
  const { isAuthenticated, isAdmin, loading } = useAuth();
  
  if (loading) return <div style={{ height: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>Loading...</div>;
  
  return isAuthenticated() && isAdmin() ? children : <Navigate to="/" replace />;
};

const App = () => {
  return (
    <div className="app-container" style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <Navbar />
      
      <main style={{ flex: '1 0 auto' }}>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/events" element={<Events />} />
          <Route path="/events/:id" element={<EventDetails />} />
          
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          
          <Route path="/checkout" element={
            <PrivateRoute>
              <Checkout />
            </PrivateRoute>
          } />
          
          <Route path="/booking-success" element={
            <PrivateRoute>
              <BookingSuccess />
            </PrivateRoute>
          } />
          
          <Route path="/my-bookings" element={
            <PrivateRoute>
              <MyBookings />
            </PrivateRoute>
          } />
          
          <Route path="/tickets/:bookingId" element={
            <PrivateRoute>
              <TicketView />
            </PrivateRoute>
          } />
          
          <Route path="/organizer" element={
            <OrganizerRoute>
              <OrganizerStudio />
            </OrganizerRoute>
          } />
          
          <Route path="/admin/*" element={
            <AdminRoute>
              <Dashboard />
            </AdminRoute>
          } />
        </Routes>
      </main>
      
      <Footer />
    </div>
  );
};

export default App;
