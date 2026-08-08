import { useGetNowShowingQuery, useGetUpcomingQuery } from '../../features/movies/movieApiSlice';
import MovieCard from '../../components/MovieCard';
import './HomePage.css';

const HomePage = () => {
  const { data: nowShowingRes, isLoading: nowShowingLoading } = useGetNowShowingQuery();
  const { data: upcomingRes, isLoading: upcomingLoading } = useGetUpcomingQuery();

  const nowShowing = nowShowingRes?.content || [];
  const upcoming = upcomingRes?.content || [];

  return (
    <div className="home-page">
      {/* Hero Section */}
      <section className="hero-section">
        <div className="container hero-content">
          <h1 className="hero-title">Experience the Magic of Cinema</h1>
          <p className="hero-subtitle">Book your tickets for the latest blockbusters in premium theaters near you.</p>
        </div>
        <div className="hero-overlay"></div>
      </section>

      <div className="container">
        {/* Now Showing */}
        <section className="movie-section mt-4 mb-4">
          <div className="section-header">
            <h2>Now Showing</h2>
          </div>
          
          {nowShowingLoading ? (
            <div className="loader-container"><div className="spinner"></div></div>
          ) : nowShowing.length > 0 ? (
            <div className="grid grid-cols-4">
              {nowShowing.map(movie => (
                <MovieCard key={movie.id} movie={movie} />
              ))}
            </div>
          ) : (
            <p className="no-data">No movies showing right now.</p>
          )}
        </section>

        {/* Upcoming Movies */}
        <section className="movie-section mb-4">
          <div className="section-header">
            <h2>Coming Soon</h2>
          </div>
          
          {upcomingLoading ? (
            <div className="loader-container"><div className="spinner"></div></div>
          ) : upcoming.length > 0 ? (
            <div className="grid grid-cols-4">
              {upcoming.map(movie => (
                <MovieCard key={movie.id} movie={movie} />
              ))}
            </div>
          ) : (
            <p className="no-data">No upcoming movies.</p>
          )}
        </section>
      </div>
    </div>
  );
};

export default HomePage;
