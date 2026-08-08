package com.moviebooking.bookingservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.moviebooking.bookingservice.dto.BookingRequest;
import com.moviebooking.bookingservice.entity.Booking;
import com.moviebooking.bookingservice.service.BookingService;
import com.moviebooking.common.enums.BookingStatus;
import com.moviebooking.common.dto.BookingDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller Slice Tests — @WebMvcTest (NO full Spring context, ultra-fast)
 * Tests HTTP layer: status codes, request validation, response format
 */
@WebMvcTest(BookingController.class)
@DisplayName("BookingController API Tests")
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookingService bookingService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    private Booking sampleBooking() {
        return Booking.builder()
                .id(1L).bookingCode("MBK-20260720-AB12").userId(1L)
                .showId(1L).movieName("Pushpa 3").theaterName("PVR")
                .screenName("Screen 1").showDate(LocalDate.of(2026, 7, 20))
                .showTime(LocalTime.of(18, 30)).totalSeats(2)
                .totalAmount(new BigDecimal("500.00"))
                .status(BookingStatus.PENDING).build();
    }

    private BookingRequest validRequest() {
        return BookingRequest.builder()
                .showId(1L).movieName("Pushpa 3").theaterName("PVR")
                .screenName("Screen 1").showDate(LocalDate.of(2026, 7, 20))
                .showTime(LocalTime.of(18, 30))
                .seats(List.of(
                        BookingRequest.SeatInfo.builder()
                                .seatId(101L).seatRow("A").seatNumber(1)
                                .seatType("REGULAR").price(new BigDecimal("250.00")).build()
                )).build();
    }

    @Nested
    @DisplayName("POST /api/bookings")
    class CreateBookingEndpoint {

        @Test
        @DisplayName("should return 201 CREATED with valid request")
        void shouldReturn201() throws Exception {
            when(bookingService.createBooking(eq(1L), any(), any()))
                    .thenReturn(sampleBooking());

            mockMvc.perform(post("/api/bookings")
                            .header("X-User-Id", "1")
                            .header("X-User-Email", "test@email.com")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest())))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.bookingCode").value("MBK-20260720-AB12"))
                    .andExpect(jsonPath("$.data.status").value("PENDING"));
        }

        @Test
        @DisplayName("should return 400 when X-User-Id header is missing")
        void shouldReturn400WhenNoUserIdHeader() throws Exception {
            mockMvc.perform(post("/api/bookings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest())))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/bookings/{id}")
    class GetBookingEndpoint {

        @Test
        @DisplayName("should return 200 with booking data")
        void shouldReturn200() throws Exception {
            when(bookingService.getBookingById(1L)).thenReturn(sampleBooking());

            mockMvc.perform(get("/api/bookings/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(1));
        }
    }

    @Nested
    @DisplayName("GET /api/bookings/user")
    class GetUserBookingsEndpoint {

        @Test
        @DisplayName("should return user's bookings list")
        void shouldReturnUserBookings() throws Exception {
            when(bookingService.getUserBookings(1L))
                    .thenReturn(List.of(sampleBooking()));

            mockMvc.perform(get("/api/bookings/user")
                            .header("X-User-Id", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].bookingCode").value("MBK-20260720-AB12"));
        }
    }

    @Nested
    @DisplayName("PUT /api/bookings/{id}/cancel")
    class CancelBookingEndpoint {

        @Test
        @DisplayName("should return 200 on successful cancellation")
        void shouldReturn200OnCancel() throws Exception {
            Booking cancelled = sampleBooking();
            cancelled.setStatus(BookingStatus.CANCELLED);
            when(bookingService.cancelBooking(1L, 1L)).thenReturn(cancelled);

            mockMvc.perform(put("/api/bookings/1/cancel")
                            .header("X-User-Id", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("CANCELLED"));
        }
    }

    @Nested
    @DisplayName("Internal Feign Endpoints")
    class InternalEndpoints {

        @Test
        @DisplayName("GET /api/bookings/internal/{id} should return BookingDto")
        void shouldReturnBookingDto() throws Exception {
            BookingDto dto = BookingDto.builder()
                    .id(1L).bookingCode("MBK-20260720-AB12")
                    .userId(1L).totalAmount(new BigDecimal("500.00"))
                    .status("PENDING").build();
            when(bookingService.getBookingAsDto(1L)).thenReturn(dto);

            mockMvc.perform(get("/api/bookings/internal/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.bookingCode").value("MBK-20260720-AB12"));
        }

        @Test
        @DisplayName("PUT /api/bookings/internal/{id}/confirm-payment should return 200")
        void shouldConfirmPayment() throws Exception {
            when(bookingService.confirmPayment(1L)).thenReturn(sampleBooking());

            mockMvc.perform(put("/api/bookings/internal/1/confirm-payment"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("Dashboard Endpoints")
    class DashboardEndpoints {

        @Test
        @DisplayName("GET /api/bookings/dashboard/upcoming should return upcoming bookings")
        void shouldReturnUpcoming() throws Exception {
            when(bookingService.getUpcomingBookings(1L))
                    .thenReturn(List.of(sampleBooking()));

            mockMvc.perform(get("/api/bookings/dashboard/upcoming")
                            .header("X-User-Id", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("GET /api/bookings/dashboard/past should return past bookings")
        void shouldReturnPast() throws Exception {
            when(bookingService.getPastBookings(1L)).thenReturn(List.of());

            mockMvc.perform(get("/api/bookings/dashboard/past")
                            .header("X-User-Id", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray());
        }
    }
}
