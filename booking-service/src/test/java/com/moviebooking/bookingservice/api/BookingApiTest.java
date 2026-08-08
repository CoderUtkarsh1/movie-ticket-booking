package com.moviebooking.bookingservice.api;

import com.moviebooking.bookingservice.entity.Booking;
import com.moviebooking.bookingservice.repository.BookingRepository;
import com.moviebooking.common.enums.BookingStatus;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
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

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * REST Assured API Tests — Full HTTP request/response validation.
 * Tests actual API endpoints with real DB via Testcontainers.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@DisplayName("Booking API Tests (REST Assured)")
class BookingApiTest {

    @Container
    @ServiceConnection
    static MSSQLServerContainer<?> sqlServer = new MSSQLServerContainer<>(
            "mcr.microsoft.com/mssql/server:2022-latest")
            .acceptLicense()
            .withPassword("TestP@ssw0rd123");

    @Container
    static KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.6.0"));

    @LocalServerPort
    private int port;

    @Autowired
    private BookingRepository bookingRepository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api/bookings";
        bookingRepository.deleteAll();
    }

    @org.springframework.test.context.DynamicPropertySource
    static void kafkaProperties(org.springframework.test.context.DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    private void seedBooking(String code, BookingStatus status) {
        Booking booking = Booking.builder()
                .bookingCode(code).userId(1L).userEmail("test@test.com")
                .showId(1L).movieName("Pushpa 3").theaterName("PVR")
                .screenName("Screen 1").showDate(LocalDate.of(2026, 7, 20))
                .showTime(LocalTime.of(18, 30)).totalSeats(2)
                .totalAmount(new BigDecimal("500.00")).status(status).build();
        bookingRepository.save(booking);
    }

    @Test
    @DisplayName("GET /api/bookings/{id} — should return booking")
    void shouldGetBookingById() {
        seedBooking("MBK-API-001", BookingStatus.CONFIRMED);
        Booking saved = bookingRepository.findByBookingCode("MBK-API-001").orElseThrow();

        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/{id}", saved.getId())
        .then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("data.bookingCode", equalTo("MBK-API-001"))
            .body("data.status", equalTo("CONFIRMED"))
            .body("data.movieName", equalTo("Pushpa 3"));
    }

    @Test
    @DisplayName("GET /api/bookings/user — should return user bookings")
    void shouldGetUserBookings() {
        seedBooking("MBK-USER-001", BookingStatus.CONFIRMED);
        seedBooking("MBK-USER-002", BookingStatus.PENDING);

        given()
            .contentType(ContentType.JSON)
            .header("X-User-Id", "1")
        .when()
            .get("/user")
        .then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("data", hasSize(2));
    }

    @Test
    @DisplayName("GET /api/bookings/{id} — should return 404 for non-existent booking")
    void shouldReturn404ForMissingBooking() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/{id}", 99999)
        .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("POST /api/bookings — should create booking via API")
    void shouldCreateBookingViaApi() {
        String requestBody = """
                {
                  "showId": 1,
                  "movieName": "Pushpa 3",
                  "theaterName": "PVR Phoenix",
                  "screenName": "Screen 1",
                  "showDate": "2026-07-20",
                  "showTime": "18:30",
                  "seats": [
                    {"seatId": 101, "seatRow": "A", "seatNumber": 1, "seatType": "REGULAR", "price": 250.00},
                    {"seatId": 102, "seatRow": "A", "seatNumber": 2, "seatType": "REGULAR", "price": 250.00}
                  ]
                }
                """;

        given()
            .contentType(ContentType.JSON)
            .header("X-User-Id", "1")
            .header("X-User-Email", "test@example.com")
            .body(requestBody)
        .when()
            .post()
        .then()
            .statusCode(201)
            .body("success", equalTo(true))
            .body("data.bookingCode", startsWith("MBK-"))
            .body("data.status", equalTo("PENDING"))
            .body("data.totalAmount", equalTo(500.0f));
    }
}
