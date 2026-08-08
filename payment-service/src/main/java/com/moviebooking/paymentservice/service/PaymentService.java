package com.moviebooking.paymentservice.service;

import com.moviebooking.paymentservice.dto.PaymentRequest;
import com.moviebooking.paymentservice.dto.WalletResponse;
import com.moviebooking.paymentservice.entity.Payment;
import com.moviebooking.paymentservice.entity.WalletTransaction;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentService {

    Payment processPayment(Long userId, PaymentRequest request);

    WalletResponse addMoney(Long userId, BigDecimal amount);

    WalletResponse getWalletBalance(Long userId);

    List<WalletTransaction> getWalletTransactions(Long userId);

    Payment getPaymentByBookingId(Long bookingId);

    List<Payment> getUserPayments(Long userId);
}
