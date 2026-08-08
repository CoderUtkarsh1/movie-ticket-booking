package com.moviebooking.theaterservice.dto;

import jakarta.validation.constraints.NotBlank;
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
public class ScreenRequest {

    @NotBlank(message = "Screen name is required")
    private String screenName;

    @NotNull(message = "Screen type is required")
    private String screenType;  // TWO_D, THREE_D, IMAX

    private List<SeatRequest> seats;
}
