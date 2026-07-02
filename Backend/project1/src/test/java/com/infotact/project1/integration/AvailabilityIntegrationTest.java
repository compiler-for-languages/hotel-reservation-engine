package com.infotact.project1.integration;

import com.infotact.project1.dto.request.AvailabilityRequestDTO;
import com.infotact.project1.dto.request.RoomRequestDTO;
import com.infotact.project1.enums.RoomStatus;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/*
 * Integration tests for Availability APIs.
 *
 * Verifies room availability
 * based on inventory and bookings.
 */
class AvailabilityIntegrationTest
        extends AbstractIntegrationTest {

    /*
     * Verifies that availability
     * is returned successfully.
     */
    @Test
    void checkAvailability_ShouldReturnAvailableRooms()
            throws Exception {

        String adminToken =
                getAdminToken();

        Long roomTypeId =
                helper.createRoomType(
                        adminToken);

        RoomRequestDTO room =
                new RoomRequestDTO();

        room.setRoomNumber(
                "A101");

        room.setRoomTypeId(
                roomTypeId);

        room.setFloorNumber(
                1);

        room.setRoomStatus(
                RoomStatus.AVAILABLE);

        mockMvc.perform(

                        post("/api/admin/room/save")

                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken)

                                .contentType(
                                        MediaType.APPLICATION_JSON)

                                .content(
                                        objectMapper.writeValueAsString(
                                                room)))

                .andExpect(status().isOk());

        AvailabilityRequestDTO request =
                new AvailabilityRequestDTO();

        request.setRoomTypeId(
                roomTypeId);

        request.setCheckInDate(
                LocalDate.now()
                        .plusDays(2));

        request.setCheckOutDate(
                LocalDate.now()
                        .plusDays(4));

        mockMvc.perform(

                        post("/api/availability/check")

                                .contentType(
                                        MediaType.APPLICATION_JSON)

                                .content(
                                        objectMapper.writeValueAsString(
                                                request)))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.roomTypeId")
                        .value(roomTypeId))

                .andExpect(jsonPath("$.available")
                        .value(true))

                .andExpect(jsonPath("$.availableRooms")
                        .value(1));
    }

    /*
     * Verifies that customer
     * availability search returns
     * customer-friendly information.
     */
    @Test
    void searchAvailability_ShouldReturnCustomerResponse()
            throws Exception {

        String adminToken =
                getAdminToken();

        Long roomTypeId =
                helper.createRoomType(
                        adminToken);

        RoomRequestDTO room =
                new RoomRequestDTO();

        room.setRoomNumber(
                "A102");

        room.setRoomTypeId(
                roomTypeId);

        room.setFloorNumber(
                1);

        room.setRoomStatus(
                RoomStatus.AVAILABLE);

        mockMvc.perform(

                        post("/api/admin/room/save")

                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken)

                                .contentType(
                                        MediaType.APPLICATION_JSON)

                                .content(
                                        objectMapper.writeValueAsString(
                                                room)))

                .andExpect(status().isOk());

        AvailabilityRequestDTO request =
                new AvailabilityRequestDTO();

        request.setRoomTypeId(
                roomTypeId);

        request.setCheckInDate(
                LocalDate.now()
                        .plusDays(3));

        request.setCheckOutDate(
                LocalDate.now()
                        .plusDays(5));

        mockMvc.perform(

                        post("/api/availability/search")

                                .contentType(
                                        MediaType.APPLICATION_JSON)

                                .content(
                                        objectMapper.writeValueAsString(
                                                request)))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.roomTypeId")
                        .value(roomTypeId))

                .andExpect(jsonPath("$.available")
                        .value(true))

                .andExpect(jsonPath("$.capacity")
                        .value(2))

                .andExpect(jsonPath("$.pricePerNight")
                        .value(2500.00))

                .andExpect(jsonPath("$.availabilityMessage")
                        .exists());
    }
}

