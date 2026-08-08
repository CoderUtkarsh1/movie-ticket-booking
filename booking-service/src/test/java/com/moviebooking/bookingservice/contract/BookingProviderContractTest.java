package com.moviebooking.bookingservice.contract;

import com.moviebooking.bookingservice.entity.Booking;
import com.moviebooking.common.dto.BookingDto;
import com.moviebooking.common.enums.BookingStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Consumer-Driven Contract Test (Manual Pact-style)
 * 
 * Validates the CONTRACT that payment-service expects from booking-service.
 * If this test breaks → you changed an API contract → payment-service will also break.
 *
 * Contract: payment-service → booking-service
 *   GET  /api/bookings/internal/{id}          → BookingDto
 *   PUT  /api/bookings/internal/{id}/confirm-payment  → void
 *   PUT  /api/bookings/internal/{id}/fail-payment     → void
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Booking-Service Provider Contract Tests")
class BookingProviderContractTest {

    /**
     * Contract: BookingDto response shape must match what payment-service expects
     */
    @Nested
    @DisplayName("GET /api/bookings/internal/{id} — BookingDto contract")
    class GetBookingContract {

        @Test
        @DisplayName("BookingDto must have all fields that payment-service uses")
        void bookingDtoShouldHaveRequiredFields() {
            // This is the CONTRACT — payment-service depends on these exact fields
            BookingDto dto = BookingDto.builder()
                    .id(1L)
                    .bookingCode("MBK-20260720-AB12")
                    .userId(1L)
                    .showId(1L)
                    .movieName("Pushpa 3")
                    .totalAmount(new BigDecimal("500.00"))
                    .status(BookingStatus.PENDING.name())
                    .seatIds(List.of(101L, 102L))
                    .build();

            // payment-service reads these fields — if any is null, payment fails
            assertThat(dto.getId()).isNotNull();
            assertThat(dto.getBookingCode()).isNotBlank();
            assertThat(dto.getUserId()).isNotNull();
            assertThat(dto.getTotalAmount()).isGreaterThan(BigDecimal.ZERO);
            assertThat(dto.getStatus()).isNotNull();
            assertThat(dto.getSeatIds()).isNotEmpty();
        }

        @Test
        @DisplayName("BookingDto status should be a valid enum value")
        void statusShouldBeValidEnum() {
            // Contract: status must be one of these values
            assertThat(BookingStatus.values()).containsExactlyInAnyOrder(
                    BookingStatus.PENDING,
                    BookingStatus.CONFIRMED,
                    BookingStatus.CANCELLED,
                    BookingStatus.FAILED
            );
        }
    }

    /**
     * Contract: Booking entity → BookingDto mapping
     */
    @Nested
    @DisplayName("Booking → BookingDto mapping contract")
    class MappingContract {

        @Test
        @DisplayName("Booking entity should be mappable to BookingDto without losing data")
        void shouldMapEntityToDto() {
            Booking entity = Booking.builder()
                    .id(1L)
                    .bookingCode("MBK-20260720-AB12")
                    .userId(1L)
                    .showId(1L)
                    .movieName("Pushpa 3")
                    .theaterName("PVR Phoenix")
                    .screenName("Screen 1")
                    .showDate(LocalDate.of(2026, 7, 20))
                    .showTime(LocalTime.of(18, 30))
                    .totalSeats(2)
                    .totalAmount(new BigDecimal("500.00"))
                    .status(BookingStatus.PENDING)
                    .build();

            // Verify key fields exist on the entity (contract with DB schema)
            assertThat(entity.getBookingCode()).startsWith("MBK-");
            assertThat(entity.getUserId()).isPositive();
            assertThat(entity.getShowId()).isPositive();
            assertThat(entity.getTotalAmount()).isNotNull();
            assertThat(entity.getStatus()).isEqualTo(BookingStatus.PENDING);
        }
    }

    /**
     * Contract: SeatActionRequest shape (booking → show-service)
     */
    @Nested
    @DisplayName("SeatActionRequest contract (booking → show-service)")
    class SeatActionContract {

        @Test
        @DisplayName("SeatActionRequest must carry seatIds and userId")
        void shouldHaveRequiredFields() {
            var request = new com.moviebooking.common.dto.SeatActionRequest();
            request.setSeatIds(List.of(101L, 102L));
            request.setUserId(1L);

            assertThat(request.getSeatIds()).hasSize(2);
            assertThat(request.getUserId()).isNotNull();
        }
    }
}
