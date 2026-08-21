import api from './api';

const adminService = {
  // Analytics
  getOverview: () => api.get('/admin/analytics/overview'),
  getRevenue: (params) => api.get('/admin/analytics/revenue', { params }),
  getEventStats: () => api.get('/admin/analytics/events'),
  // Users
  getUsers: (params) => api.get('/admin/users', { params }),
  getUser: (id) => api.get(`/admin/users/${id}`),
  updateUserRole: (id, role) => api.put(`/admin/users/${id}/role`, null, { params: { role } }),
  toggleUserActive: (id) => api.put(`/admin/users/${id}/toggle-active`),
  // Venues
  getVenues: () => api.get('/admin/venues'),
  createVenue: (data) => api.post('/admin/venues', data),
  updateVenue: (id, data) => api.put(`/admin/venues/${id}`, data),
  getVenue: (id) => api.get(`/admin/venues/${id}`),
  createSeats: (data) => api.post('/admin/venues/seats', data),
  getSeats: (venueId) => api.get(`/admin/venues/${venueId}/seats`),
  // Payments
  getPayments: (params) => api.get('/admin/payments', { params }),
  // Refunds
  getRefunds: (params) => api.get('/admin/refunds', { params }),
};

export default adminService;
