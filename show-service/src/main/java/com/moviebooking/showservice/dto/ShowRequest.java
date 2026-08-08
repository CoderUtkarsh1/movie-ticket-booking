package com.moviebooking.showservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowRequest {

    @NotBlank(message = "Movie ID is required")
    private String movieId;

    @NotNull(message = "Screen ID is required")
    private Long screenId;

    @NotNull(message = "Theater ID is required")
    private Long theaterId;

    @NotNull(message = "Show date is required")
    private LocalDate showDate;

    @NotNull(message = "Show time is required")
    private LocalTime showTime;
}
