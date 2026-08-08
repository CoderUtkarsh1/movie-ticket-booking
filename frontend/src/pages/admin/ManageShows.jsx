import { useState } from 'react';
import { useGetAllMoviesQuery } from '../../features/movies/movieApiSlice';
import { useGetTheatersQuery, useGetScreensQuery } from '../../features/theaters/theaterApiSlice';
import { useCreateShowMutation } from '../../features/shows/showApiSlice';
import { Plus } from 'lucide-react';
import './AdminPages.css';

const ManageShows = () => {
  const { data: moviesRes } = useGetAllMoviesQuery();
  const { data: theatersRes } = useGetTheatersQuery();
  
  const [createShow, { isLoading: isCreating }] = useCreateShowMutation();

  const movies = moviesRes?.content || [];
  const theaters = theatersRes || [];

  const [showForm, setShowForm] = useState(false);
  const [selectedTheater, setSelectedTheater] = useState('');
  
  const { data: screensRes } = useGetScreensQuery(selectedTheater, {
    skip: !selectedTheater
  });
  
  const screens = screensRes || [];

  const [formData, setFormData] = useState({
    movieId: '',
    theaterId: '',
    screenId: '',
    showDate: '',
    showTime: ''
  });

  const handleCreate = async (e) => {
    e.preventDefault();
    try {
      const payload = {
        ...formData,
        movieId: formData.movieId, // DO NOT parse as Int, it's a Mongo ObjectId string
        theaterId: parseInt(formData.theaterId),
        screenId: parseInt(formData.screenId),
        showTime: formData.showTime + ':00' // add seconds
      };
      
      await createShow(payload).unwrap();
      setShowForm(false);
      setFormData({ movieId: '', theaterId: '', screenId: '', showDate: '', showTime: '' });
      setSelectedTheater('');
      alert('Show added successfully');
    } catch (err) {
      alert(err.data?.message || 'Failed to add show');
    }
  };

  return (
    <div className="admin-page">
      <div className="admin-page-header">
        <h2>Manage Shows</h2>
        <button className="btn btn-primary btn-sm" onClick={() => setShowForm(!showForm)}>
          <Plus size={16} /> Add Show
        </button>
      </div>

      {showForm && (
        <div className="admin-form-container glass-card mb-4">
          <form onSubmit={handleCreate} className="grid grid-cols-2">
            
            <div className="form-group">
              <label className="form-label">Movie</label>
              <select 
                className="form-control" 
                value={formData.movieId} 
                onChange={e => setFormData({...formData, movieId: e.target.value})} 
                required
              >
                <option value="">Select Movie</option>
                {movies.map(m => (
                  <option key={m.id} value={m.id}>{m.title}</option>
                ))}
              </select>
            </div>
            
            <div className="form-group">
              <label className="form-label">Theater</label>
              <select 
                className="form-control" 
                value={formData.theaterId} 
                onChange={e => {
                  setFormData({...formData, theaterId: e.target.value, screenId: ''});
                  setSelectedTheater(e.target.value);
                }} 
                required
              >
                <option value="">Select Theater</option>
                {theaters.map(t => (
                  <option key={t.id} value={t.id}>{t.name}</option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label className="form-label">Screen</label>
              <select 
                className="form-control" 
                value={formData.screenId} 
                onChange={e => setFormData({...formData, screenId: e.target.value})} 
                required
                disabled={!selectedTheater || screens.length === 0}
              >
                <option value="">Select Screen</option>
                {screens.map(s => (
                  <option key={s.id} value={s.id}>{s.screenName} ({s.totalSeats} seats)</option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label className="form-label">Show Date</label>
              <input 
                type="date" 
                className="form-control" 
                value={formData.showDate} 
                onChange={e => setFormData({...formData, showDate: e.target.value})} 
                required 
              />
            </div>

            <div className="form-group">
              <label className="form-label">Show Time</label>
              <input 
                type="time" 
                className="form-control" 
                value={formData.showTime} 
                onChange={e => setFormData({...formData, showTime: e.target.value})} 
                required 
              />
            </div>

            <div className="form-actions" style={{ gridColumn: 'span 2' }}>
              <button type="submit" className="btn btn-success" disabled={isCreating}>
                {isCreating ? 'Saving...' : 'Save Show'}
              </button>
              <button type="button" className="btn btn-outline" onClick={() => setShowForm(false)}>Cancel</button>
            </div>
          </form>
        </div>
      )}

      <div className="glass-card p-4 text-center text-muted">
        Select a Movie and Theater to view existing shows. (API implementation for all shows list pending).
      </div>
    </div>
  );
};

export default ManageShows;
