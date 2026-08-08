package com.moviebooking.paymentservice.service.impl;

import com.moviebooking.common.dto.ApiResponse;
import com.moviebooking.common.dto.BookingDto;
import com.moviebooking.common.enums.PaymentMethod;
import com.moviebooking.common.enums.PaymentStatus;
import com.moviebooking.common.enums.TransactionType;
import com.moviebooking.common.exception.BadRequestException;
import com.moviebooking.common.exception.ResourceNotFoundException;
import com.moviebooking.paymentservice.dto.PaymentRequest;
import com.moviebooking.paymentservice.dto.WalletResponse;
import com.moviebooking.paymentservice.entity.Payment;
import com.moviebooking.paymentservice.entity.Wallet;
import com.moviebooking.paymentservice.entity.WalletTransaction;
import com.moviebooking.paymentservice.feign.BookingServiceClient;
import com.moviebooking.paymentservice.repository.PaymentRepository;
import com.moviebooking.paymentservice.repository.WalletRepository;
import com.moviebooking.paymentservice.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements com.moviebooking.paymentservice.service.PaymentService {

    private final PaymentRepository paymentRepository;
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final BookingServiceClient bookingServiceClient;

    @Value("${internal.api-key:}")
    private String internalApiKey;

    /**
     * Process payment for a booking.
     * 1. Fetch booking details via Feign
     * 2. Create/find Payment record
     * 3. Process payment (WALLET deduction or mock gateway)
     * 4. On success: call booking-service to confirm
     * 5. On failure: call booking-service to fail
     */
    @Override
    @Transactional
    public Payment processPayment(Long userId, PaymentRequest request) {
        // 1. Fetch booking from booking-service via Feign
        ApiResponse<BookingDto> bookingResponse = bookingServiceClient.getBookingById(request.getBookingId(), internalApiKey);
        BookingDto booking = bookingResponse.getData();

        if (booking == null) {
            throw new ResourceNotFoundException("Booking", "id", request.getBookingId());
        }

        // Validate ownership
        if (!booking.getUserId().equals(userId)) {
            throw new BadRequestException("Unauthorized payment attempt");
        }

        // Validate booking is PENDING
        if (!"PENDING".equals(booking.getStatus())) {
            throw new BadRequestException("Booking is not in PENDING state. Current status: " + booking.getStatus());
        }

        // 2. Check idempotency — don't create duplicate payment
        Payment payment = paymentRepository.findByBookingId(request.getBookingId())
                .orElse(null);

        if (payment != null && payment.getStatus() == PaymentStatus.SUCCESS) {
            throw new BadRequestException("Payment already completed for this booking");
        }

        if (payment == null) {
            payment = Payment.builder()
                    .bookingId(booking.getId())
                    .bookingCode(booking.getBookingCode())
                    .userId(userId)
                    .amount(booking.getTotalAmount())
                    .status(PaymentStatus.PENDING)
                    .build();
            payment = paymentRepository.save(payment);
        }

        // 3. Process based on payment method
        PaymentMethod method = PaymentMethod.valueOf(request.getPaymentMethod());
        payment.setPaymentMethod(method);
        String transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        payment.setTransactionId(transactionId);

        boolean paymentSuccess;

        if (method == PaymentMethod.WALLET) {
            paymentSuccess = processWalletPayment(userId, booking.getTotalAmount(), booking.getBookingCode());
        } else {
            // CARD, UPI, NET_BANKING — simulate gateway (always success for demo)
            paymentSuccess = simulatePaymentGateway();
        }

        payment.setCompletedAt(LocalDateTime.now());

        if (paymentSuccess) {
            payment.setStatus(PaymentStatus.SUCCESS);
            paymentRepository.save(payment);

            // 4. Confirm booking via Feign → triggers seat confirm + Kafka notification
            try {
                bookingServiceClient.confirmPayment(request.getBookingId(), internalApiKey);
                log.info("Payment SUCCESS: {} for booking {} — booking confirmed", transactionId, booking.getBookingCode());
            } catch (Exception e) {
                log.error("Payment succeeded but booking confirmation failed for {}: {}",
                        booking.getBookingCode(), e.getMessage());
                // Payment is saved as SUCCESS — booking confirmation can be retried
            }
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);

            // 5. Fail booking via Feign → triggers seat release + Kafka notification
            try {
                bookingServiceClient.failPayment(request.getBookingId(), internalApiKey);
                log.info("Payment FAILED for booking {} — booking failed, seats released", booking.getBookingCode());
            } catch (Exception e) {
                log.error("Payment failed and booking failure callback also failed for {}: {}",
                        booking.getBookingCode(), e.getMessage());
            }
        }

        return payment;
    }

    /**
     * Wallet payment: check balance, deduct, log transaction.
     * Returns true if successful, false if insufficient balance.
     */
    private boolean processWalletPayment(Long userId, BigDecimal amount, String bookingCode) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new BadRequestException("Wallet not found. Please add money first."));

        if (wallet.getBalance().compareTo(amount) < 0) {
            log.warn("Insufficient wallet balance for user {}. Required: {}, Available: {}",
                    userId, amount, wallet.getBalance());
            return false;
        }

        // Deduct balance
        BigDecimal balanceBefore = wallet.getBalance();
        wallet.setBalance(balanceBefore.subtract(amount));
        walletRepository.save(wallet);

        // Log transaction
        WalletTransaction txn = WalletTransaction.builder()
                .walletId(wallet.getId())
                .type(TransactionType.DEBIT)
                .amount(amount)
                .description("Payment for booking " + bookingCode)
                .referenceId(bookingCode)
                .balanceBefore(balanceBefore)
                .balanceAfter(wallet.getBalance())
                .build();
        walletTransactionRepository.save(txn);

        log.info("Wallet debit: ₹{} for booking {} (user {}). Balance: {} → {}",
                amount, bookingCode, userId, balanceBefore, wallet.getBalance());
        return true;
    }

    // ===== WALLET OPERATIONS =====

    @Override
    @Transactional
    public WalletResponse addMoney(Long userId, BigDecimal amount) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElse(Wallet.builder().userId(userId).balance(BigDecimal.ZERO).build());

        BigDecimal balanceBefore = wallet.getBalance();
        wallet.setBalance(balanceBefore.add(amount));
        wallet = walletRepository.save(wallet);

        // Log transaction
        WalletTransaction txn = WalletTransaction.builder()
                .walletId(wallet.getId())
                .type(TransactionType.CREDIT)
                .amount(amount)
                .description("Added money to wallet")
                .referenceId("SELF_ADD")
                .balanceBefore(balanceBefore)
                .balanceAfter(wallet.getBalance())
                .build();
        walletTransactionRepository.save(txn);

        log.info("Wallet credit: ₹{} for user {}. Balance: {} → {}", amount, userId, balanceBefore, wallet.getBalance());

        return WalletResponse.builder()
                .userId(userId)
                .balance(wallet.getBalance())
                .lastUpdated(wallet.getUpdatedAt())
                .build();
    }

    @Override
    public WalletResponse getWalletBalance(Long userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElse(Wallet.builder().userId(userId).balance(BigDecimal.ZERO).build());

        return WalletResponse.builder()
                .userId(userId)
                .balance(wallet.getBalance())
                .lastUpdated(wallet.getUpdatedAt())
                .build();
    }

    @Override
    public List<WalletTransaction> getWalletTransactions(Long userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet", "userId", userId));
        return walletTransactionRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId());
    }

    // ===== EXISTING QUERY METHODS =====

    @Override
    public Payment getPaymentByBookingId(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "bookingId", bookingId));
    }

    @Override
    public List<Payment> getUserPayments(Long userId) {
        return paymentRepository.findByUserId(userId);
    }

    /**
     * Mock payment gateway — always returns true for demo.
     * Replace with actual Razorpay/Stripe integration in production.
     */
    private boolean simulatePaymentGateway() {
        return true;
    }
}
