package com.infotact.project1.integration;

import com.infotact.project1.dto.request.AssignRoomRequestDTO;
import com.infotact.project1.dto.request.GuestRequestDTO;
import com.infotact.project1.enums.Gender;
import com.infotact.project1.enums.PaymentMethod;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/*
 * Integration tests for Reception APIs.
 *
 * Verifies receptionist workflow:
 *
 * Reservation
 *      ↓
 * Payment
 *      ↓
 * Room Assignment
 *      ↓
 * Check-In
 *      ↓
 * Check-Out
 */
class ReceptionIntegrationTest
        extends AbstractIntegrationTest {

    /*
     * Verifies that an available room
     * can be assigned to a confirmed reservation.
     */
    @Test
    void assignRoom_ShouldAssignAvailableRoom()
            throws Exception {

        String adminToken =
                getAdminToken();

        Long reservationId =
                helper.createReservation(
                        adminToken);

        Long paymentId =
                helper.createPayment(
                        adminToken,
                        reservationId,
                        PaymentMethod.UPI);

        // Confirm reservation
        mockMvc.perform(

                        patch("/api/payment/start/{paymentId}",
                                paymentId)
                                .header("Authorization", "Bearer " + adminToken)
                )

                .andExpect(status().isOk());

        mockMvc.perform(

                        patch("/api/payment/success/{paymentId}",
                                paymentId)
                                .header("Authorization", "Bearer " + adminToken)

                                .param(
                                        "gatewayPaymentId",
                                        "pay_assign")

                                .param(
                                        "gatewaySignature",
                                        "signature"))

                .andExpect(status().isOk());

        AssignRoomRequestDTO request =
                new AssignRoomRequestDTO();

        request.setReservationId(
                reservationId);

        mockMvc.perform(

                        post("/api/reception/assign-room")
                                .header("Authorization", "Bearer " + adminToken)

                                .contentType(
                                        MediaType.APPLICATION_JSON)

                                .content(
                                        objectMapper.writeValueAsString(
                                                request)))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.reservationId")
                        .value(reservationId))

                .andExpect(jsonPath("$.roomNumber")
                        .exists())

                .andExpect(jsonPath("$.assignmentStatus")
                        .value("ASSIGNED"));
    }

    /*
     * Verifies that a confirmed
     * reservation can be checked in.
     */
    @Test
    void checkIn_ShouldCheckInGuest()
            throws Exception {

        String adminToken =
                getAdminToken();

        Long reservationId =
                helper.createReservation(
                        adminToken);

        Long paymentId =
                helper.createPayment(
                        adminToken,
                        reservationId,
                        PaymentMethod.UPI);

        // Confirm reservation
        mockMvc.perform(
                        patch("/api/payment/start/{paymentId}",
                                paymentId)
                                .header("Authorization", "Bearer " + adminToken)

                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        patch("/api/payment/success/{paymentId}",
                                paymentId)
                                .header("Authorization", "Bearer " + adminToken)
                                .param(
                                        "gatewayPaymentId",
                                        "pay_checkin")
                                .param(
                                        "gatewaySignature",
                                        "signature"))
                .andExpect(status().isOk());

        // Assign room
        AssignRoomRequestDTO assign =
                new AssignRoomRequestDTO();

        assign.setReservationId(
                reservationId);

        mockMvc.perform(
                        post("/api/reception/assign-room")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                assign)))
                .andExpect(status().isOk());

        // Guest 1
        GuestRequestDTO guest1 =
                new GuestRequestDTO();

        guest1.setReservationId(
                reservationId);
        guest1.setFirstName("Rahul");
        guest1.setLastName("Sharma");
        guest1.setPhone("9999991111");
        guest1.setGender(Gender.MALE);
        guest1.setDateOfBirth(
                LocalDate.of(2000,1,1));

        mockMvc.perform(
                        post("/api/guest/save")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                guest1)))
                .andExpect(status().isOk());

        // Guest 2
        GuestRequestDTO guest2 =
                new GuestRequestDTO();

        guest2.setReservationId(
                reservationId);
        guest2.setFirstName("Amit");
        guest2.setLastName("Patil");
        guest2.setPhone("9999992222");
        guest2.setGender(Gender.MALE);
        guest2.setDateOfBirth(
                LocalDate.of(2001,2,2));

        mockMvc.perform(
                        post("/api/guest/save")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                guest2)))
                .andExpect(status().isOk());

        mockMvc.perform(
                        patch("/api/reception/check-in")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                assign)))
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.assignmentStatus")
                        .value("CHECKED_IN"))

                .andExpect(jsonPath("$.actualCheckIn")
                        .exists());
    }

    /*
     * Verifies that a checked-in
     * guest can be checked out.
     */
    @Test
    void checkOut_ShouldCheckOutGuest()
            throws Exception {

        String adminToken =
                getAdminToken();

        Long reservationId =
                helper.createReservation(
                        adminToken);

        Long paymentId =
                helper.createPayment(
                        adminToken,
                        reservationId,
                        PaymentMethod.UPI);

        // Confirm reservation
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
                                        "pay_checkout")

                                .param(
                                        "gatewaySignature",
                                        "signature"))

                .andExpect(status().isOk());

        // Assign room
        AssignRoomRequestDTO assign =
                new AssignRoomRequestDTO();

        assign.setReservationId(
                reservationId);

        mockMvc.perform(

                        post("/api/reception/assign-room")
                                .header("Authorization", "Bearer " + adminToken)

                                .contentType(
                                        MediaType.APPLICATION_JSON)

                                .content(
                                        objectMapper.writeValueAsString(
                                                assign)))

                .andExpect(status().isOk());

        // Guest 1
        GuestRequestDTO guest1 =
                new GuestRequestDTO();

        guest1.setReservationId(
                reservationId);
        guest1.setFirstName("Rahul");
        guest1.setLastName("Sharma");
        guest1.setPhone("9999991111");
        guest1.setGender(Gender.MALE);
        guest1.setDateOfBirth(
                LocalDate.of(2000,1,1));

        mockMvc.perform(

                        post("/api/guest/save")
                                .header("Authorization", "Bearer " + adminToken)

                                .contentType(
                                        MediaType.APPLICATION_JSON)

                                .content(
                                        objectMapper.writeValueAsString(
                                                guest1)))

                .andExpect(status().isOk());

        // Guest 2
        GuestRequestDTO guest2 =
                new GuestRequestDTO();

        guest2.setReservationId(
                reservationId);
        guest2.setFirstName("Amit");
        guest2.setLastName("Patil");
        guest2.setPhone("9999992222");
        guest2.setGender(Gender.MALE);
        guest2.setDateOfBirth(
                LocalDate.of(2001,2,2));

        mockMvc.perform(

                        post("/api/guest/save")
                                .header("Authorization", "Bearer " + adminToken)

                                .contentType(
                                        MediaType.APPLICATION_JSON)

                                .content(
                                        objectMapper.writeValueAsString(
                                                guest2)))

                .andExpect(status().isOk());

        // Check In
        mockMvc.perform(

                        patch("/api/reception/check-in")
                                .header("Authorization", "Bearer " + adminToken)

                                .contentType(
                                        MediaType.APPLICATION_JSON)

                                .content(
                                        objectMapper.writeValueAsString(
                                                assign)))

                .andExpect(status().isOk());

        // Check Out
        mockMvc.perform(

                        patch("/api/reception/check-out")
                                .header("Authorization", "Bearer " + adminToken)

                                .contentType(
                                        MediaType.APPLICATION_JSON)

                                .content(
                                        objectMapper.writeValueAsString(
                                                assign)))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.assignmentStatus")
                        .value("CHECKED_OUT"))

                .andExpect(jsonPath("$.actualCheckOut")
                        .exists());
    }

}