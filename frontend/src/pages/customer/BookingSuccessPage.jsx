import { useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useGetBookingByIdQuery } from '../../features/bookings/bookingApiSlice';
import { CheckCircle } from 'lucide-react';
import './BookingSuccessPage.css';

const BookingSuccessPage = () => {
  const { bookingId } = useParams();
  const { data: booking, isLoading } = useGetBookingByIdQuery(bookingId);

  if (isLoading) {
    return <div className="loader-container"><div className="spinner"></div></div>;
  }

  if (!booking) {
    return <div className="container mt-4"><h2 className="text-center">Booking not found</h2></div>;
  }

  return (
    <div className="booking-success-page container mt-4 mb-4">
      <div className="success-card glass-card">
        <div className="success-icon">
          <CheckCircle size={64} />
        </div>
        
        <h1 className="success-title">Booking Confirmed!</h1>
        <p className="success-subtitle">
          Your tickets have been booked successfully. A confirmation email has been sent.
        </p>
        
        <div className="booking-reference mt-4">
          <span className="ref-label">Booking Reference ID</span>
          <span className="ref-code">{booking.bookingCode}</span>
        </div>
        
        <div className="ticket-details mt-4">
          <div className="ticket-header">
            <h3>{booking.movieName}</h3>
            <span className="badge badge-success">{booking.status}</span>
          </div>
          
          <div className="ticket-body">
            <div className="detail-group">
              <span className="label">Theater</span>
              <span className="value">{booking.theaterName} - {booking.screenName}</span>
            </div>
            
            <div className="detail-group">
              <span className="label">Date & Time</span>
              <span className="value">
                {new Date(booking.showDate).toLocaleDateString()} at {booking.showTime.substring(0, 5)}
              </span>
            </div>
            
            <div className="detail-group">
              <span className="label">Seats ({booking.totalSeats})</span>
              <span className="value text-primary">
                {booking.bookedSeats && booking.bookedSeats.map(s => `${s.seatRow}${s.seatNumber}`).join(', ')}
              </span>
            </div>
            
            <div className="detail-group">
              <span className="label">Total Amount Paid</span>
              <span className="value">₹{booking.totalAmount}</span>
            </div>
          </div>
        </div>
        
        <div className="success-actions mt-4">
          <Link to="/dashboard" className="btn btn-primary">View My Bookings</Link>
          <Link to="/" className="btn btn-outline">Back to Home</Link>
        </div>
      </div>
    </div>
  );
};

export default BookingSuccessPage;
