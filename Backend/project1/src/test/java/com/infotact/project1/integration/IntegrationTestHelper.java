package com.infotact.project1.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infotact.project1.dto.request.RegisterRequestDTO;
import com.infotact.project1.enums.Gender;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import com.infotact.project1.dto.request.RoomTypeRequestDTO;
import com.infotact.project1.enums.RoomTypeStatus;

import java.math.BigDecimal;

import com.infotact.project1.dto.request.ReservationRequestDTO;
import com.infotact.project1.enums.PaymentMethod;

import java.time.LocalDate;
import com.infotact.project1.dto.request.PaymentRequestDTO;
import com.infotact.project1.dto.request.RoomRequestDTO;
import com.infotact.project1.enums.RoomStatus;

@Component
@RequiredArgsConstructor
public class IntegrationTestHelper {

    private final MockMvc mockMvc;

    private final ObjectMapper objectMapper;

    /*
     * Registers a customer
     * and returns the generated user id.
     */
    public Long createCustomer() throws Exception {

        RegisterRequestDTO request =
                new RegisterRequestDTO();

        request.setFirstName("Test");

        request.setLastName("Customer");

        request.setGender(
                Gender.MALE);

        request.setEmail(
                "customer"
                        + System.nanoTime()
                        + "@gmail.com");

        request.setPhone(
                String.valueOf(
                                System.nanoTime())
                        .substring(0,10));

        request.setPassword(
                "password123");

        String response =
                mockMvc.perform(

                                post("/api/auth/register")

                                        .contentType(
                                                MediaType.APPLICATION_JSON)

                                        .content(
                                                objectMapper.writeValueAsString(
                                                        request)))

                        .andReturn()

                        .getResponse()

                        .getContentAsString();

        JsonNode json =
                objectMapper.readTree(response);

        return json.get("userId")
                .asLong();
    }

    /*
     * Creates a room type
     * and returns the generated room type id.
     */
    public Long createRoomType(
            String adminToken)
            throws Exception {

        RoomTypeRequestDTO request =
                new RoomTypeRequestDTO();

        request.setName(
                "RoomType"
                        + System.nanoTime());

        request.setDescription(
                "Test Room Type");

        request.setCapacity(2);

        request.setPricePerNight(
                new BigDecimal("2500.00"));

        request.setStatus(
                RoomTypeStatus.ACTIVE);

        String response =
                mockMvc.perform(

                                post("/api/admin/roomtype/save")

                                        .header(
                                                "Authorization",
                                                "Bearer " + adminToken)

                                        .contentType(
                                                MediaType.APPLICATION_JSON)

                                        .content(
                                                objectMapper.writeValueAsString(
                                                        request)))

                        .andReturn()

                        .getResponse()

                        .getContentAsString();

        JsonNode json =
                objectMapper.readTree(response);

        return json.get("roomTypeId")
                .asLong();
    }

    /*
     * Creates a reservation
     * and returns the generated reservation id.
     */
    public Long createReservation(
            String adminToken)
            throws Exception {

        // Create prerequisite data
        Long userId =
                createCustomer();

        Long roomTypeId =
                createRoomType(
                        adminToken);

        ReservationRequestDTO request =
                new ReservationRequestDTO();

        request.setUserId(
                userId);

        request.setRoomTypeId(
                roomTypeId);

        request.setCheckInDate(
                LocalDate.now()
                        .plusDays(2));

        request.setCheckOutDate(
                LocalDate.now()
                        .plusDays(4));

        request.setGuestCount(
                2);

        request.setSpecialRequest(
                "Near Window");

        request.setPaymentMethod(
                PaymentMethod.UPI);

        String response =
                mockMvc.perform(

                                post("/api/reservation/save")

                                        .header(
                                                "Authorization",
                                                "Bearer " + adminToken)

                                        .contentType(
                                                MediaType.APPLICATION_JSON)

                                        .content(
                                                objectMapper.writeValueAsString(
                                                        request)))

                        .andReturn()

                        .getResponse()

                        .getContentAsString();

        JsonNode json =
                objectMapper.readTree(
                        response);

        return json.get("reservationId")
                .asLong();
    }

    /*
     * Creates a payment
     * and returns the generated payment id.
     */
    public Long createPayment(
            String adminToken,
            Long reservationId,
            PaymentMethod paymentMethod)
            throws Exception {

        PaymentRequestDTO request =
                new PaymentRequestDTO();

        request.setReservationId(
                reservationId);

        request.setPaymentMethod(
                paymentMethod);

        String response =
                mockMvc.perform(

                                post("/api/payment/save")

                                        .header(
                                                "Authorization",
                                                "Bearer " + adminToken)

                                        .contentType(
                                                MediaType.APPLICATION_JSON)

                                        .content(
                                                objectMapper.writeValueAsString(
                                                        request)))

                        .andReturn()

                        .getResponse()

                        .getContentAsString();

        JsonNode json =
                objectMapper.readTree(response);

        return json.get("paymentId")
                .asLong();
    }

    /*
     * Creates an available room
     * for the given room type and
     * returns the generated room id.
     */
    public Long createRoom(
            String adminToken,
            Long roomTypeId)
            throws Exception {

        RoomRequestDTO request =
                new RoomRequestDTO();

        request.setRoomNumber(
                "R"
                        + System.nanoTime());

        request.setRoomTypeId(
                roomTypeId);

        request.setFloorNumber(
                1);

        request.setRoomStatus(
                RoomStatus.AVAILABLE);

        String response =
                mockMvc.perform(

                                post("/api/admin/room/save")

                                        .header(
                                                "Authorization",
                                                "Bearer " + adminToken)

                                        .contentType(
                                                MediaType.APPLICATION_JSON)

                                        .content(
                                                objectMapper.writeValueAsString(
                                                        request)))

                        .andReturn()

                        .getResponse()

                        .getContentAsString();

        JsonNode json =
                objectMapper.readTree(
                        response);

        return json.get("roomId")
                .asLong();
    }






}