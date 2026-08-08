package com.moviebooking.bookingservice.controller;

import com.moviebooking.bookingservice.dto.BookingRequest;
import com.moviebooking.bookingservice.entity.Booking;
import com.moviebooking.bookingservice.service.BookingService;
import com.moviebooking.common.constants.AppConstants;
import com.moviebooking.common.dto.ApiResponse;
import com.moviebooking.common.dto.BookingDto;
import com.moviebooking.common.exception.UnauthorizedException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @Value("${internal.api-key:}")
    private String internalApiKey;

    @PostMapping
    public ResponseEntity<ApiResponse<Booking>> createBooking(
            @RequestHeader(AppConstants.HEADER_USER_ID) Long userId,
            @RequestHeader(value = AppConstants.HEADER_USER_EMAIL, required = false) String userEmail,
            @Valid @RequestBody BookingRequest request) {
        Booking booking = bookingService.createBooking(userId, userEmail, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Booking created. Proceed to payment.", booking));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Booking>> getBookingById(@PathVariable Long id) {
        Booking booking = bookingService.getBookingById(id);
        return ResponseEntity.ok(ApiResponse.success(booking));
    }

    @GetMapping("/user")
    public ResponseEntity<ApiResponse<List<Booking>>> getUserBookings(
            @RequestHeader(AppConstants.HEADER_USER_ID) Long userId) {
        List<Booking> bookings = bookingService.getUserBookings(userId);
        return ResponseEntity.ok(ApiResponse.success(bookings));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Booking>> cancelBooking(
            @PathVariable Long id,
            @RequestHeader(AppConstants.HEADER_USER_ID) Long userId) {
        Booking booking = bookingService.cancelBooking(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled successfully", booking));
    }

    // ===== INTERNAL FEIGN ENDPOINTS (called by payment-service) =====
    // Secured with internal API key header

    @GetMapping("/internal/{id}")
    public ResponseEntity<ApiResponse<BookingDto>> getBookingInternal(
            @PathVariable Long id,
            @RequestHeader(value = "X-Internal-Api-Key", required = false) String apiKey) {
        validateInternalApiKey(apiKey);
        BookingDto dto = bookingService.getBookingAsDto(id);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @PutMapping("/internal/{id}/confirm-payment")
    public ResponseEntity<ApiResponse<Void>> confirmPayment(
            @PathVariable Long id,
            @RequestHeader(value = "X-Internal-Api-Key", required = false) String apiKey) {
        validateInternalApiKey(apiKey);
        bookingService.confirmPayment(id);
        return ResponseEntity.ok(ApiResponse.success("Booking confirmed after payment", null));
    }

    @PutMapping("/internal/{id}/fail-payment")
    public ResponseEntity<ApiResponse<Void>> failPayment(
            @PathVariable Long id,
            @RequestHeader(value = "X-Internal-Api-Key", required = false) String apiKey) {
        validateInternalApiKey(apiKey);
        bookingService.failPayment(id);
        return ResponseEntity.ok(ApiResponse.success("Booking failed after payment failure", null));
    }

    // ===== DASHBOARD ENDPOINTS =====

    @GetMapping("/dashboard/upcoming")
    public ResponseEntity<ApiResponse<List<Booking>>> getUpcomingBookings(
            @RequestHeader(AppConstants.HEADER_USER_ID) Long userId) {
        List<Booking> bookings = bookingService.getUpcomingBookings(userId);
        return ResponseEntity.ok(ApiResponse.success(bookings));
    }

    @GetMapping("/dashboard/past")
    public ResponseEntity<ApiResponse<List<Booking>>> getPastBookings(
            @RequestHeader(AppConstants.HEADER_USER_ID) Long userId) {
        List<Booking> bookings = bookingService.getPastBookings(userId);
        return ResponseEntity.ok(ApiResponse.success(bookings));
    }

    private void validateInternalApiKey(String apiKey) {
        if (internalApiKey != null && !internalApiKey.isEmpty() && !internalApiKey.equals(apiKey)) {
            throw new UnauthorizedException("Invalid or missing internal API key");
        }
    }
}
