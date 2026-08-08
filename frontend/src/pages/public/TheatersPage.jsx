import { useState } from 'react';
import { useGetTheatersQuery } from '../../features/theaters/theaterApiSlice';
import './TheatersPage.css';

const TheatersPage = () => {
  const [city, setCity] = useState('');
  
  const { data: theatersResponse, isLoading } = useGetTheatersQuery(city);
  const theaters = theatersResponse || [];

  return (
    <div className="theaters-page container mt-4 mb-4">
      <div className="section-header">
        <h2>Partner Theaters</h2>
        
        <div className="city-filter">
          <input
            type="text"
            className="form-control"
            placeholder="Filter by city (e.g., Mumbai)"
            value={city}
            onChange={(e) => setCity(e.target.value)}
          />
        </div>
      </div>

      {isLoading ? (
        <div className="loader-container"><div className="spinner"></div></div>
      ) : theaters.length > 0 ? (
        <div className="grid grid-cols-3">
          {theaters.map(theater => (
            <div key={theater.id} className="theater-card glass-card">
              <div className="theater-card-header">
                <h3>{theater.name}</h3>
                <span className="city-badge">{theater.city}</span>
              </div>
              <p className="theater-address">{theater.address}</p>
              
              {/* <div className="theater-card-footer">
                 <Link to={`/theaters/${theater.id}`} className="btn btn-outline btn-sm">
                   View Shows
                 </Link>
              </div> */}
            </div>
          ))}
        </div>
      ) : (
        <div className="no-data glass-card">
          <p>No theaters found in {city || 'the system'}.</p>
        </div>
      )}
    </div>
  );
};

export default TheatersPage;
