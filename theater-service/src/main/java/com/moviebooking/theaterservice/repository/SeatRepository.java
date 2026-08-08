package com.moviebooking.theaterservice.repository;

import com.moviebooking.theaterservice.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByScreenId(Long screenId);
    List<Seat> findByIdIn(List<Long> ids);
}
