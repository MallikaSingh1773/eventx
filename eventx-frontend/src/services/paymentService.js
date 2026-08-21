import api from './api';

const paymentService = {
  createOrder: (data) => api.post('/payments/create-order', data),
  verifyPayment: (data) => api.post('/payments/verify', data),
  getStatus: (bookingId) => api.get(`/payments/booking/${bookingId}`),
  demoPay: (data) => api.post('/payments/demo-pay', data),
};

export default paymentService;
