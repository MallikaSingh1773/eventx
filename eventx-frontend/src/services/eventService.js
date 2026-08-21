import api from './api';

const eventService = {
  getEvents: (params) => api.get('/events', { params }),
  getEvent: (id) => api.get(`/events/${id}`),
  searchEvents: (params) => api.get('/events/search', { params }),
  getUpcomingEvents: (params) => api.get('/events/upcoming', { params }),
  getSeats: (eventId) => api.get(`/events/${eventId}/seats`),
  // Admin
  createEvent: (data) => api.post('/admin/events', data),
  updateEvent: (id, data) => api.put(`/admin/events/${id}`, data),
  deleteEvent: (id) => api.delete(`/admin/events/${id}`),
  getAllEvents: (params) => api.get('/admin/events', { params }),
};

export default eventService;
