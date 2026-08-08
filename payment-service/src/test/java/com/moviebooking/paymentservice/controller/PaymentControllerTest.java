package com.moviebooking.paymentservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moviebooking.common.enums.PaymentMethod;
import com.moviebooking.common.enums.PaymentStatus;
import com.moviebooking.paymentservice.dto.PaymentRequest;
import com.moviebooking.paymentservice.entity.Payment;
import com.moviebooking.paymentservice.service.PaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * PaymentController @WebMvcTest — HTTP layer tests
 */
@WebMvcTest(PaymentController.class)
@DisplayName("PaymentController API Tests")
class PaymentControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private PaymentService paymentService;
    @Autowired private ObjectMapper objectMapper;

    private Payment samplePayment() {
        return Payment.builder()
                .id(1L).bookingId(1L).bookingCode("MBK-20260720-AB12")
                .userId(1L).amount(new BigDecimal("500.00"))
                .paymentMethod(PaymentMethod.UPI).transactionId("TXN-ABCD1234")
                .status(PaymentStatus.SUCCESS).build();
    }

    @Nested
    @DisplayName("POST /api/payments/process")
    class ProcessPaymentEndpoint {

        @Test
        @DisplayName("should return 200 with successful payment")
        void shouldReturn200() throws Exception {
            when(paymentService.processPayment(eq(1L), any()))
                    .thenReturn(samplePayment());

            mockMvc.perform(post("/api/payments/process")
                            .header("X-User-Id", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    PaymentRequest.builder()
                                            .bookingId(1L).paymentMethod("UPI").build())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.transactionId").value("TXN-ABCD1234"));
        }
    }

    @Nested
    @DisplayName("GET /api/payments/booking/{bookingId}")
    class GetPaymentByBooking {

        @Test
        @DisplayName("should return payment for booking")
        void shouldReturnPayment() throws Exception {
            when(paymentService.getPaymentByBookingId(1L)).thenReturn(samplePayment());

            mockMvc.perform(get("/api/payments/booking/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.bookingCode").value("MBK-20260720-AB12"));
        }
    }

    @Nested
    @DisplayName("GET /api/payments/user")
    class GetUserPayments {

        @Test
        @DisplayName("should return user's payment history")
        void shouldReturnUserPayments() throws Exception {
            when(paymentService.getUserPayments(1L)).thenReturn(List.of(samplePayment()));

            mockMvc.perform(get("/api/payments/user")
                            .header("X-User-Id", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].transactionId").value("TXN-ABCD1234"));
        }
    }
}
