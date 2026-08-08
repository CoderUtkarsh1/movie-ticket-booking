package com.moviebooking.common.dto;

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
public class BookingDto {

    private Long id;
    private String bookingCode;
    private Long userId;
    private String userEmail;
    private Long showId;
    private String movieName;
    private String theaterName;
    private String screenName;
    private LocalDate showDate;
    private LocalTime showTime;
    private Integer totalSeats;
    private BigDecimal totalAmount;
    private String status;
    private List<Long> seatIds;
}
