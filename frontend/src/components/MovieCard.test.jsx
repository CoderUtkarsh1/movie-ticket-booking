import { describe, it, expect } from 'vitest';
import { screen } from '@testing-library/react';
import MovieCard from './MovieCard';
import { createMockMovie, renderWithProviders } from '../test/test-utils';

describe('MovieCard Component', () => {
  it('should render movie title', () => {
    const movie = createMockMovie({ title: 'Pushpa 3' });
    renderWithProviders(<MovieCard movie={movie} />);

    expect(screen.getByText('Pushpa 3')).toBeInTheDocument();
  });

  it('should render genre and language', () => {
    const movie = createMockMovie({ genre: 'Action', language: 'Hindi' });
    renderWithProviders(<MovieCard movie={movie} />);

    expect(screen.getByText('Action • Hindi')).toBeInTheDocument();
  });

  it('should show IMDb rating for NOW_SHOWING movies', () => {
    const movie = createMockMovie({ status: 'NOW_SHOWING', imdbRating: 8.5 });
    renderWithProviders(<MovieCard movie={movie} />);

    expect(screen.getByText(/8.5/)).toBeInTheDocument();
    expect(screen.getByText(/IMDb/)).toBeInTheDocument();
  });

  it('should show interested count for UPCOMING movies', () => {
    const movie = createMockMovie({
      status: 'UPCOMING',
      interestedUserIds: ['u1', 'u2', 'u3'],
    });
    renderWithProviders(<MovieCard movie={movie} />);

    expect(screen.getByText(/3 Interested/)).toBeInTheDocument();
  });

  it('should render placeholder when poster URL fails', () => {
    const movie = createMockMovie({ posterUrl: null, title: 'Pushpa' });
    renderWithProviders(<MovieCard movie={movie} />);

    expect(screen.getByText('P')).toBeInTheDocument(); // First char of title
  });

  it('should link to movie detail page', () => {
    const movie = createMockMovie({ id: 'mov-001' });
    renderWithProviders(<MovieCard movie={movie} />);

    const link = screen.getByRole('link');
    expect(link).toHaveAttribute('href', '/movies/mov-001');
  });
});
