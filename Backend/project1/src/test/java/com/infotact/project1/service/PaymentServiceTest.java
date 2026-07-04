package com.infotact.project1.service;

import com.infotact.project1.dto.request.PaymentRequestDTO;
import com.infotact.project1.dto.response.PaymentResponseDTO;
import com.infotact.project1.enums.PaymentMethod;
import com.infotact.project1.enums.PaymentStatus;
import com.infotact.project1.enums.ReservationStatus;
import com.infotact.project1.model.Payment;
import com.infotact.project1.model.Reservation;
import com.infotact.project1.model.RoomType;
import com.infotact.project1.repository.PaymentRepository;
import com.infotact.project1.repository.ReservationRepository;
import com.infotact.project1.service.BookingHoldService;
import com.infotact.project1.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/*
 * Unit tests for PaymentService.
 *
 * Covers:
 * - Payment creation
 * - Payment retrieval
 * - Payment state transitions
 * - Refund processing
 *
 * External dependencies are mocked using Mockito.
 */

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private BookingHoldService bookingHoldService;

    @InjectMocks
    private PaymentService paymentService;

    /*
     * Verifies that a payment
     * is created successfully.
     */
    @Test
    void createPayment_ShouldCreatePaymentSuccessfully() {

        PaymentRequestDTO request =
                new PaymentRequestDTO();

        request.setReservationId(1L);
        request.setPaymentMethod(
                PaymentMethod.UPI);

        RoomType roomType = new RoomType();
        roomType.setPricePerNight(
                new BigDecimal("2000"));

        Reservation reservation =
                new Reservation();

        reservation.setReservationId(1L);
        reservation.setRoomType(roomType);

        reservation.setCheckInDate(
                LocalDate.of(2026,7,1));

        reservation.setCheckOutDate(
                LocalDate.of(2026,7,3));

        when(reservationRepository.findById(1L))
                .thenReturn(Optional.of(reservation));

        when(paymentRepository.findByReservation(
                reservation))
                .thenReturn(Optional.empty());

        Payment payment =
                new Payment();

        payment.setPaymentId(1L);
        payment.setReservation(reservation);
        payment.setAmount(
                new BigDecimal("4000"));

        payment.setCurrency("INR");

        payment.setPaymentMethod(
                PaymentMethod.UPI);

        payment.setPaymentStatus(
                PaymentStatus.PENDING);

        payment.setGatewayOrderId(
                "order123");

        when(paymentRepository.save(any(Payment.class)))
                .thenReturn(payment);

        PaymentResponseDTO response =
                paymentService.createPayment(request);

        assertNotNull(response);

        assertEquals(
                1L,
                response.getPaymentId());

        assertEquals(
                new BigDecimal("4000"),
                response.getAmount());

        assertEquals(
                PaymentStatus.PENDING,
                response.getPaymentStatus());

        assertEquals(
                PaymentMethod.UPI,
                response.getPaymentMethod());

        verify(reservationRepository)
                .findById(1L);

        verify(paymentRepository)
                .findByReservation(reservation);

        verify(paymentRepository)
                .save(any(Payment.class));
    }

    /*
     * Verifies that payment
     * creation fails when
     * reservation does not exist.
     */
    @Test
    void createPayment_ShouldThrowException_WhenReservationDoesNotExist() {

        PaymentRequestDTO request =
                new PaymentRequestDTO();

        request.setReservationId(100L);

        when(reservationRepository.findById(100L))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> paymentService.createPayment(request));

        assertEquals(
                "Reservation not found with id: 100",
                exception.getMessage());

        verify(reservationRepository)
                .findById(100L);

        verify(paymentRepository,
                never()).save(any());
    }

    /*
     * Verifies that duplicate
     * payment creation is
     * prevented.
     */
    @Test
    void createPayment_ShouldThrowException_WhenPaymentAlreadyExists() {

        PaymentRequestDTO request =
                new PaymentRequestDTO();

        request.setReservationId(1L);

        Reservation reservation =
                new Reservation();

        reservation.setReservationId(1L);

        when(reservationRepository.findById(1L))
                .thenReturn(Optional.of(reservation));

        when(paymentRepository.findByReservation(
                reservation))
                .thenReturn(Optional.of(new Payment()));

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> paymentService.createPayment(request));

        assertEquals(
                "Payment already exists for reservation: 1",
                exception.getMessage());

        verify(paymentRepository,
                never()).save(any());
    }

    /*
     * Verifies that all payments
     * are returned successfully.
     */
    @Test
    void getAllPayments_ShouldReturnPayments() {

        Reservation reservation = new Reservation();
        reservation.setReservationId(1L);

        Payment payment = new Payment();
        payment.setPaymentId(1L);
        payment.setReservation(reservation);
        payment.setAmount(new BigDecimal("4000"));
        payment.setCurrency("INR");
        payment.setPaymentMethod(PaymentMethod.UPI);
        payment.setPaymentStatus(PaymentStatus.PENDING);

        when(paymentRepository.findAll())
                .thenReturn(List.of(payment));

        List<PaymentResponseDTO> response =
                paymentService.getAllPayments();

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(
                1L,
                response.get(0).getPaymentId());

        verify(paymentRepository).findAll();
    }

    /*
     * Verifies that an existing
     * payment is returned
     * successfully.
     */
    @Test
    void getPaymentById_ShouldReturnPayment() {

        Reservation reservation = new Reservation();
        reservation.setReservationId(1L);

        Payment payment = new Payment();
        payment.setPaymentId(1L);
        payment.setReservation(reservation);
        payment.setAmount(new BigDecimal("4000"));
        payment.setCurrency("INR");
        payment.setPaymentMethod(PaymentMethod.UPI);
        payment.setPaymentStatus(PaymentStatus.PENDING);

        when(paymentRepository.findById(1L))
                .thenReturn(Optional.of(payment));

        PaymentResponseDTO response =
                paymentService.getPaymentById(1L);

        assertNotNull(response);

        assertEquals(
                1L,
                response.getPaymentId());

        assertEquals(
                PaymentStatus.PENDING,
                response.getPaymentStatus());

        verify(paymentRepository).findById(1L);
    }

    /*
     * Verifies that requesting
     * a non-existing payment
     * throws an exception.
     */
    @Test
    void getPaymentById_ShouldThrowException_WhenPaymentDoesNotExist() {

        when(paymentRepository.findById(100L))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> paymentService.getPaymentById(100L));

        assertEquals(
                "Payment not found with id: 100",
                exception.getMessage());

        verify(paymentRepository).findById(100L);
    }

    /*
     * Verifies that payment
     * can be retrieved using
     * reservation id.
     */
    @Test
    void getPaymentByReservation_ShouldReturnPayment() {

        Reservation reservation = new Reservation();
        reservation.setReservationId(1L);

        when(reservationRepository.findById(1L))
                .thenReturn(Optional.of(reservation));

        Payment payment = new Payment();
        payment.setPaymentId(1L);
        payment.setReservation(reservation);
        payment.setAmount(new BigDecimal("4000"));
        payment.setPaymentMethod(PaymentMethod.UPI);
        payment.setPaymentStatus(PaymentStatus.SUCCESS);

        when(paymentRepository.findByReservation(reservation))
                .thenReturn(Optional.of(payment));

        PaymentResponseDTO response =
                paymentService.getPaymentByReservation(1L);

        assertNotNull(response);

        assertEquals(
                1L,
                response.getPaymentId());

        assertEquals(
                PaymentStatus.SUCCESS,
                response.getPaymentStatus());

        verify(reservationRepository).findById(1L);
        verify(paymentRepository).findByReservation(reservation);
    }

    /*
     * Verifies that requesting
     * payment for a non-existing
     * reservation throws an exception.
     */
    @Test
    void getPaymentByReservation_ShouldThrowException_WhenReservationDoesNotExist() {

        when(reservationRepository.findById(100L))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> paymentService.getPaymentByReservation(100L));

        assertEquals(
                "Reservation not found with id: 100",
                exception.getMessage());

        verify(reservationRepository).findById(100L);
    }

    /*
     * Verifies that requesting
     * payment for a reservation
     * without payment throws an exception.
     */
    @Test
    void getPaymentByReservation_ShouldThrowException_WhenPaymentDoesNotExist() {

        Reservation reservation = new Reservation();
        reservation.setReservationId(1L);

        when(reservationRepository.findById(1L))
                .thenReturn(Optional.of(reservation));

        when(paymentRepository.findByReservation(reservation))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> paymentService.getPaymentByReservation(1L));

        assertEquals(
                "Payment not found for reservation: 1",
                exception.getMessage());

        verify(paymentRepository)
                .findByReservation(reservation);
    }

    /*
     * Verifies that payments
     * can be retrieved using
     * payment status.
     */
    @Test
    void getPaymentsByStatus_ShouldReturnPayments() {

        Reservation reservation = new Reservation();
        reservation.setReservationId(1L);

        Payment payment = new Payment();
        payment.setPaymentId(1L);
        payment.setReservation(reservation);
        payment.setPaymentStatus(PaymentStatus.SUCCESS);

        when(paymentRepository.findByPaymentStatus(
                PaymentStatus.SUCCESS))
                .thenReturn(List.of(payment));

        List<PaymentResponseDTO> response =
                paymentService.getPaymentsByStatus(
                        PaymentStatus.SUCCESS);

        assertNotNull(response);

        assertEquals(
                1,
                response.size());

        assertEquals(
                PaymentStatus.SUCCESS,
                response.get(0).getPaymentStatus());

        verify(paymentRepository)
                .findByPaymentStatus(
                        PaymentStatus.SUCCESS);
    }

    /*
     * Verifies that an existing
     * payment is deleted
     * successfully.
     */
    @Test
    void deletePayment_ShouldDeletePayment() {

        Payment payment = new Payment();
        payment.setPaymentId(1L);
        payment.setPaymentStatus(PaymentStatus.PENDING);

        when(paymentRepository.findById(1L))
                .thenReturn(Optional.of(payment));

        paymentService.deletePayment(1L);

        verify(paymentRepository).findById(1L);
        verify(paymentRepository).delete(payment);
    }

    /*
     * Verifies that deleting a
     * non-existing payment
     * throws an exception.
     */
    @Test
    void deletePayment_ShouldThrowException_WhenPaymentDoesNotExist() {

        when(paymentRepository.findById(100L))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> paymentService.deletePayment(100L));

        assertEquals(
                "Payment not found with id: 100",
                exception.getMessage());

        verify(paymentRepository).findById(100L);

        verify(paymentRepository,
                never()).delete(any(Payment.class));
    }

    /*
     * Verifies that a successful
     * payment cannot be deleted.
     */
    @Test
    void deletePayment_ShouldThrowException_WhenPaymentIsSuccessful() {

        Payment payment = new Payment();

        payment.setPaymentId(1L);
        payment.setPaymentStatus(
                PaymentStatus.SUCCESS);

        when(paymentRepository.findById(1L))
                .thenReturn(Optional.of(payment));

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> paymentService.deletePayment(1L));

        assertEquals(
                "Successful payments cannot be deleted.",
                exception.getMessage());

        verify(paymentRepository).findById(1L);

        verify(paymentRepository,
                never()).delete(any(Payment.class));
    }

    /*
     * Verifies that a payment
     * moves from PENDING
     * to PROCESSING.
     */
    @Test
    void startPayment_ShouldMovePendingToProcessing() {

        Reservation reservation =
                new Reservation();

        reservation.setReservationId(1L);

        Payment payment =
                new Payment();

        payment.setPaymentId(1L);
        payment.setReservation(reservation);
        payment.setPaymentStatus(
                PaymentStatus.PENDING);

        when(paymentRepository.findById(1L))
                .thenReturn(Optional.of(payment));

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        PaymentResponseDTO response =
                paymentService.startPayment(1L);

        assertEquals(
                PaymentStatus.PROCESSING,
                response.getPaymentStatus());

        verify(paymentRepository).findById(1L);
        verify(paymentRepository).save(payment);
    }

    /*
     * Verifies that only
     * pending payments
     * can be started.
     */
    @Test
    void startPayment_ShouldThrowException_WhenPaymentIsNotPending() {

        Reservation reservation =
                new Reservation();

        reservation.setReservationId(1L);

        Payment payment =
                new Payment();

        payment.setPaymentId(1L);
        payment.setReservation(reservation);
        payment.setPaymentStatus(
                PaymentStatus.SUCCESS);

        when(paymentRepository.findById(1L))
                .thenReturn(Optional.of(payment));

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> paymentService.startPayment(1L));

        assertEquals(
                "Only pending payments can be started.",
                exception.getMessage());

        verify(paymentRepository).findById(1L);

        verify(paymentRepository,
                never()).save(any(Payment.class));
    }

    /*
     * Verifies that a processing
     * payment is marked as
     * successful.
     */
    @Test
    void markPaymentSuccess_ShouldConfirmReservation() {

        RoomType roomType = new RoomType();

        Reservation reservation =
                new Reservation();

        reservation.setReservationId(1L);
        reservation.setRoomType(roomType);
        reservation.setReservationStatus(
                ReservationStatus.PENDING);

        Payment payment =
                new Payment();

        payment.setPaymentId(1L);
        payment.setReservation(reservation);
        payment.setPaymentStatus(
                PaymentStatus.PROCESSING);

        when(paymentRepository.findById(1L))
                .thenReturn(Optional.of(payment));

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        PaymentResponseDTO response =
                paymentService.markPaymentSuccess(
                        1L,
                        "pay_123456",
                        "signature_xyz");

        assertEquals(
                PaymentStatus.SUCCESS,
                response.getPaymentStatus());

        assertEquals(
                ReservationStatus.CONFIRMED,
                reservation.getReservationStatus());

        assertEquals(
                "pay_123456",
                payment.getGatewayPaymentId());

        assertEquals(
                "signature_xyz",
                payment.getGatewaySignature());

        verify(bookingHoldService)
                .releaseActiveHold(1L);

        verify(paymentRepository).save(payment);

        verify(reservationRepository)
                .save(reservation);
    }

    /*
     * Verifies that a processing
     * payment can be marked
     * as failed.
     */
    @Test
    void markPaymentFailed_ShouldReleaseBookingHold() {

        Reservation reservation =
                new Reservation();

        reservation.setReservationId(1L);

        Payment payment =
                new Payment();

        payment.setPaymentId(1L);
        payment.setReservation(reservation);
        payment.setPaymentStatus(
                PaymentStatus.PROCESSING);

        when(paymentRepository.findById(1L))
                .thenReturn(Optional.of(payment));

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        PaymentResponseDTO response =
                paymentService.markPaymentFailed(1L);

        assertEquals(
                PaymentStatus.FAILED,
                response.getPaymentStatus());

        verify(bookingHoldService)
                .releaseActiveHold(1L);

        verify(paymentRepository).save(payment);
    }

    /*
     * Verifies that a successful
     * payment can be refunded.
     */
    @Test
    void refundPayment_ShouldCancelReservation() {

        Reservation reservation =
                new Reservation();

        reservation.setReservationId(1L);
        reservation.setReservationStatus(
                ReservationStatus.CONFIRMED);

        Payment payment =
                new Payment();

        payment.setPaymentId(1L);
        payment.setReservation(reservation);
        payment.setPaymentStatus(
                PaymentStatus.SUCCESS);

        when(paymentRepository.findById(1L))
                .thenReturn(Optional.of(payment));

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        PaymentResponseDTO response =
                paymentService.refundPayment(1L);

        assertEquals(
                PaymentStatus.REFUNDED,
                response.getPaymentStatus());

        assertEquals(
                ReservationStatus.CANCELLED,
                reservation.getReservationStatus());

        verify(paymentRepository).save(payment);

        verify(reservationRepository)
                .save(reservation);
    }

    /*
     * Verifies that only
     * successful payments
     * can be refunded.
     */
    @Test
    void refundPayment_ShouldThrowException_WhenPaymentIsNotSuccessful() {

        Reservation reservation =
                new Reservation();

        reservation.setReservationId(1L);

        Payment payment =
                new Payment();

        payment.setPaymentId(1L);
        payment.setReservation(reservation);
        payment.setPaymentStatus(
                PaymentStatus.FAILED);

        when(paymentRepository.findById(1L))
                .thenReturn(Optional.of(payment));

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> paymentService.refundPayment(1L));

        assertEquals(
                "Only successful payments can be refunded.",
                exception.getMessage());

        verify(paymentRepository).findById(1L);

        verify(paymentRepository,
                never()).save(any(Payment.class));

        verify(reservationRepository,
                never()).save(any(Reservation.class));
    }

}



