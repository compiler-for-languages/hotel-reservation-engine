package com.infotact.project1.service;

import com.infotact.project1.dto.request.PaymentRequestDTO;
import com.infotact.project1.dto.response.PaymentResponseDTO;
import com.infotact.project1.enums.PaymentStatus;
import com.infotact.project1.model.Payment;
import com.infotact.project1.model.Reservation;
import com.infotact.project1.repository.PaymentRepository;
import com.infotact.project1.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service

// Lombok generates constructor for final fields
@RequiredArgsConstructor
public class PaymentService {

    // Dependency remains immutable after injection
    private final PaymentRepository paymentRepository;

    // Dependency remains immutable after injection
    private final ReservationRepository reservationRepository;

    // Create payment
    public PaymentResponseDTO createPayment(
            PaymentRequestDTO requestDTO) {

        public PaymentResponseDTO createPayment(
                PaymentRequestDTO requestDTO) {

            Reservation reservation =
                    reservationRepository.findById(
                                    requestDTO.getReservationId())
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Reservation not found with id: "
                                                    + requestDTO.getReservationId()));

            // Prevent duplicate payment creation
            if (paymentRepository.findByReservation(
                    reservation).isPresent()) {

                throw new RuntimeException(
                        "Payment already exists for reservation: "
                                + reservation.getReservationId());
            }

            BigDecimal amount =
                    calculateAmount(reservation);

            Payment payment = new Payment();

            payment.setReservation(reservation);

            payment.setAmount(amount);

            payment.setCurrency("INR");

            payment.setPaymentMethod(
                    requestDTO.getPaymentMethod());

            // Currently hardcoded
            // TODO:
            // Replace with actual payment gateway integration
            payment.setPaymentGateway("RAZORPAY");

            // Payment always starts in PENDING state
            payment.setPaymentStatus(
                    PaymentStatus.PENDING);

            // Temporary order id
            // TODO:
            // Replace with gateway-generated order id
            payment.setGatewayOrderId(
                    UUID.randomUUID().toString());

            Payment savedPayment =
                    paymentRepository.save(payment);

            return mapToResponse(savedPayment);
        }
    }

    // Retrieve all payments
    public List<PaymentResponseDTO> getAllPayments() {

        return null;
    }

    // Retrieve payment by id
    public PaymentResponseDTO getPaymentById(
            Long paymentId) {

        return null;
    }

    // Retrieve payment by reservation
    public PaymentResponseDTO getPaymentByReservation(
            Long reservationId) {

        return null;
    }

    // Retrieve payments by status
    public List<PaymentResponseDTO> getPaymentsByStatus(
            PaymentStatus paymentStatus) {

        return null;
    }

    // Delete payment
    public void deletePayment(
            Long paymentId) {

    }

    // State Machine Methods

    // PENDING -> PROCESSING
    public PaymentResponseDTO startPayment(
            Long paymentId) {

        return null;
    }

    // PROCESSING -> SUCCESS
    public PaymentResponseDTO markPaymentSuccess(
            Long paymentId,
            String gatewayPaymentId,
            String gatewaySignature) {

        return null;
    }

    // PROCESSING -> FAILED
    public PaymentResponseDTO markPaymentFailed(
            Long paymentId) {

        return null;
    }

    // SUCCESS -> REFUNDED
    public PaymentResponseDTO refundPayment(
            Long paymentId) {

        return null;
    }

    // Helper Methods

    // Calculate payment amount from reservation
    private BigDecimal calculateAmount(
            Reservation reservation) {

        return null;
    }

    // Entity -> DTO mapper
    private PaymentResponseDTO mapToResponse(
            Payment payment) {

        return null;
    }
}