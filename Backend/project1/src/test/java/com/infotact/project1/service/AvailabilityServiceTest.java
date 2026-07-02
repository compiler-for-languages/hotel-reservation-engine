package com.infotact.project1.service;

import com.infotact.project1.dto.request.AvailabilityRequestDTO;
import com.infotact.project1.dto.response.AvailabilityCustomerResponseDTO;
import com.infotact.project1.dto.response.AvailabilityResponseDTO;
import com.infotact.project1.enums.BookingHoldStatus;
import com.infotact.project1.model.BookingHold;
import com.infotact.project1.model.RoomType;
import com.infotact.project1.repository.BookingHoldRepository;
import com.infotact.project1.repository.ReservationRepository;
import com.infotact.project1.repository.RoomRepository;
import com.infotact.project1.repository.RoomTypeRepository;
import com.infotact.project1.service.AvailabilityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/*
 * Unit tests for AvailabilityService.
 *
 * Covers:
 * - Room availability calculation
 * - Booking hold consideration
 * - Date validation
 * - Customer availability mapping
 *
 * External dependencies are mocked using Mockito.
 */

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RoomTypeRepository roomTypeRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private BookingHoldRepository bookingHoldRepository;

    @InjectMocks
    private AvailabilityService availabilityService;

    /*
     * Verifies that availability is calculated
     * correctly when rooms are available.
     */
    @Test
    void checkAvailability_ShouldReturnAvailableRooms() {

        AvailabilityRequestDTO request =
                new AvailabilityRequestDTO();

        request.setRoomTypeId(1L);
        request.setCheckInDate(LocalDate.now().plusDays(1));
        request.setCheckOutDate(LocalDate.now().plusDays(3));

        RoomType roomType = new RoomType();

        roomType.setRoomTypeId(1L);
        roomType.setName("STANDARD");
        roomType.setCapacity(2);
        roomType.setPricePerNight(
                new BigDecimal("1800"));

        when(roomTypeRepository.findById(1L))
                .thenReturn(Optional.of(roomType));

        when(roomRepository.countByRoomType(roomType))
                .thenReturn(10L);

        when(reservationRepository.countOverlappingReservations(
                1L,
                request.getCheckInDate(),
                request.getCheckOutDate()))
                .thenReturn(3L);

        when(bookingHoldRepository.findAll())
                .thenReturn(List.of());

        AvailabilityResponseDTO response =
                availabilityService.checkAvailability(request);

        assertNotNull(response);

        assertEquals(10,
                response.getTotalRooms());

        assertEquals(3,
                response.getBookedRooms());

        assertEquals(0,
                response.getActiveHolds());

        assertEquals(7,
                response.getAvailableRooms());

        assertTrue(response.isAvailable());

        verify(roomRepository)
                .countByRoomType(roomType);

        verify(reservationRepository)
                .countOverlappingReservations(
                        1L,
                        request.getCheckInDate(),
                        request.getCheckOutDate());
    }

    /*
     * Verifies that the room type is
     * reported as unavailable when
     * no rooms remain.
     */
    @Test
    void checkAvailability_ShouldReturnSoldOut_WhenNoRoomsAvailable() {

        AvailabilityRequestDTO request =
                new AvailabilityRequestDTO();

        request.setRoomTypeId(1L);
        request.setCheckInDate(LocalDate.now().plusDays(1));
        request.setCheckOutDate(LocalDate.now().plusDays(2));

        RoomType roomType = new RoomType();

        roomType.setRoomTypeId(1L);
        roomType.setName("STANDARD");

        when(roomTypeRepository.findById(1L))
                .thenReturn(Optional.of(roomType));

        when(roomRepository.countByRoomType(roomType))
                .thenReturn(5L);

        when(reservationRepository.countOverlappingReservations(
                anyLong(),
                any(),
                any()))
                .thenReturn(5L);

        when(bookingHoldRepository.findAll())
                .thenReturn(List.of());

        AvailabilityResponseDTO response =
                availabilityService.checkAvailability(request);

        assertFalse(response.isAvailable());

        assertEquals(0,
                response.getAvailableRooms());
    }

    /*
     * Verifies that requesting
     * availability for a non-existing
     * room type throws an exception.
     */
    @Test
    void checkAvailability_ShouldThrowException_WhenRoomTypeDoesNotExist() {

        AvailabilityRequestDTO request =
                new AvailabilityRequestDTO();

        request.setRoomTypeId(100L);
        request.setCheckInDate(LocalDate.now().plusDays(1));
        request.setCheckOutDate(LocalDate.now().plusDays(2));

        when(roomTypeRepository.findById(100L))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> availabilityService.checkAvailability(request));

        assertEquals(
                "Room Type not found with id: 100",
                exception.getMessage());

        verify(roomTypeRepository)
                .findById(100L);
    }

    /*
     * Verifies that invalid reservation
     * dates throw an exception.
     */
    @Test
    void checkAvailability_ShouldThrowException_WhenCheckInDateIsNotBeforeCheckOutDate() {

        AvailabilityRequestDTO request =
                new AvailabilityRequestDTO();

        request.setRoomTypeId(1L);
        request.setCheckInDate(LocalDate.now().plusDays(5));
        request.setCheckOutDate(LocalDate.now().plusDays(5));

        RoomType roomType = new RoomType();

        roomType.setRoomTypeId(1L);

        when(roomTypeRepository.findById(1L))
                .thenReturn(Optional.of(roomType));

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> availabilityService.checkAvailability(request));

        assertEquals(
                "Check-in date must be before check-out date",
                exception.getMessage());
    }

    /*
     * Verifies that active booking holds
     * reduce the available room count.
     */
    @Test
    void checkAvailability_ShouldConsiderActiveBookingHolds() {

        AvailabilityRequestDTO request =
                new AvailabilityRequestDTO();

        request.setRoomTypeId(1L);
        request.setCheckInDate(LocalDate.now().plusDays(1));
        request.setCheckOutDate(LocalDate.now().plusDays(4));

        RoomType roomType = new RoomType();

        roomType.setRoomTypeId(1L);
        roomType.setName("STANDARD");

        BookingHold hold = new BookingHold();

        hold.setRoomTypeId(1L);
        hold.setStatus(BookingHoldStatus.ACTIVE);
        hold.setCheckInDate(LocalDate.now().plusDays(2));
        hold.setCheckOutDate(LocalDate.now().plusDays(3));

        when(roomTypeRepository.findById(1L))
                .thenReturn(Optional.of(roomType));

        when(roomRepository.countByRoomType(roomType))
                .thenReturn(10L);

        when(reservationRepository.countOverlappingReservations(
                anyLong(),
                any(),
                any()))
                .thenReturn(2L);

        when(bookingHoldRepository.findAll())
                .thenReturn(List.of(hold));

        AvailabilityResponseDTO response =
                availabilityService.checkAvailability(request);

        assertEquals(1,
                response.getActiveHolds());

        assertEquals(7,
                response.getAvailableRooms());

        assertTrue(response.isAvailable());
    }

    /*
     * Verifies that the customer
     * response displays "Room Available"
     * when more than three rooms exist.
     */
    @Test
    void mapToCustomerResponse_ShouldReturnRoomAvailableMessage() {

        AvailabilityResponseDTO response =
                AvailabilityResponseDTO.builder()
                        .roomTypeId(1L)
                        .roomTypeName("STANDARD")
                        .availableRooms(5L)
                        .available(true)
                        .build();

        RoomType roomType = new RoomType();
        roomType.setRoomTypeId(1L);
        roomType.setCapacity(2);
        roomType.setPricePerNight(
                new BigDecimal("1800"));

        when(roomTypeRepository.findById(1L))
                .thenReturn(Optional.of(roomType));

        AvailabilityCustomerResponseDTO customerResponse =
                availabilityService.mapToCustomerResponse(response);

        assertEquals(
                "Room Available",
                customerResponse.getAvailabilityMessage());

        assertEquals(2,
                customerResponse.getCapacity());

        verify(roomTypeRepository).findById(1L);
    }

    /*
     * Verifies that the customer
     * response displays
     * "Only X rooms left"
     * when availability is low.
     */
    @Test
    void mapToCustomerResponse_ShouldReturnOnlyRoomsLeftMessage() {

        AvailabilityResponseDTO response =
                AvailabilityResponseDTO.builder()
                        .roomTypeId(1L)
                        .roomTypeName("STANDARD")
                        .availableRooms(3L)
                        .available(true)
                        .build();

        RoomType roomType = new RoomType();
        roomType.setRoomTypeId(1L);
        roomType.setCapacity(2);
        roomType.setPricePerNight(
                new BigDecimal("1800"));

        when(roomTypeRepository.findById(1L))
                .thenReturn(Optional.of(roomType));

        AvailabilityCustomerResponseDTO customerResponse =
                availabilityService.mapToCustomerResponse(response);

        assertEquals(
                "Only 3 rooms left",
                customerResponse.getAvailabilityMessage());

        verify(roomTypeRepository).findById(1L);
    }

    /*
     * Verifies that the customer
     * response displays
     * "Sold Out"
     * when no rooms are available.
     */
    @Test
    void mapToCustomerResponse_ShouldReturnSoldOutMessage() {

        AvailabilityResponseDTO response =
                AvailabilityResponseDTO.builder()
                        .roomTypeId(1L)
                        .roomTypeName("STANDARD")
                        .availableRooms(0L)
                        .available(false)
                        .build();

        RoomType roomType = new RoomType();
        roomType.setRoomTypeId(1L);
        roomType.setCapacity(2);
        roomType.setPricePerNight(
                new BigDecimal("1800"));

        when(roomTypeRepository.findById(1L))
                .thenReturn(Optional.of(roomType));

        AvailabilityCustomerResponseDTO customerResponse =
                availabilityService.mapToCustomerResponse(response);

        assertEquals(
                "Sold Out",
                customerResponse.getAvailabilityMessage());

        assertFalse(customerResponse.isAvailable());

        verify(roomTypeRepository).findById(1L);
    }

    /*
     * Verifies that mapping fails
     * when the room type
     * does not exist.
     */
    @Test
    void mapToCustomerResponse_ShouldThrowException_WhenRoomTypeDoesNotExist() {

        AvailabilityResponseDTO response =
                AvailabilityResponseDTO.builder()
                        .roomTypeId(100L)
                        .build();

        when(roomTypeRepository.findById(100L))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> availabilityService
                                .mapToCustomerResponse(response));

        assertEquals(
                "Room Type not found with id: 100",
                exception.getMessage());

        verify(roomTypeRepository).findById(100L);
    }

    /*
     * Verifies that available rooms
     * never become negative even
     * when bookings exceed inventory.
     */
    @Test
    void checkAvailability_ShouldNotReturnNegativeAvailableRooms() {

        AvailabilityRequestDTO request =
                new AvailabilityRequestDTO();

        request.setRoomTypeId(1L);
        request.setCheckInDate(LocalDate.now().plusDays(1));
        request.setCheckOutDate(LocalDate.now().plusDays(2));

        RoomType roomType = new RoomType();
        roomType.setRoomTypeId(1L);

        when(roomTypeRepository.findById(1L))
                .thenReturn(Optional.of(roomType));

        when(roomRepository.countByRoomType(roomType))
                .thenReturn(2L);

        when(reservationRepository.countOverlappingReservations(
                anyLong(),
                any(),
                any()))
                .thenReturn(5L);

        when(bookingHoldRepository.findAll())
                .thenReturn(List.of());

        AvailabilityResponseDTO response =
                availabilityService.checkAvailability(request);

        assertEquals(
                0,
                response.getAvailableRooms());

        assertFalse(response.isAvailable());
    }

    /*
     * Verifies that inactive booking
     * holds are ignored while
     * calculating availability.
     */
    @Test
    void checkAvailability_ShouldIgnoreInactiveBookingHolds() {

        AvailabilityRequestDTO request =
                new AvailabilityRequestDTO();

        request.setRoomTypeId(1L);
        request.setCheckInDate(LocalDate.now().plusDays(1));
        request.setCheckOutDate(LocalDate.now().plusDays(4));

        RoomType roomType = new RoomType();
        roomType.setRoomTypeId(1L);

        BookingHold hold = new BookingHold();

        hold.setRoomTypeId(1L);
        hold.setStatus(BookingHoldStatus.CANCELLED);

        hold.setCheckInDate(
                LocalDate.now().plusDays(2));

        hold.setCheckOutDate(
                LocalDate.now().plusDays(3));

        when(roomTypeRepository.findById(1L))
                .thenReturn(Optional.of(roomType));

        when(roomRepository.countByRoomType(roomType))
                .thenReturn(10L);

        when(reservationRepository.countOverlappingReservations(
                anyLong(),
                any(),
                any()))
                .thenReturn(2L);

        when(bookingHoldRepository.findAll())
                .thenReturn(List.of(hold));

        AvailabilityResponseDTO response =
                availabilityService.checkAvailability(request);

        assertEquals(
                0,
                response.getActiveHolds());

        assertEquals(
                8,
                response.getAvailableRooms());
    }

}
