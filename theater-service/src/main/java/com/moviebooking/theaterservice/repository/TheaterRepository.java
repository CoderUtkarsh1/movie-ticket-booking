package com.moviebooking.theaterservice.repository;

import com.moviebooking.theaterservice.entity.Theater;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TheaterRepository extends JpaRepository<Theater, Long> {
    List<Theater> findByCity(String city);
    List<Theater> findByCityContainingIgnoreCase(String city);
}
