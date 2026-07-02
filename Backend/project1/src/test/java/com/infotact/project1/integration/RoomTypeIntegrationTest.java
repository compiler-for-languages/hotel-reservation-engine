package com.infotact.project1.integration;

import com.infotact.project1.dto.request.RoomTypeRequestDTO;
import com.infotact.project1.enums.RoomTypeStatus;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/*
 * Integration tests for RoomType APIs.
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
class RoomTypeIntegrationTest
        extends AbstractIntegrationTest {

    /*
     * Verifies that an administrator
     * can create a room type.
     */
    @Test
    void createRoomType_ShouldCreateRoomType()
            throws Exception {

        String token = getAdminToken();

        RoomTypeRequestDTO request =
                new RoomTypeRequestDTO();

        request.setName("Deluxe");

        request.setDescription(
                "Deluxe AC Room");

        request.setCapacity(2);

        request.setPricePerNight(new BigDecimal("2500.00"));

        request.setStatus(
                RoomTypeStatus.ACTIVE);

        mockMvc.perform(

                        post("/api/admin/roomtype/save")

                                .header(
                                        "Authorization",
                                        "Bearer " + token)

                                .contentType(
                                        MediaType.APPLICATION_JSON)

                                .content(
                                        objectMapper.writeValueAsString(
                                                request)))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.name")
                        .value("Deluxe"))

                .andExpect(jsonPath("$.capacity")
                        .value(2))

                .andExpect(jsonPath("$.pricePerNight")
                        .value(2500.0));
    }

    /*
     * Verifies that a room type
     * can be retrieved using its id.
     */
    @Test
    void getRoomTypeById_ShouldReturnRoomType()
            throws Exception {

        String token = getAdminToken();

        RoomTypeRequestDTO request =
                new RoomTypeRequestDTO();

        request.setName("Suite");
        request.setDescription("Luxury Suite");
        request.setCapacity(4);
        request.setPricePerNight(new BigDecimal("2500.00"));
        request.setStatus(
                RoomTypeStatus.ACTIVE);

        String response = mockMvc.perform(

                        post("/api/admin/roomtype/save")

                                .header(
                                        "Authorization",
                                        "Bearer " + token)

                                .contentType(
                                        MediaType.APPLICATION_JSON)

                                .content(
                                        objectMapper.writeValueAsString(
                                                request)))

                .andReturn()

                .getResponse()

                .getContentAsString();

        Long roomTypeId =
                objectMapper
                        .readTree(response)
                        .get("roomTypeId")
                        .asLong();

        mockMvc.perform(

                        get("/api/admin/roomtype/get/{id}",
                                roomTypeId)

                                .header(
                                        "Authorization",
                                        "Bearer " + token))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.name")
                        .value("Suite"))

                .andExpect(jsonPath("$.pricePerNight")
                .value(2500.00));
    }

    /*
     * Verifies that all room types
     * can be retrieved successfully.
     */
    @Test
    void getAllRoomTypes_ShouldReturnRoomTypes()
            throws Exception {

        String token = getAdminToken();

        RoomTypeRequestDTO request =
                new RoomTypeRequestDTO();

        request.setName("Standard");
        request.setDescription("Standard Room");
        request.setCapacity(2);
        request.setPricePerNight(
                new BigDecimal("1500.00"));
        request.setStatus(
                RoomTypeStatus.ACTIVE);

        mockMvc.perform(

                        post("/api/admin/roomtype/save")

                                .header(
                                        "Authorization",
                                        "Bearer " + token)

                                .contentType(
                                        MediaType.APPLICATION_JSON)

                                .content(
                                        objectMapper.writeValueAsString(
                                                request)))

                .andExpect(status().isOk());

        mockMvc.perform(

                        get("/api/admin/roomtype/getall")

                                .header(
                                        "Authorization",
                                        "Bearer " + token))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$").isArray())

                .andExpect(jsonPath("$[0].name")
                        .value("Standard"));
    }

    /*
     * Verifies that an existing
     * room type can be updated.
     */
    @Test
    void updateRoomType_ShouldUpdateRoomType()
            throws Exception {

        String token = getAdminToken();

        RoomTypeRequestDTO request =
                new RoomTypeRequestDTO();

        request.setName("Suite");
        request.setDescription("Luxury Suite");
        request.setCapacity(4);
        request.setPricePerNight(
                new BigDecimal("5000.00"));
        request.setStatus(
                RoomTypeStatus.ACTIVE);

        String response = mockMvc.perform(

                        post("/api/admin/roomtype/save")

                                .header(
                                        "Authorization",
                                        "Bearer " + token)

                                .contentType(
                                        MediaType.APPLICATION_JSON)

                                .content(
                                        objectMapper.writeValueAsString(
                                                request)))

                .andReturn()

                .getResponse()

                .getContentAsString();

        Long roomTypeId =
                objectMapper.readTree(response)
                        .get("roomTypeId")
                        .asLong();

        RoomTypeRequestDTO updateRequest =
                new RoomTypeRequestDTO();

        updateRequest.setName("Executive Suite");
        updateRequest.setDescription(
                "Updated Luxury Suite");
        updateRequest.setCapacity(5);
        updateRequest.setPricePerNight(
                new BigDecimal("6500.00"));
        updateRequest.setStatus(
                RoomTypeStatus.ACTIVE);

        mockMvc.perform(

                        patch("/api/admin/roomtype/update/{roomTypeId}",
                                roomTypeId)

                                .header(
                                        "Authorization",
                                        "Bearer " + token)

                                .contentType(
                                        MediaType.APPLICATION_JSON)

                                .content(
                                        objectMapper.writeValueAsString(
                                                updateRequest)))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.name")
                        .value("Executive Suite"))

                .andExpect(jsonPath("$.capacity")
                        .value(5));
    }

    /*
     * Verifies that an existing
     * room type can be deleted.
     */
    @Test
    void deleteRoomType_ShouldDeleteRoomType()
            throws Exception {

        String token = getAdminToken();

        RoomTypeRequestDTO request =
                new RoomTypeRequestDTO();

        request.setName("Temporary");
        request.setDescription("Temporary Room");
        request.setCapacity(2);
        request.setPricePerNight(
                new BigDecimal("1000.00"));
        request.setStatus(
                RoomTypeStatus.ACTIVE);

        String response = mockMvc.perform(

                        post("/api/admin/roomtype/save")

                                .header(
                                        "Authorization",
                                        "Bearer " + token)

                                .contentType(
                                        MediaType.APPLICATION_JSON)

                                .content(
                                        objectMapper.writeValueAsString(
                                                request)))

                .andReturn()

                .getResponse()

                .getContentAsString();

        Long roomTypeId =
                objectMapper.readTree(response)
                        .get("roomTypeId")
                        .asLong();

        mockMvc.perform(

                        delete("/api/admin/roomtype/delete/{roomTypeId}",
                                roomTypeId)

                                .header(
                                        "Authorization",
                                        "Bearer " + token))

                .andExpect(status().isOk())

                .andExpect(content().string(
                        "Room Type deleted successfully"));
    }
}