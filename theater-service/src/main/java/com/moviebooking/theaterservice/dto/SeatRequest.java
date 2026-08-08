package com.moviebooking.theaterservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatRequest {

    @NotBlank(message = "Seat row is required")
    private String seatRow;

    @NotNull(message = "Seat number is required")
    private Integer seatNumber;

    private String seatType;  // REGULAR, PREMIUM, VIP

    @NotNull(message = "Price is required")
    private BigDecimal price;
}
