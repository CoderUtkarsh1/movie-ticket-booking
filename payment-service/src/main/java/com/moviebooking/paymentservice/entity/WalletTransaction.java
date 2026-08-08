package com.moviebooking.paymentservice.entity;

import com.moviebooking.common.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallet_transactions")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private Long walletId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;  // CREDIT or DEBIT

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal amount;

    private String description;   // e.g. "Added money", "Payment for MBK-20260602-AB12"

    private String referenceId;   // bookingCode or "SELF_ADD"

    @Column(precision = 12, scale = 2)
    private BigDecimal balanceBefore;

    @Column(precision = 12, scale = 2)
    private BigDecimal balanceAfter;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
