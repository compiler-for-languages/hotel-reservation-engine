package com.infotact.project1.service;

import com.infotact.project1.dto.request.PaymentRequestDTO;
import com.infotact.project1.dto.response.PaymentResponseDTO;
import com.infotact.project1.enums.PaymentStatus;
import com.infotact.project1.enums.ReservationStatus;
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

    // Dependency remains immutable after injection
    private final BookingHoldService bookingHoldService;

    // Create payment
    public PaymentResponseDTO createPayment( PaymentRequestDTO requestDTO) {


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


    // Retrieve all payments
    public List<PaymentResponseDTO> getAllPayments() {

        // Stream API for DTO conversion
        return paymentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Retrieve payment by id
    public PaymentResponseDTO getPaymentById(
            Long paymentId) {


        Payment payment = paymentRepository.findById(paymentId)

                // Prevents access to non-existent records
                .orElseThrow(() ->
                        new RuntimeException(
                                "Payment not found with id: "
                                        + paymentId));

        return mapToResponse(payment);
    }

    // Retrieve payment by reservation
    public PaymentResponseDTO getPaymentByReservation(
            Long reservationId) {

        Reservation reservation =
                reservationRepository.findById(
                                reservationId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Reservation not found with id: "
                                                + reservationId));

        Payment payment =
                paymentRepository.findByReservation(
                                reservation)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Payment not found for reservation id: "
                                                + reservationId));

        return mapToResponse(payment);
    }

    // Retrieve payments by status
    public List<PaymentResponseDTO> getPaymentsByStatus(
            PaymentStatus paymentStatus) {

        // Stream API for DTO conversion
        return paymentRepository.findByPaymentStatus(
                        paymentStatus)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Delete payment
    public void deletePayment(
            Long paymentId) {
        Payment payment =
                paymentRepository.findById(paymentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Payment not found with id: "
                                                + paymentId));

        // Prevent deletion of completed payments
        if (payment.getPaymentStatus()
                == PaymentStatus.SUCCESS) {

            throw new RuntimeException(
                    "Successful payments cannot be deleted");
        }

        paymentRepository.delete(payment);
    }


    // PENDING -> PROCESSING
    public PaymentResponseDTO startPayment(
            Long paymentId) {

        Payment payment =
                paymentRepository.findById(paymentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Payment not found with id: "
                                                + paymentId));

        if (payment.getPaymentStatus()
                != PaymentStatus.PENDING) {

            throw new RuntimeException(
                    "Only PENDING payments can be moved to PROCESSING");
        }

        payment.setPaymentStatus(
                PaymentStatus.PROCESSING);

        Payment updatedPayment =
                paymentRepository.save(payment);

        // TODO:
        // Create actual payment order using Razorpay

        // TODO:
        // Store gateway response

        return mapToResponse(updatedPayment);
    }

    // PROCESSING -> SUCCESS
    public PaymentResponseDTO markPaymentSuccess(
            Long paymentId,
            String gatewayPaymentId,
            String gatewaySignature) {

        Payment payment =
                paymentRepository.findById(paymentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Payment not found with id: "
                                                + paymentId));

        if (payment.getPaymentStatus()
                != PaymentStatus.PROCESSING) {

            throw new RuntimeException(
                    "Only PROCESSING payments can be marked as SUCCESS");
        }

        payment.setPaymentStatus(
                PaymentStatus.SUCCESS);

        payment.setGatewayPaymentId(
                gatewayPaymentId);

        payment.setGatewaySignature(
                gatewaySignature);

        payment.setPaidAt(
                LocalDateTime.now());

        // Confirm reservation after successful payment
        Reservation reservation =
                payment.getReservation();

        reservation.setReservationStatus(
                ReservationStatus.CONFIRMED);

        reservationRepository.save(
                reservation);

        // Release temporary booking hold
        bookingHoldService.releaseActiveHold(
                reservation.getReservationId());

        Payment updatedPayment =
                paymentRepository.save(payment);


        return mapToResponse(updatedPayment);
    }

    // PROCESSING -> FAILED
    public PaymentResponseDTO markPaymentFailed(
            Long paymentId) {

        Payment payment =
                paymentRepository.findById(paymentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Payment not found with id: "
                                                + paymentId));

        if (payment.getPaymentStatus()
                != PaymentStatus.PROCESSING) {

            throw new RuntimeException(
                    "Only PROCESSING payments can be marked as FAILED");
        }

        payment.setPaymentStatus(
                PaymentStatus.FAILED);

        // Retrieve associated reservation
        Reservation reservation =
                payment.getReservation();

         // Release booking hold to free Redis inventory
        bookingHoldService.releaseActiveHold(
                reservation.getReservationId());

        Payment updatedPayment =
                paymentRepository.save(payment);


        return mapToResponse(updatedPayment);
    }

    // SUCCESS -> REFUNDED
    public PaymentResponseDTO refundPayment(
            Long paymentId) {

        Payment payment =
                paymentRepository.findById(paymentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Payment not found with id: "
                                                + paymentId));

        if (payment.getPaymentStatus()
                != PaymentStatus.SUCCESS) {

            throw new RuntimeException(
                    "Only SUCCESS payments can be refunded");
        }

        payment.setPaymentStatus(
                PaymentStatus.REFUNDED);

        Payment updatedPayment =
                paymentRepository.save(payment);

        // TODO:
        // Integrate payment gateway refund API

        // TODO:
        // Create refund record

        // TODO:
        // Update reservation status

        // TODO:
        // Send refund notification

        return mapToResponse(updatedPayment);
    }

    // Calculate payment amount from reservation
    private BigDecimal calculateAmount(
            Reservation reservation) {

        long totalNights =
                ChronoUnit.DAYS.between(
                        reservation.getCheckInDate(),
                        reservation.getCheckOutDate());

        BigDecimal pricePerNight =
                reservation.getRoomType()
                        .getPricePerNight();

        // TODO:
        // Add taxes

        // TODO:
        // Apply coupon discounts

        // TODO:
        // Apply seasonal pricing

        return pricePerNight.multiply(
                BigDecimal.valueOf(totalNights));
    }

    // Entity -> DTO mapper
    private PaymentResponseDTO mapToResponse(
            Payment payment) {

        return PaymentResponseDTO.builder()
                .paymentId(payment.getPaymentId())
                .reservationId(
                        payment.getReservation()
                                .getReservationId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .paymentMethod(
                        payment.getPaymentMethod())
                .gatewayOrderId(
                        payment.getGatewayOrderId())
                .gatewayPaymentId(
                        payment.getGatewayPaymentId())
                .paymentStatus(
                        payment.getPaymentStatus())
                .paidAt(payment.getPaidAt())
                .build();
    }


}

