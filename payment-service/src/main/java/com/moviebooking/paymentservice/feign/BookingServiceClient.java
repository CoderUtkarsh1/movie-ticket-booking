package com.moviebooking.paymentservice.feign;

import com.moviebooking.common.dto.ApiResponse;
import com.moviebooking.common.dto.BookingDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "booking-service", fallback = BookingServiceFallback.class)
public interface BookingServiceClient {

    @GetMapping("/api/bookings/internal/{id}")
    ApiResponse<BookingDto> getBookingById(@PathVariable("id") Long id,
                                            @RequestHeader("X-Internal-Api-Key") String apiKey);

    @PutMapping("/api/bookings/internal/{id}/confirm-payment")
    ApiResponse<Void> confirmPayment(@PathVariable("id") Long id,
                                      @RequestHeader("X-Internal-Api-Key") String apiKey);

    @PutMapping("/api/bookings/internal/{id}/fail-payment")
    ApiResponse<Void> failPayment(@PathVariable("id") Long id,
                                    @RequestHeader("X-Internal-Api-Key") String apiKey);
}
