package com.moviebooking.showservice.service.impl;

import com.moviebooking.common.dto.ApiResponse;
import com.moviebooking.common.enums.SeatStatus;
import com.moviebooking.common.exception.BadRequestException;
import com.moviebooking.common.exception.ResourceNotFoundException;
import com.moviebooking.showservice.dto.ShowRequest;
import com.moviebooking.showservice.entity.Show;
import com.moviebooking.showservice.entity.ShowSeat;
import com.moviebooking.showservice.feign.TheaterServiceClient;
import com.moviebooking.showservice.repository.ShowRepository;
import com.moviebooking.showservice.repository.ShowSeatRepository;
import com.moviebooking.showservice.service.SeatLockService;
import com.moviebooking.showservice.service.ShowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShowServiceImpl implements ShowService {

    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;
    private final SeatLockService seatLockService;
    private final TheaterServiceClient theaterServiceClient;

    @Override
    @Transactional
    public Show createShow(ShowRequest request) {
        Show show = Show.builder()
                .movieId(request.getMovieId())
                .screenId(request.getScreenId())
                .theaterId(request.getTheaterId())
                .showDate(request.getShowDate())
                .showTime(request.getShowTime())
                .build();

        show = showRepository.save(show);
        log.info("Show created: {} for movie {} on {}", show.getId(), show.getMovieId(), show.getShowDate());

        // Auto-populate ShowSeats from Theater Service screen seat layout via Feign
        try {
            ApiResponse<List<Map<String, Object>>> response = theaterServiceClient.getSeatsByScreenId(request.getScreenId());
            List<Map<String, Object>> screenSeats = response.getData();

            if (screenSeats != null && !screenSeats.isEmpty()) {
                List<ShowSeat> showSeats = new ArrayList<>();
                for (Map<String, Object> seat : screenSeats) {
                    ShowSeat showSeat = ShowSeat.builder()
                            .show(show)
                            .seatId(((Number) seat.get("id")).longValue())
                            .seatRow((String) seat.get("seatRow"))
                            .seatNumber(((Number) seat.get("seatNumber")).intValue())
                            .seatType((String) seat.get("seatType"))
                            .price(new BigDecimal(seat.get("price").toString()))
                            .status(SeatStatus.AVAILABLE)
                            .build();
                    showSeats.add(showSeat);
                }
                showSeatRepository.saveAll(showSeats);
                log.info("Auto-populated {} seats for show {} from screen {}", showSeats.size(), show.getId(), request.getScreenId());
            } else {
                log.warn("No seats found for screen {} in theater service", request.getScreenId());
            }
        } catch (Exception e) {
            log.error("Failed to auto-populate seats from theater service for screen {}: {}", request.getScreenId(), e.getMessage());
            // Don't fail show creation if seat population fails — admin can retry
        }

        return show;
    }

    @Override
    public Show getShowById(Long id) {
        return showRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Show", "id", id));
    }

    @Override
    public List<Show> getShowsByMovieAndDate(String movieId, LocalDate date) {
        return showRepository.findByMovieIdAndShowDate(movieId, date);
    }

    @Override
    public List<Show> getShowsByMovie(String movieId) {
        return showRepository.findByMovieId(movieId);
    }

    @Override
    public boolean existsById(Long id) {
        return showRepository.existsById(id);
    }

    @Override
    @Transactional
    public List<ShowSeat> getSeatAvailability(Long showId) {
        if (!showRepository.existsById(showId)) {
            throw new ResourceNotFoundException("Show", "id", showId);
        }
        
        List<ShowSeat> seats = showSeatRepository.findByShow_Id(showId);
        log.debug("getSeatAvailability for show {}: found {} seats", showId, seats.size());

        // Self-healing: if seat is BLOCKED in DB but Redis lock has expired, reset to AVAILABLE
        boolean anyHealed = false;
        for (ShowSeat seat : seats) {
            if (seat.getStatus() == SeatStatus.BLOCKED
                    && !seatLockService.isSeatLocked(showId, seat.getSeatId())) {
                seat.setStatus(SeatStatus.AVAILABLE);
                anyHealed = true;
                log.info("Self-healing: Seat {} (show {}) reset from BLOCKED to AVAILABLE (Redis lock expired)",
                        seat.getSeatId(), showId);
            }
        }
        if (anyHealed) {
            showSeatRepository.saveAll(seats);
        }

        return seats;
    }

    @Override
    @Transactional
    public List<ShowSeat> blockSeats(Long showId, Long userId, List<Long> seatIds) {
        getShowById(showId);

        // Get the show seats
        List<ShowSeat> showSeats = showSeatRepository.findByShow_IdAndSeatIdIn(showId, seatIds);

        if (showSeats.size() != seatIds.size()) {
            throw new BadRequestException("Some seats not found for this show");
        }

        // Self-healing: reset stale BLOCKED seats (Redis lock expired) to AVAILABLE
        for (ShowSeat seat : showSeats) {
            if (seat.getStatus() == SeatStatus.BLOCKED
                    && !seatLockService.isSeatLocked(showId, seat.getSeatId())) {
                seat.setStatus(SeatStatus.AVAILABLE);
                log.info("Self-healing in blockSeats: Seat {} reset to AVAILABLE (stale lock)", seat.getSeatId());
            }
        }

        // Check if all seats are available
        for (ShowSeat seat : showSeats) {
            if (seat.getStatus() != SeatStatus.AVAILABLE) {
                throw new BadRequestException("Seat " + seat.getSeatRow() + seat.getSeatNumber() + " is not available");
            }
        }

        // Lock seats in Redis (5-min TTL) — atomic with rollback
        boolean locked = seatLockService.lockSeats(showId, seatIds, userId);
        if (!locked) {
            throw new BadRequestException("Some seats are already being booked by another user. Please try again.");
        }

        // Update seat status in DB
        for (ShowSeat seat : showSeats) {
            seat.setStatus(SeatStatus.BLOCKED);
        }
        showSeatRepository.saveAll(showSeats);

        log.info("Seats blocked for show {}: {} by user {}", showId, seatIds, userId);
        return showSeats;
    }

    @Override
    @Transactional
    public void releaseSeats(Long showId, List<Long> seatIds) {
        List<ShowSeat> showSeats = showSeatRepository.findByShow_IdAndSeatIdIn(showId, seatIds);

        for (ShowSeat seat : showSeats) {
            seat.setStatus(SeatStatus.AVAILABLE);
        }
        showSeatRepository.saveAll(showSeats);

        // Remove Redis locks
        seatLockService.releaseSeats(showId, seatIds);

        log.info("Seats released for show {}: {}", showId, seatIds);
    }

    @Override
    @Transactional
    public void confirmSeats(Long showId, List<Long> seatIds) {
        List<ShowSeat> showSeats = showSeatRepository.findByShow_IdAndSeatIdIn(showId, seatIds);

        for (ShowSeat seat : showSeats) {
            seat.setStatus(SeatStatus.BOOKED);
        }
        showSeatRepository.saveAll(showSeats);

        // Remove Redis locks (no longer needed once booked)
        seatLockService.releaseSeats(showId, seatIds);

        log.info("Seats confirmed (BOOKED) for show {}: {}", showId, seatIds);
    }
}
