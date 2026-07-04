package com.infotact.project1.integration;

import com.infotact.project1.dto.request.GuestPatchRequestDTO;
import com.infotact.project1.dto.request.GuestRequestDTO;
import com.infotact.project1.enums.Gender;
import com.infotact.project1.enums.PaymentMethod;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/*
 * Integration tests for Guest APIs.
 *
 * Verifies complete request flow:
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
class GuestIntegrationTest
        extends AbstractIntegrationTest {

    /*
     * Verifies that a guest
     * can be created successfully.
     */
    @Test
    void createGuest_ShouldCreateGuest()
            throws Exception {

        String adminToken =
                getAdminToken();

        Long reservationId =
                helper.createReservation(
                        adminToken);

        GuestRequestDTO guest =
                new GuestRequestDTO();

        guest.setReservationId(
                reservationId);

        guest.setFirstName(
                "Rahul");

        guest.setLastName(
                "Sharma");

        guest.setPhone(
                "9876543210");

        guest.setGender(
                Gender.MALE);

        guest.setDateOfBirth(
                LocalDate.of(
                        2002,
                        5,
                        10));

        mockMvc.perform(

                        post("/api/guest/save")

                                .header("Authorization", "Bearer " + adminToken)

                                .contentType(
                                        MediaType.APPLICATION_JSON)

                                .content(
                                        objectMapper.writeValueAsString(
                                                guest)))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.firstName")
                        .value("Rahul"))

                .andExpect(jsonPath("$.lastName")
                        .value("Sharma"))

                .andExpect(jsonPath("$.phone")
                        .value("9876543210"));
    }

    /*
     * Verifies that a guest
     * can be retrieved by id.
     */
    @Test
    void getGuestById_ShouldReturnGuest()
            throws Exception {

        String adminToken =
                getAdminToken();

        Long reservationId =
                helper.createReservation(
                        adminToken);

        GuestRequestDTO guest =
                new GuestRequestDTO();

        guest.setReservationId(
                reservationId);

        guest.setFirstName(
                "Amit");

        guest.setLastName(
                "Patil");

        guest.setPhone(
                "9999999999");

        guest.setGender(
                Gender.MALE);

        guest.setDateOfBirth(
                LocalDate.of(
                        2001,
                        1,
                        15));

        String response =
                mockMvc.perform(

                                post("/api/guest/save")

                                        .header("Authorization", "Bearer " + adminToken)

                                        .contentType(
                                                MediaType.APPLICATION_JSON)

                                        .content(
                                                objectMapper.writeValueAsString(
                                                        guest)))

                        .andReturn()

                        .getResponse()

                        .getContentAsString();

        Long guestId =
                objectMapper.readTree(response)
                        .get("guestId")
                        .asLong();

        mockMvc.perform(

                        get("/api/guest/get/{guestId}", guestId)

                                .header("Authorization", "Bearer " + adminToken))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.guestId")
                        .value(guestId))

                .andExpect(jsonPath("$.firstName")
                        .value("Amit"));
    }

    /*
     * Verifies that guests
     * can be retrieved using
     * reservation id.
     */
    @Test
    void getGuestsByReservation_ShouldReturnGuests()
            throws Exception {

        String adminToken =
                getAdminToken();

        Long reservationId =
                helper.createReservation(
                        adminToken);

        GuestRequestDTO guest =
                new GuestRequestDTO();

        guest.setReservationId(
                reservationId);

        guest.setFirstName("Ravi");
        guest.setLastName("Kumar");
        guest.setPhone("9876500000");
        guest.setGender(Gender.MALE);
        guest.setDateOfBirth(
                LocalDate.of(
                        2000,
                        3,
                        12));

        mockMvc.perform(

                        post("/api/guest/save")
                                .header("Authorization", "Bearer " + adminToken)

                                .contentType(
                                        MediaType.APPLICATION_JSON)

                                .content(
                                        objectMapper.writeValueAsString(
                                                guest)))

                .andExpect(status().isOk());

        mockMvc.perform(

                        get("/api/guest/getbyreservation")

                                .header("Authorization", "Bearer " + adminToken)

                                .param(
                                        "reservationId",
                                        reservationId.toString()))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$").isArray())

                .andExpect(jsonPath("$[0].firstName")
                        .value("Ravi"));
    }

    /*
     * Verifies that guest
     * details can be updated.
     */
    @Test
    void updateGuest_ShouldUpdateGuest()
            throws Exception {

        String adminToken =
                getAdminToken();

        Long reservationId =
                helper.createReservation(
                        adminToken);

        GuestRequestDTO guest =
                new GuestRequestDTO();

        guest.setReservationId(
                reservationId);

        guest.setFirstName("Akash");
        guest.setLastName("Patil");
        guest.setPhone("9999991111");
        guest.setGender(Gender.MALE);
        guest.setDateOfBirth(
                LocalDate.of(
                        2001,
                        5,
                        15));

        String response =
                mockMvc.perform(

                                post("/api/guest/save")
                                        .header("Authorization", "Bearer " + adminToken)

                                        .contentType(
                                                MediaType.APPLICATION_JSON)

                                        .content(
                                                objectMapper.writeValueAsString(
                                                        guest)))

                        .andReturn()

                        .getResponse()

                        .getContentAsString();

        Long guestId =
                objectMapper.readTree(response)
                        .get("guestId")
                        .asLong();

        GuestPatchRequestDTO patch =
                new GuestPatchRequestDTO();

        patch.setFirstName(
                "Updated");

        patch.setPhone(
                "8888888888");

        mockMvc.perform(

                        patch("/api/guest/update/{guestId}", guestId)

                                .header("Authorization", "Bearer " + adminToken)

                                .contentType(
                                        MediaType.APPLICATION_JSON)

                                .content(
                                        objectMapper.writeValueAsString(
                                                patch)))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.firstName")
                        .value("Updated"))

                .andExpect(jsonPath("$.phone")
                        .value("8888888888"));
    }

    /*
     * Verifies that a guest
     * can be deleted successfully.
     */
    @Test
    void deleteGuest_ShouldDeleteGuest()
            throws Exception {

        String adminToken =
                getAdminToken();

        Long reservationId =
                helper.createReservation(
                        adminToken);

        GuestRequestDTO guest =
                new GuestRequestDTO();

        guest.setReservationId(
                reservationId);

        guest.setFirstName("Delete");
        guest.setLastName("Guest");
        guest.setPhone("7777777777");
        guest.setGender(Gender.MALE);
        guest.setDateOfBirth(
                LocalDate.of(
                        1999,
                        8,
                        20));

        String response =
                mockMvc.perform(

                                post("/api/guest/save")
                                        .header("Authorization", "Bearer " + adminToken)

                                        .contentType(
                                                MediaType.APPLICATION_JSON)

                                        .content(
                                                objectMapper.writeValueAsString(
                                                        guest)))

                        .andReturn()

                        .getResponse()

                        .getContentAsString();

        Long guestId =
                objectMapper.readTree(response)
                        .get("guestId")
                        .asLong();

        mockMvc.perform(

                        delete("/api/guest/delete/{guestId}", guestId)

                                .header("Authorization", "Bearer " + adminToken))

                .andExpect(status().isOk())

                .andExpect(content().string(
                        "Guest deleted successfully"));
    }
}