package com.moviebooking.showservice.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatBlockRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotEmpty(message = "Seat IDs are required")
    private List<Long> seatIds;
}
