package com.moviebooking.bookingservice.feign;

import com.moviebooking.common.dto.ApiResponse;
import com.moviebooking.common.dto.SeatActionRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ShowServiceFallback implements ShowServiceClient {

    @Override
    public ApiResponse<Void> confirmSeats(Long showId, SeatActionRequest request) {
        log.error("[CIRCUIT BREAKER] show-service is DOWN — confirmSeats fallback triggered for showId={}, seatIds={}",
                showId, request.getSeatIds());
        // Booking is already CONFIRMED in DB. Seats will self-heal via Redis TTL expiry,
        // or can be retried manually / via a scheduled reconciliation job.
        return ApiResponse.success("Fallback: show-service unavailable, seats will self-heal", null);
    }

    @Override
    public ApiResponse<Void> releaseSeats(Long showId, SeatActionRequest request) {
        log.error("[CIRCUIT BREAKER] show-service is DOWN — releaseSeats fallback triggered for showId={}, seatIds={}",
                showId, request.getSeatIds());
        // Seats locked in Redis have a TTL (default 5 min). They will auto-release.
        return ApiResponse.success("Fallback: show-service unavailable, seats will auto-release via TTL", null);
    }
}
