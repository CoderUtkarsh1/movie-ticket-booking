package com.moviebooking.movieservice.controller;

import com.moviebooking.common.constants.AppConstants;
import com.moviebooking.common.dto.ApiResponse;
import com.moviebooking.common.dto.PagedResponse;
import com.moviebooking.movieservice.dto.MovieRequest;
import com.moviebooking.movieservice.dto.ReviewRequest;
import com.moviebooking.movieservice.entity.Movie;
import com.moviebooking.movieservice.service.MovieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    // ===== PUBLIC ENDPOINTS =====

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<Movie>>> getAllMovies(
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        PagedResponse<Movie> response = movieService.getAllMovies(genre, language, status, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Movie>> getMovieById(@PathVariable String id) {
        Movie movie = movieService.getMovieById(id);
        return ResponseEntity.ok(ApiResponse.success(movie));
    }

    @GetMapping("/now-showing")
    public ResponseEntity<ApiResponse<PagedResponse<Movie>>> getNowShowing(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        PagedResponse<Movie> response = movieService.getNowShowingMovies(page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<PagedResponse<Movie>>> getUpcoming(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        PagedResponse<Movie> response = movieService.getUpcomingMovies(page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PagedResponse<Movie>>> searchMovies(
            @RequestParam String q,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        PagedResponse<Movie> response = movieService.searchMovies(q, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ===== ADMIN ENDPOINTS =====

    @PostMapping
    public ResponseEntity<ApiResponse<Movie>> createMovie(@Valid @RequestBody MovieRequest request) {
        Movie movie = movieService.createMovie(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Movie created successfully", movie));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Movie>> updateMovie(
            @PathVariable String id,
            @RequestBody MovieRequest request) {
        Movie movie = movieService.updateMovie(id, request);
        return ResponseEntity.ok(ApiResponse.success("Movie updated successfully", movie));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMovie(@PathVariable String id) {
        movieService.deleteMovie(id);
        return ResponseEntity.ok(ApiResponse.success("Movie deleted successfully", null));
    }

    // ===== REVIEW ENDPOINTS =====

    @PostMapping("/{id}/reviews")
    public ResponseEntity<ApiResponse<Movie>> addReview(
            @PathVariable String id,
            @RequestHeader(value = AppConstants.HEADER_USER_ID) Long userId,
            @RequestHeader(value = "X-User-Name", required = false, defaultValue = "Anonymous") String userName,
            @Valid @RequestBody ReviewRequest request) {
        Movie movie = movieService.addReview(id, userId, userName, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Review added successfully", movie));
    }
    @PostMapping("/{id}/interested")
    public ResponseEntity<ApiResponse<Movie>> toggleInterested(
            @PathVariable String id,
            @RequestHeader(value = AppConstants.HEADER_USER_ID) Long userId) {
        Movie movie = movieService.toggleInterested(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Interest toggled successfully", movie));
    }
}
