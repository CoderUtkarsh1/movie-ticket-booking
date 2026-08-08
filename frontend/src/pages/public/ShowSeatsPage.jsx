import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useSelector } from 'react-redux';
import { selectIsAuthenticated, selectCurrentUser } from '../../features/auth/authSlice';
import { useGetShowSeatsQuery, useBlockSeatsMutation, useGetShowByIdQuery } from '../../features/shows/showApiSlice';
import { useCreateBookingMutation } from '../../features/bookings/bookingApiSlice';
import { useGetMovieByIdQuery } from '../../features/movies/movieApiSlice';
import { useGetTheaterByIdQuery } from '../../features/theaters/theaterApiSlice';
import SeatMap from '../../components/SeatMap';
import './ShowSeatsPage.css';

const ShowSeatsPage = () => {
  const { id: showId } = useParams();
  const navigate = useNavigate();
  const isAuthenticated = useSelector(selectIsAuthenticated);
  const user = useSelector(selectCurrentUser);
  
  const [selectedSeats, setSelectedSeats] = useState([]);
  
  const { data: seats, isLoading: seatsLoading, refetch } = useGetShowSeatsQuery(showId);
  const { data: show } = useGetShowByIdQuery(showId);
  const { data: movie } = useGetMovieByIdQuery(show?.movieId, { skip: !show?.movieId });
  const { data: theater } = useGetTheaterByIdQuery(show?.theaterId, { skip: !show?.theaterId });
  const [blockSeats, { isLoading: blocking }] = useBlockSeatsMutation();
  const [createBooking, { isLoading: bookingCreating }] = useCreateBookingMutation();

  // Set up polling for real-time seat availability updates
  useEffect(() => {
    const interval = setInterval(() => {
      refetch();
    }, 10000); // 10 seconds
    return () => clearInterval(interval);
  }, [refetch]);

  const totalAmount = selectedSeats.reduce((sum, seat) => sum + seat.price, 0);

  const handleProceed = async () => {
    if (!isAuthenticated) {
      alert('Please login to proceed with booking');
      navigate('/login');
      return;
    }

    if (selectedSeats.length === 0) return;

    try {
      const seatIds = selectedSeats.map(s => s.seatId);
      
      // 1. Block seats in show-service
      await blockSeats({ showId, seatIds, userId: user.userId }).unwrap();
      
      // 2. We need some show metadata for the booking.
      // In a real app, the seat API or a separate show API would return this.
      // For now, we extract it from the first seat (assuming it might have it) 
      // or we pass placeholders and let the backend handle it if it fetches it.
      // Actually, our backend BookingRequest needs showId, movieName, theaterName, etc.
      // Let's assume the backend fetches this internally or we need to pass it.
      // Since our DTO requires them, let's pass them.
      
      // Find screen name from theater data
      const screen = theater?.screens?.find(s => s.id === show?.screenId);
      const screenName = screen ? screen.screenName : "Screen " + show?.screenId;
      
      const bookingRequest = {
        showId: Number(showId),
        movieName: movie?.title || "Unknown Movie",
        theaterName: theater?.name || "Unknown Theater",
        screenName: screenName,
        showDate: show?.showDate || new Date().toISOString().split('T')[0],
        showTime: show?.showTime || "12:00:00",
        seats: selectedSeats.map(s => ({
          seatId: s.seatId,
          seatRow: s.seatRow,
          seatNumber: s.seatNumber,
          seatType: s.seatType,
          price: s.price
        }))
      };
      
      // 3. Create pending booking in booking-service
      const response = await createBooking(bookingRequest).unwrap();
      
      // 4. Navigate to checkout
      navigate(`/checkout/${response.id}`);
      
    } catch (err) {
      console.error('Booking error:', err);
      alert(err.data?.message || 'Failed to proceed. Seats might have been taken. Please try again.');
      refetch();
      setSelectedSeats([]);
    }
  };

  return (
    <div className="show-seats-page container mt-4 mb-4">
      <div className="seat-selection-layout">
        
        <div className="seat-map-section glass-card">
          <div className="section-header">
            <h2>Select Seats</h2>
            <button onClick={() => refetch()} className="btn btn-outline btn-sm">Refresh</button>
          </div>
          
          {seatsLoading ? (
            <div className="loader-container"><div className="spinner"></div></div>
          ) : seats ? (
            <SeatMap 
              seats={seats} 
              selectedSeats={selectedSeats} 
              onSeatSelect={setSelectedSeats} 
              maxSelection={10}
            />
          ) : (
            <p className="no-data">Failed to load seats.</p>
          )}
        </div>
        
        <div className="booking-summary-section glass-card">
          <h3>Booking Summary</h3>
          <hr className="divider" />
          
          <div className="summary-content">
            {selectedSeats.length > 0 ? (
              <>
                <div className="selected-seats-list">
                  {selectedSeats.map(seat => (
                    <div key={seat.seatId} className="summary-row">
                      <span>Row {seat.seatRow} - {seat.seatNumber} ({seat.seatType})</span>
                      <span>₹{seat.price}</span>
                    </div>
                  ))}
                </div>
                
                <hr className="divider" />
                
                <div className="summary-row total-row">
                  <span>Total Amount</span>
                  <span className="total-amount">₹{totalAmount}</span>
                </div>
                
                <button 
                  className="btn btn-primary w-100 mt-3"
                  onClick={handleProceed}
                  disabled={blocking || bookingCreating}
                >
                  {blocking || bookingCreating ? 'Processing...' : `Proceed to Pay ₹${totalAmount}`}
                </button>
              </>
            ) : (
              <div className="empty-summary text-center text-muted mt-4">
                <p>No seats selected.</p>
                <p>Click on available seats to select them.</p>
              </div>
            )}
          </div>
        </div>

      </div>
    </div>
  );
};

export default ShowSeatsPage;
