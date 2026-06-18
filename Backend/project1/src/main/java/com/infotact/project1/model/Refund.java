package com.infotact.project1.model;

import com.infotact.project1.enums.RefundStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/*
 * Represents money returned to a customer.
 * Created only when an approved refund is processed.
 * Linked to the original payment transaction.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "refunds")
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long refundId;

    // Original payment against which refund is issued
    @OneToOne
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    // BigDecimal avoids floating-point precision issues in monetary calculations
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal refundAmount;

    // Example: CUSTOMER_CANCELLATION, HOTEL_CANCELLATION
    @Column(nullable = false)
    private String refundReason;

    // Tracks the refund processing lifecycle
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RefundStatus refundStatus;

    // Reference returned by payment gateway after refund processing
    private String gatewayRefundId;

    // Admin or system that initiated the refund
    private String processedBy;

    // Automatically managed audit timestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Timestamp when refund was successfully processed
    private LocalDateTime processedAt;

    // Populate timestamp when refund record is created
    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
