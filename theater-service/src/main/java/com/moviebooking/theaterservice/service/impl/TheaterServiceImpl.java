package com.moviebooking.theaterservice.service.impl;

import com.moviebooking.common.enums.ScreenType;
import com.moviebooking.common.enums.SeatType;
import com.moviebooking.common.exception.ResourceNotFoundException;
import com.moviebooking.theaterservice.dto.ScreenRequest;
import com.moviebooking.theaterservice.dto.TheaterRequest;
import com.moviebooking.theaterservice.entity.Screen;
import com.moviebooking.theaterservice.entity.Seat;
import com.moviebooking.theaterservice.entity.Theater;
import com.moviebooking.theaterservice.repository.ScreenRepository;
import com.moviebooking.theaterservice.repository.SeatRepository;
import com.moviebooking.theaterservice.repository.TheaterRepository;
import com.moviebooking.theaterservice.service.TheaterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TheaterServiceImpl implements TheaterService {

    private final TheaterRepository theaterRepository;
    private final ScreenRepository screenRepository;
    private final SeatRepository seatRepository;

    @Override
    @Transactional
    public Theater createTheater(TheaterRequest request) {
        Theater theater = Theater.builder()
                .name(request.getName())
                .city(request.getCity())
                .address(request.getAddress())
                .totalScreens(request.getTotalScreens() != null ? request.getTotalScreens() : 0)
                .contactNo(request.getContactNo())
                .build();

        theater = theaterRepository.save(theater);
        log.info("Theater created: {} in {}", theater.getName(), theater.getCity());
        return theater;
    }

    @Override
    public Theater getTheaterById(Long id) {
        return theaterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Theater", "id", id));
    }

    @Override
    public List<Theater> getTheatersByCity(String city) {
        return theaterRepository.findByCityContainingIgnoreCase(city);
    }

    @Override
    public List<Theater> getAllTheaters() {
        return theaterRepository.findAll();
    }

    @Override
    @Transactional
    public Screen addScreen(Long theaterId, ScreenRequest request) {
        Theater theater = getTheaterById(theaterId);

        Screen screen = Screen.builder()
                .theater(theater)
                .screenName(request.getScreenName())
                .screenType(ScreenType.valueOf(request.getScreenType()))
                .build();

        screen = screenRepository.save(screen);

        // Create seats if provided
        if (request.getSeats() != null && !request.getSeats().isEmpty()) {
            List<Seat> seats = new ArrayList<>();
            Screen finalScreen = screen;
            request.getSeats().forEach(seatReq -> {
                Seat seat = Seat.builder()
                        .screen(finalScreen)
                        .seatRow(seatReq.getSeatRow())
                        .seatNumber(seatReq.getSeatNumber())
                        .seatType(seatReq.getSeatType() != null ? SeatType.valueOf(seatReq.getSeatType()) : SeatType.REGULAR)
                        .price(seatReq.getPrice())
                        .build();
                seats.add(seat);
            });
            seatRepository.saveAll(seats);
            screen.setSeats(seats);
            screen.setTotalSeats(seats.size());
            screen = screenRepository.save(screen);
        }

        // Update theater screen count
        theater.setTotalScreens(theater.getScreens().size());
        theaterRepository.save(theater);

        log.info("Screen added: {} to theater: {}", screen.getScreenName(), theater.getName());
        return screen;
    }

    @Override
    public List<Screen> getScreensByTheaterId(Long theaterId) {
        getTheaterById(theaterId); // Verify theater exists
        return screenRepository.findByTheaterId(theaterId);
    }

    @Override
    public List<Seat> getSeatsByScreenId(Long screenId) {
        return seatRepository.findByScreenId(screenId);
    }
}
