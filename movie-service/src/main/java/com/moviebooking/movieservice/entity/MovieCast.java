package com.moviebooking.movieservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieCast {

    private String actorName;
    private String role;       // Character name
    private String imageUrl;
}
