package com.infotact.project1.service;

import com.infotact.project1.dto.request.PaymentRequestDTO;
import com.infotact.project1.dto.response.PaymentResponseDTO;
import com.infotact.project1.enums.PaymentStatus;
import com.infotact.project1.enums.ReservationStatus;
import com.infotact.project1.exception.BusinessExceptions;
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
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final BookingHoldService bookingHoldService;

    public PaymentResponseDTO createPayment(PaymentRequestDTO requestDTO) {

        Reservation reservation = reservationRepository.findById(requestDTO.getReservationId())
                .orElseThrow(() -> BusinessExceptions.reservationNotFound(requestDTO.getReservationId()));

        if (reservation.getReservationStatus() == ReservationStatus.CHECKED_OUT) {
            throw BusinessExceptions.reservationAlreadyCheckedOut();
        }

        if (paymentRepository.findByReservation(Optional.of(reservation)).isPresent()) {
            throw BusinessExceptions.paymentAlreadyExists(reservation.getReservationId());
        }

        BigDecimal amount = calculateAmount(reservation);

        Payment payment = new Payment();
        payment.setReservation(reservation);
        payment.setAmount(amount);
        payment.setCurrency("INR");
        payment.setPaymentMethod(requestDTO.getPaymentMethod());
        payment.setPaymentGateway("RAZORPAY");
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setGatewayOrderId(UUID.randomUUID().toString());

        Payment savedPayment = paymentRepository.save(payment);

        return mapToResponse(savedPayment);
    }

    public List<PaymentResponseDTO> getAllPayments() {
        return paymentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public PaymentResponseDTO getPaymentById(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> BusinessExceptions.paymentNotFound(paymentId));
        return mapToResponse(payment);
    }

    public PaymentResponseDTO getPaymentByReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> BusinessExceptions.reservationNotFound(reservationId));

        Payment payment = paymentRepository.findByReservation(Optional.of(reservation))
                .orElseThrow(() -> BusinessExceptions.paymentNotFoundForReservation(reservationId));

        return mapToResponse(payment);
    }

    public List<PaymentResponseDTO> getPaymentsByStatus(PaymentStatus paymentStatus) {
        return paymentRepository.findByPaymentStatus(paymentStatus)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public void deletePayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> BusinessExceptions.paymentNotFound(paymentId));

        if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
            throw BusinessExceptions.paymentDeleteNotAllowed();
        }

        paymentRepository.delete(payment);
    }

    public PaymentResponseDTO startPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> BusinessExceptions.paymentNotFound(paymentId));

        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw BusinessExceptions.paymentStartInvalid();
        }

        payment.setPaymentStatus(PaymentStatus.PROCESSING);

        Payment updatedPayment = paymentRepository.save(payment);

        return mapToResponse(updatedPayment);
    }

    public PaymentResponseDTO markPaymentSuccess(
            Long paymentId,
            String gatewayPaymentId,
            String gatewaySignature) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> BusinessExceptions.paymentNotFound(paymentId));

        if (payment.getPaymentStatus() != PaymentStatus.PROCESSING) {
            throw BusinessExceptions.paymentSuccessInvalid();
        }

        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setGatewayPaymentId(gatewayPaymentId);
        payment.setGatewaySignature(gatewaySignature);
        payment.setPaidAt(LocalDateTime.now());

        Reservation reservation = payment.getReservation();
        reservation.setReservationStatus(ReservationStatus.CONFIRMED);
        reservationRepository.save(reservation);

        bookingHoldService.releaseActiveHold(reservation.getReservationId());

        Payment updatedPayment = paymentRepository.save(payment);

        return mapToResponse(updatedPayment);
    }

    public PaymentResponseDTO markPaymentFailed(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> BusinessExceptions.paymentNotFound(paymentId));

        if (payment.getPaymentStatus() != PaymentStatus.PROCESSING) {
            throw BusinessExceptions.paymentFailedInvalid();
        }

        payment.setPaymentStatus(PaymentStatus.FAILED);

        Reservation reservation = payment.getReservation();
        bookingHoldService.releaseActiveHold(reservation.getReservationId());

        Payment updatedPayment = paymentRepository.save(payment);

        return mapToResponse(updatedPayment);
    }

    public PaymentResponseDTO refundPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> BusinessExceptions.paymentNotFound(paymentId));

        if (payment.getPaymentStatus() != PaymentStatus.SUCCESS) {
            throw BusinessExceptions.paymentRefundInvalid();
        }

        payment.setPaymentStatus(PaymentStatus.REFUNDED);

        Reservation reservation = payment.getReservation();
        reservation.setReservationStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);

        Payment updatedPayment = paymentRepository.save(payment);

        return mapToResponse(updatedPayment);
    }

    private BigDecimal calculateAmount(Reservation reservation) {
        long totalNights = ChronoUnit.DAYS.between(
                reservation.getCheckInDate(),
                reservation.getCheckOutDate());

        BigDecimal pricePerNight = reservation.getRoomType().getPricePerNight();

        return pricePerNight.multiply(BigDecimal.valueOf(totalNights));
    }

    private PaymentResponseDTO mapToResponse(Payment payment) {
        return PaymentResponseDTO.builder()
                .paymentId(payment.getPaymentId())
                .reservationId(payment.getReservation().getReservationId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .paymentMethod(payment.getPaymentMethod())
                .gatewayOrderId(payment.getGatewayOrderId())
                .gatewayPaymentId(payment.getGatewayPaymentId())
                .gatewaySignature(payment.getGatewaySignature())
                .paymentStatus(payment.getPaymentStatus())
                .paidAt(payment.getPaidAt())
                .build();
    }
}
