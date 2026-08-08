package com.moviebooking.showservice.repository;

import com.moviebooking.showservice.entity.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ShowRepository extends JpaRepository<Show, Long> {

    List<Show> findByMovieIdAndShowDate(String movieId, LocalDate showDate);

    List<Show> findByMovieId(String movieId);

    List<Show> findByScreenIdAndShowDate(Long screenId, LocalDate showDate);

    List<Show> findByTheaterIdAndShowDate(Long theaterId, LocalDate showDate);
}
