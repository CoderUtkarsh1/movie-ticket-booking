import { useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useGetMovieByIdQuery, useToggleInterestedMutation } from '../../features/movies/movieApiSlice';
import { useGetShowsQuery } from '../../features/shows/showApiSlice';
import { useGetTheatersQuery } from '../../features/theaters/theaterApiSlice';
import { useSelector } from 'react-redux';
import { Heart } from 'lucide-react';
import './MovieDetailPage.css';

const MovieDetailPage = () => {
  const { id } = useParams();
  const { user } = useSelector((state) => state.auth);
  
  // Default to today
  const [selectedDate, setSelectedDate] = useState(new Date().toISOString().split('T')[0]);

  const { data: movie, isLoading: movieLoading } = useGetMovieByIdQuery(id);
  const { data: shows, isLoading: showsLoading } = useGetShowsQuery({ movieId: id, date: selectedDate });
  const { data: theaters } = useGetTheatersQuery();
  const [toggleInterested, { isLoading: isToggling }] = useToggleInterestedMutation();

  const [imgError, setImgError] = useState(false);
  const [showBooking, setShowBooking] = useState(false);

  // Generate next 7 days for the date selector
  const dateOptions = Array.from({ length: 7 }).map((_, i) => {
    const d = new Date();
    d.setDate(d.getDate() + i);
    return {
      value: d.toISOString().split('T')[0],
      label: i === 0 ? 'Today' : i === 1 ? 'Tomorrow' : d.toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric' })
    };
  });

  if (movieLoading) {
    return <div className="loader-container"><div className="spinner"></div></div>;
  }

  if (!movie) {
    return <div className="container mt-4"><h2 className="text-center">Movie not found</h2></div>;
  }

  // Group shows by theater
  const showsByTheater = shows?.reduce((acc, show) => {
    const theater = theaters?.find(t => t.id === show.theaterId);
    const theaterName = theater ? theater.name : `Theater ${show.theaterId}`;
    
    const screen = theater?.screens?.find(s => s.id === show.screenId);
    const screenName = screen ? screen.screenName : `Screen ${show.screenId}`;
    
    const enrichedShow = { ...show, theaterName, screenName };

    if (!acc[enrichedShow.theaterName]) {
      acc[enrichedShow.theaterName] = [];
    }
    acc[enrichedShow.theaterName].push(enrichedShow);
    return acc;
  }, {}) || {};

  const isInterested = user && movie.interestedUserIds?.includes(user.id);
  const interestedCount = movie.interestedUserIds?.length || 0;

  const handleInterestedClick = async () => {
    if (!user) {
      alert("Please login to show your interest!");
      return;
    }
    try {
      await toggleInterested(id).unwrap();
    } catch (err) {
      alert("Failed to update interest.");
    }
  };

  const handleBookClick = () => {
    setShowBooking(true);
    setTimeout(() => {
      document.getElementById('showtimes-section')?.scrollIntoView({ behavior: 'smooth' });
    }, 100);
  };

  return (
    <div className="movie-detail-page">
      {/* Movie Header Info */}
      <section className="movie-header">
        <div className="container movie-header-content">
          <div className="movie-poster-large">
            {movie.posterUrl && !imgError ? (
              <img 
                src={movie.posterUrl} 
                alt={movie.title} 
                style={{ width: '100%', height: '100%', objectFit: 'cover', borderRadius: '12px' }} 
                onError={() => setImgError(true)}
              />
            ) : (
              <div className="poster-placeholder">
                {movie.title.charAt(0)}
              </div>
            )}
          </div>
          <div className="movie-metadata">
            <h1 className="movie-title-large">{movie.title}</h1>
            <div className="movie-tags">
              <span className="tag">{movie.genre}</span>
              <span className="tag">{movie.language}</span>
              <span className="tag">{movie.duration} mins</span>
              <span className="tag">{movie.status}</span>
            </div>
            <p className="movie-description">{movie.description}</p>
            <div className="movie-cast">
              <strong>Cast: </strong> {movie.cast.map(c => typeof c === 'object' ? c.actorName : c).join(', ')}
            </div>
            {movie.status !== 'UPCOMING' && (
              <div style={{ marginTop: '1.5rem' }}>
                <button 
                  className="btn btn-primary"
                  onClick={handleBookClick}
                  style={{ padding: '10px 24px', fontSize: '1.1rem' }}
                >
                  Book Tickets
                </button>
              </div>
            )}
          </div>
        </div>
      </section>

      {/* Dynamic Section based on Status */}
      {movie.status === 'UPCOMING' ? (
        <section className="container mt-4 mb-4 text-center">
          <div className="glass-card" style={{ padding: '3rem', maxWidth: '100%', margin: '0 auto' }}>
            <h2 style={{ marginBottom: '2rem' }}>Coming Soon</h2>
            <button 
              className={`btn ${isInterested ? 'btn-primary' : 'btn-outline'}`}
              style={{ padding: '12px 30px', fontSize: '1.1rem', display: 'flex', alignItems: 'center', gap: '10px', margin: '0 auto' }}
              onClick={handleInterestedClick}
              disabled={isToggling}
            >
              <Heart fill={isInterested ? 'currentColor' : 'none'} size={20} />
              {isInterested ? 'Interested' : 'I am Interested'}
            </button>
            <div style={{ marginTop: '1rem', fontSize: '1.2rem', fontWeight: 'bold', color: 'var(--primary-color)' }}>
              {interestedCount} {interestedCount === 1 ? 'person is' : 'people are'} interested
            </div>
          </div>
        </section>
      ) : showBooking ? (
        <section id="showtimes-section" className="showtimes-section container mt-4 mb-4">
          <h2>Book Tickets</h2>
          
          {/* Date Selector */}
          <div className="date-selector mt-3 mb-4">
            {dateOptions.map(date => (
              <button
                key={date.value}
                className={`date-btn ${selectedDate === date.value ? 'active' : ''}`}
                onClick={() => setSelectedDate(date.value)}
              >
                {date.label}
              </button>
            ))}
          </div>

          {/* Shows List */}
          {showsLoading ? (
            <div className="loader-container"><div className="spinner"></div></div>
          ) : Object.keys(showsByTheater).length > 0 ? (
            <div className="theater-shows-list">
              {Object.entries(showsByTheater).map(([theaterName, theaterShows]) => (
                <div key={theaterName} className="theater-shows-card glass-card">
                  <div className="theater-info">
                    <h3>{theaterName}</h3>
                    <p className="text-muted">Screen: {theaterShows[0].screenName}</p>
                  </div>
                  <div className="show-times">
                    {theaterShows.map(show => (
                      <Link
                        key={show.id}
                        to={`/shows/${show.id}/seats`}
                        className="show-time-btn"
                      >
                        {show.showTime.substring(0, 5)}
                      </Link>
                    ))}
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="no-shows glass-card text-center">
              <p>No shows available for the selected date.</p>
            </div>
          )}
        </section>
      ) : null}
    </div>
  );
};

export default MovieDetailPage;
