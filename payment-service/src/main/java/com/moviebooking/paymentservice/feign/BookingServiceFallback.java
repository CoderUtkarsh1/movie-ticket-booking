package com.moviebooking.paymentservice.feign;

import com.moviebooking.common.dto.ApiResponse;
import com.moviebooking.common.dto.BookingDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BookingServiceFallback implements BookingServiceClient {

    @Override
    public ApiResponse<BookingDto> getBookingById(Long id, String apiKey) {
        log.error("[CIRCUIT BREAKER] booking-service is DOWN — getBookingById fallback triggered for bookingId={}", id);
        // Cannot proceed without booking data — return error response so caller can handle it
        return ApiResponse.error("Fallback: booking-service is temporarily unavailable");
    }

    @Override
    public ApiResponse<Void> confirmPayment(Long id, String apiKey) {
        log.error("[CIRCUIT BREAKER] booking-service is DOWN — confirmPayment fallback triggered for bookingId={}", id);
        // Payment is already saved as SUCCESS in payment DB.
        // Booking confirmation can be retried via a reconciliation mechanism.
        return ApiResponse.success("Fallback: booking-service unavailable, confirmation will be retried", null);
    }

    @Override
    public ApiResponse<Void> failPayment(Long id, String apiKey) {
        log.error("[CIRCUIT BREAKER] booking-service is DOWN — failPayment fallback triggered for bookingId={}", id);
        // Payment is already saved as FAILED in payment DB.
        // Booking failure can be retried via a reconciliation mechanism.
        return ApiResponse.success("Fallback: booking-service unavailable, failure will be retried", null);
    }
}
