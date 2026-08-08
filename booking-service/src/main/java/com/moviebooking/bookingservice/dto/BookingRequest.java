package com.moviebooking.bookingservice.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {

    @NotNull(message = "Show ID is required")
    private Long showId;

    private String movieName;
    private String theaterName;
    private String screenName;
    private LocalDate showDate;
    private LocalTime showTime;

    @NotEmpty(message = "Seats are required")
    private List<SeatInfo> seats;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeatInfo {
        private Long seatId;
        private String seatRow;
        private Integer seatNumber;
        private String seatType;
        private BigDecimal price;
    }
}
