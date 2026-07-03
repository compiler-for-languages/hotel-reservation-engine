package com.infotact.project1.integration;

import com.infotact.project1.enums.PaymentMethod;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/*
 * Integration tests for Payment APIs.
 *
 * Verifies complete payment workflow:
 *
 * HTTP Request
 *      ↓
 * Controller
 *      ↓
 * Service
 *      ↓
 * Repository
 *      ↓
 * H2 Database
 */
class PaymentIntegrationTest
        extends AbstractIntegrationTest {

    /*
     * Verifies that a payment
     * can be created successfully.
     */
    @Test
    void createPayment_ShouldCreatePayment()
            throws Exception {

        String adminToken =
                getAdminToken();

        Long reservationId =
                helper.createReservation(
                        adminToken);

        Long paymentId =
                helper.getPaymentIdByReservation(
                        adminToken,
                        reservationId);

        mockMvc.perform(

                        get("/api/payment/get/{paymentId}", paymentId)
                                .header("Authorization", "Bearer " + adminToken)

                )

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.paymentId")
                        .value(paymentId))

                .andExpect(jsonPath("$.reservationId")
                        .value(reservationId))

                .andExpect(jsonPath("$.paymentMethod")
                        .value("UPI"))

                .andExpect(jsonPath("$.paymentStatus")
                        .value("PENDING"));
    }

    /*
     * Verifies that payment
     * can be retrieved by id.
     */
    @Test
    void getPaymentById_ShouldReturnPayment()
            throws Exception {

        String adminToken =
                getAdminToken();

        Long reservationId =
                helper.createReservation(
                        adminToken);

        Long paymentId =
                helper.getPaymentIdByReservation(
                        adminToken,
                        reservationId);

        mockMvc.perform(

                        get("/api/payment/get/{paymentId}", paymentId)
                                .header("Authorization", "Bearer " + adminToken)

                )

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.paymentId")
                        .value(paymentId))

                .andExpect(jsonPath("$.paymentMethod")
                        .value("UPI"));
    }

    /*
     * Verifies that a payment
     * can be retrieved using
     * its reservation id.
     */
    @Test
    void getPaymentByReservation_ShouldReturnPayment()
            throws Exception {

        String adminToken =
                getAdminToken();

        Long reservationId =
                helper.createReservation(
                        adminToken);

//        helper.createPayment(
//                adminToken,
//                reservationId,
//                PaymentMethod.UPI);

        mockMvc.perform(

                        get("/api/payment/reservation/{reservationId}",
                                reservationId)
                                .header("Authorization", "Bearer " + adminToken)

                )

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.reservationId")
                        .value(reservationId))

                .andExpect(jsonPath("$.paymentMethod")
                        .value("UPI"))

                .andExpect(jsonPath("$.paymentStatus")
                        .value("PENDING"));
    }

    /*
     * Verifies that payments
     * can be retrieved using
     * payment status.
     */
    @Test
    void getPaymentsByStatus_ShouldReturnPayments()
            throws Exception {

        String adminToken =
                getAdminToken();

        Long reservationId =
                helper.createReservation(
                        adminToken);

//        helper.createPayment(
//                adminToken,
//                reservationId,
//                PaymentMethod.CARD);

        mockMvc.perform(

                        get("/api/payment/status")

                                .header("Authorization", "Bearer " + adminToken)

                                .param(
                                        "paymentStatus",
                                        "PENDING"))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$").isArray())

                .andExpect(jsonPath("$[0].paymentStatus")
                        .value("PENDING"));
    }

    /*
     * Verifies that a pending payment
     * moves to PROCESSING state.
     */
    @Test
    void startPayment_ShouldMoveToProcessing()
            throws Exception {

        String adminToken =
                getAdminToken();

        Long reservationId =
                helper.createReservation(
                        adminToken);

        Long paymentId =
                helper.getPaymentIdByReservation(
                        adminToken,
                        reservationId);

                mockMvc.perform(
                                patch("/api/payment/start/{paymentId}", paymentId)
                                        .header("Authorization", "Bearer " + adminToken)
                        )
                        .andExpect(status().isOk())

                .andExpect(jsonPath("$.paymentId")
                        .value(paymentId))

                .andExpect(jsonPath("$.paymentStatus")
                        .value("PROCESSING"));

    }

    /*
     * Verifies that a processing payment
     * can be marked as SUCCESS.
     */
    @Test
    void markPaymentSuccess_ShouldMarkPaymentSuccess()
            throws Exception {

        String adminToken =
                getAdminToken();

        Long reservationId =
                helper.createReservation(
                        adminToken);

        Long paymentId =
                helper.getPaymentIdByReservation(
                        adminToken,
                        reservationId);

        // Move payment to PROCESSING first
        mockMvc.perform(

                        patch("/api/payment/start/{paymentId}",
                                paymentId)

                                .header("Authorization", "Bearer " + adminToken))

                .andExpect(status().isOk());

        mockMvc.perform(

                        patch("/api/payment/success/{paymentId}",
                                paymentId)

                                .header("Authorization", "Bearer " + adminToken)

                                .param(
                                        "gatewayPaymentId",
                                        "pay_test_12345")

                                .param(
                                        "gatewaySignature",
                                        "signature_test"))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.paymentStatus")
                        .value("SUCCESS"))

                .andExpect(jsonPath("$.gatewayPaymentId")
                        .value("pay_test_12345"));
    }

    /*
     * Verifies that a processing payment
     * can be marked as FAILED.
     */
    @Test
    void markPaymentFailed_ShouldMarkPaymentFailed()
            throws Exception {

        String adminToken =
                getAdminToken();

        Long reservationId =
                helper.createReservation(
                        adminToken);

        Long paymentId =
                helper.getPaymentIdByReservation(
                        adminToken,
                        reservationId);

        // Move payment to PROCESSING first
        mockMvc.perform(

                        patch("/api/payment/start/{paymentId}",
                                paymentId)

                                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        mockMvc.perform(

                        patch("/api/payment/fail/{paymentId}",
                                paymentId)

                                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.paymentStatus")
                        .value("FAILED"));
    }

    /*
     * Verifies that a successful payment
     * can be refunded.
     */
    @Test
    void refundPayment_ShouldRefundPayment()
            throws Exception {

        String adminToken =
                getAdminToken();

        Long reservationId =
                helper.createReservation(
                        adminToken);

        Long paymentId =
                helper.getPaymentIdByReservation(
                        adminToken,
                        reservationId);

        // PENDING -> PROCESSING
        mockMvc.perform(

                patch("/api/payment/start/{paymentId}",
                        paymentId)

                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // PROCESSING -> SUCCESS
        mockMvc.perform(

                        patch("/api/payment/success/{paymentId}",
                                paymentId)

                                .header("Authorization", "Bearer " + adminToken)

                                .param(
                                        "gatewayPaymentId",
                                        "pay_refund_test")

                                .param(
                                        "gatewaySignature",
                                        "signature_refund"))

                .andExpect(status().isOk());

        // SUCCESS -> REFUNDED
        mockMvc.perform(

                        patch("/api/payment/refund/{paymentId}",
                                paymentId)

                                .header("Authorization", "Bearer " + adminToken))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.paymentStatus")
                        .value("REFUNDED"));
    }
}



