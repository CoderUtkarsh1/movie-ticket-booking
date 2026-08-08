package com.moviebooking.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEvent implements Serializable {

    private Long paymentId;
    private Long bookingId;
    private String bookingCode;
    private Long userId;
    private String userEmail;
    private BigDecimal amount;
    private String paymentMethod;  // UPI, CARD, WALLET
    private String transactionId;
    private String status;  // PENDING, SUCCESS, FAILED, REFUNDED
    private LocalDateTime timestamp;
}
