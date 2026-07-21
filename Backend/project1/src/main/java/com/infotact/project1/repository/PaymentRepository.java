package com.infotact.project1.repository;

import com.infotact.project1.enums.PaymentStatus;
import com.infotact.project1.model.Payment;
import com.infotact.project1.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/*
 * Repository responsible for Payment database operations.
 *
 * Provides CRUD functionality and custom query methods
 * related to payment tracking and payment state management.
 *
 * Each payment is associated with a reservation and stores
 * transaction information such as amount, status and gateway details.
 */

@Repository

/*
 * JpaRepository provides:
 *
 * save()
 * findById()
 * findAll()
 * delete()
 * deleteById()
 * existsById()
 *
 * Entity Type : Payment
 * Primary Key : Long (paymentId)
 */
public interface PaymentRepository
        extends JpaRepository<Payment, Long> {

    /*
     * Retrieves payment associated with a reservation.
     *
     * Business Rule:
     * One reservation is expected to have one primary payment.
     *
     * Example:
     * Reservation #10
     *        ↓
     * Payment #25
     *
     * Spring automatically generates:
     *
     * SELECT *
     * FROM payments
     * WHERE reservation_id = ?;
     */
    Optional<Payment> findByReservation(
            Optional<Reservation> reservation);

    /*
     * Retrieves all payments having a specific status.
     *
     * Examples:
     * PENDING
     * PROCESSING
     * SUCCESS
     * FAILED
     * REFUNDED
     *
     * Useful for:
     * - Payment monitoring
     * - Admin dashboards
     * - Refund processing
     * - Failed payment analysis
     *
     * Spring automatically generates:
     *
     * SELECT *
     * FROM payments
     * WHERE payment_status = ?;
     */
    List<Payment> findByPaymentStatus(
            PaymentStatus paymentStatus);

    Optional<Payment> findByReservationReservationId(Long reservationId);
}