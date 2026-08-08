package com.moviebooking.paymentservice.controller;

import com.moviebooking.common.constants.AppConstants;
import com.moviebooking.common.dto.ApiResponse;
import com.moviebooking.paymentservice.dto.PaymentRequest;
import com.moviebooking.paymentservice.entity.Payment;
import com.moviebooking.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/process")
    public ResponseEntity<ApiResponse<Payment>> processPayment(
            @RequestHeader(AppConstants.HEADER_USER_ID) Long userId,
            @Valid @RequestBody PaymentRequest request) {
        Payment payment = paymentService.processPayment(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Payment processed", payment));
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<ApiResponse<Payment>> getPaymentByBooking(@PathVariable Long bookingId) {
        Payment payment = paymentService.getPaymentByBookingId(bookingId);
        return ResponseEntity.ok(ApiResponse.success(payment));
    }

    @GetMapping("/user")
    public ResponseEntity<ApiResponse<List<Payment>>> getUserPayments(
            @RequestHeader(AppConstants.HEADER_USER_ID) Long userId) {
        List<Payment> payments = paymentService.getUserPayments(userId);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }
}
