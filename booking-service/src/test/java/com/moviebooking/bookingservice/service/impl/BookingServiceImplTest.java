package com.moviebooking.bookingservice.service.impl;

import com.moviebooking.bookingservice.dto.BookingRequest;
import com.moviebooking.bookingservice.entity.BookedSeat;
import com.moviebooking.bookingservice.entity.Booking;
import com.moviebooking.bookingservice.feign.ShowServiceClient;
import com.moviebooking.bookingservice.kafka.BookingEventProducer;
import com.moviebooking.bookingservice.repository.BookingRepository;
import com.moviebooking.common.dto.ApiResponse;
import com.moviebooking.common.dto.SeatActionRequest;
import com.moviebooking.common.enums.BookingStatus;
import com.moviebooking.common.event.BookingEvent;
import com.moviebooking.common.exception.BadRequestException;
import com.moviebooking.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit Tests for BookingServiceImpl — 2026 Industry Standard
 * 
 * Pattern: @ExtendWith(MockitoExtension) — NO Spring context, pure unit tests
 * Assertions: AssertJ fluent API
 * Organization: @Nested BDD-style grouping
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BookingServiceImpl Unit Tests")
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingEventProducer bookingEventProducer;

    @Mock
    private ShowServiceClient showServiceClient;

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Captor
    private ArgumentCaptor<Booking> bookingCaptor;

    @Captor
    private ArgumentCaptor<BookingEvent> eventCaptor;

    // ===== Helper Methods =====

    private BookingRequest buildValidRequest() {
        return BookingRequest.builder()
                .showId(1L)
                .movieName("Pushpa 3")
                .theaterName("PVR Phoenix")
                .screenName("Screen 1")
                .showDate(LocalDate.of(2026, 7, 20))
                .showTime(LocalTime.of(18, 30))
                .seats(List.of(
                        BookingRequest.SeatInfo.builder()
                                .seatId(101L).seatRow("A").seatNumber(1)
                                .seatType("REGULAR").price(new BigDecimal("250.00")).build(),
                        BookingRequest.SeatInfo.builder()
                                .seatId(102L).seatRow("A").seatNumber(2)
                                .seatType("REGULAR").price(new BigDecimal("250.00")).build()
                ))
                .build();
    }

    private Booking buildPendingBooking() {
        Booking booking = Booking.builder()
                .id(1L)
                .bookingCode("MBK-20260720-AB12")
                .userId(1L)
                .userEmail("test@email.com")
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

        booking.setBookedSeats(List.of(
                BookedSeat.builder().id(1L).booking(booking).seatId(101L)
                        .seatRow("A").seatNumber(1).seatType("REGULAR")
                        .price(new BigDecimal("250.00")).build(),
                BookedSeat.builder().id(2L).booking(booking).seatId(102L)
                        .seatRow("A").seatNumber(2).seatType("REGULAR")
                        .price(new BigDecimal("250.00")).build()
        ));
        return booking;
    }

    // ===== Test Groups =====

    @Nested
    @DisplayName("createBooking()")
    class CreateBooking {

        @Test
        @DisplayName("should generate booking code in MBK-yyyyMMdd-XXXX format")
        void shouldGenerateCorrectBookingCodeFormat() {
            // Given
            BookingRequest request = buildValidRequest();
            when(bookingRepository.save(any(Booking.class)))
                    .thenAnswer(invocation -> {
                        Booking b = invocation.getArgument(0);
                        b.setId(1L);
                        return b;
                    });

            // When
            Booking result = bookingService.createBooking(1L, "test@email.com", request);

            // Then
            assertThat(result.getBookingCode())
                    .startsWith("MBK-")
                    .hasSize(17); // MBK-20260717-AB12
        }

        @Test
        @DisplayName("should calculate total amount from all seat prices")
        void shouldCalculateTotalAmount() {
            // Given
            BookingRequest request = buildValidRequest(); // 250 + 250 = 500
            when(bookingRepository.save(any(Booking.class)))
                    .thenAnswer(inv -> {
                        Booking b = inv.getArgument(0);
                        b.setId(1L);
                        return b;
                    });

            // When
            Booking result = bookingService.createBooking(1L, "test@email.com", request);

            // Then
            assertThat(result.getTotalAmount()).isEqualByComparingTo(new BigDecimal("500.00"));
        }

        @Test
        @DisplayName("should set initial status as PENDING")
        void shouldSetStatusPending() {
            BookingRequest request = buildValidRequest();
            when(bookingRepository.save(any(Booking.class)))
                    .thenAnswer(inv -> {
                        Booking b = inv.getArgument(0);
                        b.setId(1L);
                        return b;
                    });

            Booking result = bookingService.createBooking(1L, "test@email.com", request);

            assertThat(result.getStatus()).isEqualTo(BookingStatus.PENDING);
        }

        @Test
        @DisplayName("should associate all booked seats to the booking")
        void shouldAssociateBookedSeats() {
            BookingRequest request = buildValidRequest();
            when(bookingRepository.save(any(Booking.class)))
                    .thenAnswer(inv -> {
                        Booking b = inv.getArgument(0);
                        b.setId(1L);
                        return b;
                    });

            Booking result = bookingService.createBooking(1L, "test@email.com", request);

            assertThat(result.getBookedSeats()).hasSize(2);
            assertThat(result.getTotalSeats()).isEqualTo(2);
        }

        @Test
        @DisplayName("should save booking twice — once for entity, once with seats")
        void shouldSaveTwice() {
            BookingRequest request = buildValidRequest();
            when(bookingRepository.save(any(Booking.class)))
                    .thenAnswer(inv -> {
                        Booking b = inv.getArgument(0);
                        b.setId(1L);
                        return b;
                    });

            bookingService.createBooking(1L, "test@email.com", request);

            verify(bookingRepository, times(2)).save(any(Booking.class));
        }
    }

    @Nested
    @DisplayName("confirmPayment()")
    class ConfirmPayment {

        @Test
        @DisplayName("should change status from PENDING → CONFIRMED")
        void shouldConfirmBooking() {
            Booking booking = buildPendingBooking();
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
            when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
            when(showServiceClient.confirmSeats(anyLong(), any())).thenReturn(ApiResponse.success(null));

            Booking result = bookingService.confirmPayment(1L);

            assertThat(result.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        }

        @Test
        @DisplayName("should throw BadRequestException when booking is not PENDING")
        void shouldThrowWhenNotPending() {
            Booking booking = buildPendingBooking();
            booking.setStatus(BookingStatus.CONFIRMED); // already confirmed
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

            assertThatThrownBy(() -> bookingService.confirmPayment(1L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("not in PENDING state");
        }

        @Test
        @DisplayName("should call showServiceClient.confirmSeats via Feign")
        void shouldCallFeignConfirmSeats() {
            Booking booking = buildPendingBooking();
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
            when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(showServiceClient.confirmSeats(anyLong(), any())).thenReturn(ApiResponse.success(null));

            bookingService.confirmPayment(1L);

            ArgumentCaptor<SeatActionRequest> captor = ArgumentCaptor.forClass(SeatActionRequest.class);
            verify(showServiceClient).confirmSeats(eq(1L), captor.capture());
            assertThat(captor.getValue().getSeatIds()).containsExactly(101L, 102L);
            assertThat(captor.getValue().getUserId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("should still confirm booking even if Feign call fails (resilience)")
        void shouldConfirmEvenIfFeignFails() {
            Booking booking = buildPendingBooking();
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
            when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(showServiceClient.confirmSeats(anyLong(), any()))
                    .thenThrow(new RuntimeException("show-service is DOWN"));

            Booking result = bookingService.confirmPayment(1L);

            // Booking should STILL be confirmed — self-healing design
            assertThat(result.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        }

        @Test
        @DisplayName("should publish BOOKING_CONFIRMED Kafka event")
        void shouldPublishKafkaEvent() {
            Booking booking = buildPendingBooking();
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
            when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(showServiceClient.confirmSeats(anyLong(), any())).thenReturn(ApiResponse.success(null));

            bookingService.confirmPayment(1L);

            verify(bookingEventProducer).sendBookingConfirmed(eventCaptor.capture());
            BookingEvent event = eventCaptor.getValue();
            assertThat(event.getStatus()).isEqualTo("CONFIRMED");
            assertThat(event.getBookingCode()).isEqualTo("MBK-20260720-AB12");
            assertThat(event.getUserId()).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("failPayment()")
    class FailPayment {

        @Test
        @DisplayName("should change status from PENDING → FAILED")
        void shouldFailBooking() {
            Booking booking = buildPendingBooking();
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
            when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Booking result = bookingService.failPayment(1L);

            assertThat(result.getStatus()).isEqualTo(BookingStatus.FAILED);
        }

        @Test
        @DisplayName("should release seats via Feign on failure")
        void shouldReleaseSeatsOnFailure() {
            Booking booking = buildPendingBooking();
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
            when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            bookingService.failPayment(1L);

            verify(showServiceClient).releaseSeats(eq(1L), any(SeatActionRequest.class));
        }

        @Test
        @DisplayName("should publish BOOKING_CANCELLED Kafka event on failure")
        void shouldPublishCancelledEvent() {
            Booking booking = buildPendingBooking();
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
            when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            bookingService.failPayment(1L);

            verify(bookingEventProducer).sendBookingCancelled(eventCaptor.capture());
            assertThat(eventCaptor.getValue().getStatus()).isEqualTo("CANCELLED");
        }
    }

    @Nested
    @DisplayName("cancelBooking()")
    class CancelBooking {

        @Test
        @DisplayName("should cancel own booking successfully")
        void shouldCancelOwnBooking() {
            Booking booking = buildPendingBooking();
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
            when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Booking result = bookingService.cancelBooking(1L, 1L); // userId=1 matches

            assertThat(result.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        }

        @Test
        @DisplayName("should throw when cancelling other user's booking")
        void shouldThrowWhenCancellingOthersBooking() {
            Booking booking = buildPendingBooking(); // userId = 1
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

            assertThatThrownBy(() -> bookingService.cancelBooking(1L, 999L)) // wrong userId
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("only cancel your own");
        }

        @Test
        @DisplayName("should throw when booking is already cancelled")
        void shouldThrowWhenAlreadyCancelled() {
            Booking booking = buildPendingBooking();
            booking.setStatus(BookingStatus.CANCELLED);
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

            assertThatThrownBy(() -> bookingService.cancelBooking(1L, 1L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("already cancelled");
        }
    }

    @Nested
    @DisplayName("getBookingById()")
    class GetBookingById {

        @Test
        @DisplayName("should return booking when found")
        void shouldReturnBooking() {
            Booking booking = buildPendingBooking();
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

            Booking result = bookingService.getBookingById(1L);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getBookingCode()).isEqualTo("MBK-20260720-AB12");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookingService.getBookingById(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getBookingAsDto()")
    class GetBookingAsDto {

        @Test
        @DisplayName("should convert Booking entity to BookingDto correctly")
        void shouldConvertToDto() {
            Booking booking = buildPendingBooking();
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

            var dto = bookingService.getBookingAsDto(1L);

            assertThat(dto.getId()).isEqualTo(1L);
            assertThat(dto.getBookingCode()).isEqualTo("MBK-20260720-AB12");
            assertThat(dto.getStatus()).isEqualTo("PENDING");
            assertThat(dto.getSeatIds()).containsExactly(101L, 102L);
            assertThat(dto.getTotalAmount()).isEqualByComparingTo(new BigDecimal("500.00"));
        }
    }
}
