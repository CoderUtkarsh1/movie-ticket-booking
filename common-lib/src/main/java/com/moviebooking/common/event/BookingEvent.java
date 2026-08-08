package com.moviebooking.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingEvent implements Serializable {

    private Long bookingId;
    private String bookingCode;
    private Long userId;
    private String userEmail;
    private Long showId;
    private String movieName;
    private String theaterName;
    private String screenName;
    private LocalDate showDate;
    private LocalTime showTime;
    private int totalSeats;
    private BigDecimal totalAmount;
    private List<Long> seatIds;
    private List<SeatInfo> seats;
    private String status;  // PENDING, CONFIRMED, CANCELLED
    private Instant timestamp;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeatInfo implements Serializable {
        private String seatRow;
        private int seatNumber;
        private String seatType;
        private BigDecimal price;
    }
}
