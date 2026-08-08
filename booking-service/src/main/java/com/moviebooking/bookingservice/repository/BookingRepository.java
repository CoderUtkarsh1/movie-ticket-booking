package com.moviebooking.bookingservice.repository;

import com.moviebooking.bookingservice.entity.Booking;
import com.moviebooking.common.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<Booking> findByBookingCode(String bookingCode);

    // Dashboard: upcoming confirmed bookings (show date >= today)
    List<Booking> findByUserIdAndStatusAndShowDateGreaterThanEqualOrderByShowDateAsc(
            Long userId, BookingStatus status, LocalDate date);

    // Dashboard: past bookings (show date < today) regardless of status
    List<Booking> findByUserIdAndShowDateBeforeOrderByShowDateDesc(
            Long userId, LocalDate date);
}
