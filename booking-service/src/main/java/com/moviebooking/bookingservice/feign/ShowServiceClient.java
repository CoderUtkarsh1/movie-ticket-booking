package com.moviebooking.bookingservice.feign;

import com.moviebooking.common.dto.ApiResponse;
import com.moviebooking.common.dto.SeatActionRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "show-service", fallback = ShowServiceFallback.class)
public interface ShowServiceClient {

    @PutMapping("/api/shows/{showId}/seats/confirm")
    ApiResponse<Void> confirmSeats(@PathVariable("showId") Long showId,
                                    @RequestBody SeatActionRequest request);

    @PutMapping("/api/shows/{showId}/seats/release")
    ApiResponse<Void> releaseSeats(@PathVariable("showId") Long showId,
                                    @RequestBody SeatActionRequest request);
}
