import api from './api';

const bookingService = {
  lockSeats: (data) => api.post('/bookings/lock-seats', data),
  getMyBookings: (params) => api.get('/bookings/my', { params }),
  getBooking: (id) => api.get(`/bookings/${id}`),
  cancelBooking: (id) => api.post(`/bookings/${id}/cancel`),
  requestRefund: (id, data) => api.post(`/bookings/${id}/refund`, data),
  // Admin
  getAllBookings: (params) => api.get('/admin/bookings', { params }),
};

export default bookingService;
