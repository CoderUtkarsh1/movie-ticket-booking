package com.moviebooking.showservice.service;

import com.moviebooking.showservice.dto.ShowRequest;
import com.moviebooking.showservice.entity.Show;
import com.moviebooking.showservice.entity.ShowSeat;

import java.time.LocalDate;
import java.util.List;

public interface ShowService {

    Show createShow(ShowRequest request);

    boolean existsById(Long id);

    Show getShowById(Long id);

    List<Show> getShowsByMovieAndDate(String movieId, LocalDate date);

    List<Show> getShowsByMovie(String movieId);

    List<ShowSeat> getSeatAvailability(Long showId);

    List<ShowSeat> blockSeats(Long showId, Long userId, List<Long> seatIds);

    void releaseSeats(Long showId, List<Long> seatIds);

    void confirmSeats(Long showId, List<Long> seatIds);
}
