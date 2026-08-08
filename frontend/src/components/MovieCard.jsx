import { useState } from 'react';
import { Link } from 'react-router-dom';
import './MovieCard.css';

const MovieCard = ({ movie }) => {
  const [imgError, setImgError] = useState(false);

  return (
    <Link to={`/movies/${movie.id}`} className="movie-card glass-card" style={{ textDecoration: 'none', color: 'inherit', display: 'block' }}>
      <div className="movie-poster">
        {movie.posterUrl && !imgError ? (
          <img 
            src={movie.posterUrl} 
            alt={movie.title} 
            style={{ width: '100%', height: '100%', objectFit: 'cover' }} 
            onError={() => setImgError(true)}
          />
        ) : (
          <div className="poster-placeholder">
            {movie.title.charAt(0)}
          </div>
        )}
      </div>
      <div className="movie-info">
        <h3 className="movie-title">{movie.title}</h3>
        <p className="movie-genre">{movie.genre} • {movie.language}</p>
        <div className="movie-footer">
          {movie.status === 'UPCOMING' ? (
            <span className="movie-rating" style={{ color: 'var(--primary-color)' }}>
              ❤️ {movie.interestedUserIds?.length || 0} Interested
            </span>
          ) : (
            <span className="movie-rating">★ {movie.imdbRating || movie.rating || 'N/A'} {movie.imdbRating ? '(IMDb)' : ''}</span>
          )}
        </div>
      </div>
    </Link>
  );
};

export default MovieCard;
