import { Routes, Route } from 'react-router-dom';
import Navbar from './components/Navbar';
import ProtectedRoute from './components/ProtectedRoute';
import AdminRoute from './components/AdminRoute';
import ErrorBoundary from './components/ErrorBoundary';

// Public Pages
import HomePage from './pages/public/HomePage';
import MovieDetailPage from './pages/public/MovieDetailPage';
import TheatersPage from './pages/public/TheatersPage';
import ShowSeatsPage from './pages/public/ShowSeatsPage';
import LoginPage from './pages/auth/LoginPage';

// Customer Pages
import CustomerDashboard from './pages/customer/CustomerDashboard';
import MyBookingsPage from './pages/customer/MyBookingsPage';
import WalletPage from './pages/customer/WalletPage';
import NotificationsPage from './pages/customer/NotificationsPage';
import CheckoutPage from './pages/customer/CheckoutPage';
import BookingSuccessPage from './pages/customer/BookingSuccessPage';

// Admin Pages
import AdminDashboard from './pages/admin/AdminDashboard';
import ManageMovies from './pages/admin/ManageMovies';
import ManageTheaters from './pages/admin/ManageTheaters';
import ManageShows from './pages/admin/ManageShows';

const App = () => {
  return (
    <ErrorBoundary>
      <Navbar />
      <main className="main-content">
        <Routes>
          {/* Public Routes */}
          <Route path="/" element={<HomePage />} />
          <Route path="/movies/:id" element={<MovieDetailPage />} />
          <Route path="/theaters" element={<TheatersPage />} />
          <Route path="/shows/:id/seats" element={<ShowSeatsPage />} />
          <Route path="/login" element={<LoginPage />} />

          {/* Customer Routes (Protected) */}
          <Route element={<ProtectedRoute />}>
            <Route path="/dashboard" element={<CustomerDashboard />}>
              <Route index element={<MyBookingsPage />} />
              <Route path="wallet" element={<WalletPage />} />
              <Route path="notifications" element={<NotificationsPage />} />
            </Route>
            <Route path="/checkout/:bookingId" element={<CheckoutPage />} />
            <Route path="/booking-success/:bookingId" element={<BookingSuccessPage />} />
          </Route>

          {/* Admin Routes (Admin Only) */}
          <Route element={<AdminRoute />}>
            <Route path="/admin" element={<AdminDashboard />}>
              <Route index element={<ManageMovies />} />
              <Route path="theaters" element={<ManageTheaters />} />
              <Route path="shows" element={<ManageShows />} />
            </Route>
          </Route>
        </Routes>
      </main>
    </ErrorBoundary>
  );
};

export default App;
