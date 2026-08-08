import { describe, it, expect, vi } from 'vitest';
import { screen, fireEvent } from '@testing-library/react';
import BookingCard from './BookingCard';
import { createMockBooking, renderWithProviders } from '../test/test-utils';

// Mock the booking API slice
vi.mock('../features/bookings/bookingApiSlice', () => ({
  useCancelBookingMutation: () => [vi.fn(), { isLoading: false }],
}));

describe('BookingCard Component', () => {
  it('should render movie name', () => {
    const booking = createMockBooking({ movieName: 'Pushpa 3' });
    renderWithProviders(<BookingCard booking={booking} isUpcoming={true} />);

    expect(screen.getByText('Pushpa 3')).toBeInTheDocument();
  });

  it('should render theater and screen', () => {
    const booking = createMockBooking({
      theaterName: 'PVR Phoenix',
      screenName: 'Screen 1',
    });
    renderWithProviders(<BookingCard booking={booking} isUpcoming={true} />);

    expect(screen.getByText('PVR Phoenix - Screen 1')).toBeInTheDocument();
  });

  it('should render booking code', () => {
    const booking = createMockBooking({ bookingCode: 'MBK-20260720-AB12' });
    renderWithProviders(<BookingCard booking={booking} isUpcoming={false} />);

    expect(screen.getByText('ID: MBK-20260720-AB12')).toBeInTheDocument();
  });

  it('should render status badge', () => {
    const booking = createMockBooking({ status: 'CONFIRMED' });
    renderWithProviders(<BookingCard booking={booking} isUpcoming={false} />);

    expect(screen.getByText('CONFIRMED')).toBeInTheDocument();
  });

  it('should show Cancel button for upcoming CONFIRMED bookings', () => {
    const booking = createMockBooking({ status: 'CONFIRMED' });
    renderWithProviders(<BookingCard booking={booking} isUpcoming={true} />);

    expect(screen.getByText('Cancel')).toBeInTheDocument();
  });

  it('should NOT show Cancel button for CANCELLED bookings', () => {
    const booking = createMockBooking({ status: 'CANCELLED' });
    renderWithProviders(<BookingCard booking={booking} isUpcoming={true} />);

    expect(screen.queryByText('Cancel')).not.toBeInTheDocument();
  });

  it('should toggle details on View Details click', () => {
    const booking = createMockBooking({
      totalSeats: 2,
      totalAmount: 500.00,
    });
    renderWithProviders(<BookingCard booking={booking} isUpcoming={false} />);

    // Initially no details
    expect(screen.queryByText('Total Seats')).not.toBeInTheDocument();

    // Click to show
    fireEvent.click(screen.getByText('View Details'));
    expect(screen.getByText('Total Seats')).toBeInTheDocument();
    expect(screen.getByText('2')).toBeInTheDocument();
    expect(screen.getByText('₹500')).toBeInTheDocument();

    // Click to hide
    fireEvent.click(screen.getByText('Hide Details'));
    expect(screen.queryByText('Total Seats')).not.toBeInTheDocument();
  });
});
