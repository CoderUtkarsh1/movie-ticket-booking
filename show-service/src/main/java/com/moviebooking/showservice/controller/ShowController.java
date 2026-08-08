package com.moviebooking.showservice.controller;

import com.moviebooking.common.dto.ApiResponse;
import com.moviebooking.showservice.dto.SeatBlockRequest;
import com.moviebooking.showservice.dto.ShowRequest;
import com.moviebooking.showservice.entity.Show;
import com.moviebooking.showservice.entity.ShowSeat;
import com.moviebooking.showservice.service.ShowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/shows")
@RequiredArgsConstructor
public class ShowController {

    private final ShowService showService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Show>>> getShows(
            @RequestParam(required = false) String movieId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<Show> shows;
        if (movieId != null && date != null) {
            shows = showService.getShowsByMovieAndDate(movieId, date);
        } else if (movieId != null) {
            shows = showService.getShowsByMovie(movieId);
        } else {
            shows = List.of();
        }
        return ResponseEntity.ok(ApiResponse.success(shows));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Show>> getShowById(@PathVariable Long id) {
        Show show = showService.getShowById(id);
        return ResponseEntity.ok(ApiResponse.success(show));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Show>> createShow(@Valid @RequestBody ShowRequest request) {
        Show show = showService.createShow(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Show scheduled successfully", show));
    }

    @GetMapping("/{id}/seats")
    public ResponseEntity<ApiResponse<List<ShowSeat>>> getSeatAvailability(@PathVariable Long id) {
        List<ShowSeat> seats = showService.getSeatAvailability(id);
        return ResponseEntity.ok(ApiResponse.success(seats));
    }

    @PutMapping("/{id}/seats/block")
    public ResponseEntity<ApiResponse<List<ShowSeat>>> blockSeats(
            @PathVariable Long id,
            @Valid @RequestBody SeatBlockRequest request) {
        List<ShowSeat> blockedSeats = showService.blockSeats(id, request.getUserId(), request.getSeatIds());
        return ResponseEntity.ok(ApiResponse.success("Seats blocked for 5 minutes", blockedSeats));
    }

    @PutMapping("/{id}/seats/release")
    public ResponseEntity<ApiResponse<Void>> releaseSeats(
            @PathVariable Long id,
            @Valid @RequestBody SeatBlockRequest request) {
        showService.releaseSeats(id, request.getSeatIds());
        return ResponseEntity.ok(ApiResponse.success("Seats released successfully", null));
    }

    // Internal endpoint - called by Booking Service via Feign
    @PutMapping("/{id}/seats/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmSeats(
            @PathVariable Long id,
            @RequestBody SeatBlockRequest request) {
        showService.confirmSeats(id, request.getSeatIds());
        return ResponseEntity.ok(ApiResponse.success("Seats confirmed", null));
    }
}
