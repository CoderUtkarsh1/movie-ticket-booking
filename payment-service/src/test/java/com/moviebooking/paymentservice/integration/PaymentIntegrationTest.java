package com.moviebooking.paymentservice.integration;

import com.moviebooking.paymentservice.entity.Wallet;
import com.moviebooking.paymentservice.repository.PaymentRepository;
import com.moviebooking.paymentservice.repository.WalletRepository;
import com.moviebooking.paymentservice.repository.WalletTransactionRepository;
import com.moviebooking.paymentservice.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration Test — Full context with REAL SQL Server via Testcontainers.
 * Tests wallet operations end-to-end.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@DisplayName("Payment Integration Tests (Testcontainers)")
class PaymentIntegrationTest {

    @Container
    @ServiceConnection
    static MSSQLServerContainer<?> sqlServer = new MSSQLServerContainer<>(
            "mcr.microsoft.com/mssql/server:2022-latest")
            .acceptLicense()
            .withPassword("TestP@ssw0rd123");

    @Autowired private PaymentService paymentService;
    @Autowired private WalletRepository walletRepository;
    @Autowired private WalletTransactionRepository walletTransactionRepository;
    @Autowired private PaymentRepository paymentRepository;

    @BeforeEach
    void setUp() {
        walletTransactionRepository.deleteAll();
        paymentRepository.deleteAll();
        walletRepository.deleteAll();
    }

    @Test
    @DisplayName("should add money to wallet and persist in real DB")
    void shouldAddMoneyToWallet() {
        var response = paymentService.addMoney(1L, new BigDecimal("1000.00"));

        assertThat(response.getBalance()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(response.getUserId()).isEqualTo(1L);

        // Verify persisted
        Wallet wallet = walletRepository.findByUserId(1L).orElseThrow();
        assertThat(wallet.getBalance()).isEqualByComparingTo(new BigDecimal("1000.00"));
    }

    @Test
    @DisplayName("should add money multiple times and accumulate balance")
    void shouldAccumulateBalance() {
        paymentService.addMoney(1L, new BigDecimal("500.00"));
        paymentService.addMoney(1L, new BigDecimal("300.00"));

        var response = paymentService.getWalletBalance(1L);
        assertThat(response.getBalance()).isEqualByComparingTo(new BigDecimal("800.00"));
    }

    @Test
    @DisplayName("should return zero balance for new user")
    void shouldReturnZeroForNewUser() {
        var response = paymentService.getWalletBalance(999L);
        assertThat(response.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("should log wallet transaction on addMoney")
    void shouldLogWalletTransaction() {
        paymentService.addMoney(1L, new BigDecimal("1000.00"));

        Wallet wallet = walletRepository.findByUserId(1L).orElseThrow();
        var transactions = walletTransactionRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId());

        assertThat(transactions).hasSize(1);
        assertThat(transactions.get(0).getAmount()).isEqualByComparingTo(new BigDecimal("1000.00"));
    }
}
