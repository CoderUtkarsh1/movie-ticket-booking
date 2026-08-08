package com.moviebooking.bookingservice.service;

import com.moviebooking.bookingservice.dto.BookingRequest;
import com.moviebooking.bookingservice.entity.Booking;
import com.moviebooking.common.dto.BookingDto;

import java.util.List;

public interface BookingService {

    Booking createBooking(Long userId, String userEmail, BookingRequest request);

    Booking confirmPayment(Long bookingId);

    Booking failPayment(Long bookingId);

    Booking getBookingById(Long id);

    BookingDto getBookingAsDto(Long id);

    List<Booking> getUserBookings(Long userId);

    List<Booking> getUpcomingBookings(Long userId);

    List<Booking> getPastBookings(Long userId);

    Booking cancelBooking(Long id, Long userId);
}
