package com.moviebooking.movieservice.entity;

import com.moviebooking.common.enums.MovieStatus;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "movies")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Movie {

    @Id
    private String id;

    @Indexed
    private String title;

    private String description;

    private Integer duration; // in minutes

    @Indexed
    private String language;

    @Indexed
    private String genre;

    private LocalDate releaseDate;

    private Double rating;
    
    private Double imdbRating;

    private String posterUrl;

    private String trailerUrl;

    @Indexed
    @Builder.Default
    private MovieStatus status = MovieStatus.UPCOMING;

    @Builder.Default
    private List<MovieCast> cast = new ArrayList<>();

    @Builder.Default
    private List<Review> reviews = new ArrayList<>();

    @Builder.Default
    private java.util.Set<Long> interestedUserIds = new java.util.HashSet<>();

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
