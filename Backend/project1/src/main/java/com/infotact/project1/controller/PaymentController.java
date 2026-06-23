package com.infotact.project1.controller;

import com.infotact.project1.dto.request.PaymentRequestDTO;
import com.infotact.project1.dto.response.PaymentResponseDTO;
import com.infotact.project1.enums.PaymentStatus;
import com.infotact.project1.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payment")

// Lombok generates constructor for final fields
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // Create a new payment
    @PostMapping("/save")
    public PaymentResponseDTO createPayment(
            @RequestBody PaymentRequestDTO requestDTO) {

        return paymentService.createPayment(requestDTO);
    }

    // Retrieve all payments
    @GetMapping("/getall")
    public List<PaymentResponseDTO> getAllPayments() {

        return paymentService.getAllPayments();
    }

    // Retrieve payment by id
    @GetMapping("/get/{paymentId}")
    public PaymentResponseDTO getPaymentById(
            @PathVariable Long paymentId) {

        return paymentService.getPaymentById(paymentId);
    }

    // Retrieve payment by reservation
    @GetMapping("/reservation/{reservationId}")
    public PaymentResponseDTO getPaymentByReservation(
            @PathVariable Long reservationId) {

        return paymentService.getPaymentByReservation(
                reservationId);
    }

    // Retrieve payments by status
    @GetMapping("/status")
    public List<PaymentResponseDTO> getPaymentsByStatus(
            @RequestParam PaymentStatus paymentStatus) {

        return paymentService.getPaymentsByStatus(
                paymentStatus);
    }

    // Delete payment
    @DeleteMapping("/delete/{paymentId}")
    public String deletePayment(
            @PathVariable Long paymentId) {

        paymentService.deletePayment(paymentId);

        return "Payment deleted successfully";
    }

    // PENDING -> PROCESSING
    @PatchMapping("/start/{paymentId}")
    public PaymentResponseDTO startPayment(
            @PathVariable Long paymentId) {

        return paymentService.startPayment(paymentId);
    }

    // PROCESSING -> SUCCESS
    @PatchMapping("/success/{paymentId}")
    public PaymentResponseDTO markPaymentSuccess(
            @PathVariable Long paymentId,
            @RequestParam String gatewayPaymentId,
            @RequestParam String gatewaySignature) {

        return paymentService.markPaymentSuccess(
                paymentId,
                gatewayPaymentId,
                gatewaySignature);
    }

    // PROCESSING -> FAILED
    @PatchMapping("/fail/{paymentId}")
    public PaymentResponseDTO markPaymentFailed(
            @PathVariable Long paymentId) {

        return paymentService.markPaymentFailed(
                paymentId);
    }

    // SUCCESS -> REFUNDED
    @PatchMapping("/refund/{paymentId}")
    public PaymentResponseDTO refundPayment(
            @PathVariable Long paymentId) {

        return paymentService.refundPayment(
                paymentId);
    }
}