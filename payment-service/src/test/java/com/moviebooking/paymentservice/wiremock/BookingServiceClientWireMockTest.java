package com.moviebooking.paymentservice.wiremock;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * WireMock Tests — Simulates booking-service HTTP responses at the wire level.
 * Tests how payment-service handles various booking-service response scenarios.
 */
@WireMockTest
@DisplayName("BookingServiceClient WireMock Tests")
class BookingServiceClientWireMockTest {

    @Test
    @DisplayName("should handle successful booking fetch")
    void shouldHandleSuccessfulBookingFetch(WireMockRuntimeInfo wmInfo) throws Exception {
        String bookingJson = """
                {
                  "success": true,
                  "data": {
                    "id": 1,
                    "bookingCode": "MBK-20260720-AB12",
                    "userId": 1,
                    "status": "PENDING",
                    "totalAmount": 500.00,
                    "seatIds": [101, 102]
                  }
                }
                """;

        stubFor(get(urlPathMatching("/api/bookings/internal/\\d+"))
                .willReturn(okJson(bookingJson)));

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(wmInfo.getHttpBaseUrl() + "/api/bookings/internal/1"))
                .header("X-Internal-Api-Key", "test-key")
                .GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("MBK-20260720-AB12");
        assertThat(response.body()).contains("PENDING");
    }

    @Test
    @DisplayName("should handle booking not found (404)")
    void shouldHandleBookingNotFound(WireMockRuntimeInfo wmInfo) throws Exception {
        stubFor(get(urlPathMatching("/api/bookings/internal/\\d+"))
                .willReturn(notFound().withBody("{\"success\":false,\"message\":\"Booking not found\"}")));

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(wmInfo.getHttpBaseUrl() + "/api/bookings/internal/999"))
                .header("X-Internal-Api-Key", "test-key")
                .GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(404);
    }

    @Test
    @DisplayName("should handle confirm-payment success")
    void shouldHandleConfirmPayment(WireMockRuntimeInfo wmInfo) throws Exception {
        stubFor(put(urlPathMatching("/api/bookings/internal/\\d+/confirm-payment"))
                .willReturn(okJson("{\"success\":true}")));

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(wmInfo.getHttpBaseUrl() + "/api/bookings/internal/1/confirm-payment"))
                .header("X-Internal-Api-Key", "test-key")
                .PUT(HttpRequest.BodyPublishers.noBody()).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
        verify(putRequestedFor(urlPathMatching("/api/bookings/internal/\\d+/confirm-payment")));
    }

    @Test
    @DisplayName("should handle booking-service down (connection refused)")
    void shouldHandleBookingServiceTimeout(WireMockRuntimeInfo wmInfo) throws Exception {
        stubFor(get(urlPathMatching("/api/bookings/internal/\\d+"))
                .willReturn(ok().withFixedDelay(6000)));

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(wmInfo.getHttpBaseUrl() + "/api/bookings/internal/1"))
                .timeout(java.time.Duration.ofSeconds(3))
                .GET().build();

        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () ->
                client.send(request, HttpResponse.BodyHandlers.ofString()));
    }
}
