package com.moviebooking.movieservice.service;

import com.moviebooking.common.dto.PagedResponse;
import com.moviebooking.movieservice.dto.MovieRequest;
import com.moviebooking.movieservice.dto.ReviewRequest;
import com.moviebooking.movieservice.entity.Movie;

public interface MovieService {

    Movie createMovie(MovieRequest request);

    Movie updateMovie(String id, MovieRequest request);

    Movie getMovieById(String id);

    void deleteMovie(String id);

    PagedResponse<Movie> getAllMovies(String genre, String language, String status, int page, int size);

    PagedResponse<Movie> searchMovies(String keyword, int page, int size);

    PagedResponse<Movie> getNowShowingMovies(int page, int size);

    PagedResponse<Movie> getUpcomingMovies(int page, int size);

    Movie addReview(String movieId, Long userId, String userName, ReviewRequest request);

    Movie toggleInterested(String movieId, Long userId);
}
