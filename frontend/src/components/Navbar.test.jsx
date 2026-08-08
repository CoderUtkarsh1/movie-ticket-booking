import { describe, it, expect, vi } from 'vitest';
import { screen } from '@testing-library/react';
import Navbar from './Navbar';
import { renderWithProviders } from '../test/test-utils';

// Mock the auth API slice
vi.mock('../features/auth/authApiSlice', () => ({
  useLogoutMutation: () => [vi.fn(), { isLoading: false }],
}));

describe('Navbar Component', () => {
  it('should render logo text', () => {
    renderWithProviders(<Navbar />);
    expect(screen.getByText('MovieTickets')).toBeInTheDocument();
  });

  it('should render Movies and Theaters links', () => {
    renderWithProviders(<Navbar />);
    expect(screen.getByText('Movies')).toBeInTheDocument();
    expect(screen.getByText('Theaters')).toBeInTheDocument();
  });

  it('should show Login button when not authenticated', () => {
    renderWithProviders(<Navbar />, {
      preloadedState: {
        auth: { user: null, token: null, isAuthenticated: false, isAdmin: false },
      },
    });

    expect(screen.getByText('Login / Register')).toBeInTheDocument();
  });

  it('should show user name and Logout when authenticated', () => {
    renderWithProviders(<Navbar />, {
      preloadedState: {
        auth: {
          user: { name: 'Utkarsh Singh', email: 'utkarsh@test.com' },
          token: 'fake-jwt',
          isAuthenticated: true,
          isAdmin: false,
        },
      },
    });

    expect(screen.getByText('Hi, Utkarsh')).toBeInTheDocument();
    expect(screen.getByText('Logout')).toBeInTheDocument();
    expect(screen.queryByText('Login / Register')).not.toBeInTheDocument();
  });

  it('should show Admin link for admin users', () => {
    renderWithProviders(<Navbar />, {
      preloadedState: {
        auth: {
          user: { name: 'Admin User', email: 'admin@test.com', role: 'ADMIN' },
          token: 'fake-jwt',
          isAuthenticated: true,
          isAdmin: true,
        },
      },
    });

    expect(screen.getByTitle('Admin Dashboard')).toBeInTheDocument();
  });

  it('should show Dashboard link for regular users', () => {
    renderWithProviders(<Navbar />, {
      preloadedState: {
        auth: {
          user: { name: 'Regular User', email: 'user@test.com' },
          token: 'fake-jwt',
          isAuthenticated: true,
          isAdmin: false,
        },
      },
    });

    expect(screen.getByTitle('My Dashboard')).toBeInTheDocument();
  });
});
