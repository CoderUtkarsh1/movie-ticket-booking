import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useGetBookingByIdQuery } from '../../features/bookings/bookingApiSlice';
import { useProcessPaymentMutation } from '../../features/payments/paymentApiSlice';
import { useGetWalletBalanceQuery } from '../../features/wallet/walletApiSlice';
import './CheckoutPage.css';

const CheckoutPage = () => {
  const { bookingId } = useParams();
  const navigate = useNavigate();

  const { data: bookingResponse, isLoading: bookingLoading } = useGetBookingByIdQuery(bookingId);
  const { data: walletResponse, isLoading: walletLoading } = useGetWalletBalanceQuery();
  const [processPayment, { isLoading: processing }] = useProcessPaymentMutation();

  const [paymentMethod, setPaymentMethod] = useState('WALLET');
  const [timeLeft, setTimeLeft] = useState(300); // 5 minutes timer

  const booking = bookingResponse;
  const balance = walletResponse?.balance || 0;

  useEffect(() => {
    if (timeLeft <= 0) {
      alert('Session expired. Booking cancelled.');
      navigate('/');
      return;
    }

    const timerId = setInterval(() => {
      setTimeLeft(prev => prev - 1);
    }, 1000);

    return () => clearInterval(timerId);
  }, [timeLeft, navigate]);

  const formatTime = (seconds) => {
    const m = Math.floor(seconds / 60);
    const s = seconds % 60;
    return `${m}:${s < 10 ? '0' : ''}${s}`;
  };

  const handlePayment = async () => {
    if (paymentMethod === 'WALLET' && balance < booking.totalAmount) {
      alert('Insufficient wallet balance. Please add money or choose another method.');
      return;
    }

    try {
      await processPayment({
        bookingId: parseInt(bookingId),
        paymentMethod: paymentMethod,
        amount: booking.totalAmount
      }).unwrap();

      navigate(`/booking-success/${bookingId}`);
    } catch (err) {
      alert(err.data?.message || 'Payment failed. Please try again.');
    }
  };

  if (bookingLoading || walletLoading) {
    return <div className="loader-container"><div className="spinner"></div></div>;
  }

  if (!booking) {
    return <div className="container mt-4"><h2 className="text-center">Booking not found</h2></div>;
  }

  if (booking.status !== 'PENDING') {
    return (
      <div className="container mt-4 text-center">
        <h2>This booking is already {booking.status.toLowerCase()}.</h2>
        <button onClick={() => navigate('/dashboard')} className="btn btn-primary mt-3">Go to Dashboard</button>
      </div>
    );
  }

  return (
    <div className="checkout-page container mt-4 mb-4">
      <div className="checkout-header text-center mb-4">
        <h1>Checkout</h1>
        <div className="timer-badge">
          Time left to complete payment: <span className="timer-countdown">{formatTime(timeLeft)}</span>
        </div>
      </div>

      <div className="checkout-layout">
        <div className="payment-section glass-card">
          <h3>Select Payment Method</h3>
          <hr className="divider" />
          
          <div className="payment-methods mt-3">
            <label className={`payment-method-card ${paymentMethod === 'WALLET' ? 'selected' : ''}`}>
              <input 
                type="radio" 
                name="paymentMethod" 
                value="WALLET" 
                checked={paymentMethod === 'WALLET'}
                onChange={(e) => setPaymentMethod(e.target.value)}
              />
              <div className="method-info">
                <h4>My Wallet</h4>
                <p>Available Balance: ₹{balance.toFixed(2)}</p>
                {balance < booking.totalAmount && (
                  <span className="text-danger small mt-1">Insufficient Balance</span>
                )}
              </div>
            </label>

            <label className={`payment-method-card ${paymentMethod === 'CREDIT_CARD' ? 'selected' : ''}`}>
              <input 
                type="radio" 
                name="paymentMethod" 
                value="CREDIT_CARD" 
                checked={paymentMethod === 'CREDIT_CARD'}
                onChange={(e) => setPaymentMethod(e.target.value)}
              />
              <div className="method-info">
                <h4>Credit / Debit Card</h4>
                <p>Visa, MasterCard, Amex</p>
              </div>
            </label>

            <label className={`payment-method-card ${paymentMethod === 'UPI' ? 'selected' : ''}`}>
              <input 
                type="radio" 
                name="paymentMethod" 
                value="UPI" 
                checked={paymentMethod === 'UPI'}
                onChange={(e) => setPaymentMethod(e.target.value)}
              />
              <div className="method-info">
                <h4>UPI</h4>
                <p>Google Pay, PhonePe, Paytm</p>
              </div>
            </label>
          </div>

          <div className="mt-4 pt-3 border-top">
            <button 
              className="btn btn-primary w-100" 
              onClick={handlePayment}
              disabled={processing || (paymentMethod === 'WALLET' && balance < booking.totalAmount)}
            >
              {processing ? 'Processing...' : `Pay ₹${booking.totalAmount}`}
            </button>
          </div>
        </div>

        <div className="order-summary-section glass-card">
          <h3>Order Summary</h3>
          <hr className="divider" />
          
          <div className="summary-item">
            <span className="text-muted">Movie</span>
            <span className="font-weight-600">{booking.movieName}</span>
          </div>
          <div className="summary-item">
            <span className="text-muted">Theater</span>
            <span>{booking.theaterName} - {booking.screenName}</span>
          </div>
          <div className="summary-item">
            <span className="text-muted">Date & Time</span>
            <span>{new Date(booking.showDate).toLocaleDateString()} | {booking.showTime.substring(0,5)}</span>
          </div>
          <div className="summary-item">
            <span className="text-muted">Seats ({booking.totalSeats})</span>
            <span>
              {booking.bookedSeats && booking.bookedSeats.map(s => `${s.seatRow}${s.seatNumber}`).join(', ')}
            </span>
          </div>
          
          <hr className="divider" />
          
          <div className="summary-item total-item">
            <span>Total Payable</span>
            <span className="total-price">₹{booking.totalAmount}</span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default CheckoutPage;
