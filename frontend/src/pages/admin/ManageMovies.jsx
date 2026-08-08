import { useState } from 'react';
import { useGetAllMoviesQuery, useCreateMovieMutation, useUpdateMovieMutation, useDeleteMovieMutation } from '../../features/movies/movieApiSlice';
import { Plus, Trash2, Edit } from 'lucide-react';
import './AdminPages.css';

const ManageMovies = () => {
  const { data: moviesResponse, isLoading } = useGetAllMoviesQuery();
  const [createMovie, { isLoading: isCreating }] = useCreateMovieMutation();
  const [updateMovie, { isLoading: isUpdating }] = useUpdateMovieMutation();
  const [deleteMovie] = useDeleteMovieMutation();

  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    duration: '',
    language: 'Hindi',
    releaseDate: '',
    genre: 'Action',
    director: '',
    cast: '',
    censorRating: 'UA',
    imdbRating: '',
    status: 'NOW_SHOWING'
  });

  const movies = moviesResponse?.content || [];

  const handleEditClick = (movie) => {
    setEditingId(movie.id);
    setFormData({
      title: movie.title,
      description: movie.description,
      duration: movie.duration,
      language: movie.language,
      releaseDate: movie.releaseDate,
      genre: movie.genre,
      director: movie.director || '',
      cast: movie.cast?.map(c => typeof c === 'object' ? c.actorName : c).join(', ') || '',
      censorRating: movie.censorRating || 'UA',
      imdbRating: movie.imdbRating || '',
      status: movie.status || 'NOW_SHOWING'
    });
    setShowForm(true);
  };

  const handleAddClick = () => {
    setEditingId(null);
    setFormData({
      title: '', description: '', duration: '', language: 'Hindi', 
      releaseDate: '', genre: 'Action', director: '', cast: '', censorRating: 'UA', imdbRating: '', status: 'NOW_SHOWING'
    });
    setShowForm(!showForm);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const payload = {
        ...formData,
        duration: parseInt(formData.duration),
        imdbRating: formData.imdbRating ? parseFloat(formData.imdbRating) : null,
        cast: formData.cast.split(',').map(c => ({ actorName: c.trim() }))
      };
      
      if (editingId) {
        await updateMovie({ id: editingId, ...payload }).unwrap();
        alert('Movie updated successfully');
      } else {
        await createMovie(payload).unwrap();
        alert('Movie added successfully');
      }
      
      setShowForm(false);
      setEditingId(null);
    } catch (err) {
      alert(editingId ? 'Failed to update movie' : 'Failed to add movie');
    }
  };

  const handleDelete = async (id) => {
    if(window.confirm('Are you sure you want to delete this movie?')) {
      try {
        await deleteMovie(id).unwrap();
      } catch (err) {
        alert('Failed to delete movie');
      }
    }
  };

  return (
    <div className="admin-page">
      <div className="admin-page-header">
        <h2>Manage Movies</h2>
        <button className="btn btn-primary btn-sm" onClick={handleAddClick}>
          <Plus size={16} /> {showForm && !editingId ? 'Close Form' : 'Add Movie'}
        </button>
      </div>

      {showForm && (
        <div className="admin-form-container glass-card mb-4">
          <h3 className="mb-3">{editingId ? 'Edit Movie' : 'Add New Movie'}</h3>
          <form onSubmit={handleSubmit} className="grid grid-cols-2">
            <div className="form-group">
              <label className="form-label">Title</label>
              <input type="text" className="form-control" value={formData.title} onChange={e => setFormData({...formData, title: e.target.value})} required />
            </div>
            <div className="form-group">
              <label className="form-label">Genre</label>
              <input type="text" className="form-control" value={formData.genre} onChange={e => setFormData({...formData, genre: e.target.value})} required />
            </div>
            <div className="form-group">
              <label className="form-label">Language</label>
              <input type="text" className="form-control" value={formData.language} onChange={e => setFormData({...formData, language: e.target.value})} required />
            </div>
            <div className="form-group">
              <label className="form-label">Duration (mins)</label>
              <input type="number" className="form-control" value={formData.duration} onChange={e => setFormData({...formData, duration: e.target.value})} required />
            </div>
            <div className="form-group">
              <label className="form-label">Release Date</label>
              <input type="date" className="form-control" value={formData.releaseDate} onChange={e => setFormData({...formData, releaseDate: e.target.value})} required />
            </div>
            <div className="form-group">
              <label className="form-label">Director</label>
              <input type="text" className="form-control" value={formData.director} onChange={e => setFormData({...formData, director: e.target.value})} required />
            </div>
            <div className="form-group">
              <label className="form-label">Cast (comma separated)</label>
              <input type="text" className="form-control" value={formData.cast} onChange={e => setFormData({...formData, cast: e.target.value})} required />
            </div>
            <div className="form-group">
              <label className="form-label">Censor Rating</label>
              <select className="form-control" value={formData.censorRating} onChange={e => setFormData({...formData, censorRating: e.target.value})}>
                <option value="U">U</option>
                <option value="UA">UA</option>
                <option value="A">A</option>
              </select>
            </div>
            <div className="form-group">
              <label className="form-label">IMDb Rating (e.g. 8.5)</label>
              <input type="number" step="0.1" min="0" max="10" className="form-control" value={formData.imdbRating} onChange={e => setFormData({...formData, imdbRating: e.target.value})} />
            </div>
            <div className="form-group">
              <label className="form-label">Status</label>
              <select className="form-control" value={formData.status} onChange={e => setFormData({...formData, status: e.target.value})}>
                <option value="NOW_SHOWING">Now Showing</option>
                <option value="UPCOMING">Upcoming</option>
              </select>
            </div>
            <div className="form-group" style={{ gridColumn: 'span 2' }}>
              <label className="form-label">Description</label>
              <textarea className="form-control" rows="3" value={formData.description} onChange={e => setFormData({...formData, description: e.target.value})} required></textarea>
            </div>
            <div className="form-actions" style={{ gridColumn: 'span 2' }}>
              <button type="submit" className="btn btn-success" disabled={isCreating || isUpdating}>
                {isCreating || isUpdating ? 'Saving...' : (editingId ? 'Update Movie' : 'Save Movie')}
              </button>
              <button type="button" className="btn btn-outline" onClick={() => { setShowForm(false); setEditingId(null); }}>Cancel</button>
            </div>
          </form>
        </div>
      )}

      <div className="data-table-container">
        {isLoading ? (
          <div className="loader-container"><div className="spinner"></div></div>
        ) : (
          <table className="data-table">
            <thead>
              <tr>
                <th>Title</th>
                <th>Status</th>
                <th>Language</th>
                <th>Duration</th>
                <th>Release Date</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {movies.map(movie => (
                <tr key={movie.id}>
                  <td>{movie.title}</td>
                  <td>
                    <span className="tag" style={{ fontSize: '0.8rem', padding: '2px 6px' }}>
                      {movie.status === 'UPCOMING' ? 'Coming Soon' : 'Now Showing'}
                    </span>
                  </td>
                  <td>{movie.language}</td>
                  <td>{movie.duration}m</td>
                  <td>{movie.releaseDate}</td>
                  <td>
                    <div style={{ display: 'flex', gap: '10px' }}>
                      <button className="icon-btn text-primary" onClick={() => handleEditClick(movie)}>
                        <Edit size={18} />
                      </button>
                      <button className="icon-btn text-danger" onClick={() => handleDelete(movie.id)}>
                        <Trash2 size={18} />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
              {movies.length === 0 && (
                <tr>
                  <td colSpan="6" className="text-center text-muted">No movies found.</td>
                </tr>
              )}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
};

export default ManageMovies;
