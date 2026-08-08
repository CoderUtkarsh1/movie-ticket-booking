import { Outlet, NavLink } from 'react-router-dom';
import { Ticket, Wallet, Bell } from 'lucide-react';
import './CustomerDashboard.css';

const CustomerDashboard = () => {
  return (
    <div className="customer-dashboard container mt-4 mb-4">
      <div className="dashboard-layout">
        
        <aside className="dashboard-sidebar glass-card">
          <div className="sidebar-header">
            <h3>My Dashboard</h3>
          </div>
          <nav className="sidebar-nav">
            <NavLink to="/dashboard" end className={({ isActive }) => isActive ? 'nav-item active' : 'nav-item'}>
              <Ticket size={20} />
              <span>My Bookings</span>
            </NavLink>
            <NavLink to="/dashboard/wallet" className={({ isActive }) => isActive ? 'nav-item active' : 'nav-item'}>
              <Wallet size={20} />
              <span>My Wallet</span>
            </NavLink>
            <NavLink to="/dashboard/notifications" className={({ isActive }) => isActive ? 'nav-item active' : 'nav-item'}>
              <Bell size={20} />
              <span>Notifications</span>
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

export default CustomerDashboard;
