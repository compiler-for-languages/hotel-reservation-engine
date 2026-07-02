package com.infotact.project1.service;

import com.infotact.project1.dto.request.*;
import com.infotact.project1.dto.response.AvailabilityResponseDTO;
import com.infotact.project1.dto.response.ReservationResponseDTO;
import com.infotact.project1.enums.PaymentMethod;
import com.infotact.project1.enums.ReservationStatus;
import com.infotact.project1.model.Reservation;
import com.infotact.project1.model.RoomType;
import com.infotact.project1.model.User;
import com.infotact.project1.repository.ReservationRepository;
import com.infotact.project1.repository.RoomTypeRepository;
import com.infotact.project1.repository.UserRepository;
import com.infotact.project1.service.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/*
 * Unit tests for ReservationService.
 *
 * Covers:
 * - Reservation creation
 * - Availability validation
 * - Lock acquisition
 * - Booking hold creation
 * - Payment creation
 * - CRUD operations
 *
 * External dependencies are mocked using Mockito.
 */

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoomTypeRepository roomTypeRepository;

    @Mock
    private AvailabilityService availabilityService;

    @Mock
    private LockService lockService;

    @Mock
    private BookingHoldService bookingHoldService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private RLock lock;

    @InjectMocks
    private ReservationService reservationService;

    /*
     * Verifies that a reservation
     * is created successfully.
     */
    @Test
    void createReservation_ShouldCreateReservationSuccessfully() {

        ReservationRequestDTO request =
                new ReservationRequestDTO();

        request.setUserId(1L);
        request.setRoomTypeId(1L);
        request.setCheckInDate(LocalDate.now().plusDays(1));
        request.setCheckOutDate(LocalDate.now().plusDays(3));
        request.setGuestCount(2);
        request.setSpecialRequest("Near Window");
        request.setPaymentMethod(PaymentMethod.UPI);

        User user = new User();
        user.setUserId(1L);
        user.setFirstName("Shrikanth");
        user.setLastName("Sanagoudar");

        RoomType roomType = new RoomType();
        roomType.setRoomTypeId(1L);
        roomType.setName("DELUXE");
        roomType.setCapacity(3);
        roomType.setPricePerNight(new BigDecimal("2500"));

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(roomTypeRepository.findById(1L))
                .thenReturn(Optional.of(roomType));

        when(lockService.acquireLock(anyString()))
                .thenReturn(lock);

        AvailabilityResponseDTO availability =
                AvailabilityResponseDTO.builder()
                        .available(true)
                        .availableRooms(5L)
                        .build();

        when(availabilityService.checkAvailability(any()))
                .thenReturn(availability);

        Reservation reservation = new Reservation();

        reservation.setReservationId(10L);
        reservation.setUser(user);
        reservation.setRoomType(roomType);
        reservation.setCheckInDate(request.getCheckInDate());
        reservation.setCheckOutDate(request.getCheckOutDate());
        reservation.setGuestCount(2);
        reservation.setReservationStatus(
                ReservationStatus.PENDING);
        reservation.setSpecialRequest(
                "Near Window");

        when(reservationRepository.save(any(Reservation.class)))
                .thenReturn(reservation);

        doNothing()
                .when(bookingHoldService)
                .createHold(any());

        doNothing()
                .when(paymentService)
                .createPayment(any());

        ReservationResponseDTO response =
                reservationService.createReservation(request);

        assertNotNull(response);

        assertEquals(
                10L,
                response.getReservationId());

        assertEquals(
                ReservationStatus.PENDING,
                response.getReservationStatus());

        assertEquals(
                "DELUXE",
                response.getRoomTypeName());

        verify(userRepository).findById(1L);
        verify(roomTypeRepository).findById(1L);

        verify(lockService)
                .acquireLock("roomType:1");

        verify(availabilityService)
                .checkAvailability(any());

        verify(reservationRepository)
                .save(any(Reservation.class));

        verify(bookingHoldService)
                .createHold(any());

        verify(paymentService)
                .createPayment(any());

        verify(lockService)
                .releaseLock(lock);
    }

    /*
     * Verifies that reservation creation
     * fails when the customer
     * does not exist.
     */
    @Test
    void createReservation_ShouldThrowException_WhenUserDoesNotExist() {

        ReservationRequestDTO request =
                new ReservationRequestDTO();

        request.setUserId(100L);

        when(userRepository.findById(100L))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> reservationService.createReservation(request));

        assertEquals(
                "User not found with id: 100",
                exception.getMessage());

        verify(userRepository).findById(100L);

        verify(lockService, never())
                .acquireLock(anyString());
    }

    /*
     * Verifies that reservation creation
     * fails when the room type
     * does not exist.
     */
    @Test
    void createReservation_ShouldThrowException_WhenRoomTypeDoesNotExist() {

        ReservationRequestDTO request =
                new ReservationRequestDTO();

        request.setUserId(1L);
        request.setRoomTypeId(100L);

        User user = new User();
        user.setUserId(1L);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(roomTypeRepository.findById(100L))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> reservationService.createReservation(request));

        assertEquals(
                "Room Type not found with id: 100",
                exception.getMessage());

        verify(roomTypeRepository).findById(100L);

        verify(lockService, never())
                .acquireLock(anyString());
    }

    /*
     * Verifies that reservation creation
     * fails when check-in and
     * check-out dates are invalid.
     */
    @Test
    void createReservation_ShouldThrowException_WhenDatesAreInvalid() {

        ReservationRequestDTO request =
                new ReservationRequestDTO();

        request.setUserId(1L);
        request.setRoomTypeId(1L);

        request.setCheckInDate(
                LocalDate.now().plusDays(5));

        request.setCheckOutDate(
                LocalDate.now().plusDays(5));

        User user = new User();
        user.setUserId(1L);

        RoomType roomType = new RoomType();
        roomType.setRoomTypeId(1L);
        roomType.setCapacity(3);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(roomTypeRepository.findById(1L))
                .thenReturn(Optional.of(roomType));

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> reservationService.createReservation(request));

        assertEquals(
                "Check-out date must be after check-in date",
                exception.getMessage());

        verify(lockService, never())
                .acquireLock(anyString());
    }

    /*
     * Verifies that reservation creation
     * fails when requested guests
     * exceed room capacity.
     */
    @Test
    void createReservation_ShouldThrowException_WhenRoomCapacityExceeded() {

        ReservationRequestDTO request =
                new ReservationRequestDTO();

        request.setUserId(1L);
        request.setRoomTypeId(1L);
        request.setGuestCount(5);

        request.setCheckInDate(
                LocalDate.now().plusDays(1));

        request.setCheckOutDate(
                LocalDate.now().plusDays(3));

        User user = new User();
        user.setUserId(1L);

        RoomType roomType = new RoomType();
        roomType.setRoomTypeId(1L);
        roomType.setCapacity(2);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(roomTypeRepository.findById(1L))
                .thenReturn(Optional.of(roomType));

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> reservationService.createReservation(request));

        assertEquals(
                "Room capacity exceeded.",
                exception.getMessage());

        verify(lockService, never())
                .acquireLock(anyString());
    }

    /*
     * Verifies that reservation creation
     * fails when no rooms
     * are available.
     */
    @Test
    void createReservation_ShouldThrowException_WhenRoomUnavailable() {

        ReservationRequestDTO request =
                new ReservationRequestDTO();

        request.setUserId(1L);
        request.setRoomTypeId(1L);
        request.setGuestCount(2);

        request.setCheckInDate(
                LocalDate.now().plusDays(1));

        request.setCheckOutDate(
                LocalDate.now().plusDays(3));

        User user = new User();
        user.setUserId(1L);

        RoomType roomType = new RoomType();
        roomType.setRoomTypeId(1L);
        roomType.setName("DELUXE");
        roomType.setCapacity(3);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(roomTypeRepository.findById(1L))
                .thenReturn(Optional.of(roomType));

        when(lockService.acquireLock(anyString()))
                .thenReturn(lock);

        AvailabilityResponseDTO availability =
                AvailabilityResponseDTO.builder()
                        .available(false)
                        .availableRooms(0L)
                        .build();

        when(availabilityService.checkAvailability(any()))
                .thenReturn(availability);

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> reservationService.createReservation(request));

        assertEquals(
                "No rooms available for room type: DELUXE",
                exception.getMessage());

        verify(lockService)
                .releaseLock(lock);
    }

    /*
     * Verifies that booking hold
     * is released when payment
     * creation fails.
     */
    @Test
    void createReservation_ShouldReleaseBookingHold_WhenPaymentCreationFails() {

        ReservationRequestDTO request =
                new ReservationRequestDTO();

        request.setUserId(1L);
        request.setRoomTypeId(1L);
        request.setGuestCount(2);
        request.setPaymentMethod(PaymentMethod.UPI);

        request.setCheckInDate(
                LocalDate.now().plusDays(1));

        request.setCheckOutDate(
                LocalDate.now().plusDays(3));

        User user = new User();
        user.setUserId(1L);

        RoomType roomType = new RoomType();
        roomType.setRoomTypeId(1L);
        roomType.setName("DELUXE");
        roomType.setCapacity(3);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(roomTypeRepository.findById(1L))
                .thenReturn(Optional.of(roomType));

        when(lockService.acquireLock(anyString()))
                .thenReturn(lock);

        when(availabilityService.checkAvailability(any()))
                .thenReturn(
                        AvailabilityResponseDTO.builder()
                                .available(true)
                                .availableRooms(5L)
                                .build());

        Reservation reservation =
                new Reservation();

        reservation.setReservationId(10L);
        reservation.setUser(user);
        reservation.setRoomType(roomType);
        reservation.setReservationStatus(
                ReservationStatus.PENDING);

        when(reservationRepository.save(any()))
                .thenReturn(reservation);

        doNothing()
                .when(bookingHoldService)
                .createHold(any());

        doThrow(new RuntimeException("Payment Failed"))
                .when(paymentService)
                .createPayment(any());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> reservationService.createReservation(request));

        assertEquals(
                "Payment Failed",
                exception.getMessage());

        verify(bookingHoldService)
                .releaseActiveHold(10L);

        verify(lockService)
                .releaseLock(lock);
    }

    /*
     * Verifies that all reservations
     * are returned successfully.
     */
    @Test
    void getAllReservations_ShouldReturnReservations() {

        User user = new User();
        user.setFirstName("Shrikanth");
        user.setLastName("Sanagoudar");

        RoomType roomType = new RoomType();
        roomType.setName("DELUXE");

        Reservation reservation = new Reservation();
        reservation.setReservationId(1L);
        reservation.setUser(user);
        reservation.setRoomType(roomType);
        reservation.setReservationStatus(
                ReservationStatus.PENDING);

        when(reservationRepository.findAll())
                .thenReturn(List.of(reservation));

        List<ReservationResponseDTO> response =
                reservationService.getAllReservations();

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(
                "Shrikanth Sanagoudar",
                response.get(0).getUserName());

        verify(reservationRepository).findAll();
    }

    /*
     * Verifies that an existing
     * reservation is returned
     * successfully.
     */
    @Test
    void getReservationById_ShouldReturnReservation() {

        User user = new User();
        user.setFirstName("Shrikanth");
        user.setLastName("Sanagoudar");

        RoomType roomType = new RoomType();
        roomType.setName("DELUXE");

        Reservation reservation = new Reservation();
        reservation.setReservationId(1L);
        reservation.setUser(user);
        reservation.setRoomType(roomType);
        reservation.setReservationStatus(
                ReservationStatus.PENDING);

        when(reservationRepository.findById(1L))
                .thenReturn(Optional.of(reservation));

        ReservationResponseDTO response =
                reservationService.getReservationById(1L);

        assertNotNull(response);
        assertEquals(
                1L,
                response.getReservationId());

        assertEquals(
                "DELUXE",
                response.getRoomTypeName());

        verify(reservationRepository).findById(1L);
    }

    /*
     * Verifies that requesting
     * a non-existing reservation
     * throws an exception.
     */
    @Test
    void getReservationById_ShouldThrowException_WhenReservationDoesNotExist() {

        when(reservationRepository.findById(100L))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> reservationService.getReservationById(100L));

        assertEquals(
                "Reservation not found with id: 100",
                exception.getMessage());

        verify(reservationRepository).findById(100L);
    }

    /*
     * Verifies that reservations
     * belonging to a customer
     * are returned successfully.
     */
    @Test
    void getReservationsByUser_ShouldReturnReservations() {

        User user = new User();
        user.setUserId(1L);
        user.setFirstName("Shrikanth");
        user.setLastName("Sanagoudar");

        RoomType roomType = new RoomType();
        roomType.setName("DELUXE");

        Reservation reservation = new Reservation();
        reservation.setReservationId(1L);
        reservation.setUser(user);
        reservation.setRoomType(roomType);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(reservationRepository.findByUser(user))
                .thenReturn(List.of(reservation));

        List<ReservationResponseDTO> response =
                reservationService.getReservationsByUser(1L);

        assertNotNull(response);
        assertEquals(1, response.size());

        assertEquals(
                "Shrikanth Sanagoudar",
                response.get(0).getUserName());

        verify(userRepository).findById(1L);

        verify(reservationRepository)
                .findByUser(user);
    }

    /*
     * Verifies that requesting
     * reservations for a non-existing
     * customer throws an exception.
     */
    @Test
    void getReservationsByUser_ShouldThrowException_WhenUserDoesNotExist() {

        when(userRepository.findById(100L))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> reservationService.getReservationsByUser(100L));

        assertEquals(
                "User not found with id: 100",
                exception.getMessage());

        verify(userRepository).findById(100L);
    }

    /*
     * Verifies that reservations
     * can be retrieved using
     * reservation status.
     */
    @Test
    void getReservationsByStatus_ShouldReturnReservations() {

        User user = new User();
        user.setFirstName("Shrikanth");
        user.setLastName("Sanagoudar");

        RoomType roomType = new RoomType();
        roomType.setName("DELUXE");

        Reservation reservation = new Reservation();
        reservation.setReservationId(1L);
        reservation.setUser(user);
        reservation.setRoomType(roomType);
        reservation.setReservationStatus(
                ReservationStatus.CONFIRMED);

        when(reservationRepository.findByReservationStatus(
                ReservationStatus.CONFIRMED))
                .thenReturn(List.of(reservation));

        List<ReservationResponseDTO> response =
                reservationService.getReservationsByStatus(
                        ReservationStatus.CONFIRMED);

        assertNotNull(response);

        assertEquals(1, response.size());

        assertEquals(
                ReservationStatus.CONFIRMED,
                response.get(0).getReservationStatus());

        verify(reservationRepository)
                .findByReservationStatus(
                        ReservationStatus.CONFIRMED);
    }

    /*
     * Verifies that reservation
     * details are updated
     * successfully.
     */
    @Test
    void updateReservation_ShouldUpdateReservation() {

        User user = new User();
        user.setFirstName("Shrikanth");
        user.setLastName("Sanagoudar");

        RoomType roomType = new RoomType();
        roomType.setName("DELUXE");

        Reservation reservation = new Reservation();

        reservation.setReservationId(1L);
        reservation.setUser(user);
        reservation.setRoomType(roomType);
        reservation.setReservationStatus(
                ReservationStatus.PENDING);
        reservation.setGuestCount(2);
        reservation.setSpecialRequest("None");

        ReservationPatchRequestDTO request =
                new ReservationPatchRequestDTO();

        request.setReservationStatus(
                ReservationStatus.CONFIRMED);

        request.setGuestCount(3);

        request.setSpecialRequest(
                "High Floor");

        when(reservationRepository.findById(1L))
                .thenReturn(Optional.of(reservation));

        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        ReservationResponseDTO response =
                reservationService.updateReservation(
                        1L,
                        request);

        assertEquals(
                ReservationStatus.CONFIRMED,
                response.getReservationStatus());

        assertEquals(
                3,
                response.getGuestCount());

        assertEquals(
                "High Floor",
                response.getSpecialRequest());

        verify(reservationRepository).findById(1L);
        verify(reservationRepository).save(reservation);
    }

    /*
     * Verifies that updating a
     * non-existing reservation
     * throws an exception.
     */
    @Test
    void updateReservation_ShouldThrowException_WhenReservationDoesNotExist() {

        ReservationPatchRequestDTO request =
                new ReservationPatchRequestDTO();

        when(reservationRepository.findById(100L))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> reservationService.updateReservation(
                                100L,
                                request));

        assertEquals(
                "Reservation not found with id: 100",
                exception.getMessage());

        verify(reservationRepository).findById(100L);

        verify(reservationRepository,
                never()).save(any());
    }

    /*
     * Verifies that an existing
     * reservation is deleted
     * successfully.
     */
    @Test
    void deleteReservation_ShouldDeleteReservation() {

        User user = new User();
        user.setFirstName("Shrikanth");
        user.setLastName("Sanagoudar");

        RoomType roomType = new RoomType();
        roomType.setName("DELUXE");

        Reservation reservation =
                new Reservation();

        reservation.setReservationId(1L);
        reservation.setUser(user);
        reservation.setRoomType(roomType);

        when(reservationRepository.findById(1L))
                .thenReturn(Optional.of(reservation));

        reservationService.deleteReservation(1L);

        verify(reservationRepository).findById(1L);
        verify(reservationRepository).delete(reservation);
    }

    /*
     * Verifies that deleting a
     * non-existing reservation
     * throws an exception.
     */
    @Test
    void deleteReservation_ShouldThrowException_WhenReservationDoesNotExist() {

        when(reservationRepository.findById(100L))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> reservationService.deleteReservation(100L));

        assertEquals(
                "Reservation not found with id: 100",
                exception.getMessage());

        verify(reservationRepository).findById(100L);

        verify(reservationRepository,
                never()).delete(any(Reservation.class));
    }

}





