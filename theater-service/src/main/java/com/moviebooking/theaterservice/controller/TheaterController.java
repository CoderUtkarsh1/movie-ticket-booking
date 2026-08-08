package com.moviebooking.theaterservice.controller;

import com.moviebooking.common.dto.ApiResponse;
import com.moviebooking.theaterservice.dto.ScreenRequest;
import com.moviebooking.theaterservice.dto.TheaterRequest;
import com.moviebooking.theaterservice.entity.Screen;
import com.moviebooking.theaterservice.entity.Theater;
import com.moviebooking.theaterservice.service.TheaterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/theaters")
@RequiredArgsConstructor
public class TheaterController {

    private final TheaterService theaterService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Theater>>> getTheaters(
            @RequestParam(required = false) String city) {
        List<Theater> theaters;
        if (city != null && !city.isBlank()) {
            theaters = theaterService.getTheatersByCity(city);
        } else {
            theaters = theaterService.getAllTheaters();
        }
        return ResponseEntity.ok(ApiResponse.success(theaters));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Theater>> getTheaterById(@PathVariable Long id) {
        Theater theater = theaterService.getTheaterById(id);
        return ResponseEntity.ok(ApiResponse.success(theater));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Theater>> createTheater(@Valid @RequestBody TheaterRequest request) {
        Theater theater = theaterService.createTheater(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Theater created successfully", theater));
    }

    @GetMapping("/{id}/screens")
    public ResponseEntity<ApiResponse<List<Screen>>> getScreens(@PathVariable Long id) {
        List<Screen> screens = theaterService.getScreensByTheaterId(id);
        return ResponseEntity.ok(ApiResponse.success(screens));
    }

    @PostMapping("/{id}/screens")
    public ResponseEntity<ApiResponse<Screen>> addScreen(
            @PathVariable Long id,
            @Valid @RequestBody ScreenRequest request) {
        Screen screen = theaterService.addScreen(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Screen added successfully", screen));
    }
}
