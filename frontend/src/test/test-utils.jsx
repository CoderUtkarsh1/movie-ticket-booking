import { render } from '@testing-library/react';
import { configureStore } from '@reduxjs/toolkit';
import { Provider } from 'react-redux';
import { MemoryRouter } from 'react-router-dom';
import authReducer from '../features/auth/authSlice';

/**
 * Test utility — wraps component with Redux Provider + Router
 * for component testing with full context.
 */
export function renderWithProviders(
  ui,
  {
    preloadedState = {},
    store = configureStore({
      reducer: {
        auth: authReducer,
      },
      preloadedState,
    }),
    route = '/',
    ...renderOptions
  } = {}
) {
  function Wrapper({ children }) {
    return (
      <Provider store={store}>
        <MemoryRouter initialEntries={[route]}>
          {children}
        </MemoryRouter>
      </Provider>
    );
  }

  return { store, ...render(ui, { wrapper: Wrapper, ...renderOptions }) };
}

/**
 * Creates a mock movie object for testing
 */
export function createMockMovie(overrides = {}) {
  return {
    id: 'mov-001',
    title: 'Pushpa 3: The Rule',
    genre: 'Action',
    language: 'Hindi',
    status: 'NOW_SHOWING',
    posterUrl: 'https://example.com/pushpa3.jpg',
    imdbRating: 8.5,
    rating: 4.5,
    interestedUserIds: [],
    ...overrides,
  };
}

/**
 * Creates a mock booking object for testing
 */
export function createMockBooking(overrides = {}) {
  return {
    id: 1,
    bookingCode: 'MBK-20260720-AB12',
    movieName: 'Pushpa 3: The Rule',
    theaterName: 'PVR Phoenix',
    screenName: 'Screen 1',
    showDate: '2026-07-20',
    showTime: '18:30:00',
    totalSeats: 2,
    totalAmount: 500.00,
    status: 'CONFIRMED',
    bookedSeats: [
      { seatRow: 'A', seatNumber: 1 },
      { seatRow: 'A', seatNumber: 2 },
    ],
    ...overrides,
  };
}
