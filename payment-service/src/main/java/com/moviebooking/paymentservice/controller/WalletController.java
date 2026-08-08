package com.moviebooking.paymentservice.controller;

import com.moviebooking.common.constants.AppConstants;
import com.moviebooking.common.dto.ApiResponse;
import com.moviebooking.paymentservice.dto.WalletAddRequest;
import com.moviebooking.paymentservice.dto.WalletResponse;
import com.moviebooking.paymentservice.entity.WalletTransaction;
import com.moviebooking.paymentservice.service.impl.PaymentServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final PaymentServiceImpl paymentService;

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<WalletResponse>> addMoney(
            @RequestHeader(AppConstants.HEADER_USER_ID) Long userId,
            @Valid @RequestBody WalletAddRequest request) {
        WalletResponse response = paymentService.addMoney(userId, request.getAmount());
        return ResponseEntity.ok(ApiResponse.success("Money added successfully", response));
    }

    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<WalletResponse>> getBalance(
            @RequestHeader(AppConstants.HEADER_USER_ID) Long userId) {
        WalletResponse response = paymentService.getWalletBalance(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<List<WalletTransaction>>> getTransactions(
            @RequestHeader(AppConstants.HEADER_USER_ID) Long userId) {
        List<WalletTransaction> transactions = paymentService.getWalletTransactions(userId);
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }
}
