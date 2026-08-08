import { useState } from 'react';
import { useCancelBookingMutation } from '../features/bookings/bookingApiSlice';
import './BookingCard.css';

const BookingCard = ({ booking, isUpcoming }) => {
  const [cancelBooking, { isLoading }] = useCancelBookingMutation();
  const [showDetails, setShowDetails] = useState(false);

  const handleCancel = async () => {
    if (window.confirm('Are you sure you want to cancel this booking? This action cannot be undone.')) {
      try {
        await cancelBooking(booking.id).unwrap();
        alert('Booking cancelled successfully.');
      } catch (err) {
        alert(err.data?.message || 'Failed to cancel booking.');
      }
    }
  };

  const statusClass = `status-${booking.status.toLowerCase()}`;

  return (
    <div className="booking-card glass-card">
      <div className="booking-main">
        <div className="booking-movie-info">
          <h3>{booking.movieName}</h3>
          <p className="booking-theater">
            {booking.theaterName} - {booking.screenName}
          </p>
          <div className="booking-datetime">
            <span className="booking-date">{new Date(booking.showDate).toLocaleDateString()}</span>
            <span className="booking-time">{booking.showTime.substring(0, 5)}</span>
          </div>
        </div>

        <div className="booking-status-section">
          <div className={`status-badge ${statusClass}`}>{booking.status}</div>
          <div className="booking-id">ID: {booking.bookingCode}</div>
          
          <div className="booking-actions">
            <button 
              className="btn btn-outline btn-sm"
              onClick={() => setShowDetails(!showDetails)}
            >
              {showDetails ? 'Hide Details' : 'View Details'}
            </button>
            
            {isUpcoming && booking.status === 'CONFIRMED' && (
              <button 
                className="btn btn-danger btn-sm"
                onClick={handleCancel}
                disabled={isLoading}
              >
                {isLoading ? 'Cancelling...' : 'Cancel'}
              </button>
            )}
          </div>
        </div>
      </div>

      {showDetails && (
        <div className="booking-details mt-3">
          <hr className="divider" />
          <div className="details-grid">
            <div>
              <span className="detail-label">Total Seats</span>
              <span className="detail-value">{booking.totalSeats}</span>
            </div>
            <div>
              <span className="detail-label">Total Amount</span>
              <span className="detail-value">₹{booking.totalAmount}</span>
            </div>
            {/* If backend returns bookedSeats array, we could list them here */}
            {booking.bookedSeats && (
              <div className="seats-list-full">
                <span className="detail-label">Seats</span>
                <span className="detail-value">
                  {booking.bookedSeats.map(s => `${s.seatRow}${s.seatNumber}`).join(', ')}
                </span>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default BookingCard;
