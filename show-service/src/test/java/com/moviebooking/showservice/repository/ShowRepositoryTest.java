package com.moviebooking.showservice.repository;

import com.moviebooking.common.enums.SeatStatus;
import com.moviebooking.common.enums.ShowStatus;
import com.moviebooking.showservice.entity.Show;
import com.moviebooking.showservice.entity.ShowSeat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Show Repository Tests — @DataJpaTest + Testcontainers (REAL SQL Server)
 */
@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("ShowRepository Tests (Testcontainers SQL Server)")
class ShowRepositoryTest {

    @Container
    @ServiceConnection
    static MSSQLServerContainer<?> sqlServer = new MSSQLServerContainer<>(
            "mcr.microsoft.com/mssql/server:2022-latest")
            .acceptLicense()
            .withPassword("TestP@ssw0rd123");

    @Autowired private ShowRepository showRepository;
    @Autowired private ShowSeatRepository showSeatRepository;
    @Autowired private TestEntityManager entityManager;

    private Show testShow;

    @BeforeEach
    void setUp() {
        showSeatRepository.deleteAll();
        showRepository.deleteAll();
        entityManager.flush();

        testShow = Show.builder()
                .movieId("mov-001").screenId(1L).theaterId(1L)
                .showDate(LocalDate.of(2026, 7, 20))
                .showTime(LocalTime.of(18, 30))
                .status(ShowStatus.OPEN).build();
        testShow = entityManager.persistAndFlush(testShow);
    }

    private ShowSeat createSeat(Long seatId, String row, int num, SeatStatus status) {
        ShowSeat seat = ShowSeat.builder()
                .show(testShow).seatId(seatId).seatRow(row)
                .seatNumber(num).seatType("REGULAR")
                .price(new BigDecimal("250.00")).status(status).build();
        return entityManager.persistAndFlush(seat);
    }

    @Nested
    @DisplayName("ShowRepository queries")
    class ShowQueries {

        @Test
        @DisplayName("should find shows by movieId and showDate")
        void shouldFindByMovieAndDate() {
            List<Show> results = showRepository.findByMovieIdAndShowDate("mov-001", LocalDate.of(2026, 7, 20));
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getMovieId()).isEqualTo("mov-001");
        }

        @Test
        @DisplayName("should find all shows by movieId")
        void shouldFindByMovieId() {
            // Add another show for same movie, different date
            Show show2 = Show.builder()
                    .movieId("mov-001").screenId(2L).theaterId(1L)
                    .showDate(LocalDate.of(2026, 7, 21))
                    .showTime(LocalTime.of(21, 0)).status(ShowStatus.OPEN).build();
            entityManager.persistAndFlush(show2);

            List<Show> results = showRepository.findByMovieId("mov-001");
            assertThat(results).hasSize(2);
        }

        @Test
        @DisplayName("should return empty for non-existent movie")
        void shouldReturnEmptyForBadMovie() {
            List<Show> results = showRepository.findByMovieId("non-existent");
            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("ShowSeatRepository queries")
    class SeatQueries {

        @Test
        @DisplayName("should find all seats by showId")
        void shouldFindByShowId() {
            createSeat(101L, "A", 1, SeatStatus.AVAILABLE);
            createSeat(102L, "A", 2, SeatStatus.BOOKED);

            List<ShowSeat> results = showSeatRepository.findByShow_Id(testShow.getId());
            assertThat(results).hasSize(2);
        }

        @Test
        @DisplayName("should find seats by showId and status")
        void shouldFindByShowIdAndStatus() {
            createSeat(101L, "A", 1, SeatStatus.AVAILABLE);
            createSeat(102L, "A", 2, SeatStatus.BOOKED);
            createSeat(103L, "A", 3, SeatStatus.AVAILABLE);

            List<ShowSeat> available = showSeatRepository.findByShow_IdAndStatus(
                    testShow.getId(), SeatStatus.AVAILABLE);

            assertThat(available).hasSize(2);
            assertThat(available).allSatisfy(s -> assertThat(s.getStatus()).isEqualTo(SeatStatus.AVAILABLE));
        }

        @Test
        @DisplayName("should find seats by showId and seatId list")
        void shouldFindByShowIdAndSeatIds() {
            createSeat(101L, "A", 1, SeatStatus.AVAILABLE);
            createSeat(102L, "A", 2, SeatStatus.AVAILABLE);
            createSeat(103L, "A", 3, SeatStatus.AVAILABLE);

            List<ShowSeat> results = showSeatRepository.findByShow_IdAndSeatIdIn(
                    testShow.getId(), List.of(101L, 103L));

            assertThat(results).hasSize(2);
            assertThat(results).extracting(ShowSeat::getSeatId).containsExactlyInAnyOrder(101L, 103L);
        }

        @Test
        @DisplayName("should return empty when no seats match")
        void shouldReturnEmptyWhenNoMatch() {
            List<ShowSeat> results = showSeatRepository.findByShow_IdAndSeatIdIn(
                    testShow.getId(), List.of(999L));
            assertThat(results).isEmpty();
        }
    }
}
