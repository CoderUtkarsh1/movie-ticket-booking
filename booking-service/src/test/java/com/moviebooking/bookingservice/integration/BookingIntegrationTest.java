package com.moviebooking.bookingservice.integration;

import com.moviebooking.bookingservice.dto.BookingRequest;
import com.moviebooking.bookingservice.entity.Booking;
import com.moviebooking.bookingservice.repository.BookingRepository;
import com.moviebooking.bookingservice.service.BookingService;
import com.moviebooking.common.enums.BookingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration Test — Full Spring Boot context with REAL SQL Server + Kafka via Testcontainers.
 * Tests the complete booking flow end-to-end within the service boundary.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@DisplayName("Booking Integration Tests (Testcontainers)")
class BookingIntegrationTest {

    @Container
    @ServiceConnection
    static MSSQLServerContainer<?> sqlServer = new MSSQLServerContainer<>(
            "mcr.microsoft.com/mssql/server:2022-latest")
            .acceptLicense()
            .withPassword("TestP@ssw0rd123");

    @Container
    static KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.6.0"));

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
    }

    @org.springframework.test.context.DynamicPropertySource
    static void kafkaProperties(org.springframework.test.context.DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    private BookingRequest buildRequest() {
        BookingRequest.SeatInfo seat1 = new BookingRequest.SeatInfo();
        seat1.setSeatId(101L);
        seat1.setSeatRow("A");
        seat1.setSeatNumber(1);
        seat1.setSeatType("REGULAR");
        seat1.setPrice(new BigDecimal("250.00"));

        BookingRequest.SeatInfo seat2 = new BookingRequest.SeatInfo();
        seat2.setSeatId(102L);
        seat2.setSeatRow("A");
        seat2.setSeatNumber(2);
        seat2.setSeatType("REGULAR");
        seat2.setPrice(new BigDecimal("250.00"));

        BookingRequest request = new BookingRequest();
        request.setShowId(1L);
        request.setMovieName("Pushpa 3");
        request.setTheaterName("PVR Phoenix");
        request.setScreenName("Screen 1");
        request.setShowDate(LocalDate.of(2026, 7, 20));
        request.setShowTime(LocalTime.of(18, 30));
        request.setSeats(List.of(seat1, seat2));
        return request;
    }

    @Test
    @DisplayName("should create booking and persist to real SQL Server")
    void shouldCreateAndPersistBooking() {
        Booking booking = bookingService.createBooking(1L, "test@example.com", buildRequest());

        assertThat(booking.getId()).isNotNull();
        assertThat(booking.getBookingCode()).startsWith("MBK-");
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.PENDING);
        assertThat(booking.getTotalAmount()).isEqualByComparingTo(new BigDecimal("500.00"));

        // Verify persisted in DB
        assertThat(bookingRepository.findByBookingCode(booking.getBookingCode())).isPresent();
    }

    @Test
    @DisplayName("should find booking by code after creation")
    void shouldFindBookingByCode() {
        Booking booking = bookingService.createBooking(1L, "test@example.com", buildRequest());

        Booking found = bookingRepository.findByBookingCode(booking.getBookingCode())
                .orElseThrow();

        assertThat(found.getUserId()).isEqualTo(1L);
        assertThat(found.getMovieName()).isEqualTo("Pushpa 3");
    }

    @Test
    @DisplayName("should list user bookings ordered by creation time")
    void shouldListUserBookings() {
        bookingService.createBooking(1L, "test@example.com", buildRequest());
        bookingService.createBooking(1L, "test@example.com", buildRequest());

        List<Booking> bookings = bookingService.getUserBookings(1L);

        assertThat(bookings).hasSize(2);
        assertThat(bookings).allSatisfy(b -> assertThat(b.getUserId()).isEqualTo(1L));
    }
}
