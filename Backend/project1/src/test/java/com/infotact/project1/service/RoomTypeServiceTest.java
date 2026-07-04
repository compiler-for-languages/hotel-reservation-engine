package com.infotact.project1.unittests;

import com.infotact.project1.dto.request.RoomTypeRequestDTO;
import com.infotact.project1.dto.response.RoomTypeResponseDTO;
import com.infotact.project1.enums.RoomTypeStatus;
import com.infotact.project1.model.RoomType;
import com.infotact.project1.repository.RoomTypeRepository;
import com.infotact.project1.service.RoomTypeService;
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
 * Unit tests for RoomTypeService.
 *
 * Covers:
 * - Room type creation
 * - Room type retrieval
 * - Room type update
 * - Room type deletion
 *
 * External dependencies are mocked using Mockito.
 */

@ExtendWith(MockitoExtension.class)
class RoomTypeServiceTest {

    @Mock
    private RoomTypeRepository roomTypeRepository;

    @InjectMocks
    private RoomTypeService roomTypeService;

    /*
     * Verifies that a room type is
     * created successfully.
     */
    @Test
    void createRoomType_ShouldCreateRoomTypeSuccessfully() {

        RoomTypeRequestDTO request = new RoomTypeRequestDTO();

        request.setName("STANDARD_DOUBLE");
        request.setDescription("Standard room for two guests");
        request.setPricePerNight(
                new BigDecimal("1800.00"));
        request.setCapacity(2);
        request.setStatus(RoomTypeStatus.ACTIVE);

        when(roomTypeRepository.findByName(
                request.getName()))
                .thenReturn(Optional.empty());

        RoomType savedRoomType = new RoomType();

        savedRoomType.setRoomTypeId(1L);
        savedRoomType.setName("STANDARD_DOUBLE");
        savedRoomType.setDescription(
                "Standard room for two guests");
        savedRoomType.setPricePerNight(
                new BigDecimal("1800.00"));
        savedRoomType.setCapacity(2);
        savedRoomType.setStatus(
                RoomTypeStatus.ACTIVE);

        when(roomTypeRepository.save(any(RoomType.class)))
                .thenReturn(savedRoomType);

        RoomTypeResponseDTO response =
                roomTypeService.createRoomType(request);

        assertNotNull(response);

        assertEquals(
                1L,
                response.getRoomTypeId());

        assertEquals(
                "STANDARD_DOUBLE",
                response.getName());

        assertEquals(
                RoomTypeStatus.ACTIVE,
                response.getStatus());

        verify(roomTypeRepository)
                .findByName(request.getName());

        verify(roomTypeRepository)
                .save(any(RoomType.class));
    }

    /*
     * Verifies that creating a duplicate
     * room type throws an exception.
     */
    @Test
    void createRoomType_ShouldThrowException_WhenNameAlreadyExists() {

        RoomTypeRequestDTO request =
                new RoomTypeRequestDTO();

        request.setName("STANDARD_DOUBLE");

        RoomType existingRoomType =
                new RoomType();

        existingRoomType.setName(
                "STANDARD_DOUBLE");

        when(roomTypeRepository.findByName(
                request.getName()))
                .thenReturn(Optional.of(existingRoomType));

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> roomTypeService.createRoomType(request));

        assertEquals(
                "Room type already exists.",
                exception.getMessage());

        verify(roomTypeRepository)
                .findByName(request.getName());

        verify(roomTypeRepository,
                never()).save(any());
    }

    /*
     * Verifies that all room types
     * are returned successfully.
     */
    @Test
    void getAllRoomTypes_ShouldReturnAllRoomTypes() {

        RoomType roomType1 = new RoomType();

        roomType1.setRoomTypeId(1L);
        roomType1.setName("STANDARD_SINGLE");
        roomType1.setStatus(
                RoomTypeStatus.ACTIVE);

        RoomType roomType2 = new RoomType();

        roomType2.setRoomTypeId(2L);
        roomType2.setName("SUITE");
        roomType2.setStatus(
                RoomTypeStatus.ACTIVE);

        when(roomTypeRepository.findAll())
                .thenReturn(List.of(
                        roomType1,
                        roomType2));

        List<RoomTypeResponseDTO> roomTypes =
                roomTypeService.getAllRoomTypes();

        assertNotNull(roomTypes);

        assertEquals(2,
                roomTypes.size());

        verify(roomTypeRepository)
                .findAll();
    }

    /*
     * Verifies that a room type is
     * retrieved successfully using
     * its id.
     */
    @Test
    void getRoomTypeById_ShouldReturnRoomType() {

        RoomType roomType =
                new RoomType();

        roomType.setRoomTypeId(1L);
        roomType.setName("SUITE");
        roomType.setDescription(
                "Luxury Suite");
        roomType.setPricePerNight(
                new BigDecimal("5500.00"));
        roomType.setCapacity(4);
        roomType.setStatus(
                RoomTypeStatus.ACTIVE);

        when(roomTypeRepository.findById(1L))
                .thenReturn(Optional.of(roomType));

        RoomTypeResponseDTO response =
                roomTypeService.getRoomTypeById(1L);

        assertNotNull(response);

        assertEquals(
                1L,
                response.getRoomTypeId());

        assertEquals(
                "SUITE",
                response.getName());

        verify(roomTypeRepository)
                .findById(1L);
    }

    /*
     * Verifies that requesting a
     * non-existing room type
     * throws an exception.
     */
    @Test
    void getRoomTypeById_ShouldThrowException_WhenRoomTypeDoesNotExist() {

        when(roomTypeRepository.findById(100L))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> roomTypeService.getRoomTypeById(100L));

        assertEquals(
                "Room Type not found with id: 100",
                exception.getMessage());

        verify(roomTypeRepository)
                .findById(100L);
    }

    /*
     * Verifies that a room type is
     * retrieved successfully using
     * its name.
     */
    @Test
    void getRoomTypeByName_ShouldReturnRoomType() {

        RoomType roomType = new RoomType();

        roomType.setRoomTypeId(1L);
        roomType.setName("SUITE");
        roomType.setDescription("Luxury Suite");
        roomType.setPricePerNight(
                new BigDecimal("5500.00"));
        roomType.setCapacity(4);
        roomType.setStatus(RoomTypeStatus.ACTIVE);

        when(roomTypeRepository.findByName("SUITE"))
                .thenReturn(Optional.of(roomType));

        RoomTypeResponseDTO response =
                roomTypeService.getRoomTypeByName("SUITE");

        assertNotNull(response);
        assertEquals(1L, response.getRoomTypeId());
        assertEquals("SUITE", response.getName());

        verify(roomTypeRepository)
                .findByName("SUITE");
    }

    /*
     * Verifies that requesting a
     * non-existing room type by
     * name throws an exception.
     */
    @Test
    void getRoomTypeByName_ShouldThrowException_WhenNameDoesNotExist() {

        when(roomTypeRepository.findByName("DELUXE"))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> roomTypeService.getRoomTypeByName("DELUXE"));

        assertEquals(
                "Room Type not found with name: DELUXE",
                exception.getMessage());

        verify(roomTypeRepository)
                .findByName("DELUXE");
    }

    /*
     * Verifies that a room type
     * is updated successfully.
     */
    @Test
    void updateRoomType_ShouldUpdateRoomTypeSuccessfully() {

        RoomType roomType = new RoomType();

        roomType.setRoomTypeId(1L);
        roomType.setName("STANDARD_SINGLE");
        roomType.setDescription("Old Description");
        roomType.setPricePerNight(
                new BigDecimal("1500.00"));
        roomType.setCapacity(1);
        roomType.setStatus(RoomTypeStatus.ACTIVE);

        RoomTypeRequestDTO request =
                new RoomTypeRequestDTO();

        request.setName("EXECUTIVE");
        request.setDescription(
                "Executive room for business travellers");
        request.setPricePerNight(
                new BigDecimal("3500.00"));
        request.setCapacity(2);
        request.setStatus(RoomTypeStatus.INACTIVE);

        when(roomTypeRepository.findById(1L))
                .thenReturn(Optional.of(roomType));

        when(roomTypeRepository.save(any(RoomType.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        RoomTypeResponseDTO response =
                roomTypeService.updateRoomType(
                        1L,
                        request);

        assertEquals(
                "EXECUTIVE",
                response.getName());

        assertEquals(
                "Executive room for business travellers",
                response.getDescription());

        assertEquals(
                new BigDecimal("3500.00"),
                response.getPricePerNight());

        assertEquals(
                2,
                response.getCapacity());

        assertEquals(
                RoomTypeStatus.INACTIVE,
                response.getStatus());

        verify(roomTypeRepository)
                .findById(1L);

        verify(roomTypeRepository)
                .save(any(RoomType.class));
    }

    /*
     * Verifies that updating a
     * non-existing room type
     * throws an exception.
     */
    @Test
    void updateRoomType_ShouldThrowException_WhenRoomTypeDoesNotExist() {

        RoomTypeRequestDTO request =
                new RoomTypeRequestDTO();

        when(roomTypeRepository.findById(100L))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> roomTypeService.updateRoomType(
                                100L,
                                request));

        assertEquals(
                "Room Type not found with id: 100",
                exception.getMessage());

        verify(roomTypeRepository)
                .findById(100L);

        verify(roomTypeRepository,
                never()).save(any());
    }

    /*
     * Verifies that a room type
     * is deleted successfully.
     */
    @Test
    void deleteRoomType_ShouldDeleteRoomTypeSuccessfully() {

        RoomType roomType =
                new RoomType();

        roomType.setRoomTypeId(1L);

        when(roomTypeRepository.findById(1L))
                .thenReturn(Optional.of(roomType));

        roomTypeService.deleteRoomType(1L);

        verify(roomTypeRepository)
                .findById(1L);

        verify(roomTypeRepository)
                .delete(roomType);
    }

    /*
     * Verifies that deleting a
     * non-existing room type
     * throws an exception.
     */
    @Test
    void deleteRoomType_ShouldThrowException_WhenRoomTypeDoesNotExist() {

        when(roomTypeRepository.findById(100L))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> roomTypeService.deleteRoomType(100L));

        assertEquals(
                "Room Type not found with id: 100",
                exception.getMessage());

        verify(roomTypeRepository)
                .findById(100L);

        verify(roomTypeRepository,
                never()).delete(any(RoomType.class));
    }

}