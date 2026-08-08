package com.moviebooking.paymentservice.service.impl;

import com.moviebooking.common.dto.ApiResponse;
import com.moviebooking.common.dto.BookingDto;
import com.moviebooking.common.enums.PaymentMethod;
import com.moviebooking.common.enums.PaymentStatus;
import com.moviebooking.common.enums.TransactionType;
import com.moviebooking.common.exception.BadRequestException;
import com.moviebooking.paymentservice.dto.PaymentRequest;
import com.moviebooking.paymentservice.entity.Payment;
import com.moviebooking.paymentservice.entity.Wallet;
import com.moviebooking.paymentservice.entity.WalletTransaction;
import com.moviebooking.paymentservice.feign.BookingServiceClient;
import com.moviebooking.paymentservice.repository.PaymentRepository;
import com.moviebooking.paymentservice.repository.WalletRepository;
import com.moviebooking.paymentservice.repository.WalletTransactionRepository;
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
import static org.mockito.Mockito.*;

/**
 * Unit Tests for PaymentServiceImpl — 2026 Industry Standard
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentServiceImpl Unit Tests")
class PaymentServiceImplTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private WalletRepository walletRepository;
    @Mock private WalletTransactionRepository walletTransactionRepository;
    @Mock private BookingServiceClient bookingServiceClient;

    @InjectMocks private PaymentServiceImpl paymentService;

    @Captor private ArgumentCaptor<Payment> paymentCaptor;
    @Captor private ArgumentCaptor<WalletTransaction> txnCaptor;

    // ===== Helpers =====

    private BookingDto buildPendingBookingDto() {
        return BookingDto.builder()
                .id(1L).bookingCode("MBK-20260720-AB12").userId(1L)
                .showId(1L).movieName("Pushpa 3").theaterName("PVR")
                .screenName("Screen 1").showDate(LocalDate.of(2026, 7, 20))
                .showTime(LocalTime.of(18, 30)).totalSeats(2)
                .totalAmount(new BigDecimal("500.00")).status("PENDING")
                .seatIds(List.of(101L, 102L)).build();
    }

    private void stubBookingFetch(BookingDto dto) {
        when(bookingServiceClient.getBookingById(anyLong(), any()))
                .thenReturn(ApiResponse.success(dto));
    }

    private PaymentRequest upiRequest() {
        return PaymentRequest.builder().bookingId(1L).paymentMethod("UPI").build();
    }

    private PaymentRequest walletRequest() {
        return PaymentRequest.builder().bookingId(1L).paymentMethod("WALLET").build();
    }

    // ===== Tests =====

    @Nested
    @DisplayName("processPayment() — UPI/CARD (mock gateway)")
    class ProcessPaymentGateway {

        @Test
        @DisplayName("should process UPI payment successfully")
        void shouldProcessUpiPayment() {
            stubBookingFetch(buildPendingBookingDto());
            when(paymentRepository.findByBookingId(1L)).thenReturn(Optional.empty());
            when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
                Payment p = inv.getArgument(0);
                p.setId(1L);
                return p;
            });

            Payment result = paymentService.processPayment(1L, upiRequest());

            assertThat(result.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
            assertThat(result.getPaymentMethod()).isEqualTo(PaymentMethod.UPI);
            assertThat(result.getTransactionId()).startsWith("TXN-");
        }

        @Test
        @DisplayName("should call booking confirm Feign on success")
        void shouldConfirmBookingOnSuccess() {
            stubBookingFetch(buildPendingBookingDto());
            when(paymentRepository.findByBookingId(1L)).thenReturn(Optional.empty());
            when(paymentRepository.save(any())).thenAnswer(inv -> {
                Payment p = inv.getArgument(0);
                p.setId(1L);
                return p;
            });

            paymentService.processPayment(1L, upiRequest());

            verify(bookingServiceClient).confirmPayment(eq(1L), any());
        }

        @Test
        @DisplayName("should handle Feign failure gracefully — payment still saved as SUCCESS")
        void shouldHandleFeignFailure() {
            stubBookingFetch(buildPendingBookingDto());
            when(paymentRepository.findByBookingId(1L)).thenReturn(Optional.empty());
            when(paymentRepository.save(any())).thenAnswer(inv -> {
                Payment p = inv.getArgument(0);
                p.setId(1L);
                return p;
            });
            doThrow(new RuntimeException("booking-service DOWN"))
                    .when(bookingServiceClient).confirmPayment(anyLong(), any());

            Payment result = paymentService.processPayment(1L, upiRequest());

            // Payment is still SUCCESS — booking will be retried
            assertThat(result.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        }
    }

    @Nested
    @DisplayName("processPayment() — WALLET")
    class ProcessWalletPayment {

        @Test
        @DisplayName("should deduct wallet balance on successful payment")
        void shouldDeductWalletBalance() {
            stubBookingFetch(buildPendingBookingDto()); // totalAmount = 500
            when(paymentRepository.findByBookingId(1L)).thenReturn(Optional.empty());
            when(paymentRepository.save(any())).thenAnswer(inv -> {
                Payment p = inv.getArgument(0);
                p.setId(1L);
                return p;
            });

            Wallet wallet = Wallet.builder().id(1L).userId(1L)
                    .balance(new BigDecimal("1000.00")).build();
            when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(wallet));
            when(walletRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Payment result = paymentService.processPayment(1L, walletRequest());

            assertThat(result.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
            assertThat(wallet.getBalance()).isEqualByComparingTo(new BigDecimal("500.00")); // 1000 - 500
        }

        @Test
        @DisplayName("should fail payment when wallet has insufficient balance")
        void shouldFailOnInsufficientBalance() {
            stubBookingFetch(buildPendingBookingDto()); // totalAmount = 500
            when(paymentRepository.findByBookingId(1L)).thenReturn(Optional.empty());
            when(paymentRepository.save(any())).thenAnswer(inv -> {
                Payment p = inv.getArgument(0);
                p.setId(1L);
                return p;
            });

            Wallet wallet = Wallet.builder().id(1L).userId(1L)
                    .balance(new BigDecimal("100.00")).build(); // only 100
            when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(wallet));

            Payment result = paymentService.processPayment(1L, walletRequest());

            assertThat(result.getStatus()).isEqualTo(PaymentStatus.FAILED);
            verify(bookingServiceClient).failPayment(eq(1L), any());
        }

        @Test
        @DisplayName("should log DEBIT wallet transaction on success")
        void shouldLogWalletTransaction() {
            stubBookingFetch(buildPendingBookingDto());
            when(paymentRepository.findByBookingId(1L)).thenReturn(Optional.empty());
            when(paymentRepository.save(any())).thenAnswer(inv -> {
                Payment p = inv.getArgument(0);
                p.setId(1L);
                return p;
            });

            Wallet wallet = Wallet.builder().id(1L).userId(1L)
                    .balance(new BigDecimal("1000.00")).build();
            when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(wallet));
            when(walletRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            paymentService.processPayment(1L, walletRequest());

            verify(walletTransactionRepository).save(txnCaptor.capture());
            WalletTransaction txn = txnCaptor.getValue();
            assertThat(txn.getType()).isEqualTo(TransactionType.DEBIT);
            assertThat(txn.getAmount()).isEqualByComparingTo(new BigDecimal("500.00"));
            assertThat(txn.getBalanceBefore()).isEqualByComparingTo(new BigDecimal("1000.00"));
            assertThat(txn.getBalanceAfter()).isEqualByComparingTo(new BigDecimal("500.00"));
        }
    }

    @Nested
    @DisplayName("processPayment() — Validation")
    class ProcessPaymentValidation {

        @Test
        @DisplayName("should throw BadRequestException for wrong userId (ownership check)")
        void shouldThrowForWrongUser() {
            BookingDto dto = buildPendingBookingDto(); // userId = 1
            stubBookingFetch(dto);

            assertThatThrownBy(() -> paymentService.processPayment(999L, upiRequest()))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Unauthorized");
        }

        @Test
        @DisplayName("should throw BadRequestException for non-PENDING booking")
        void shouldThrowForNonPendingBooking() {
            BookingDto dto = buildPendingBookingDto();
            dto.setStatus("CONFIRMED"); // not PENDING
            stubBookingFetch(dto);

            assertThatThrownBy(() -> paymentService.processPayment(1L, upiRequest()))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("not in PENDING state");
        }

        @Test
        @DisplayName("should reject duplicate payment (idempotency)")
        void shouldRejectDuplicatePayment() {
            stubBookingFetch(buildPendingBookingDto());
            Payment existing = Payment.builder().id(1L).bookingId(1L)
                    .status(PaymentStatus.SUCCESS).build();
            when(paymentRepository.findByBookingId(1L)).thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> paymentService.processPayment(1L, upiRequest()))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("already completed");
        }
    }

    @Nested
    @DisplayName("addMoney()")
    class AddMoney {

        @Test
        @DisplayName("should add money and update wallet balance")
        void shouldAddMoney() {
            Wallet wallet = Wallet.builder().id(1L).userId(1L)
                    .balance(new BigDecimal("500.00")).build();
            when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(wallet));
            when(walletRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            var response = paymentService.addMoney(1L, new BigDecimal("300.00"));

            assertThat(response.getBalance()).isEqualByComparingTo(new BigDecimal("800.00"));
        }

        @Test
        @DisplayName("should create new wallet for first-time user")
        void shouldCreateWalletForNewUser() {
            when(walletRepository.findByUserId(99L)).thenReturn(Optional.empty());
            when(walletRepository.save(any())).thenAnswer(inv -> {
                Wallet w = inv.getArgument(0);
                w.setId(1L);
                return w;
            });

            var response = paymentService.addMoney(99L, new BigDecimal("1000.00"));

            assertThat(response.getBalance()).isEqualByComparingTo(new BigDecimal("1000.00"));
            assertThat(response.getUserId()).isEqualTo(99L);
        }

        @Test
        @DisplayName("should log CREDIT wallet transaction")
        void shouldLogCreditTransaction() {
            Wallet wallet = Wallet.builder().id(1L).userId(1L)
                    .balance(new BigDecimal("500.00")).build();
            when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(wallet));
            when(walletRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            paymentService.addMoney(1L, new BigDecimal("300.00"));

            verify(walletTransactionRepository).save(txnCaptor.capture());
            assertThat(txnCaptor.getValue().getType()).isEqualTo(TransactionType.CREDIT);
            assertThat(txnCaptor.getValue().getAmount()).isEqualByComparingTo(new BigDecimal("300.00"));
        }
    }

    @Nested
    @DisplayName("getWalletBalance()")
    class GetWalletBalance {

        @Test
        @DisplayName("should return zero balance for new user (no wallet)")
        void shouldReturnZeroForNewUser() {
            when(walletRepository.findByUserId(99L)).thenReturn(Optional.empty());

            var response = paymentService.getWalletBalance(99L);

            assertThat(response.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }
}
