package com.moviebooking.paymentservice.api;

import com.moviebooking.paymentservice.repository.PaymentRepository;
import com.moviebooking.paymentservice.repository.WalletRepository;
import com.moviebooking.paymentservice.repository.WalletTransactionRepository;
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
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

/**
 * REST Assured API Tests for Payment Service.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@DisplayName("Payment API Tests (REST Assured)")
class PaymentApiTest {

    @Container
    @ServiceConnection
    static MSSQLServerContainer<?> sqlServer = new MSSQLServerContainer<>(
            "mcr.microsoft.com/mssql/server:2022-latest")
            .acceptLicense()
            .withPassword("TestP@ssw0rd123");

    @LocalServerPort
    private int port;

    @Autowired private WalletTransactionRepository walletTransactionRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private WalletRepository walletRepository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        walletTransactionRepository.deleteAll();
        paymentRepository.deleteAll();
        walletRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/payments/wallet/add — should add money to wallet")
    void shouldAddMoneyToWallet() {
        given()
            .contentType(ContentType.JSON)
            .header("X-User-Id", "1")
            .body("{\"amount\": 1000.00}")
        .when()
            .post("/api/payments/wallet/add")
        .then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("data.balance", equalTo(1000.0f))
            .body("data.userId", equalTo(1));
    }

    @Test
    @DisplayName("GET /api/payments/wallet/balance — should return wallet balance")
    void shouldGetWalletBalance() {
        // Add money first
        given().contentType(ContentType.JSON).header("X-User-Id", "1")
                .body("{\"amount\": 500.00}").post("/api/payments/wallet/add");

        given()
            .contentType(ContentType.JSON)
            .header("X-User-Id", "1")
        .when()
            .get("/api/payments/wallet/balance")
        .then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("data.balance", equalTo(500.0f));
    }

    @Test
    @DisplayName("GET /api/payments/wallet/balance — should return zero for new user")
    void shouldReturnZeroForNewUser() {
        given()
            .contentType(ContentType.JSON)
            .header("X-User-Id", "999")
        .when()
            .get("/api/payments/wallet/balance")
        .then()
            .statusCode(200)
            .body("data.balance", equalTo(0));
    }
}
