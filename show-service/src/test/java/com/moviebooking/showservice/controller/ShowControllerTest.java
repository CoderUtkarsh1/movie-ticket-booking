package com.moviebooking.showservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.moviebooking.common.enums.SeatStatus;
import com.moviebooking.common.enums.ShowStatus;
import com.moviebooking.showservice.dto.SeatBlockRequest;
import com.moviebooking.showservice.dto.ShowRequest;
import com.moviebooking.showservice.entity.Show;
import com.moviebooking.showservice.entity.ShowSeat;
import com.moviebooking.showservice.service.ShowService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ShowController @WebMvcTest — HTTP layer tests
 */
@WebMvcTest(ShowController.class)
@DisplayName("ShowController API Tests")
class ShowControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private ShowService showService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    private Show sampleShow() {
        return Show.builder()
                .id(1L).movieId("mov-001").screenId(1L).theaterId(1L)
                .showDate(LocalDate.of(2026, 7, 20))
                .showTime(LocalTime.of(18, 30))
                .status(ShowStatus.OPEN).build();
    }

    private ShowSeat sampleSeat(Long seatId) {
        return ShowSeat.builder()
                .id(seatId).seatId(seatId).seatRow("A").seatNumber(seatId.intValue())
                .seatType("REGULAR").price(new BigDecimal("250.00"))
                .status(SeatStatus.AVAILABLE).build();
    }

    @Nested
    @DisplayName("GET /api/shows")
    class GetShows {

        @Test
        @DisplayName("should return shows by movieId and date")
        void shouldReturnShowsByMovieAndDate() throws Exception {
            when(showService.getShowsByMovieAndDate("mov-001", LocalDate.of(2026, 7, 20)))
                    .thenReturn(List.of(sampleShow()));

            mockMvc.perform(get("/api/shows")
                            .param("movieId", "mov-001")
                            .param("date", "2026-07-20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].movieId").value("mov-001"));
        }
    }

    @Nested
    @DisplayName("GET /api/shows/{id}")
    class GetShowById {

        @Test
        @DisplayName("should return show by ID")
        void shouldReturnShow() throws Exception {
            when(showService.getShowById(1L)).thenReturn(sampleShow());

            mockMvc.perform(get("/api/shows/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(1));
        }
    }

    @Nested
    @DisplayName("POST /api/shows")
    class CreateShowEndpoint {

        @Test
        @DisplayName("should return 201 CREATED for valid show request")
        void shouldReturn201() throws Exception {
            when(showService.createShow(any())).thenReturn(sampleShow());

            ShowRequest request = ShowRequest.builder()
                    .movieId("mov-001").screenId(1L).theaterId(1L)
                    .showDate(LocalDate.of(2026, 7, 20))
                    .showTime(LocalTime.of(18, 30)).build();

            mockMvc.perform(post("/api/shows")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("PUT /api/shows/{id}/seats/block")
    class BlockSeatsEndpoint {

        @Test
        @DisplayName("should return 200 with blocked seats")
        void shouldReturn200() throws Exception {
            when(showService.blockSeats(eq(1L), eq(1L), eq(List.of(101L, 102L))))
                    .thenReturn(List.of(sampleSeat(101L), sampleSeat(102L)));

            SeatBlockRequest request = SeatBlockRequest.builder()
                    .userId(1L).seatIds(List.of(101L, 102L)).build();

            mockMvc.perform(put("/api/shows/1/seats/block")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data").isNotEmpty());
        }
    }

    @Nested
    @DisplayName("PUT /api/shows/{id}/seats/confirm")
    class ConfirmSeatsEndpoint {

        @Test
        @DisplayName("should return 200 on seat confirmation (Feign internal)")
        void shouldReturn200() throws Exception {
            SeatBlockRequest request = SeatBlockRequest.builder()
                    .userId(1L).seatIds(List.of(101L)).build();

            mockMvc.perform(put("/api/shows/1/seats/confirm")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }
}
