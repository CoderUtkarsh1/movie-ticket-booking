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
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit Tests for ShowServiceImpl — 2026 Industry Standard
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ShowServiceImpl Unit Tests")
class ShowServiceImplTest {

    @Mock private ShowRepository showRepository;
    @Mock private ShowSeatRepository showSeatRepository;
    @Mock private SeatLockService seatLockService;
    @Mock private EntityManager entityManager;
    @Mock private TheaterServiceClient theaterServiceClient;

    @InjectMocks private ShowServiceImpl showService;

    // ===== Helpers =====

    private Show buildShow() {
        return Show.builder()
                .id(1L).movieId("mov-001").screenId(1L).theaterId(1L)
                .showDate(LocalDate.of(2026, 7, 20))
                .showTime(LocalTime.of(18, 30)).build();
    }

    private ShowSeat buildSeat(Long seatId, String row, int number, SeatStatus status) {
        return ShowSeat.builder()
                .id(seatId).show(buildShow()).seatId(seatId)
                .seatRow(row).seatNumber(number).seatType("REGULAR")
                .price(new BigDecimal("250.00")).status(status).build();
    }

    @Nested
    @DisplayName("createShow()")
    class CreateShow {

        @Test
        @DisplayName("should create show and save to repository")
        void shouldCreateShow() {
            ShowRequest request = ShowRequest.builder()
                    .movieId("mov-001").screenId(1L).theaterId(1L)
                    .showDate(LocalDate.of(2026, 7, 20))
                    .showTime(LocalTime.of(18, 30)).build();

            when(showRepository.save(any(Show.class))).thenAnswer(inv -> {
                Show s = inv.getArgument(0);
                s.setId(1L);
                return s;
            });

            // Mock theater-service Feign response with seat layout
            List<Map<String, Object>> seats = List.of(
                    Map.of("id", 101, "seatRow", "A", "seatNumber", 1, "seatType", "REGULAR", "price", 250.00),
                    Map.of("id", 102, "seatRow", "A", "seatNumber", 2, "seatType", "REGULAR", "price", 250.00)
            );
            when(theaterServiceClient.getSeatsByScreenId(1L))
                    .thenReturn(ApiResponse.success(seats));

            Show result = showService.createShow(request);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getMovieId()).isEqualTo("mov-001");
            verify(showSeatRepository).saveAll(any()); // seats populated from theater-service
        }

        @Test
        @DisplayName("should still create show even if theater-service Feign fails")
        void shouldCreateShowEvenIfFeignFails() {
            ShowRequest request = ShowRequest.builder()
                    .movieId("mov-001").screenId(1L).theaterId(1L)
                    .showDate(LocalDate.of(2026, 7, 20))
                    .showTime(LocalTime.of(18, 30)).build();

            when(showRepository.save(any())).thenAnswer(inv -> {
                Show s = inv.getArgument(0);
                s.setId(1L);
                return s;
            });
            when(theaterServiceClient.getSeatsByScreenId(1L))
                    .thenThrow(new RuntimeException("theater-service DOWN"));

            Show result = showService.createShow(request);

            assertThat(result.getId()).isEqualTo(1L); // show still created
            verify(showSeatRepository, never()).saveAll(any()); // no seats populated
        }
    }

    @Nested
    @DisplayName("getSeatAvailability()")
    class GetSeatAvailability {

        @Test
        @DisplayName("should return all seats for a show")
        void shouldReturnAllSeats() {
            when(showRepository.existsById(1L)).thenReturn(true);
            List<ShowSeat> seats = List.of(
                    buildSeat(101L, "A", 1, SeatStatus.AVAILABLE),
                    buildSeat(102L, "A", 2, SeatStatus.BOOKED)
            );
            when(showSeatRepository.findByShow_Id(1L)).thenReturn(seats);

            List<ShowSeat> result = showService.getSeatAvailability(1L);

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("should self-heal: reset BLOCKED seats with expired Redis lock to AVAILABLE")
        void shouldSelfHealExpiredLocks() {
            when(showRepository.existsById(1L)).thenReturn(true);

            ShowSeat staleSeat = buildSeat(101L, "A", 1, SeatStatus.BLOCKED);
            when(showSeatRepository.findByShow_Id(1L)).thenReturn(List.of(staleSeat));
            when(seatLockService.isSeatLocked(1L, 101L)).thenReturn(false); // Redis expired!

            List<ShowSeat> result = showService.getSeatAvailability(1L);

            assertThat(result.get(0).getStatus()).isEqualTo(SeatStatus.AVAILABLE); // self-healed!
            verify(showSeatRepository).saveAll(any()); // saved updated status
        }

        @Test
        @DisplayName("should NOT reset BLOCKED seats that still have valid Redis lock")
        void shouldNotResetValidLocks() {
            when(showRepository.existsById(1L)).thenReturn(true);

            ShowSeat blockedSeat = buildSeat(101L, "A", 1, SeatStatus.BLOCKED);
            when(showSeatRepository.findByShow_Id(1L)).thenReturn(List.of(blockedSeat));
            when(seatLockService.isSeatLocked(1L, 101L)).thenReturn(true); // still locked

            List<ShowSeat> result = showService.getSeatAvailability(1L);

            assertThat(result.get(0).getStatus()).isEqualTo(SeatStatus.BLOCKED); // unchanged
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException for invalid show")
        void shouldThrowForInvalidShow() {
            when(showRepository.existsById(999L)).thenReturn(false);

            assertThatThrownBy(() -> showService.getSeatAvailability(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("blockSeats()")
    class BlockSeats {

        @Test
        @DisplayName("should block available seats and lock in Redis")
        void shouldBlockAvailableSeats() {
            when(showRepository.findById(1L)).thenReturn(Optional.of(buildShow()));
            List<ShowSeat> seats = List.of(
                    buildSeat(101L, "A", 1, SeatStatus.AVAILABLE),
                    buildSeat(102L, "A", 2, SeatStatus.AVAILABLE)
            );
            when(showSeatRepository.findByShow_IdAndSeatIdIn(eq(1L), eq(List.of(101L, 102L))))
                    .thenReturn(seats);
            when(seatLockService.lockSeats(eq(1L), eq(List.of(101L, 102L)), eq(1L)))
                    .thenReturn(true);

            List<ShowSeat> result = showService.blockSeats(1L, 1L, List.of(101L, 102L));

            assertThat(result).allSatisfy(seat ->
                    assertThat(seat.getStatus()).isEqualTo(SeatStatus.BLOCKED));
            verify(showSeatRepository).saveAll(any());
        }

        @Test
        @DisplayName("should throw when some seats not found for show")
        void shouldThrowWhenSeatsNotFound() {
            when(showRepository.findById(1L)).thenReturn(Optional.of(buildShow()));
            when(showSeatRepository.findByShow_IdAndSeatIdIn(eq(1L), eq(List.of(101L, 102L))))
                    .thenReturn(List.of(buildSeat(101L, "A", 1, SeatStatus.AVAILABLE)));
            // Only 1 seat found but 2 requested

            assertThatThrownBy(() -> showService.blockSeats(1L, 1L, List.of(101L, 102L)))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("seats not found");
        }

        @Test
        @DisplayName("should throw when seat is already BOOKED")
        void shouldThrowWhenSeatBooked() {
            when(showRepository.findById(1L)).thenReturn(Optional.of(buildShow()));
            List<ShowSeat> seats = List.of(
                    buildSeat(101L, "A", 1, SeatStatus.BOOKED) // already booked!
            );
            when(showSeatRepository.findByShow_IdAndSeatIdIn(eq(1L), eq(List.of(101L))))
                    .thenReturn(seats);

            assertThatThrownBy(() -> showService.blockSeats(1L, 1L, List.of(101L)))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("not available");
        }

        @Test
        @DisplayName("should throw when Redis lock fails (race condition)")
        void shouldThrowWhenRedisLockFails() {
            when(showRepository.findById(1L)).thenReturn(Optional.of(buildShow()));
            List<ShowSeat> seats = List.of(
                    buildSeat(101L, "A", 1, SeatStatus.AVAILABLE)
            );
            when(showSeatRepository.findByShow_IdAndSeatIdIn(eq(1L), eq(List.of(101L))))
                    .thenReturn(seats);
            when(seatLockService.lockSeats(anyLong(), any(), anyLong()))
                    .thenReturn(false); // Another user won the race!

            assertThatThrownBy(() -> showService.blockSeats(1L, 1L, List.of(101L)))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("already being booked");
        }
    }

    @Nested
    @DisplayName("confirmSeats()")
    class ConfirmSeats {

        @Test
        @DisplayName("should change seat status to BOOKED and release Redis locks")
        void shouldConfirmSeats() {
            List<ShowSeat> seats = List.of(
                    buildSeat(101L, "A", 1, SeatStatus.BLOCKED)
            );
            when(showSeatRepository.findByShow_IdAndSeatIdIn(eq(1L), eq(List.of(101L))))
                    .thenReturn(seats);

            showService.confirmSeats(1L, List.of(101L));

            assertThat(seats.get(0).getStatus()).isEqualTo(SeatStatus.BOOKED);
            verify(showSeatRepository).saveAll(any());
            verify(seatLockService).releaseSeats(eq(1L), eq(List.of(101L)));
        }
    }

    @Nested
    @DisplayName("releaseSeats()")
    class ReleaseSeats {

        @Test
        @DisplayName("should change seat status to AVAILABLE and remove Redis locks")
        void shouldReleaseSeats() {
            List<ShowSeat> seats = List.of(
                    buildSeat(101L, "A", 1, SeatStatus.BLOCKED)
            );
            when(showSeatRepository.findByShow_IdAndSeatIdIn(eq(1L), eq(List.of(101L))))
                    .thenReturn(seats);

            showService.releaseSeats(1L, List.of(101L));

            assertThat(seats.get(0).getStatus()).isEqualTo(SeatStatus.AVAILABLE);
            verify(seatLockService).releaseSeats(eq(1L), eq(List.of(101L)));
        }
    }
}
