package com.moviebooking.bookingservice.repository;

import com.moviebooking.bookingservice.entity.Booking;
import com.moviebooking.common.enums.BookingStatus;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository Slice Tests — @DataJpaTest + Testcontainers (REAL SQL Server)
 */
@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("BookingRepository Tests (Testcontainers SQL Server)")
class BookingRepositoryTest {

    @Container
    @ServiceConnection
    static MSSQLServerContainer<?> sqlServer = new MSSQLServerContainer<>(
            "mcr.microsoft.com/mssql/server:2022-latest")
            .acceptLicense()
            .withPassword("TestP@ssw0rd123");

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
    }

    private Booking createBooking(Long userId, String code, BookingStatus status, LocalDate showDate) {
        Booking booking = Booking.builder()
                .bookingCode(code)
                .userId(userId)
                .userEmail("user" + userId + "@test.com")
                .showId(1L)
                .movieName("Pushpa 3")
                .theaterName("PVR Phoenix")
                .screenName("Screen 1")
                .showDate(showDate)
                .showTime(LocalTime.of(18, 30))
                .totalSeats(2)
                .totalAmount(new BigDecimal("500.00"))
                .status(status)
                .build();
        return entityManager.persistAndFlush(booking);
    }

    @Nested
    @DisplayName("findByUserIdOrderByCreatedAtDesc()")
    class FindByUserId {

        @Test
        @DisplayName("should find all bookings for a user")
        void shouldFindUserBookings() {
            createBooking(1L, "MBK-001", BookingStatus.CONFIRMED, LocalDate.of(2026, 7, 20));
            createBooking(1L, "MBK-002", BookingStatus.PENDING, LocalDate.of(2026, 7, 21));
            createBooking(2L, "MBK-003", BookingStatus.CONFIRMED, LocalDate.of(2026, 7, 22));

            List<Booking> results = bookingRepository.findByUserIdOrderByCreatedAtDesc(1L);

            assertThat(results).hasSize(2);
            assertThat(results).allSatisfy(b -> assertThat(b.getUserId()).isEqualTo(1L));
        }

        @Test
        @DisplayName("should return empty list when user has no bookings")
        void shouldReturnEmptyForNewUser() {
            List<Booking> results = bookingRepository.findByUserIdOrderByCreatedAtDesc(999L);
            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByBookingCode()")
    class FindByBookingCode {

        @Test
        @DisplayName("should find booking by unique booking code")
        void shouldFindByCode() {
            createBooking(1L, "MBK-20260720-XY99", BookingStatus.CONFIRMED, LocalDate.of(2026, 7, 20));

            Optional<Booking> result = bookingRepository.findByBookingCode("MBK-20260720-XY99");

            assertThat(result).isPresent();
            assertThat(result.get().getBookingCode()).isEqualTo("MBK-20260720-XY99");
        }

        @Test
        @DisplayName("should return empty for non-existent code")
        void shouldReturnEmptyForBadCode() {
            Optional<Booking> result = bookingRepository.findByBookingCode("FAKE-CODE");
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Upcoming & Past bookings")
    class UpcomingPast {

        @Test
        @DisplayName("should find upcoming CONFIRMED bookings")
        void shouldFindUpcoming() {
            LocalDate future = LocalDate.now().plusDays(5);
            LocalDate past = LocalDate.now().minusDays(5);

            createBooking(1L, "MBK-FUTURE", BookingStatus.CONFIRMED, future);
            createBooking(1L, "MBK-PAST", BookingStatus.CONFIRMED, past);

            List<Booking> results = bookingRepository
                    .findByUserIdAndStatusAndShowDateGreaterThanEqualOrderByShowDateAsc(
                            1L, BookingStatus.CONFIRMED, LocalDate.now());

            assertThat(results).hasSize(1);
            assertThat(results.get(0).getBookingCode()).isEqualTo("MBK-FUTURE");
        }
    }
}
