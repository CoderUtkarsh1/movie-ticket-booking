import { useState } from 'react';
import { useGetTheatersQuery, useCreateTheaterMutation, useGetScreensQuery, useAddScreenMutation } from '../../features/theaters/theaterApiSlice';
import { Plus, Trash2, Monitor, ChevronDown, ChevronUp } from 'lucide-react';
import './AdminPages.css';

const SEAT_TYPES = ['REGULAR', 'PREMIUM', 'VIP'];
const SCREEN_TYPES = ['TWO_D', 'THREE_D', 'IMAX'];
const DEFAULT_PRICES = { REGULAR: 150, PREMIUM: 250, VIP: 400 };

const ManageTheaters = () => {
  const { data: theatersResponse, isLoading } = useGetTheatersQuery();
  const [createTheater, { isLoading: isCreating }] = useCreateTheaterMutation();

  const [showForm, setShowForm] = useState(false);
  const [expandedTheater, setExpandedTheater] = useState(null);
  const [showScreenForm, setShowScreenForm] = useState(null); // theaterId

  const [formData, setFormData] = useState({ name: '', city: '', address: '' });

  const [screenForm, setScreenForm] = useState({
    screenName: '',
    screenType: 'TWO_D',
    rows: 5,
    seatsPerRow: 10,
    seatPrices: { REGULAR: 150, PREMIUM: 250, VIP: 400 },
    // Rows A,B = VIP, C,D = PREMIUM, rest = REGULAR
    rowConfig: {} // will be auto-generated
  });

  const theaters = theatersResponse || [];

  const handleCreateTheater = async (e) => {
    e.preventDefault();
    try {
      await createTheater(formData).unwrap();
      setShowForm(false);
      setFormData({ name: '', city: '', address: '' });
      alert('Theater added successfully');
    } catch (err) {
      alert('Failed to add theater');
    }
  };

  const generateSeats = () => {
    const seats = [];
    const totalRows = screenForm.rows;
    for (let r = 0; r < totalRows; r++) {
      const rowLetter = String.fromCharCode(65 + r); // A, B, C...
      let seatType = 'REGULAR';
      if (r < 2) seatType = 'VIP';
      else if (r < 4) seatType = 'PREMIUM';
      
      for (let s = 1; s <= screenForm.seatsPerRow; s++) {
        seats.push({
          seatRow: rowLetter,
          seatNumber: s,
          seatType: seatType,
          price: screenForm.seatPrices[seatType]
        });
      }
    }
    return seats;
  };

  return (
    <div className="admin-page">
      <div className="admin-page-header">
        <h2>Manage Theaters</h2>
        <button className="btn btn-primary btn-sm" onClick={() => setShowForm(!showForm)}>
          <Plus size={16} /> Add Theater
        </button>
      </div>

      {showForm && (
        <div className="admin-form-container glass-card mb-4">
          <form onSubmit={handleCreateTheater} className="grid grid-cols-2">
            <div className="form-group">
              <label className="form-label">Theater Name</label>
              <input type="text" className="form-control" value={formData.name} onChange={e => setFormData({...formData, name: e.target.value})} required />
            </div>
            <div className="form-group">
              <label className="form-label">City</label>
              <input type="text" className="form-control" value={formData.city} onChange={e => setFormData({...formData, city: e.target.value})} required />
            </div>
            <div className="form-group" style={{ gridColumn: 'span 2' }}>
              <label className="form-label">Full Address</label>
              <textarea className="form-control" rows="2" value={formData.address} onChange={e => setFormData({...formData, address: e.target.value})} required></textarea>
            </div>
            <div className="form-actions" style={{ gridColumn: 'span 2' }}>
              <button type="submit" className="btn btn-success" disabled={isCreating}>
                {isCreating ? 'Saving...' : 'Save Theater'}
              </button>
              <button type="button" className="btn btn-outline" onClick={() => setShowForm(false)}>Cancel</button>
            </div>
          </form>
        </div>
      )}

      <div className="data-table-container">
        {isLoading ? (
          <div className="loader-container"><div className="spinner"></div></div>
        ) : (
          <div className="theater-list">
            {theaters.map(theater => (
              <TheaterCard 
                key={theater.id} 
                theater={theater}
                isExpanded={expandedTheater === theater.id}
                onToggle={() => setExpandedTheater(expandedTheater === theater.id ? null : theater.id)}
                showScreenForm={showScreenForm === theater.id}
                onToggleScreenForm={() => setShowScreenForm(showScreenForm === theater.id ? null : theater.id)}
                screenForm={screenForm}
                setScreenForm={setScreenForm}
                generateSeats={generateSeats}
              />
            ))}
            {theaters.length === 0 && (
              <div className="glass-card p-4 text-center text-muted">No theaters found.</div>
            )}
          </div>
        )}
      </div>
    </div>
  );
};

const TheaterCard = ({ theater, isExpanded, onToggle, showScreenForm, onToggleScreenForm, screenForm, setScreenForm, generateSeats }) => {
  const { data: screensRes, isLoading: screensLoading } = useGetScreensQuery(theater.id, { skip: !isExpanded });
  const [addScreen, { isLoading: isAddingScreen }] = useAddScreenMutation();
  const screens = screensRes || [];

  const handleAddScreen = async (e) => {
    e.preventDefault();
    try {
      const seats = generateSeats();
      const payload = {
        screenName: screenForm.screenName,
        screenType: screenForm.screenType,
        seats: seats
      };
      await addScreen({ theaterId: theater.id, ...payload }).unwrap();
      onToggleScreenForm();
      setScreenForm(prev => ({ ...prev, screenName: '' }));
      alert(`Screen added with ${seats.length} seats!`);
    } catch (err) {
      alert('Failed to add screen: ' + (err.data?.message || err.message));
    }
  };

  return (
    <div className="glass-card mb-3" style={{ padding: '1rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', cursor: 'pointer' }} onClick={onToggle}>
        <div>
          <strong style={{ fontSize: '1.1rem' }}>{theater.name}</strong>
          <span style={{ color: 'var(--text-secondary)', marginLeft: '12px' }}>{theater.city}</span>
        </div>
        <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
          <button className="btn btn-primary btn-sm" onClick={(e) => { e.stopPropagation(); onToggleScreenForm(); }}>
            <Monitor size={14} /> Add Screen
          </button>
          {isExpanded ? <ChevronUp size={20} /> : <ChevronDown size={20} />}
        </div>
      </div>
      
      <div style={{ color: 'var(--text-secondary)', fontSize: '0.85rem', marginTop: '4px' }}>{theater.address}</div>

      {isExpanded && (
        <div style={{ marginTop: '1rem', borderTop: '1px solid rgba(255,255,255,0.1)', paddingTop: '1rem' }}>
          <h4 style={{ marginBottom: '0.5rem' }}>Screens</h4>
          {screensLoading ? (
            <div className="spinner" style={{width: '20px', height: '20px'}}></div>
          ) : screens.length > 0 ? (
            <table className="data-table">
              <thead>
                <tr>
                  <th>Screen</th>
                  <th>Type</th>
                  <th>Total Seats</th>
                </tr>
              </thead>
              <tbody>
                {screens.map(s => (
                  <tr key={s.id}>
                    <td>{s.screenName || s.name}</td>
                    <td>{s.screenType}</td>
                    <td>{s.totalSeats}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          ) : (
            <p style={{ color: 'var(--text-secondary)' }}>No screens added yet.</p>
          )}
        </div>
      )}

      {showScreenForm && (
        <div style={{ marginTop: '1rem', borderTop: '1px solid rgba(255,255,255,0.1)', paddingTop: '1rem' }}>
          <h4 style={{ marginBottom: '0.75rem' }}>Add New Screen</h4>
          <form onSubmit={handleAddScreen} className="grid grid-cols-2">
            <div className="form-group">
              <label className="form-label">Screen Name</label>
              <input type="text" className="form-control" placeholder="e.g. Audi 1" value={screenForm.screenName} onChange={e => setScreenForm({...screenForm, screenName: e.target.value})} required />
            </div>
            <div className="form-group">
              <label className="form-label">Screen Type</label>
              <select className="form-control" value={screenForm.screenType} onChange={e => setScreenForm({...screenForm, screenType: e.target.value})}>
                {SCREEN_TYPES.map(t => <option key={t} value={t}>{t.replace('_', ' ')}</option>)}
              </select>
            </div>
            <div className="form-group">
              <label className="form-label">Number of Rows</label>
              <input type="number" className="form-control" min="1" max="26" value={screenForm.rows} onChange={e => setScreenForm({...screenForm, rows: parseInt(e.target.value) || 1})} required />
            </div>
            <div className="form-group">
              <label className="form-label">Seats Per Row</label>
              <input type="number" className="form-control" min="1" max="30" value={screenForm.seatsPerRow} onChange={e => setScreenForm({...screenForm, seatsPerRow: parseInt(e.target.value) || 1})} required />
            </div>

            <div style={{ gridColumn: 'span 2', marginBottom: '1rem' }}>
              <label className="form-label" style={{ marginBottom: '0.5rem', display: 'block' }}>Seat Prices (₹)</label>
              <div style={{ display: 'flex', gap: '1rem' }}>
                {SEAT_TYPES.map(type => (
                  <div key={type} style={{ flex: 1 }}>
                    <label style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>{type}</label>
                    <input 
                      type="number" 
                      className="form-control" 
                      value={screenForm.seatPrices[type]} 
                      onChange={e => setScreenForm({...screenForm, seatPrices: {...screenForm.seatPrices, [type]: parseFloat(e.target.value) || 0}})}
                    />
                  </div>
                ))}
              </div>
              <p style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: '6px' }}>
                Row A-B = VIP, C-D = Premium, Rest = Regular
              </p>
            </div>

            <div style={{ gridColumn: 'span 2', marginBottom: '0.75rem', padding: '0.75rem', background: 'rgba(255,255,255,0.05)', borderRadius: '8px' }}>
              <strong>Preview: </strong>
              {screenForm.rows} rows × {screenForm.seatsPerRow} seats = <strong>{screenForm.rows * screenForm.seatsPerRow} total seats</strong>
              <span style={{ marginLeft: '12px', color: 'var(--text-secondary)' }}>
                ({Math.min(2, screenForm.rows) * screenForm.seatsPerRow} VIP, {Math.min(2, Math.max(0, screenForm.rows - 2)) * screenForm.seatsPerRow} Premium, {Math.max(0, screenForm.rows - 4) * screenForm.seatsPerRow} Regular)
              </span>
            </div>

            <div className="form-actions" style={{ gridColumn: 'span 2' }}>
              <button type="submit" className="btn btn-success" disabled={isAddingScreen}>
                {isAddingScreen ? 'Adding...' : `Add Screen (${screenForm.rows * screenForm.seatsPerRow} seats)`}
              </button>
              <button type="button" className="btn btn-outline" onClick={onToggleScreenForm}>Cancel</button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
};

export default ManageTheaters;
