import { useState } from 'react';
import { useGetUpcomingBookingsQuery, useGetPastBookingsQuery } from '../../features/bookings/bookingApiSlice';
import BookingCard from '../../components/BookingCard';
import './MyBookingsPage.css';

const MyBookingsPage = () => {
  const [activeTab, setActiveTab] = useState('upcoming');
  
  const { data: upcomingRes, isLoading: upcomingLoading } = useGetUpcomingBookingsQuery();
  const { data: pastRes, isLoading: pastLoading } = useGetPastBookingsQuery();

  const upcoming = upcomingRes || [];
  const past = pastRes || [];

  return (
    <div className="my-bookings">
      <div className="dashboard-page-header">
        <h2>My Bookings</h2>
      </div>

      <div className="booking-tabs">
        <button 
          className={`tab-btn ${activeTab === 'upcoming' ? 'active' : ''}`}
          onClick={() => setActiveTab('upcoming')}
        >
          Upcoming
        </button>
        <button 
          className={`tab-btn ${activeTab === 'past' ? 'active' : ''}`}
          onClick={() => setActiveTab('past')}
        >
          Past History
        </button>
      </div>

      <div className="bookings-list mt-4">
        {activeTab === 'upcoming' ? (
          upcomingLoading ? (
            <div className="loader-container"><div className="spinner"></div></div>
          ) : upcoming.length > 0 ? (
            <div className="grid grid-cols-1 gap-3">
              {upcoming.map(booking => (
                <BookingCard key={booking.id} booking={booking} isUpcoming={true} />
              ))}
            </div>
          ) : (
            <div className="empty-state">
              <p>You have no upcoming bookings.</p>
            </div>
          )
        ) : (
          pastLoading ? (
            <div className="loader-container"><div className="spinner"></div></div>
          ) : past.length > 0 ? (
            <div className="grid grid-cols-1 gap-3">
              {past.map(booking => (
                <BookingCard key={booking.id} booking={booking} isUpcoming={false} />
              ))}
            </div>
          ) : (
            <div className="empty-state">
              <p>No past booking history found.</p>
            </div>
          )
        )}
      </div>
    </div>
  );
};

export default MyBookingsPage;
