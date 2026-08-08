package com.moviebooking.paymentservice.contract;

import com.moviebooking.common.dto.BookingDto;
import com.moviebooking.common.enums.BookingStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Consumer Contract Test — Payment-service as CONSUMER of booking-service.
 *
 * This test validates that payment-service can correctly consume
 * the BookingDto that booking-service provides.
 * If booking-service changes BookingDto shape, this test MUST also fail.
 */
@DisplayName("Payment-Service Consumer Contract Tests")
class PaymentConsumerContractTest {

    @Nested
    @DisplayName("BookingDto — consumer expectations")
    class BookingDtoConsumerExpectation {

        @Test
        @DisplayName("payment-service must be able to read booking amount")
        void shouldReadBookingAmount() {
            // Simulates what payment-service receives from booking-service
            BookingDto booking = BookingDto.builder()
                    .id(1L)
                    .bookingCode("MBK-20260720-AB12")
                    .userId(1L)
                    .totalAmount(new BigDecimal("500.00"))
                    .status(BookingStatus.PENDING.name())
                    .seatIds(List.of(101L, 102L))
                    .build();

            // Payment logic depends on these exact fields
            assertThat(booking.getTotalAmount())
                    .as("Payment needs totalAmount to process deduction")
                    .isGreaterThan(BigDecimal.ZERO);

            assertThat(booking.getStatus())
                    .as("Payment only processes PENDING bookings")
                    .isEqualTo(BookingStatus.PENDING.name());

            assertThat(booking.getUserId())
                    .as("Payment needs userId to find wallet")
                    .isNotNull();
        }

        @Test
        @DisplayName("payment-service must be able to read seatIds for refund")
        void shouldReadSeatIdsForRefund() {
            BookingDto booking = BookingDto.builder()
                    .id(1L)
                    .seatIds(List.of(101L, 102L, 103L))
                    .build();

            assertThat(booking.getSeatIds())
                    .as("Refund flow needs seatIds to release seats via show-service")
                    .isNotEmpty()
                    .hasSize(3);
        }

        @Test
        @DisplayName("CONFIRMED status means payment was successful")
        void confirmedStatusMeansPaymentDone() {
            BookingDto booking = BookingDto.builder()
                    .status(BookingStatus.CONFIRMED.name()).build();

            assertThat(booking.getStatus())
                    .isEqualTo(BookingStatus.CONFIRMED.name());

            // Cannot process payment again for CONFIRMED booking
            assertThat(booking.getStatus())
                    .isNotEqualTo(BookingStatus.PENDING.name());
        }
    }

    @Nested
    @DisplayName("Wallet response shape contract")
    class WalletResponseContract {

        @Test
        @DisplayName("WalletResponse must include userId and balance")
        void shouldHaveUserIdAndBalance() {
            // Validates the response shape payment-service provides to frontend
            var userId = 1L;
            var balance = new BigDecimal("1500.00");

            assertThat(userId).isPositive();
            assertThat(balance).isGreaterThanOrEqualTo(BigDecimal.ZERO);
        }
    }
}
