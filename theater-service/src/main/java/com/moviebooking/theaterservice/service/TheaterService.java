package com.moviebooking.theaterservice.service;

import com.moviebooking.theaterservice.dto.ScreenRequest;
import com.moviebooking.theaterservice.dto.TheaterRequest;
import com.moviebooking.theaterservice.entity.Screen;
import com.moviebooking.theaterservice.entity.Seat;
import com.moviebooking.theaterservice.entity.Theater;

import java.util.List;

public interface TheaterService {

    Theater createTheater(TheaterRequest request);

    Theater getTheaterById(Long id);

    List<Theater> getTheatersByCity(String city);

    List<Theater> getAllTheaters();

    Screen addScreen(Long theaterId, ScreenRequest request);

    List<Screen> getScreensByTheaterId(Long theaterId);

    List<Seat> getSeatsByScreenId(Long screenId);
}
