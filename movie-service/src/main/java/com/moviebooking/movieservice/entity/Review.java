package com.moviebooking.movieservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    private Long userId;
    private String userName;
    private Double rating;
    private String comment;
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
