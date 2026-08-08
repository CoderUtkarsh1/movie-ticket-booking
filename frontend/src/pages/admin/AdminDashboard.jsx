import { Outlet, NavLink } from 'react-router-dom';
import { Film, MonitorPlay, CalendarDays } from 'lucide-react';
import './AdminDashboard.css';

const AdminDashboard = () => {
  return (
    <div className="admin-dashboard container mt-4 mb-4">
      <div className="dashboard-layout">
        
        <aside className="dashboard-sidebar glass-card">
          <div className="sidebar-header">
            <h3>Admin Portal</h3>
          </div>
          <nav className="sidebar-nav">
            <NavLink to="/admin" end className={({ isActive }) => isActive ? 'nav-item active' : 'nav-item'}>
              <Film size={20} />
              <span>Manage Movies</span>
            </NavLink>
            <NavLink to="/admin/theaters" className={({ isActive }) => isActive ? 'nav-item active' : 'nav-item'}>
              <MonitorPlay size={20} />
              <span>Manage Theaters</span>
            </NavLink>
            <NavLink to="/admin/shows" className={({ isActive }) => isActive ? 'nav-item active' : 'nav-item'}>
              <CalendarDays size={20} />
              <span>Manage Shows</span>
            </NavLink>
          </nav>
        </aside>

        <main className="dashboard-content glass-card">
          <Outlet />
        </main>

      </div>
    </div>
  );
};

export default AdminDashboard;
