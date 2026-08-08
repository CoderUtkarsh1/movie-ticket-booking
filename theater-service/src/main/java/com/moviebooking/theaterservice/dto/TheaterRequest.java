package com.moviebooking.theaterservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TheaterRequest {

    @NotBlank(message = "Theater name is required")
    private String name;

    @NotBlank(message = "City is required")
    private String city;

    private String address;

    private Integer totalScreens;

    private String contactNo;
}
