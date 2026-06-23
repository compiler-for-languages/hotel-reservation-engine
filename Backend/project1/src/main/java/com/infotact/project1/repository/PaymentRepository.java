package com.infotact.project1.repository;

import com.infotact.project1.enums.PaymentStatus;
import com.infotact.project1.model.Payment;
import com.infotact.project1.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository

// JpaRepository provides built-in CRUD operations
public interface PaymentRepository
        extends JpaRepository<Payment, Long> {

    // Retrieve payment by reservation
    Optional<Payment> findByReservation(
            Reservation reservation);

    // Retrieve payments by status
    List<Payment> findByPaymentStatus(
            PaymentStatus paymentStatus);
}