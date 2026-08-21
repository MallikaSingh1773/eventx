import api from './api';

const ticketService = {
  getTicket: (id) => api.get(`/tickets/${id}`),
  getTicketsForBooking: (bookingId) => api.get(`/tickets/booking/${bookingId}`),
  // Admin
  verifyTicket: (data) => api.post('/admin/tickets/verify', data),
};

export default ticketService;
