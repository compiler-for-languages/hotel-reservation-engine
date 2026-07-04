package com.infotact.project1.integration;

import com.infotact.project1.dto.request.RegisterRequestDTO;
import com.infotact.project1.dto.request.ReservationPatchRequestDTO;
import com.infotact.project1.dto.request.ReservationRequestDTO;
import com.infotact.project1.dto.request.RoomTypeRequestDTO;
import com.infotact.project1.enums.Gender;
import com.infotact.project1.enums.PaymentMethod;
import com.infotact.project1.enums.RoomTypeStatus;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDate;
import com.infotact.project1.dto.request.RoomRequestDTO;
import com.infotact.project1.enums.RoomStatus;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/*
 * Integration tests for Reservation APIs.
 *
 * Verifies complete reservation workflow:
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
class ReservationIntegrationTest
        extends AbstractIntegrationTest {

    /*
     * Verifies that a customer
     * can create a reservation.
     */
    @Test
    void createReservation_ShouldCreateReservation()
            throws Exception {

        String adminToken = getAdminToken();

        // Register customer

        RegisterRequestDTO customer =
                new RegisterRequestDTO();

        customer.setFirstName("Shrikanth");
        customer.setLastName("Sanagoudar");
        customer.setGender(Gender.MALE);
        customer.setEmail("customer@gmail.com");
        customer.setPhone("9876543210");
        customer.setPassword("password123");

        String customerResponse =
                mockMvc.perform(

                                post("/api/auth/register")

                                        .contentType(
                                                MediaType.APPLICATION_JSON)

                                        .content(
                                                objectMapper.writeValueAsString(
                                                        customer)))

                        .andExpect(status().isOk())

                        .andReturn()

                        .getResponse()

                        .getContentAsString();

        Long userId =
                objectMapper.readTree(customerResponse)
                        .get("userId")
                        .asLong();

        // Create Room Type

        RoomTypeRequestDTO roomType =
                new RoomTypeRequestDTO();

        roomType.setName("Deluxe");
        roomType.setDescription("Deluxe Room");
        roomType.setCapacity(2);
        roomType.setPricePerNight(
                new BigDecimal("2500.00"));
        roomType.setStatus(
                RoomTypeStatus.ACTIVE);

        String roomTypeResponse =
                mockMvc.perform(

                                post("/api/admin/roomtype/save")

                                        .header(
                                                "Authorization",
                                                "Bearer " + adminToken)

                                        .contentType(
                                                MediaType.APPLICATION_JSON)

                                        .content(
                                                objectMapper.writeValueAsString(
                                                        roomType)))

                        .andReturn()

                        .getResponse()

                        .getContentAsString();

        Long roomTypeId =
                objectMapper.readTree(roomTypeResponse)
                        .get("roomTypeId")
                        .asLong();

        RoomRequestDTO room =
                new RoomRequestDTO();

        room.setRoomNumber("101");
        room.setRoomTypeId(roomTypeId);
        room.setFloorNumber(1);
        room.setRoomStatus(RoomStatus.AVAILABLE);

        mockMvc.perform(

                        post("/api/admin/room/save")

                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken)

                                .contentType(
                                        MediaType.APPLICATION_JSON)

                                .content(
                                        objectMapper.writeValueAsString(room)))

                .andExpect(status().isOk());

        ReservationRequestDTO reservation =
                new ReservationRequestDTO();

        reservation.setUserId(userId);
        reservation.setRoomTypeId(roomTypeId);
        reservation.setCheckInDate(
                LocalDate.now().plusDays(2));
        reservation.setCheckOutDate(
                LocalDate.now().plusDays(4));
        reservation.setGuestCount(2);
        reservation.setSpecialRequest(
                "High Floor");
        reservation.setPaymentMethod(
                PaymentMethod.UPI);

        mockMvc.perform(

                        post("/api/reservation/save")

                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken)

                                .contentType(
                                        MediaType.APPLICATION_JSON)

                                .content(
                                        objectMapper.writeValueAsString(
                                                reservation)))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.guestCount")
                        .value(2))

                .andExpect(jsonPath("$.reservationStatus")
                        .value("PENDING"))

                .andExpect(jsonPath("$.specialRequest")
                        .value("High Floor"));
    }

    /*
     * Verifies that a reservation
     * can be retrieved by its id.
     */
    @Test
    void getReservationById_ShouldReturnReservation()
            throws Exception {

        String adminToken = getAdminToken();

        // ---------- Register Customer ----------

        RegisterRequestDTO customer =
                new RegisterRequestDTO();

        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setGender(Gender.MALE);
        customer.setEmail("john@gmail.com");
        customer.setPhone("9999999991");
        customer.setPassword("password123");

        String customerResponse =
                mockMvc.perform(

                                post("/api/auth/register")

                                        .contentType(
                                                MediaType.APPLICATION_JSON)

                                        .content(
                                                objectMapper.writeValueAsString(
                                                        customer)))

                        .andReturn()

                        .getResponse()

                        .getContentAsString();

        Long userId =
                objectMapper.readTree(customerResponse)
                        .get("userId")
                        .asLong();

        // ---------- Create Room Type ----------

        RoomTypeRequestDTO roomType =
                new RoomTypeRequestDTO();

        roomType.setName("Suite");
        roomType.setDescription("Luxury Suite");
        roomType.setCapacity(2);
        roomType.setPricePerNight(
                new BigDecimal("5000.00"));
        roomType.setStatus(RoomTypeStatus.ACTIVE);

        String roomTypeResponse =
                mockMvc.perform(

                                post("/api/admin/roomtype/save")

                                        .header(
                                                "Authorization",
                                                "Bearer " + adminToken)

                                        .contentType(
                                                MediaType.APPLICATION_JSON)

                                        .content(
                                                objectMapper.writeValueAsString(
                                                        roomType)))

                        .andReturn()

                        .getResponse()

                        .getContentAsString();

        Long roomTypeId =
                objectMapper.readTree(roomTypeResponse)
                        .get("roomTypeId")
                        .asLong();

        RoomRequestDTO room =
                new RoomRequestDTO();

        room.setRoomNumber("102");
        room.setRoomTypeId(roomTypeId);
        room.setFloorNumber(1);
        room.setRoomStatus(RoomStatus.AVAILABLE);

        mockMvc.perform(

                        post("/api/admin/room/save")

                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken)

                                .contentType(
                                        MediaType.APPLICATION_JSON)

                                .content(
                                        objectMapper.writeValueAsString(room)))

                .andExpect(status().isOk());

        // ---------- Create Reservation ----------

        ReservationRequestDTO reservation =
                new ReservationRequestDTO();

        reservation.setUserId(userId);
        reservation.setRoomTypeId(roomTypeId);
        reservation.setCheckInDate(
                LocalDate.now().plusDays(1));
        reservation.setCheckOutDate(
                LocalDate.now().plusDays(3));
        reservation.setGuestCount(2);
        reservation.setSpecialRequest("Sea View");
        reservation.setPaymentMethod(
                PaymentMethod.UPI);

        String reservationResponse =
                mockMvc.perform(

                                post("/api/reservation/save")

                                        .header(
                                                "Authorization",
                                                "Bearer " + adminToken)

                                        .contentType(
                                                MediaType.APPLICATION_JSON)

                                        .content(
                                                objectMapper.writeValueAsString(
                                                        reservation)))

                        .andReturn()

                        .getResponse()

                        .getContentAsString();

        Long reservationId =
                objectMapper.readTree(
                                reservationResponse)
                        .get("reservationId")
                        .asLong();

        mockMvc.perform(

                        get("/api/reservation/get/{reservationId}",
                                reservationId)

                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.reservationId")
                        .value(reservationId))

                .andExpect(jsonPath("$.guestCount")
                        .value(2))

                .andExpect(jsonPath("$.specialRequest")
                        .value("Sea View"));
    }

    /*
     * Verifies that reservations
     * can be retrieved for
     * a specific customer.
     */
    @Test
    void getReservationsByUser_ShouldReturnReservations()
            throws Exception {

        String adminToken = getAdminToken();

        RegisterRequestDTO customer =
                new RegisterRequestDTO();

        customer.setFirstName("Alice");
        customer.setLastName("Smith");
        customer.setGender(Gender.FEMALE);
        customer.setEmail("alice@gmail.com");
        customer.setPhone("9999999992");
        customer.setPassword("password123");

        String customerResponse =
                mockMvc.perform(

                                post("/api/auth/register")

                                        .contentType(
                                                MediaType.APPLICATION_JSON)

                                        .content(
                                                objectMapper.writeValueAsString(
                                                        customer)))

                        .andReturn()

                        .getResponse()

                        .getContentAsString();

        Long userId =
                objectMapper.readTree(customerResponse)
                        .get("userId")
                        .asLong();

        RoomTypeRequestDTO roomType =
                new RoomTypeRequestDTO();

        roomType.setName("Executive");
        roomType.setDescription("Executive Room");
        roomType.setCapacity(2);
        roomType.setPricePerNight(
                new BigDecimal("3500.00"));
        roomType.setStatus(RoomTypeStatus.ACTIVE);

        String roomTypeResponse =
                mockMvc.perform(

                                post("/api/admin/roomtype/save")

                                        .header(
                                                "Authorization",
                                                "Bearer " + adminToken)

                                        .contentType(
                                                MediaType.APPLICATION_JSON)

                                        .content(
                                                objectMapper.writeValueAsString(
                                                        roomType)))

                        .andReturn()

                        .getResponse()

                        .getContentAsString();

        Long roomTypeId =
                objectMapper.readTree(roomTypeResponse)
                        .get("roomTypeId")
                        .asLong();

        RoomRequestDTO room =
                new RoomRequestDTO();

        room.setRoomNumber("103");
        room.setRoomTypeId(roomTypeId);
        room.setFloorNumber(1);
        room.setRoomStatus(RoomStatus.AVAILABLE);

        mockMvc.perform(

                        post("/api/admin/room/save")

                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken)

                                .contentType(
                                        MediaType.APPLICATION_JSON)

                                .content(
                                        objectMapper.writeValueAsString(room)))

                .andExpect(status().isOk());

        ReservationRequestDTO reservation =
                new ReservationRequestDTO();

        reservation.setUserId(userId);
        reservation.setRoomTypeId(roomTypeId);
        reservation.setCheckInDate(
                LocalDate.now().plusDays(5));
        reservation.setCheckOutDate(
                LocalDate.now().plusDays(7));
        reservation.setGuestCount(2);
        reservation.setSpecialRequest("Near Lift");
        reservation.setPaymentMethod(
                PaymentMethod.UPI);

        mockMvc.perform(

                        post("/api/reservation/save")

                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken)

                                .contentType(
                                        MediaType.APPLICATION_JSON)

                                .content(
                                        objectMapper.writeValueAsString(
                                                reservation)))

                .andExpect(status().isOk());

        mockMvc.perform(

                        get("/api/reservation/getbyuser")

                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken)

                                .param(
                                        "userId",
                                        userId.toString()))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$").isArray())

                .andExpect(jsonPath("$[0].guestCount")
                        .value(2));
    }

    /*
     * Verifies that reservations
     * can be retrieved by
     * reservation status.
     */
    @Test
    void getReservationsByStatus_ShouldReturnReservations()
            throws Exception {

        String adminToken = getAdminToken();

        RegisterRequestDTO customer =
                new RegisterRequestDTO();

        customer.setFirstName("David");
        customer.setLastName("Miller");
        customer.setGender(Gender.MALE);
        customer.setEmail("david@gmail.com");
        customer.setPhone("9999999993");
        customer.setPassword("password123");

        String customerResponse =
                mockMvc.perform(

                                post("/api/auth/register")

                                        .contentType(
                                                MediaType.APPLICATION_JSON)

                                        .content(
                                                objectMapper.writeValueAsString(
                                                        customer)))

                        .andReturn()

                        .getResponse()

                        .getContentAsString();

        Long userId =
                objectMapper.readTree(customerResponse)
                        .get("userId")
                        .asLong();

        RoomTypeRequestDTO roomType =
                new RoomTypeRequestDTO();

        roomType.setName("Premium");
        roomType.setDescription("Premium Room");
        roomType.setCapacity(2);
        roomType.setPricePerNight(
                new BigDecimal("4500.00"));
        roomType.setStatus(RoomTypeStatus.ACTIVE);

        String roomTypeResponse =
                mockMvc.perform(

                                post("/api/admin/roomtype/save")

                                        .header(
                                                "Authorization",
                                                "Bearer " + adminToken)

                                        .contentType(
                                                MediaType.APPLICATION_JSON)

                                        .content(
                                                objectMapper.writeValueAsString(
                                                        roomType)))

                        .andReturn()

                        .getResponse()

                        .getContentAsString();

        Long roomTypeId =
                objectMapper.readTree(roomTypeResponse)
                        .get("roomTypeId")
                        .asLong();

        RoomRequestDTO room =
                new RoomRequestDTO();

        room.setRoomNumber("104");
        room.setRoomTypeId(roomTypeId);
        room.setFloorNumber(1);
        room.setRoomStatus(RoomStatus.AVAILABLE);

        mockMvc.perform(

                        post("/api/admin/room/save")

                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken)

                                .contentType(
                                        MediaType.APPLICATION_JSON)

                                .content(
                                        objectMapper.writeValueAsString(room)))

                .andExpect(status().isOk());

        ReservationRequestDTO reservation =
                new ReservationRequestDTO();

        reservation.setUserId(userId);
        reservation.setRoomTypeId(roomTypeId);
        reservation.setCheckInDate(
                LocalDate.now().plusDays(2));
        reservation.setCheckOutDate(
                LocalDate.now().plusDays(4));
        reservation.setGuestCount(2);
        reservation.setPaymentMethod(
                PaymentMethod.UPI);

        mockMvc.perform(

                        post("/api/reservation/save")

                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken)

                                .contentType(
                                        MediaType.APPLICATION_JSON)

                                .content(
                                        objectMapper.writeValueAsString(
                                                reservation)))

                .andExpect(status().isOk());

        mockMvc.perform(

                        get("/api/reservation/getbystatus")

                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken)

                                .param(
                                        "status",
                                        "PENDING"))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$").isArray())

                .andExpect(jsonPath("$[0].reservationStatus")
                        .value("PENDING"));
    }

    /*
     * Verifies that an existing
     * reservation can be updated.
     */
    @Test
    void updateReservation_ShouldUpdateReservation()
            throws Exception {

        String adminToken = getAdminToken();

        RegisterRequestDTO customer =
                new RegisterRequestDTO();

        customer.setFirstName("Tom");
        customer.setLastName("Wilson");
        customer.setGender(Gender.MALE);
        customer.setEmail("tom@gmail.com");
        customer.setPhone("9999999994");
        customer.setPassword("password123");

        String customerResponse =
                mockMvc.perform(

                                post("/api/auth/register")

                                        .contentType(
                                                MediaType.APPLICATION_JSON)

                                        .content(
                                                objectMapper.writeValueAsString(
                                                        customer)))

                        .andReturn()

                        .getResponse()

                        .getContentAsString();

        Long userId =
                objectMapper.readTree(customerResponse)
                        .get("userId")
                        .asLong();

        RoomTypeRequestDTO roomType =
                new RoomTypeRequestDTO();

        roomType.setName("Family");
        roomType.setDescription("Family Room");
        roomType.setCapacity(4);
        roomType.setPricePerNight(
                new BigDecimal("6000.00"));
        roomType.setStatus(RoomTypeStatus.ACTIVE);

        String roomTypeResponse =
                mockMvc.perform(

                                post("/api/admin/roomtype/save")

                                        .header(
                                                "Authorization",
                                                "Bearer " + adminToken)

                                        .contentType(
                                                MediaType.APPLICATION_JSON)

                                        .content(
                                                objectMapper.writeValueAsString(
                                                        roomType)))

                        .andReturn()

                        .getResponse()

                        .getContentAsString();

        Long roomTypeId =
                objectMapper.readTree(roomTypeResponse)
                        .get("roomTypeId")
                        .asLong();

        RoomRequestDTO room =
                new RoomRequestDTO();

        room.setRoomNumber("105");
        room.setRoomTypeId(roomTypeId);
        room.setFloorNumber(1);
        room.setRoomStatus(RoomStatus.AVAILABLE);

        mockMvc.perform(

                        post("/api/admin/room/save")

                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken)

                                .contentType(
                                        MediaType.APPLICATION_JSON)

                                .content(
                                        objectMapper.writeValueAsString(room)))

                .andExpect(status().isOk());

        ReservationRequestDTO reservation =
                new ReservationRequestDTO();

        reservation.setUserId(userId);
        reservation.setRoomTypeId(roomTypeId);
        reservation.setCheckInDate(LocalDate.now().plusDays(3));
        reservation.setCheckOutDate(LocalDate.now().plusDays(5));
        reservation.setGuestCount(2);
        reservation.setPaymentMethod(PaymentMethod.UPI);

        String reservationResponse =
                mockMvc.perform(

                                post("/api/reservation/save")

                                        .header(
                                                "Authorization",
                                                "Bearer " + adminToken)

                                        .contentType(MediaType.APPLICATION_JSON)

                                        .content(
                                                objectMapper.writeValueAsString(
                                                        reservation)))

                        .andReturn()

                        .getResponse()

                        .getContentAsString();

        Long reservationId =
                objectMapper.readTree(reservationResponse)
                        .get("reservationId")
                        .asLong();

        ReservationPatchRequestDTO patch =
                new ReservationPatchRequestDTO();

        patch.setSpecialRequest("Airport Pickup");
        patch.setGuestCount(3);

        mockMvc.perform(

                        patch("/api/reservation/update/{reservationId}",
                                reservationId)

                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken)

                                .contentType(
                                        MediaType.APPLICATION_JSON)

                                .content(
                                        objectMapper.writeValueAsString(
                                                patch)))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.guestCount")
                        .value(3))

                .andExpect(jsonPath("$.specialRequest")
                        .value("Airport Pickup"));
    }

    /*
     * Verifies that an existing
     * reservation can be deleted.
     */
    @Test
    void deleteReservation_ShouldDeleteReservation()
            throws Exception {

        String adminToken = getAdminToken();

        RegisterRequestDTO customer =
                new RegisterRequestDTO();

        customer.setFirstName("Peter");
        customer.setLastName("Parker");
        customer.setGender(Gender.MALE);
        customer.setEmail("peter@gmail.com");
        customer.setPhone("9999999995");
        customer.setPassword("password123");

        String customerResponse =
                mockMvc.perform(

                                post("/api/auth/register")

                                        .contentType(MediaType.APPLICATION_JSON)

                                        .content(
                                                objectMapper.writeValueAsString(
                                                        customer)))

                        .andReturn()

                        .getResponse()

                        .getContentAsString();

        Long userId =
                objectMapper.readTree(customerResponse)
                        .get("userId")
                        .asLong();

        RoomTypeRequestDTO roomType =
                new RoomTypeRequestDTO();

        roomType.setName("Business");
        roomType.setDescription("Business Room");
        roomType.setCapacity(2);
        roomType.setPricePerNight(
                new BigDecimal("7000.00"));
        roomType.setStatus(RoomTypeStatus.ACTIVE);

        String roomTypeResponse =
                mockMvc.perform(

                                post("/api/admin/roomtype/save")

                                        .header(
                                                "Authorization",
                                                "Bearer " + adminToken)

                                        .contentType(MediaType.APPLICATION_JSON)

                                        .content(
                                                objectMapper.writeValueAsString(
                                                        roomType)))

                        .andReturn()

                        .getResponse()

                        .getContentAsString();

        Long roomTypeId =
                objectMapper.readTree(roomTypeResponse)
                        .get("roomTypeId")
                        .asLong();

        RoomRequestDTO room =
                new RoomRequestDTO();

        room.setRoomNumber("106");
        room.setRoomTypeId(roomTypeId);
        room.setFloorNumber(1);
        room.setRoomStatus(RoomStatus.AVAILABLE);

        mockMvc.perform(

                        post("/api/admin/room/save")

                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken)

                                .contentType(
                                        MediaType.APPLICATION_JSON)

                                .content(
                                        objectMapper.writeValueAsString(room)))

                .andExpect(status().isOk());

        ReservationRequestDTO reservation =
                new ReservationRequestDTO();

        reservation.setUserId(userId);
        reservation.setRoomTypeId(roomTypeId);
        reservation.setCheckInDate(LocalDate.now().plusDays(2));
        reservation.setCheckOutDate(LocalDate.now().plusDays(4));
        reservation.setGuestCount(2);
        reservation.setPaymentMethod(PaymentMethod.UPI);

        String reservationResponse =
                mockMvc.perform(

                                post("/api/reservation/save")

                                        .header(
                                                "Authorization",
                                                "Bearer " + adminToken)

                                        .contentType(MediaType.APPLICATION_JSON)

                                        .content(
                                                objectMapper.writeValueAsString(
                                                        reservation)))

                        .andReturn()

                        .getResponse()

                        .getContentAsString();

        Long reservationId =
                objectMapper.readTree(reservationResponse)
                        .get("reservationId")
                        .asLong();

        mockMvc.perform(

                        delete("/api/reservation/delete/{reservationId}",
                                reservationId)

                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken))

                .andExpect(status().isOk())

                .andExpect(content().string(
                        "Reservation deleted successfully"));
    }

}



