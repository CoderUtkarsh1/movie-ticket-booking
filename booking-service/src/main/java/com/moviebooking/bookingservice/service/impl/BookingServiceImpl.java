package com.moviebooking.bookingservice.service.impl;

import com.moviebooking.bookingservice.dto.BookingRequest;
import com.moviebooking.bookingservice.entity.BookedSeat;
import com.moviebooking.bookingservice.entity.Booking;
import com.moviebooking.bookingservice.feign.ShowServiceClient;
import com.moviebooking.bookingservice.kafka.BookingEventProducer;
import com.moviebooking.bookingservice.repository.BookingRepository;
import com.moviebooking.common.constants.AppConstants;
import com.moviebooking.common.dto.BookingDto;
import com.moviebooking.common.dto.SeatActionRequest;
import com.moviebooking.common.enums.BookingStatus;
import com.moviebooking.common.event.BookingEvent;
import com.moviebooking.common.exception.BadRequestException;
import com.moviebooking.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements com.moviebooking.bookingservice.service.BookingService {

    private final BookingRepository bookingRepository;
    private final BookingEventProducer bookingEventProducer;
    private final ShowServiceClient showServiceClient;

    @Override
    @Transactional
    public Booking createBooking(Long userId, String userEmail, BookingRequest request) {
        // Generate unique booking code
        String bookingCode = generateBookingCode();

        // Calculate total amount
        BigDecimal totalAmount = request.getSeats().stream()
                .map(BookingRequest.SeatInfo::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Create booking
        Booking booking = Booking.builder()
                .bookingCode(bookingCode)
                .userId(userId)
                .userEmail(userEmail)
                .showId(request.getShowId())
                .movieName(request.getMovieName())
                .theaterName(request.getTheaterName())
                .screenName(request.getScreenName())
                .showDate(request.getShowDate())
                .showTime(request.getShowTime())
                .totalSeats(request.getSeats().size())
                .totalAmount(totalAmount)
                .status(BookingStatus.PENDING)
                .build();

        booking = bookingRepository.save(booking);

        // Create booked seats
        Booking finalBooking = booking;
        List<BookedSeat> bookedSeats = request.getSeats().stream().map(seat ->
                BookedSeat.builder()
                        .booking(finalBooking)
                        .seatId(seat.getSeatId())
                        .seatRow(seat.getSeatRow())
                        .seatNumber(seat.getSeatNumber())
                        .seatType(seat.getSeatType())
                        .price(seat.getPrice())
                        .build()
        ).collect(Collectors.toList());

        booking.setBookedSeats(bookedSeats);
        booking = bookingRepository.save(booking);

        log.info("Booking created: {} for user {} (NO Kafka event — sync flow)", bookingCode, userId);

        // NO Kafka event on creation — payment-service will call us via Feign
        return booking;
    }

    /**
     * Called by payment-service via Feign after successful payment.
     * Confirms booking + confirms seats in show-service + publishes Kafka for notifications.
     */
    @Override
    @Transactional
    public Booking confirmPayment(Long bookingId) {
        Booking booking = getBookingById(bookingId);

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BadRequestException("Booking is not in PENDING state, cannot confirm");
        }

        // 1. Update booking status
        booking.setStatus(BookingStatus.CONFIRMED);
        booking = bookingRepository.save(booking);
        log.info("Booking {} confirmed after payment", booking.getBookingCode());

        // 2. Confirm seats in show-service via Feign (BLOCKED → BOOKED)
        try {
            List<Long> seatIds = booking.getBookedSeats().stream()
                    .map(BookedSeat::getSeatId)
                    .collect(Collectors.toList());

            SeatActionRequest seatRequest = SeatActionRequest.builder()
                    .userId(booking.getUserId())
                    .seatIds(seatIds)
                    .build();

            showServiceClient.confirmSeats(booking.getShowId(), seatRequest);
            log.info("Seats confirmed in show-service for booking {}", booking.getBookingCode());
        } catch (Exception e) {
            log.error("Failed to confirm seats in show-service for booking {}: {}",
                    booking.getBookingCode(), e.getMessage());
            // Booking is already CONFIRMED — seats will self-heal or can be manually fixed
        }

        // 3. Publish Kafka event for notification-service
        BookingEvent event = buildBookingEvent(booking, "CONFIRMED");
        bookingEventProducer.sendBookingConfirmed(event);

        return booking;
    }

    /**
     * Called by payment-service via Feign after failed payment.
     * Fails booking + releases seats in show-service + publishes Kafka for notifications.
     */
    @Override
    @Transactional
    public Booking failPayment(Long bookingId) {
        Booking booking = getBookingById(bookingId);

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BadRequestException("Booking is not in PENDING state, cannot fail");
        }

        // 1. Update booking status
        booking.setStatus(BookingStatus.FAILED);
        booking = bookingRepository.save(booking);
        log.info("Booking {} failed after payment failure", booking.getBookingCode());

        // 2. Release seats in show-service via Feign (BLOCKED → AVAILABLE)
        try {
            List<Long> seatIds = booking.getBookedSeats().stream()
                    .map(BookedSeat::getSeatId)
                    .collect(Collectors.toList());

            SeatActionRequest seatRequest = SeatActionRequest.builder()
                    .userId(booking.getUserId())
                    .seatIds(seatIds)
                    .build();

            showServiceClient.releaseSeats(booking.getShowId(), seatRequest);
            log.info("Seats released in show-service for failed booking {}", booking.getBookingCode());
        } catch (Exception e) {
            log.error("Failed to release seats in show-service for booking {}: {}",
                    booking.getBookingCode(), e.getMessage());
        }

        // 3. Publish Kafka event for notification-service
        BookingEvent event = buildBookingEvent(booking, "CANCELLED");
        bookingEventProducer.sendBookingCancelled(event);

        return booking;
    }

    @Override
    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));
    }

    /**
     * Returns booking data as a DTO for Feign responses (avoids exposing JPA entities).
     */
    @Override
    public BookingDto getBookingAsDto(Long id) {
        Booking booking = getBookingById(id);
        List<Long> seatIds = booking.getBookedSeats().stream()
                .map(BookedSeat::getSeatId)
                .collect(Collectors.toList());

        return BookingDto.builder()
                .id(booking.getId())
                .bookingCode(booking.getBookingCode())
                .userId(booking.getUserId())
                .showId(booking.getShowId())
                .movieName(booking.getMovieName())
                .theaterName(booking.getTheaterName())
                .screenName(booking.getScreenName())
                .showDate(booking.getShowDate())
                .showTime(booking.getShowTime())
                .totalSeats(booking.getTotalSeats())
                .totalAmount(booking.getTotalAmount())
                .status(booking.getStatus().name())
                .seatIds(seatIds)
                .build();
    }

    @Override
    public List<Booking> getUserBookings(Long userId) {
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Dashboard: upcoming confirmed bookings (showDate >= today)
     */
    @Override
    public List<Booking> getUpcomingBookings(Long userId) {
        return bookingRepository.findByUserIdAndStatusAndShowDateGreaterThanEqualOrderByShowDateAsc(
                userId, BookingStatus.CONFIRMED, LocalDate.now());
    }

    /**
     * Dashboard: past bookings (showDate < today)
     */
    @Override
    public List<Booking> getPastBookings(Long userId) {
        return bookingRepository.findByUserIdAndShowDateBeforeOrderByShowDateDesc(
                userId, LocalDate.now());
    }

    @Override
    @Transactional
    public Booking cancelBooking(Long id, Long userId) {
        Booking booking = getBookingById(id);

        if (!booking.getUserId().equals(userId)) {
            throw new BadRequestException("You can only cancel your own bookings");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BadRequestException("Booking is already cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking = bookingRepository.save(booking);

        // Release seats in show-service via Feign
        try {
            List<Long> seatIds = booking.getBookedSeats().stream()
                    .map(BookedSeat::getSeatId)
                    .collect(Collectors.toList());

            SeatActionRequest seatRequest = SeatActionRequest.builder()
                    .userId(userId)
                    .seatIds(seatIds)
                    .build();

            showServiceClient.releaseSeats(booking.getShowId(), seatRequest);
        } catch (Exception e) {
            log.error("Failed to release seats for cancelled booking {}: {}",
                    booking.getBookingCode(), e.getMessage());
        }

        // Publish cancellation event for notifications
        BookingEvent event = buildBookingEvent(booking, "CANCELLED");
        bookingEventProducer.sendBookingCancelled(event);
        log.info("Booking cancelled: {}", booking.getBookingCode());

        return booking;
    }

    private BookingEvent buildBookingEvent(Booking booking, String status) {
        List<Long> seatIds = booking.getBookedSeats().stream()
                .map(BookedSeat::getSeatId)
                .collect(Collectors.toList());

        return BookingEvent.builder()
                .bookingId(booking.getId())
                .bookingCode(booking.getBookingCode())
                .userId(booking.getUserId())
                .userEmail(booking.getUserEmail())
                .showId(booking.getShowId())
                .movieName(booking.getMovieName())
                .theaterName(booking.getTheaterName())
                .screenName(booking.getScreenName())
                .showDate(booking.getShowDate())
                .showTime(booking.getShowTime())
                .totalSeats(booking.getTotalSeats())
                .totalAmount(booking.getTotalAmount())
                .seatIds(seatIds)
                .status(status)
                .timestamp(Instant.now())
                .build();
    }

    private String generateBookingCode() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uuid = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return AppConstants.BOOKING_CODE_PREFIX + "-" + date + "-" + uuid;
    }
}
