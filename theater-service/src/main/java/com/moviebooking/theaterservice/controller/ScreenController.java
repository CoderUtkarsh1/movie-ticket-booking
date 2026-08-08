package com.moviebooking.theaterservice.controller;

import com.moviebooking.common.dto.ApiResponse;
import com.moviebooking.theaterservice.entity.Seat;
import com.moviebooking.theaterservice.service.TheaterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/screens")
@RequiredArgsConstructor
public class ScreenController {

    private final TheaterService theaterService;

    @GetMapping("/{id}/seats")
    public ResponseEntity<ApiResponse<List<Seat>>> getSeatLayout(@PathVariable Long id) {
        List<Seat> seats = theaterService.getSeatsByScreenId(id);
        return ResponseEntity.ok(ApiResponse.success(seats));
    }
}
