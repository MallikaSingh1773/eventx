import api from './api';

const organizerService = {
  getMyEvents: (params) => api.get('/organizer/events', { params }),
  createEvent: (data) => api.post('/organizer/events', data),
  updateEvent: (id, data) => api.put(`/organizer/events/${id}`, data),
  publishEvent: (id) => api.post(`/organizer/events/${id}/publish`),
  cancelEvent: (id) => api.delete(`/organizer/events/${id}`),
  getVenues: () => api.get('/venues'),
  createVenue: (data) => api.post('/organizer/venues', data),
};

export default organizerService;
