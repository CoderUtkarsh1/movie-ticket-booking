package com.moviebooking.paymentservice.repository;

import com.moviebooking.common.enums.PaymentMethod;
import com.moviebooking.common.enums.PaymentStatus;
import com.moviebooking.paymentservice.entity.Payment;
import com.moviebooking.paymentservice.entity.Wallet;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Payment Repository Tests — @DataJpaTest + Testcontainers (REAL SQL Server)
 */
@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("PaymentRepository Tests (Testcontainers SQL Server)")
class PaymentRepositoryTest {

    @Container
    @ServiceConnection
    static MSSQLServerContainer<?> sqlServer = new MSSQLServerContainer<>(
            "mcr.microsoft.com/mssql/server:2022-latest")
            .acceptLicense()
            .withPassword("TestP@ssw0rd123");

    @Autowired private PaymentRepository paymentRepository;
    @Autowired private WalletRepository walletRepository;
    @Autowired private TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
        walletRepository.deleteAll();
    }

    private Payment createPayment(Long bookingId, Long userId, String txnId, PaymentStatus status) {
        Payment payment = Payment.builder()
                .bookingId(bookingId)
                .bookingCode("MBK-" + bookingId)
                .userId(userId)
                .amount(new BigDecimal("500.00"))
                .paymentMethod(PaymentMethod.UPI)
                .transactionId(txnId)
                .status(status)
                .build();
        return entityManager.persistAndFlush(payment);
    }

    @Nested
    @DisplayName("PaymentRepository queries")
    class PaymentQueries {

        @Test
        @DisplayName("should find payment by bookingId")
        void shouldFindByBookingId() {
            createPayment(1L, 1L, "TXN-001", PaymentStatus.SUCCESS);

            Optional<Payment> result = paymentRepository.findByBookingId(1L);

            assertThat(result).isPresent();
            assertThat(result.get().getTransactionId()).isEqualTo("TXN-001");
        }

        @Test
        @DisplayName("should return empty for non-existent bookingId")
        void shouldReturnEmptyForBadBookingId() {
            Optional<Payment> result = paymentRepository.findByBookingId(999L);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should find all payments by userId")
        void shouldFindByUserId() {
            createPayment(1L, 1L, "TXN-001", PaymentStatus.SUCCESS);
            createPayment(2L, 1L, "TXN-002", PaymentStatus.FAILED);
            createPayment(3L, 2L, "TXN-003", PaymentStatus.SUCCESS); // different user

            List<Payment> results = paymentRepository.findByUserId(1L);

            assertThat(results).hasSize(2);
            assertThat(results).allSatisfy(p -> assertThat(p.getUserId()).isEqualTo(1L));
        }

        @Test
        @DisplayName("should find payment by unique transactionId")
        void shouldFindByTransactionId() {
            createPayment(1L, 1L, "TXN-UNIQUE-123", PaymentStatus.SUCCESS);

            Optional<Payment> result = paymentRepository.findByTransactionId("TXN-UNIQUE-123");

            assertThat(result).isPresent();
            assertThat(result.get().getBookingId()).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("WalletRepository queries")
    class WalletQueries {

        @Test
        @DisplayName("should find wallet by userId")
        void shouldFindWalletByUserId() {
            Wallet wallet = Wallet.builder()
                    .userId(1L).balance(new BigDecimal("1000.00")).build();
            entityManager.persistAndFlush(wallet);

            Optional<Wallet> result = walletRepository.findByUserId(1L);

            assertThat(result).isPresent();
            assertThat(result.get().getBalance()).isEqualByComparingTo(new BigDecimal("1000.00"));
        }

        @Test
        @DisplayName("should return empty for user without wallet")
        void shouldReturnEmptyForNoWallet() {
            Optional<Wallet> result = walletRepository.findByUserId(999L);
            assertThat(result).isEmpty();
        }
    }
}
