export const formatCurrency = (amount) => {
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    minimumFractionDigits: 0,
  }).format(amount);
};

export const formatDate = (date) => {
  return new Date(date).toLocaleDateString('en-IN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });
};

export const formatDateTime = (date) => {
  return new Date(date).toLocaleString('en-IN', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
};

export const formatTime = (time) => {
  if (!time) return '';
  const [hours, minutes] = time.split(':');
  const h = parseInt(hours);
  const ampm = h >= 12 ? 'PM' : 'AM';
  const h12 = h % 12 || 12;
  return `${h12}:${minutes} ${ampm}`;
};

export const getStatusColor = (status) => {
  const map = {
    CONFIRMED: 'success', VALID: 'success', PUBLISHED: 'success', SUCCESS: 'success', PROCESSED: 'success',
    PENDING: 'warning', PAYMENT_PENDING: 'warning', DRAFT: 'warning', REFUND_PENDING: 'warning',
    CANCELLED: 'danger', EXPIRED: 'danger', FAILED: 'danger', USED: 'info',
    REFUNDED: 'info', COMPLETED: 'info',
  };
  return map[status] || 'info';
};

export const getCategoryIcon = (category) => {
  const map = {
    CONCERT: '🎵', CONFERENCE: '💼', SPORTS: '⚽', THEATER: '🎭',
    COMEDY: '😂', FESTIVAL: '🎪', WORKSHOP: '🔧', OTHER: '📌',
  };
  return map[category] || '📌';
};

export const getCategoryLabel = (category) => {
  return category?.charAt(0) + category?.slice(1).toLowerCase();
};
