import React, { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { Clock, CreditCard, Shield, AlertTriangle, ChevronLeft, Loader2 } from 'lucide-react';
import paymentService from '../../services/paymentService';
import { formatCurrency, formatDate } from '../../utils/helpers';
import { useAuth } from '../../context/AuthContext';

const Checkout = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { user } = useAuth();
  
  const [timeLeft, setTimeLeft] = useState(() => location.state?.ttlSeconds || 0);
  const [processing, setProcessing] = useState(false);
  const [error, setError] = useState(null);
  
  const state = location.state;

  const waitForPaymentConfirmation = async (bookingId) => {
    for (let i = 0; i < 20; i += 1) {
      const statusRes = await paymentService.getStatus(bookingId);
      const data = statusRes.data;
      if (data?.status === 'SUCCESS' || data?.bookingStatus === 'CONFIRMED') {
        return true;
      }
      if (data?.status === 'FAILED') {
        throw new Error('Payment failed. Please try again.');
      }
      await new Promise((resolve) => setTimeout(resolve, 1500));
    }
    return false;
  };

  const waitForRazorpay = () => new Promise((resolve, reject) => {
    if (window.Razorpay) {
      resolve();
      return;
    }
    let attempts = 0;
    const timer = setInterval(() => {
      if (window.Razorpay) {
        clearInterval(timer);
        resolve();
      } else if (++attempts > 50) {
        clearInterval(timer);
        reject(new Error('Razorpay checkout failed to load. Check your network and refresh.'));
      }
    }, 100);
  });

  useEffect(() => {
    if (!state || !state.bookingId) {
      navigate('/events');
      return;
    }
    
    setTimeLeft(state.ttlSeconds);
    
    const timer = setInterval(() => {
      setTimeLeft(prev => {
        if (prev <= 1) {
          clearInterval(timer);
          return 0;
        }
        return prev - 1;
      });
    }, 1000);
    
    return () => clearInterval(timer);
  }, [state, navigate]);

  if (!state) return null;

  const isExpired = timeLeft <= 0;
  
  const formatTimeLeft = (seconds) => {
    const m = Math.floor(seconds / 60);
    const s = seconds % 60;
    return `${m}:${s < 10 ? '0' : ''}${s}`;
  };

  const handlePayment = async () => {
    if (isExpired) return;
    
    try {
      setProcessing(true);
      setError(null);

      await waitForRazorpay();

      const orderRes = await paymentService.createOrder({ bookingId: state.bookingId });
      
      if (!orderRes.success) {
        throw new Error(orderRes.message || 'Failed to create payment order');
      }
      
      const orderData = orderRes.data;
      const amountInPaise = orderData.amountInPaise
        ?? Math.round(Number(orderData.amount) * 100);
      
      const options = {
        key: orderData.keyId,
        amount: amountInPaise,
        currency: orderData.currency || 'INR',
        name: 'EventX',
        description: `Ticket Booking for ${state.eventTitle}`,
        order_id: orderData.razorpayOrderId,
        handler: async function(response) {
          try {
            setProcessing(true);
            await paymentService.verifyPayment({
              razorpayOrderId: response.razorpay_order_id,
              razorpayPaymentId: response.razorpay_payment_id,
              razorpaySignature: response.razorpay_signature
            });

            const confirmed = await waitForPaymentConfirmation(state.bookingId);
            if (confirmed) {
              navigate('/booking-success', { state: { bookingId: state.bookingId } });
            } else {
              setError('Payment received. Confirmation is still processing — check My tickets in a moment.');
            }
          } catch (err) {
            setError(err.response?.data?.message || err.message || 'Payment verification failed.');
          } finally {
            setProcessing(false);
          }
        },
        prefill: {
          name: user?.name || '',
          email: user?.email || '',
        },
        notes: {
          bookingId: String(state.bookingId),
        },
        theme: {
          color: '#7c3aed'
        },
        modal: {
          ondismiss: function() {
            setProcessing(false);
          }
        }
      };
      
      const rzp = new window.Razorpay(options);
      
      rzp.on('payment.failed', function (response) {
        setError('Payment failed: ' + (response.error?.description || 'Unknown error'));
        setProcessing(false);
      });
      
      rzp.open();
      
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Something went wrong processing your payment.');
      setProcessing(false);
    }
  };

  const subtotal = state.totalAmount;
  // The server owns the payable total; never add client-only fees here.
  const grandTotal = subtotal;

  return (
    <>
      <style>{`
        .spinner { animation: spin 1s linear infinite; }
        @keyframes spin { 100% { transform: rotate(360deg); } }
        .checkout-grid { display: grid; grid-template-columns: 1fr; gap: 2rem; }
        @media (min-width: 768px) {
          .checkout-grid { grid-template-columns: 1.5fr 1fr; align-items: start; }
        }
      `}</style>
      
      <div style={styles.container}>
        <button style={styles.backBtn} onClick={() => navigate(-1)}>
          <ChevronLeft size={20} /> Back
        </button>

        <h1 style={styles.pageTitle}>Checkout</h1>

        {isExpired ? (
          <div style={styles.expiredAlert}>
            <AlertTriangle size={32} color="var(--danger)" />
            <h2>Session Expired</h2>
            <p>Your reserved seats have been released. Please go back and select seats again.</p>
            <button style={styles.primaryBtn} onClick={() => navigate('/events')}>
              Browse Events
            </button>
          </div>
        ) : (
          <div className="checkout-grid">
            {/* Left side */}
            <div style={styles.leftCol}>
              <div style={styles.timerCard}>
                <Clock size={24} color="var(--warning)" />
                <div>
                  <div style={styles.timerTitle}>Time Remaining</div>
                  <div style={styles.timerValue}>{formatTimeLeft(timeLeft)}</div>
                </div>
                <div style={{ marginLeft: 'auto', color: 'var(--text-secondary)', fontSize: '0.85rem', maxWidth: '200px' }}>
                  Please complete your payment within the given time to secure your seats.
                </div>
              </div>

              {error && (
                <div style={styles.errorAlert}>
                  <AlertTriangle size={20} /> {error}
                </div>
              )}

              <div style={styles.card}>
                <h2 style={styles.cardTitle}>Order Details</h2>
                <div style={{ marginBottom: '1.5rem', paddingBottom: '1.5rem', borderBottom: '1px solid var(--border)' }}>
                  <h3 style={{ margin: '0 0 0.5rem 0', color: 'var(--text-primary)' }}>{state.eventTitle}</h3>
                  <div style={{ color: 'var(--text-secondary)' }}>{formatDate(state.eventDate)}</div>
                </div>

                <h3 style={{ fontSize: '1rem', marginBottom: '1rem', color: 'var(--text-primary)' }}>Selected Seats</h3>
                <div style={styles.itemsList}>
                  {state.items && state.items.map((item, idx) => (
                    <div key={idx} style={styles.itemRow}>
                      <div>
                        <div style={{ color: 'var(--text-primary)', fontWeight: '500' }}>
                          Seat {item.seatNumber || (item.section + '-' + item.row + item.seatNumber)}
                        </div>
                        <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>{item.category}</div>
                      </div>
                      <div style={{ fontWeight: '500' }}>{formatCurrency(item.price)}</div>
                    </div>
                  ))}
                </div>
              </div>
            </div>

            {/* Right side */}
            <div style={styles.rightCol}>
              <div style={styles.card}>
                <h2 style={styles.cardTitle}>Order Summary</h2>
                
                <div style={styles.summaryRow}>
                  <span style={{ color: 'var(--text-secondary)' }}>Tickets Subtotal</span>
                  <span>{formatCurrency(subtotal)}</span>
                </div>
                
                <div style={{ ...styles.summaryRow, ...styles.grandTotal }}>
                  <span>Total Amount</span>
                  <span style={{ color: 'var(--primary-light)' }}>{formatCurrency(grandTotal)}</span>
                </div>

                <div style={styles.secureNote}>
                  <Shield size={16} color="var(--success)" /> 
                  <Shield size={16} color="var(--success)" /> 
                  Pay securely with Razorpay. Use test card 4111 1111 1111 1111, any future expiry, any CVV.
                </div>

                <button 
                  style={{ ...styles.primaryBtn, width: '100%', marginTop: '1.5rem' }}
                  onClick={handlePayment}
                  disabled={processing || isExpired}
                >
                  {processing ? (
                    <><Loader2 className="spinner" size={20} /> Processing...</>
                  ) : (
                    <><CreditCard size={20} /> Pay with Razorpay</>
                  )}
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </>
  );
};

const styles = {
  container: { maxWidth: '1000px', margin: '0 auto', padding: '2rem 1rem' },
  backBtn: { display: 'flex', alignItems: 'center', gap: '0.5rem', background: 'none', border: 'none', color: 'var(--text-secondary)', cursor: 'pointer', fontSize: '1rem', padding: 0, marginBottom: '1.5rem' },
  pageTitle: { fontSize: '2rem', color: 'var(--text-primary)', marginBottom: '2rem' },
  leftCol: { display: 'flex', flexDirection: 'column', gap: '1.5rem' },
  rightCol: { display: 'flex', flexDirection: 'column', gap: '1.5rem' },
  timerCard: { display: 'flex', alignItems: 'center', gap: '1rem', background: 'rgba(245, 158, 11, 0.1)', border: '1px solid rgba(245, 158, 11, 0.3)', padding: '1.5rem', borderRadius: '12px' },
  timerTitle: { fontSize: '0.9rem', color: 'var(--warning)', fontWeight: 'bold' },
  timerValue: { fontSize: '1.5rem', color: 'var(--text-primary)', fontWeight: 'bold' },
  card: { background: 'var(--bg-surface)', border: '1px solid var(--border)', padding: '2rem', borderRadius: '12px' },
  cardTitle: { fontSize: '1.25rem', color: 'var(--text-primary)', marginBottom: '1.5rem', borderBottom: '1px solid var(--border)', paddingBottom: '1rem' },
  itemsList: { display: 'flex', flexDirection: 'column', gap: '1rem' },
  itemRow: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', background: 'var(--bg-secondary)', padding: '1rem', borderRadius: '8px' },
  summaryRow: { display: 'flex', justifyContent: 'space-between', marginBottom: '1rem', fontSize: '1rem' },
  grandTotal: { borderTop: '1px solid var(--border)', paddingTop: '1rem', marginTop: '0.5rem', fontWeight: 'bold', fontSize: '1.2rem', color: 'var(--text-primary)' },
  primaryBtn: { display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.5rem', background: 'var(--primary)', color: 'white', border: 'none', padding: '1rem', borderRadius: '8px', fontSize: '1.1rem', fontWeight: 'bold', cursor: 'pointer', transition: 'all 0.2s' },
  secureNote: { display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.5rem', marginTop: '1.5rem', fontSize: '0.85rem', color: 'var(--text-muted)' },
  expiredAlert: { background: 'var(--bg-surface)', border: '1px solid var(--danger)', padding: '3rem', borderRadius: '12px', textAlign: 'center', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '1rem' },
  errorAlert: { background: 'rgba(239, 68, 68, 0.1)', border: '1px solid var(--danger)', color: 'var(--danger)', padding: '1rem', borderRadius: '8px', display: 'flex', alignItems: 'center', gap: '0.5rem' }
};

export default Checkout;
