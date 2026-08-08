package com.moviebooking.showservice.repository;

import com.moviebooking.common.enums.SeatStatus;
import com.moviebooking.showservice.entity.ShowSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShowSeatRepository extends JpaRepository<ShowSeat, Long> {

    @Query("SELECT ss FROM ShowSeat ss WHERE ss.show.id = :showId")
    List<ShowSeat> findByShow_Id(@Param("showId") Long showId);

    @Query("SELECT ss FROM ShowSeat ss WHERE ss.show.id = :showId AND ss.status = :status")
    List<ShowSeat> findByShow_IdAndStatus(@Param("showId") Long showId, @Param("status") SeatStatus status);

    @Query("SELECT ss FROM ShowSeat ss WHERE ss.show.id = :showId AND ss.seatId IN :seatIds")
    List<ShowSeat> findByShow_IdAndSeatIdIn(@Param("showId") Long showId, @Param("seatIds") List<Long> seatIds);
}
