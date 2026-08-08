import { Link, useNavigate } from 'react-router-dom';
import { useSelector, useDispatch } from 'react-redux';
import { selectIsAuthenticated, selectIsAdmin, logout, selectCurrentUser } from '../features/auth/authSlice';
import { useLogoutMutation } from '../features/auth/authApiSlice';
import { Film, User, LogOut, LayoutDashboard, Settings } from 'lucide-react';
import './Navbar.css';

const Navbar = () => {
  const isAuthenticated = useSelector(selectIsAuthenticated);
  const isAdmin = useSelector(selectIsAdmin);
  const user = useSelector(selectCurrentUser);
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const [logoutApi] = useLogoutMutation();

  const handleLogout = async () => {
    try {
      await logoutApi().unwrap();  // Clear httpOnly cookies on server
    } catch (err) {
      // Even if server logout fails, clear local state
      console.error('Server logout failed:', err);
    }
    dispatch(logout());  // Clear Redux state + localStorage
    navigate('/');
  };

  return (
    <nav className="navbar">
      <div className="container navbar-container">
        <Link to="/" className="navbar-logo">
          <Film className="logo-icon" />
          <span>MovieTickets</span>
        </Link>
        
        <div className="navbar-links">
          <Link to="/" className="nav-link">Movies</Link>
          <Link to="/theaters" className="nav-link">Theaters</Link>
        </div>

        <div className="navbar-auth">
          {isAuthenticated ? (
            <div className="user-menu">
              <span className="welcome-text">Hi, {user?.name?.split(' ')[0]}</span>
              {isAdmin ? (
                <Link to="/admin" className="nav-icon-link" title="Admin Dashboard">
                  <Settings size={20} />
                </Link>
              ) : (
                <Link to="/dashboard" className="nav-icon-link" title="My Dashboard">
                  <LayoutDashboard size={20} />
                </Link>
              )}
              <button onClick={handleLogout} className="btn btn-outline nav-btn">
                <LogOut size={16} style={{ marginRight: '6px' }} />
                Logout
              </button>
            </div>
          ) : (
            <div className="auth-buttons">
              <Link to="/login" className="btn btn-primary">Login / Register</Link>
            </div>
          )}
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
