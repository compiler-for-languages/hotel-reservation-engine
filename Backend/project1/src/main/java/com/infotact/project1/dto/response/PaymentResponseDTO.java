package com.infotact.project1.dto.response;

import com.infotact.project1.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResponseDTO {

    private Long paymentId;

    private Long reservationId;

    private BigDecimal amount;

    private String currency;

    private String paymentMethod;

    private PaymentStatus paymentStatus;

    private String gatewayOrderId;

    private String gatewayPaymentId;

    private LocalDateTime paidAt;
}