package com.infotact.project1.integration;

import com.infotact.project1.dto.request.RoomPatchRequestDTO;
import com.infotact.project1.dto.request.RoomRequestDTO;
import com.infotact.project1.dto.request.RoomTypeRequestDTO;
import com.infotact.project1.enums.RoomStatus;
import com.infotact.project1.enums.RoomTypeStatus;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/*
 * Integration tests for Room APIs.
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
class RoomIntegrationTest extends AbstractIntegrationTest {

    /*
     * Verifies that an administrator
     * can create a room successfully.
     */
    @Test
    void createRoom_ShouldCreateRoom() throws Exception {

        String token = getAdminToken();

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

        String roomTypeResponse = mockMvc.perform(

                        post("/api/admin/roomtype/save")

                                .header(
                                        "Authorization",
                                        "Bearer " + token)

                                .contentType(
                                        MediaType.APPLICATION_JSON)

                                .content(
                                        objectMapper.writeValueAsString(roomType)))

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
        room.setFloorNumber(1);
        room.setRoomStatus(RoomStatus.AVAILABLE);
        room.setRoomTypeId(roomTypeId);

        mockMvc.perform(

                        post("/api/admin/room/save")

                                .header(
                                        "Authorization",
                                        "Bearer " + token)

                                .contentType(
                                        MediaType.APPLICATION_JSON)

                                .content(
                                        objectMapper.writeValueAsString(room)))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.roomNumber")
                        .value("101"))

                .andExpect(jsonPath("$.floorNumber")
                        .value(1))

                .andExpect(jsonPath("$.roomStatus")
                        .value("AVAILABLE"));
    }

    /*
     * Verifies that a room
     * can be retrieved by id.
     */
    @Test
    void getRoomById_ShouldReturnRoom() throws Exception {

        String token = getAdminToken();

        RoomTypeRequestDTO roomType =
                new RoomTypeRequestDTO();

        roomType.setName("Suite");
        roomType.setDescription("Luxury Suite");
        roomType.setCapacity(4);
        roomType.setPricePerNight(
                new BigDecimal("5000.00"));
        roomType.setStatus(RoomTypeStatus.ACTIVE);

        String roomTypeResponse = mockMvc.perform(

                        post("/api/admin/roomtype/save")

                                .header(
                                        "Authorization",
                                        "Bearer " + token)

                                .contentType(
                                        MediaType.APPLICATION_JSON)

                                .content(
                                        objectMapper.writeValueAsString(roomType)))

                .andReturn()

                .getResponse()

                .getContentAsString();

        Long roomTypeId =
                objectMapper.readTree(roomTypeResponse)
                        .get("roomTypeId")
                        .asLong();

        RoomRequestDTO room =
                new RoomRequestDTO();

        room.setRoomNumber("201");
        room.setFloorNumber(2);
        room.setRoomStatus(RoomStatus.AVAILABLE);
        room.setRoomTypeId(roomTypeId);

        String roomResponse = mockMvc.perform(

                        post("/api/admin/room/save")

                                .header(
                                        "Authorization",
                                        "Bearer " + token)

                                .contentType(
                                        MediaType.APPLICATION_JSON)

                                .content(
                                        objectMapper.writeValueAsString(room)))

                .andReturn()

                .getResponse()

                .getContentAsString();

        Long roomId =
                objectMapper.readTree(roomResponse)
                        .get("roomId")
                        .asLong();

        mockMvc.perform(

                        get("/api/admin/room/get/{roomId}",
                                roomId)

                                .header(
                                        "Authorization",
                                        "Bearer " + token))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.roomNumber")
                        .value("201"));
    }

    /*
     * Verifies that all rooms
     * can be retrieved successfully.
     */
    @Test
    void getAllRooms_ShouldReturnRooms() throws Exception {

        String token = getAdminToken();

        RoomTypeRequestDTO roomType =
                new RoomTypeRequestDTO();

        roomType.setName("Standard");
        roomType.setDescription("Standard Room");
        roomType.setCapacity(2);
        roomType.setPricePerNight(
                new BigDecimal("1500.00"));
        roomType.setStatus(RoomTypeStatus.ACTIVE);

        String roomTypeResponse =
                mockMvc.perform(

                                post("/api/admin/roomtype/save")

                                        .header(
                                                "Authorization",
                                                "Bearer " + token)

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

        room.setRoomNumber("301");
        room.setFloorNumber(3);
        room.setRoomStatus(RoomStatus.AVAILABLE);
        room.setRoomTypeId(roomTypeId);

        mockMvc.perform(

                        post("/api/admin/room/save")

                                .header(
                                        "Authorization",
                                        "Bearer " + token)

                                .contentType(
                                        MediaType.APPLICATION_JSON)

                                .content(
                                        objectMapper.writeValueAsString(room)))

                .andExpect(status().isOk());

        mockMvc.perform(

                        get("/api/admin/room/getall")

                                .header(
                                        "Authorization",
                                        "Bearer " + token))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$").isArray())

                .andExpect(jsonPath("$[0].roomNumber")
                        .value("301"));
    }

    /*
     * Verifies that rooms can
     * be retrieved using
     * room type id.
     */
    @Test
    void getRoomsByRoomType_ShouldReturnRooms()
            throws Exception {

        String token = getAdminToken();

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
                                                "Bearer " + token)

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

        room.setRoomNumber("401");
        room.setFloorNumber(4);
        room.setRoomStatus(RoomStatus.AVAILABLE);
        room.setRoomTypeId(roomTypeId);

        mockMvc.perform(

                        post("/api/admin/room/save")

                                .header(
                                        "Authorization",
                                        "Bearer " + token)

                                .contentType(
                                        MediaType.APPLICATION_JSON)

                                .content(
                                        objectMapper.writeValueAsString(room)))

                .andExpect(status().isOk());

        mockMvc.perform(

                        get("/api/admin/room/getbyroomtype")

                                .header(
                                        "Authorization",
                                        "Bearer " + token)

                                .param(
                                        "roomTypeId",
                                        roomTypeId.toString()))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$").isArray())

                .andExpect(jsonPath("$[0].roomNumber")
                        .value("401"));
    }

    /*
     * Verifies that rooms can
     * be filtered by room type
     * and room status.
     */
    @Test
    void getRoomsByRoomTypeAndStatus_ShouldReturnRooms()
            throws Exception {

        String token = getAdminToken();

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
                                                "Bearer " + token)

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

        room.setRoomNumber("501");
        room.setFloorNumber(5);
        room.setRoomStatus(RoomStatus.AVAILABLE);
        room.setRoomTypeId(roomTypeId);

        mockMvc.perform(

                        post("/api/admin/room/save")

                                .header(
                                        "Authorization",
                                        "Bearer " + token)

                                .contentType(
                                        MediaType.APPLICATION_JSON)

                                .content(
                                        objectMapper.writeValueAsString(room)))

                .andExpect(status().isOk());

        mockMvc.perform(

                        get("/api/admin/room/filter")

                                .header(
                                        "Authorization",
                                        "Bearer " + token)

                                .param(
                                        "roomTypeId",
                                        roomTypeId.toString())

                                .param(
                                        "roomStatus",
                                        RoomStatus.AVAILABLE.name()))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$").isArray())

                .andExpect(jsonPath("$[0].roomNumber")
                        .value("501"));
    }

    /*
     * Verifies that an existing
     * room can be updated successfully.
     */
    @Test
    void updateRoom_ShouldUpdateRoom()
            throws Exception {

        String token = getAdminToken();

        RoomTypeRequestDTO roomType =
                new RoomTypeRequestDTO();

        roomType.setName("Deluxe");
        roomType.setDescription("Deluxe Room");
        roomType.setCapacity(2);
        roomType.setPricePerNight(
                new BigDecimal("2500.00"));
        roomType.setStatus(RoomTypeStatus.ACTIVE);

        String roomTypeResponse =
                mockMvc.perform(

                                post("/api/admin/roomtype/save")

                                        .header(
                                                "Authorization",
                                                "Bearer " + token)

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

        room.setRoomNumber("601");
        room.setFloorNumber(6);
        room.setRoomStatus(RoomStatus.AVAILABLE);
        room.setRoomTypeId(roomTypeId);

        String roomResponse =
                mockMvc.perform(

                                post("/api/admin/room/save")

                                        .header(
                                                "Authorization",
                                                "Bearer " + token)

                                        .contentType(
                                                MediaType.APPLICATION_JSON)

                                        .content(
                                                objectMapper.writeValueAsString(
                                                        room)))

                        .andReturn()

                        .getResponse()

                        .getContentAsString();

        Long roomId =
                objectMapper.readTree(roomResponse)
                        .get("roomId")
                        .asLong();

        RoomPatchRequestDTO updateRequest =
                new RoomPatchRequestDTO();

        updateRequest.setRoomNumber("602");
        updateRequest.setRoomStatus(
                RoomStatus.MAINTENANCE);

        mockMvc.perform(

                        patch("/api/admin/room/update/{roomId}",
                                roomId)

                                .header(
                                        "Authorization",
                                        "Bearer " + token)

                                .contentType(
                                        MediaType.APPLICATION_JSON)

                                .content(
                                        objectMapper.writeValueAsString(
                                                updateRequest)))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.roomNumber")
                        .value("602"))

                .andExpect(jsonPath("$.roomStatus")
                        .value("MAINTENANCE"));
    }

    /*
     * Verifies that an existing
     * room can be deleted successfully.
     */
    @Test
    void deleteRoom_ShouldDeleteRoom()
            throws Exception {

        String token = getAdminToken();

        RoomTypeRequestDTO roomType =
                new RoomTypeRequestDTO();

        roomType.setName("Temporary");
        roomType.setDescription("Temporary Room");
        roomType.setCapacity(2);
        roomType.setPricePerNight(
                new BigDecimal("1200.00"));
        roomType.setStatus(RoomTypeStatus.ACTIVE);

        String roomTypeResponse =
                mockMvc.perform(

                                post("/api/admin/roomtype/save")

                                        .header(
                                                "Authorization",
                                                "Bearer " + token)

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

        room.setRoomNumber("701");
        room.setFloorNumber(7);
        room.setRoomStatus(RoomStatus.AVAILABLE);
        room.setRoomTypeId(roomTypeId);

        String roomResponse =
                mockMvc.perform(

                                post("/api/admin/room/save")

                                        .header(
                                                "Authorization",
                                                "Bearer " + token)

                                        .contentType(
                                                MediaType.APPLICATION_JSON)

                                        .content(
                                                objectMapper.writeValueAsString(
                                                        room)))

                        .andReturn()

                        .getResponse()

                        .getContentAsString();

        Long roomId =
                objectMapper.readTree(roomResponse)
                        .get("roomId")
                        .asLong();

        mockMvc.perform(

                        delete("/api/admin/room/delete/{roomId}",
                                roomId)

                                .header(
                                        "Authorization",
                                        "Bearer " + token))

                .andExpect(status().isOk())

                .andExpect(content().string(
                        "Room deleted successfully"));
    }
}


