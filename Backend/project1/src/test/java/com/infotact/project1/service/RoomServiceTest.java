package com.infotact.project1.service;

import com.infotact.project1.dto.request.RoomPatchRequestDTO;
import com.infotact.project1.dto.request.RoomRequestDTO;
import com.infotact.project1.dto.response.RoomResponseDTO;
import com.infotact.project1.enums.RoomStatus;
import com.infotact.project1.enums.RoomTypeStatus;
import com.infotact.project1.model.Room;
import com.infotact.project1.model.RoomType;
import com.infotact.project1.repository.RoomRepository;
import com.infotact.project1.repository.RoomTypeRepository;
import com.infotact.project1.service.RoomService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/*
 * Unit tests for RoomService.
 *
 * Covers:
 * - Room creation
 * - Room retrieval
 * - Room update
 * - Room deletion
 * - Room filtering
 *
 * External dependencies are mocked using Mockito.
 */

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RoomTypeRepository roomTypeRepository;

    @InjectMocks
    private RoomService roomService;

    /*
     * Verifies that a room is created
     * successfully.
     */
    @Test
    void createRoom_ShouldCreateRoomSuccessfully() {

        RoomRequestDTO request = new RoomRequestDTO();

        request.setRoomNumber("101");
        request.setFloorNumber(1);
        request.setRoomTypeId(1L);
        request.setRoomStatus(RoomStatus.AVAILABLE);

        RoomType roomType = new RoomType();

        roomType.setRoomTypeId(1L);
        roomType.setName("STANDARD_DOUBLE");
        roomType.setStatus(RoomTypeStatus.ACTIVE);
        roomType.setCapacity(2);
        roomType.setPricePerNight(
                new BigDecimal("1800"));

        when(roomTypeRepository.findById(1L))
                .thenReturn(Optional.of(roomType));

        when(roomRepository.findByRoomNumber("101"))
                .thenReturn(Optional.empty());

        Room savedRoom = new Room();

        savedRoom.setRoomId(1L);
        savedRoom.setRoomNumber("101");
        savedRoom.setFloorNumber(1);
        savedRoom.setRoomStatus(RoomStatus.AVAILABLE);
        savedRoom.setRoomType(roomType);

        when(roomRepository.save(any(Room.class)))
                .thenReturn(savedRoom);

        RoomResponseDTO response =
                roomService.createRoom(request);

        assertNotNull(response);

        assertEquals(1L,
                response.getRoomId());

        assertEquals("101",
                response.getRoomNumber());

        assertEquals(
                RoomStatus.AVAILABLE,
                response.getRoomStatus());

        verify(roomTypeRepository)
                .findById(1L);

        verify(roomRepository)
                .findByRoomNumber("101");

        verify(roomRepository)
                .save(any(Room.class));
    }

    /*
     * Verifies that room creation fails
     * when the room type does not exist.
     */
    @Test
    void createRoom_ShouldThrowException_WhenRoomTypeDoesNotExist() {

        RoomRequestDTO request =
                new RoomRequestDTO();

        request.setRoomTypeId(10L);

        when(roomTypeRepository.findById(10L))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> roomService.createRoom(request));

        assertEquals(
                "Room Type not found with id: 10",
                exception.getMessage());

        verify(roomTypeRepository)
                .findById(10L);

        verify(roomRepository,
                never()).save(any());
    }

    /*
     * Verifies that room creation fails
     * when the room number already exists.
     */
    @Test
    void createRoom_ShouldThrowException_WhenRoomNumberAlreadyExists() {

        RoomRequestDTO request =
                new RoomRequestDTO();

        request.setRoomNumber("101");
        request.setRoomTypeId(1L);

        RoomType roomType =
                new RoomType();

        roomType.setRoomTypeId(1L);

        when(roomTypeRepository.findById(1L))
                .thenReturn(Optional.of(roomType));

        Room existingRoom =
                new Room();

        existingRoom.setRoomNumber("101");

        when(roomRepository.findByRoomNumber("101"))
                .thenReturn(Optional.of(existingRoom));

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> roomService.createRoom(request));

        assertEquals(
                "Room number already exists: 101",
                exception.getMessage());

        verify(roomRepository,
                never()).save(any());
    }

    /*
     * Verifies that all rooms are
     * returned successfully.
     */
    @Test
    void getAllRooms_ShouldReturnAllRooms() {

        RoomType roomType =
                new RoomType();

        roomType.setName("STANDARD");

        Room room1 = new Room();

        room1.setRoomId(1L);
        room1.setRoomNumber("101");
        room1.setRoomStatus(RoomStatus.AVAILABLE);
        room1.setRoomType(roomType);

        Room room2 = new Room();

        room2.setRoomId(2L);
        room2.setRoomNumber("102");
        room2.setRoomStatus(RoomStatus.OCCUPIED);
        room2.setRoomType(roomType);

        when(roomRepository.findAll())
                .thenReturn(List.of(room1, room2));

        List<RoomResponseDTO> rooms =
                roomService.getAllRooms();

        assertEquals(2,
                rooms.size());

        verify(roomRepository)
                .findAll();
    }

    /*
     * Verifies that a room is
     * retrieved successfully
     * using room id.
     */
    @Test
    void getRoomById_ShouldReturnRoom() {

        RoomType roomType =
                new RoomType();

        roomType.setName("STANDARD");

        Room room = new Room();

        room.setRoomId(1L);
        room.setRoomNumber("101");
        room.setFloorNumber(1);
        room.setRoomStatus(RoomStatus.AVAILABLE);
        room.setRoomType(roomType);

        when(roomRepository.findById(1L))
                .thenReturn(Optional.of(room));

        RoomResponseDTO response =
                roomService.getRoomById(1L);

        assertEquals(1L,
                response.getRoomId());

        assertEquals("101",
                response.getRoomNumber());

        verify(roomRepository)
                .findById(1L);
    }

    /*
     * Verifies that requesting a
     * non-existing room throws
     * an exception.
     */
    @Test
    void getRoomById_ShouldThrowException_WhenRoomDoesNotExist() {

        when(roomRepository.findById(100L))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> roomService.getRoomById(100L));

        assertEquals(
                "Room not found with id: 100",
                exception.getMessage());

        verify(roomRepository)
                .findById(100L);
    }

    /*
     * Verifies that a room is
     * retrieved successfully
     * using room number.
     */
    @Test
    void getRoomByRoomNumber_ShouldReturnRoom() {

        RoomType roomType = new RoomType();
        roomType.setName("STANDARD");

        Room room = new Room();
        room.setRoomId(1L);
        room.setRoomNumber("101");
        room.setFloorNumber(1);
        room.setRoomStatus(RoomStatus.AVAILABLE);
        room.setRoomType(roomType);

        when(roomRepository.findByRoomNumber("101"))
                .thenReturn(Optional.of(room));

        RoomResponseDTO response =
                roomService.getRoomByRoomNumber("101");

        assertNotNull(response);
        assertEquals(1L, response.getRoomId());
        assertEquals("101", response.getRoomNumber());

        verify(roomRepository).findByRoomNumber("101");
    }

    /*
     * Verifies that requesting a
     * non-existing room number
     * throws an exception.
     */
    @Test
    void getRoomByRoomNumber_ShouldThrowException_WhenRoomDoesNotExist() {

        when(roomRepository.findByRoomNumber("999"))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> roomService.getRoomByRoomNumber("999"));

        assertEquals(
                "Room not found with room number: 999",
                exception.getMessage());

        verify(roomRepository).findByRoomNumber("999");
    }

    /*
     * Verifies that all rooms
     * belonging to a room type
     * are returned successfully.
     */
    @Test
    void getRoomsByRoomType_ShouldReturnRooms() {

        RoomType roomType = new RoomType();
        roomType.setRoomTypeId(1L);
        roomType.setName("STANDARD");

        Room room = new Room();
        room.setRoomId(1L);
        room.setRoomNumber("101");
        room.setRoomStatus(RoomStatus.AVAILABLE);
        room.setRoomType(roomType);

        when(roomTypeRepository.findById(1L))
                .thenReturn(Optional.of(roomType));

        when(roomRepository.findByRoomType(roomType))
                .thenReturn(List.of(room));

        List<RoomResponseDTO> response =
                roomService.getRoomsByRoomType(1L);

        assertEquals(1, response.size());

        verify(roomTypeRepository).findById(1L);
        verify(roomRepository).findByRoomType(roomType);
    }

    /*
     * Verifies that requesting rooms
     * using an invalid room type id
     * throws an exception.
     */
    @Test
    void getRoomsByRoomType_ShouldThrowException_WhenRoomTypeDoesNotExist() {

        when(roomTypeRepository.findById(100L))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> roomService.getRoomsByRoomType(100L));

        assertEquals(
                "Room Type not found with id: 100",
                exception.getMessage());

        verify(roomTypeRepository).findById(100L);
    }

    /*
     * Verifies that rooms are
     * filtered successfully by
     * room type and room status.
     */
    @Test
    void getRoomsByRoomTypeAndStatus_ShouldReturnRooms() {

        RoomType roomType = new RoomType();
        roomType.setRoomTypeId(1L);
        roomType.setName("STANDARD");

        Room room = new Room();
        room.setRoomId(1L);
        room.setRoomNumber("101");
        room.setRoomStatus(RoomStatus.AVAILABLE);
        room.setRoomType(roomType);

        when(roomTypeRepository.findById(1L))
                .thenReturn(Optional.of(roomType));

        when(roomRepository.findByRoomTypeAndRoomStatus(
                roomType,
                RoomStatus.AVAILABLE))
                .thenReturn(List.of(room));

        List<RoomResponseDTO> response =
                roomService.getRoomsByRoomTypeAndStatus(
                        1L,
                        RoomStatus.AVAILABLE);

        assertEquals(1, response.size());

        verify(roomRepository)
                .findByRoomTypeAndRoomStatus(
                        roomType,
                        RoomStatus.AVAILABLE);
    }

    /*
     * Verifies that filtering rooms
     * using an invalid room type
     * throws an exception.
     */
    @Test
    void getRoomsByRoomTypeAndStatus_ShouldThrowException_WhenRoomTypeDoesNotExist() {

        when(roomTypeRepository.findById(100L))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> roomService.getRoomsByRoomTypeAndStatus(
                                100L,
                                RoomStatus.AVAILABLE));

        assertEquals(
                "Room Type not found. Please provide a valid room type id.",
                exception.getMessage());

        verify(roomTypeRepository).findById(100L);
    }

    /*
     * Verifies that room details
     * are updated successfully.
     */
    @Test
    void updateRoom_ShouldUpdateRoomSuccessfully() {

        RoomType roomType = new RoomType();
        roomType.setName("STANDARD");

        Room room = new Room();
        room.setRoomId(1L);
        room.setRoomNumber("101");
        room.setRoomStatus(RoomStatus.AVAILABLE);
        room.setRoomType(roomType);

        RoomPatchRequestDTO request =
                new RoomPatchRequestDTO();

        request.setRoomNumber("102");
        request.setRoomStatus(RoomStatus.MAINTENANCE);

        when(roomRepository.findById(1L))
                .thenReturn(Optional.of(room));

        when(roomRepository.findByRoomNumber("102"))
                .thenReturn(Optional.empty());

        when(roomRepository.save(any(Room.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RoomResponseDTO response =
                roomService.updateRoom(1L, request);

        assertEquals("102", response.getRoomNumber());
        assertEquals(RoomStatus.MAINTENANCE,
                response.getRoomStatus());

        verify(roomRepository).save(any(Room.class));
    }

    /*
     * Verifies that updating a
     * non-existing room throws
     * an exception.
     */
    @Test
    void updateRoom_ShouldThrowException_WhenRoomDoesNotExist() {

        RoomPatchRequestDTO request =
                new RoomPatchRequestDTO();

        when(roomRepository.findById(100L))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> roomService.updateRoom(
                                100L,
                                request));

        assertEquals(
                "Room not found with id: 100",
                exception.getMessage());

        verify(roomRepository).findById(100L);
    }

    /*
     * Verifies that updating a room
     * with an existing room number
     * throws an exception.
     */
    @Test
    void updateRoom_ShouldThrowException_WhenRoomNumberAlreadyExists() {

        Room currentRoom = new Room();
        currentRoom.setRoomId(1L);

        Room existingRoom = new Room();
        existingRoom.setRoomId(2L);

        RoomPatchRequestDTO request =
                new RoomPatchRequestDTO();

        request.setRoomNumber("102");

        when(roomRepository.findById(1L))
                .thenReturn(Optional.of(currentRoom));

        when(roomRepository.findByRoomNumber("102"))
                .thenReturn(Optional.of(existingRoom));

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> roomService.updateRoom(
                                1L,
                                request));

        assertEquals(
                "Room number already exists: 102",
                exception.getMessage());

        verify(roomRepository, never())
                .save(any(Room.class));
    }

    /*
     * Verifies that a room
     * is deleted successfully.
     */
    @Test
    void deleteRoom_ShouldDeleteRoomSuccessfully() {

        Room room = new Room();
        room.setRoomId(1L);

        when(roomRepository.findById(1L))
                .thenReturn(Optional.of(room));

        roomService.deleteRoom(1L);

        verify(roomRepository).findById(1L);
        verify(roomRepository).delete(room);
    }

    /*
     * Verifies that deleting a
     * non-existing room throws
     * an exception.
     */
    @Test
    void deleteRoom_ShouldThrowException_WhenRoomDoesNotExist() {

        when(roomRepository.findById(100L))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> roomService.deleteRoom(100L));

        assertEquals(
                "Room not found with id: 100",
                exception.getMessage());

        verify(roomRepository).findById(100L);
        verify(roomRepository, never()).delete(any(Room.class));
    }

}

