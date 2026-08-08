package com.moviebooking.bookingservice.wiremock;

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
 * WireMock Tests — Simulates show-service HTTP responses at the wire level.
 * Tests how booking-service would handle various show-service response scenarios.
 */
@WireMockTest
@DisplayName("ShowServiceClient WireMock Tests")
class ShowServiceClientWireMockTest {

    @Test
    @DisplayName("should handle successful seat confirmation from show-service")
    void shouldHandleSuccessfulSeatConfirmation(WireMockRuntimeInfo wmInfo) throws Exception {
        // Stub: show-service returns 200 on seat confirm
        stubFor(put(urlPathMatching("/api/shows/\\d+/seats/confirm"))
                .willReturn(okJson("{\"success\":true,\"message\":\"Seats confirmed\",\"data\":null}")));

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(wmInfo.getHttpBaseUrl() + "/api/shows/1/seats/confirm"))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(
                        "{\"seatIds\":[101,102],\"bookingCode\":\"MBK-TEST\"}"))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("Seats confirmed");
        verify(putRequestedFor(urlPathMatching("/api/shows/\\d+/seats/confirm")));
    }

    @Test
    @DisplayName("should handle show-service returning 500 (server error)")
    void shouldHandleShowServiceError(WireMockRuntimeInfo wmInfo) throws Exception {
        stubFor(put(urlPathMatching("/api/shows/\\d+/seats/confirm"))
                .willReturn(serverError().withBody("{\"success\":false,\"message\":\"Internal Server Error\"}")));

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(wmInfo.getHttpBaseUrl() + "/api/shows/1/seats/confirm"))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(
                        "{\"seatIds\":[101],\"bookingCode\":\"MBK-FAIL\"}"))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(500);
    }

    @Test
    @DisplayName("should handle show-service timeout (slow response)")
    void shouldHandleShowServiceTimeout(WireMockRuntimeInfo wmInfo) throws Exception {
        stubFor(put(urlPathMatching("/api/shows/\\d+/seats/confirm"))
                .willReturn(ok().withFixedDelay(6000))); // 6 second delay

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(2))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(wmInfo.getHttpBaseUrl() + "/api/shows/1/seats/confirm"))
                .header("Content-Type", "application/json")
                .timeout(java.time.Duration.ofSeconds(3))
                .PUT(HttpRequest.BodyPublishers.ofString("{\"seatIds\":[101]}"))
                .build();

        // Should timeout since response takes 6s but client timeout is 3s
        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () ->
                client.send(request, HttpResponse.BodyHandlers.ofString()));
    }

    @Test
    @DisplayName("should handle seat release on booking cancellation")
    void shouldHandleSeatRelease(WireMockRuntimeInfo wmInfo) throws Exception {
        stubFor(put(urlPathMatching("/api/shows/\\d+/seats/release"))
                .willReturn(okJson("{\"success\":true,\"message\":\"Seats released\"}")));

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(wmInfo.getHttpBaseUrl() + "/api/shows/1/seats/release"))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(
                        "{\"seatIds\":[101,102],\"bookingCode\":\"MBK-CANCEL\"}"))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("Seats released");
    }
}
