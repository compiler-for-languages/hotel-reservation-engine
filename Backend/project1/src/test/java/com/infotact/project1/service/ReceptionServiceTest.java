package com.infotact.project1.service;

import com.infotact.project1.dto.request.AssignRoomRequestDTO;
import com.infotact.project1.dto.response.*;
import com.infotact.project1.enums.AssignmentStatus;
import com.infotact.project1.enums.ReservationStatus;
import com.infotact.project1.enums.RoomStatus;
import com.infotact.project1.model.*;
import com.infotact.project1.repository.*;
import com.infotact.project1.service.GuestService;
import com.infotact.project1.service.ReceptionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/*
 * Unit tests for ReceptionService.
 *
 * Covers:
 * - Room assignment
 * - Guest check-in
 * - Guest check-out
 * - Reception dashboard
 *
 * External dependencies are mocked using Mockito.
 */

@ExtendWith(MockitoExtension.class)
class ReceptionServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RoomAssignmentRepository roomAssignmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GuestRepository guestRepository;

    @Mock
    private GuestService guestService;

    @InjectMocks
    private ReceptionService receptionService;

    /*
     * Verifies that an available room
     * is assigned successfully to
     * a confirmed reservation.
     */
    @Test
    void assignRoom_ShouldAssignRoomSuccessfully() {

        AssignRoomRequestDTO request =
                new AssignRoomRequestDTO();

        request.setReservationId(1L);

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
        reservation.setReservationStatus(
                ReservationStatus.CONFIRMED);

        Room room = new Room();

        room.setRoomId(1L);
        room.setRoomNumber("101");
        room.setRoomType(roomType);
        room.setRoomStatus(RoomStatus.AVAILABLE);

        when(reservationRepository.findById(1L))
                .thenReturn(Optional.of(reservation));

        when(roomAssignmentRepository.existsByReservation(
                reservation))
                .thenReturn(false);

        when(roomRepository.findByRoomTypeAndRoomStatus(
                roomType,
                RoomStatus.AVAILABLE))
                .thenReturn(List.of(room));

        RoomAssignment assignment =
                new RoomAssignment();

        assignment.setAssignmentId(1L);
        assignment.setReservation(reservation);
        assignment.setRoom(room);
        assignment.setStatus(
                AssignmentStatus.ASSIGNED);

        when(roomRepository.save(any(Room.class)))
                .thenReturn(room);

        when(roomAssignmentRepository.save(any(RoomAssignment.class)))
                .thenReturn(assignment);

        RoomAssignmentResponseDTO response =
                receptionService.assignRoom(request);

        assertNotNull(response);

        assertEquals(
                1L,
                response.getAssignmentId());

        assertEquals(
                "101",
                response.getRoomNumber());

        assertEquals(
                AssignmentStatus.ASSIGNED,
                response.getAssignmentStatus());

        assertEquals(
                RoomStatus.OCCUPIED,
                room.getRoomStatus());

        verify(roomRepository).save(room);

        verify(roomAssignmentRepository)
                .save(any(RoomAssignment.class));
    }

    /*
     * Verifies that assigning a room
     * fails when the reservation
     * does not exist.
     */
    @Test
    void assignRoom_ShouldThrowException_WhenReservationDoesNotExist() {

        AssignRoomRequestDTO request =
                new AssignRoomRequestDTO();

        request.setReservationId(100L);

        when(reservationRepository.findById(100L))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> receptionService.assignRoom(request));

        assertEquals(
                "Reservation not found",
                exception.getMessage());

        verify(reservationRepository)
                .findById(100L);
    }

    /*
     * Verifies that only confirmed
     * reservations can be
     * assigned a room.
     */
    @Test
    void assignRoom_ShouldThrowException_WhenReservationNotConfirmed() {

        AssignRoomRequestDTO request =
                new AssignRoomRequestDTO();

        request.setReservationId(1L);

        Reservation reservation =
                new Reservation();

        reservation.setReservationStatus(
                ReservationStatus.PENDING);

        when(reservationRepository.findById(1L))
                .thenReturn(Optional.of(reservation));

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> receptionService.assignRoom(request));

        assertEquals(
                "Only confirmed reservations can be assigned a room",
                exception.getMessage());
    }

    /*
     * Verifies that duplicate
     * room assignment is
     * prevented.
     */
    @Test
    void assignRoom_ShouldThrowException_WhenRoomAlreadyAssigned() {

        AssignRoomRequestDTO request =
                new AssignRoomRequestDTO();

        request.setReservationId(1L);

        Reservation reservation =
                new Reservation();

        reservation.setReservationStatus(
                ReservationStatus.CONFIRMED);

        when(reservationRepository.findById(1L))
                .thenReturn(Optional.of(reservation));

        when(roomAssignmentRepository.existsByReservation(
                reservation))
                .thenReturn(true);

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> receptionService.assignRoom(request));

        assertEquals(
                "Room has already been assigned",
                exception.getMessage());
    }

    /*
     * Verifies that room assignment
     * fails when no rooms are
     * available.
     */
    @Test
    void assignRoom_ShouldThrowException_WhenNoRoomsAvailable() {

        AssignRoomRequestDTO request =
                new AssignRoomRequestDTO();

        request.setReservationId(1L);

        RoomType roomType =
                new RoomType();

        Reservation reservation =
                new Reservation();

        reservation.setReservationStatus(
                ReservationStatus.CONFIRMED);

        reservation.setRoomType(roomType);

        when(reservationRepository.findById(1L))
                .thenReturn(Optional.of(reservation));

        when(roomAssignmentRepository.existsByReservation(
                reservation))
                .thenReturn(false);

        when(roomRepository.findByRoomTypeAndRoomStatus(
                roomType,
                RoomStatus.AVAILABLE))
                .thenReturn(List.of());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> receptionService.assignRoom(request));

        assertEquals(
                "No available rooms found",
                exception.getMessage());
    }

    /*
     * Verifies that a confirmed
     * reservation is checked in
     * successfully.
     */
    @Test
    void checkIn_ShouldCheckInSuccessfully() {

        AssignRoomRequestDTO request =
                new AssignRoomRequestDTO();

        request.setReservationId(1L);

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
        reservation.setReservationStatus(
                ReservationStatus.CONFIRMED);

        Room room = new Room();
        room.setRoomNumber("101");
        room.setRoomType(roomType);

        RoomAssignment assignment =
                new RoomAssignment();

        assignment.setAssignmentId(1L);
        assignment.setReservation(reservation);
        assignment.setRoom(room);
        assignment.setStatus(
                AssignmentStatus.ASSIGNED);

        when(reservationRepository.findById(1L))
                .thenReturn(Optional.of(reservation));

        when(roomAssignmentRepository.findByReservation(
                reservation))
                .thenReturn(Optional.of(assignment));

        doNothing()
                .when(guestService)
                .validateGuestDetailsCompleted(1L);

        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        when(roomAssignmentRepository.save(any(RoomAssignment.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        RoomAssignmentResponseDTO response =
                receptionService.checkIn(request);

        assertNotNull(response);

        assertEquals(
                AssignmentStatus.CHECKED_IN,
                response.getAssignmentStatus());

        assertEquals(
                ReservationStatus.CHECKED_IN,
                reservation.getReservationStatus());

        assertNotNull(
                assignment.getActualCheckIn());

        verify(guestService)
                .validateGuestDetailsCompleted(1L);

        verify(reservationRepository)
                .save(reservation);

        verify(roomAssignmentRepository)
                .save(assignment);
    }

    /*
     * Verifies that check-in
     * fails when the reservation
     * does not exist.
     */
    @Test
    void checkIn_ShouldThrowException_WhenReservationNotFound() {

        AssignRoomRequestDTO request =
                new AssignRoomRequestDTO();

        request.setReservationId(100L);

        when(reservationRepository.findById(100L))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> receptionService.checkIn(request));

        assertEquals(
                "Reservation not found",
                exception.getMessage());

        verify(reservationRepository)
                .findById(100L);
    }

    /*
     * Verifies that only confirmed
     * reservations are eligible
     * for check-in.
     */
    @Test
    void checkIn_ShouldThrowException_WhenReservationNotConfirmed() {

        AssignRoomRequestDTO request =
                new AssignRoomRequestDTO();

        request.setReservationId(1L);

        Reservation reservation =
                new Reservation();

        reservation.setReservationStatus(
                ReservationStatus.PENDING);

        when(reservationRepository.findById(1L))
                .thenReturn(Optional.of(reservation));

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> receptionService.checkIn(request));

        assertEquals(
                "Reservation is not eligible for check-in",
                exception.getMessage());
    }

    /*
     * Verifies that check-in
     * fails when no room
     * assignment exists.
     */
    @Test
    void checkIn_ShouldThrowException_WhenRoomNotAssigned() {

        AssignRoomRequestDTO request =
                new AssignRoomRequestDTO();

        request.setReservationId(1L);

        Reservation reservation =
                new Reservation();

        reservation.setReservationStatus(
                ReservationStatus.CONFIRMED);

        when(reservationRepository.findById(1L))
                .thenReturn(Optional.of(reservation));

        when(roomAssignmentRepository.findByReservation(
                reservation))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> receptionService.checkIn(request));

        assertEquals(
                "Room has not been assigned",
                exception.getMessage());
    }

    /*
     * Verifies that duplicate
     * check-in is prevented.
     */
    @Test
    void checkIn_ShouldThrowException_WhenAlreadyCheckedIn() {

        AssignRoomRequestDTO request =
                new AssignRoomRequestDTO();

        request.setReservationId(1L);

        Reservation reservation =
                new Reservation();

        reservation.setReservationStatus(
                ReservationStatus.CONFIRMED);

        RoomAssignment assignment =
                new RoomAssignment();

        assignment.setStatus(
                AssignmentStatus.CHECKED_IN);

        when(reservationRepository.findById(1L))
                .thenReturn(Optional.of(reservation));

        when(roomAssignmentRepository.findByReservation(
                reservation))
                .thenReturn(Optional.of(assignment));

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> receptionService.checkIn(request));

        assertEquals(
                "Guest has already checked in",
                exception.getMessage());
    }

    /*
     * Verifies that a checked-in
     * guest can be checked out
     * successfully.
     */
    @Test
    void checkOut_ShouldCheckOutSuccessfully() {

        AssignRoomRequestDTO request =
                new AssignRoomRequestDTO();

        request.setReservationId(1L);

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
        reservation.setReservationStatus(
                ReservationStatus.CHECKED_IN);

        Room room = new Room();

        room.setRoomId(1L);
        room.setRoomNumber("101");
        room.setRoomType(roomType);
        room.setRoomStatus(RoomStatus.OCCUPIED);

        RoomAssignment assignment =
                new RoomAssignment();

        assignment.setAssignmentId(1L);
        assignment.setReservation(reservation);
        assignment.setRoom(room);
        assignment.setStatus(
                AssignmentStatus.CHECKED_IN);

        when(reservationRepository.findById(1L))
                .thenReturn(Optional.of(reservation));

        when(roomAssignmentRepository.findByReservation(
                reservation))
                .thenReturn(Optional.of(assignment));

        when(roomRepository.save(any(Room.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        when(roomAssignmentRepository.save(any(RoomAssignment.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        RoomAssignmentResponseDTO response =
                receptionService.checkOut(request);

        assertNotNull(response);

        assertEquals(
                AssignmentStatus.CHECKED_OUT,
                response.getAssignmentStatus());

        assertEquals(
                ReservationStatus.CHECKED_OUT,
                reservation.getReservationStatus());

        assertEquals(
                RoomStatus.AVAILABLE,
                room.getRoomStatus());

        assertNotNull(
                assignment.getActualCheckOut());

        verify(roomRepository).save(room);

        verify(reservationRepository)
                .save(reservation);

        verify(roomAssignmentRepository)
                .save(assignment);
    }

    /*
     * Verifies that check-out
     * fails when the reservation
     * does not exist.
     */
    @Test
    void checkOut_ShouldThrowException_WhenReservationNotFound() {

        AssignRoomRequestDTO request =
                new AssignRoomRequestDTO();

        request.setReservationId(100L);

        when(reservationRepository.findById(100L))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> receptionService.checkOut(request));

        assertEquals(
                "Reservation not found",
                exception.getMessage());

        verify(reservationRepository)
                .findById(100L);
    }

    /*
     * Verifies that only
     * checked-in reservations
     * can be checked out.
     */
    @Test
    void checkOut_ShouldThrowException_WhenReservationNotCheckedIn() {

        AssignRoomRequestDTO request =
                new AssignRoomRequestDTO();

        request.setReservationId(1L);

        Reservation reservation =
                new Reservation();

        reservation.setReservationStatus(
                ReservationStatus.CONFIRMED);

        when(reservationRepository.findById(1L))
                .thenReturn(Optional.of(reservation));

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> receptionService.checkOut(request));

        assertEquals(
                "Guest is not checked in",
                exception.getMessage());
    }

    /*
     * Verifies that check-out
     * fails when room assignment
     * does not exist.
     */
    @Test
    void checkOut_ShouldThrowException_WhenRoomAssignmentNotFound() {

        AssignRoomRequestDTO request =
                new AssignRoomRequestDTO();

        request.setReservationId(1L);

        Reservation reservation =
                new Reservation();

        reservation.setReservationStatus(
                ReservationStatus.CHECKED_IN);

        when(reservationRepository.findById(1L))
                .thenReturn(Optional.of(reservation));

        when(roomAssignmentRepository.findByReservation(
                reservation))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> receptionService.checkOut(request));

        assertEquals(
                "Room assignment not found",
                exception.getMessage());
    }

    /*
     * Verifies that only
     * checked-in assignments
     * can be checked out.
     */
    @Test
    void checkOut_ShouldThrowException_WhenAssignmentStatusInvalid() {

        AssignRoomRequestDTO request =
                new AssignRoomRequestDTO();

        request.setReservationId(1L);

        Reservation reservation =
                new Reservation();

        reservation.setReservationStatus(
                ReservationStatus.CHECKED_IN);

        RoomAssignment assignment =
                new RoomAssignment();

        assignment.setStatus(
                AssignmentStatus.ASSIGNED);

        when(reservationRepository.findById(1L))
                .thenReturn(Optional.of(reservation));

        when(roomAssignmentRepository.findByReservation(
                reservation))
                .thenReturn(Optional.of(assignment));

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> receptionService.checkOut(request));

        assertEquals(
                "Guest has not checked in",
                exception.getMessage());
    }

    /*
     * Verifies that today's
     * confirmed arrivals are
     * returned successfully.
     */
    @Test
    void getTodayArrivals_ShouldReturnArrivals() {

        User user = new User();
        user.setFirstName("Shrikanth");
        user.setLastName("Sanagoudar");
        user.setPhone("8861150224");

        RoomType roomType = new RoomType();
        roomType.setName("DELUXE");

        Reservation reservation = new Reservation();

        reservation.setReservationId(1L);
        reservation.setUser(user);
        reservation.setRoomType(roomType);
        reservation.setGuestCount(2);
        reservation.setCheckInDate(LocalDate.now());
        reservation.setCheckOutDate(LocalDate.now().plusDays(2));

        RoomAssignment assignment =
                new RoomAssignment();

        assignment.setStatus(
                AssignmentStatus.ASSIGNED);

        when(reservationRepository
                .findByReservationStatusAndCheckInDate(
                        ReservationStatus.CONFIRMED,
                        LocalDate.now()))
                .thenReturn(List.of(reservation));

        when(roomAssignmentRepository.findByReservation(
                reservation))
                .thenReturn(Optional.of(assignment));

        List<TodayArrivalResponseDTO> response =
                receptionService.getTodayArrivals();

        assertNotNull(response);

        assertEquals(1, response.size());

        assertEquals(
                "Shrikanth Sanagoudar",
                response.get(0).getCustomerName());

        assertTrue(
                response.get(0).isRoomAssigned());
    }

    /*
     * Verifies that currently
     * checked-in guests are
     * returned successfully.
     */
    @Test
    void getCurrentGuests_ShouldReturnGuests() {

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
        reservation.setCheckInDate(LocalDate.now());
        reservation.setCheckOutDate(LocalDate.now().plusDays(2));

        Room room = new Room();
        room.setRoomNumber("101");
        room.setRoomType(roomType);

        RoomAssignment assignment =
                new RoomAssignment();

        assignment.setReservation(reservation);
        assignment.setRoom(room);
        assignment.setStatus(
                AssignmentStatus.CHECKED_IN);

        assignment.setActualCheckIn(
                LocalDateTime.now());

        Guest guest = new Guest();

        guest.setGuestId(1L);
        guest.setFirstName("Rahul");
        guest.setLastName("Kumar");

        when(roomAssignmentRepository.findByStatus(
                AssignmentStatus.CHECKED_IN))
                .thenReturn(List.of(assignment));

        when(guestRepository.findByReservation(
                reservation))
                .thenReturn(List.of(guest));

        List<CurrentGuestResponseDTO> response =
                receptionService.getCurrentGuests();

        assertNotNull(response);

        assertEquals(1, response.size());

        assertEquals(
                "101",
                response.get(0).getRoomNumber());

        assertEquals(
                1,
                response.get(0).getGuests().size());
    }

    /*
     * Verifies that today's
     * departures are returned
     * successfully.
     */
    @Test
    void getTodayDepartures_ShouldReturnDepartures() {

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
        reservation.setCheckOutDate(LocalDate.now());

        Room room = new Room();
        room.setRoomNumber("101");
        room.setRoomType(roomType);

        RoomAssignment assignment =
                new RoomAssignment();

        assignment.setReservation(reservation);
        assignment.setRoom(room);
        assignment.setStatus(
                AssignmentStatus.CHECKED_IN);

        assignment.setActualCheckIn(
                LocalDateTime.now());

        when(reservationRepository
                .findByReservationStatusAndCheckOutDate(
                        ReservationStatus.CHECKED_IN,
                        LocalDate.now()))
                .thenReturn(List.of(reservation));

        when(roomAssignmentRepository.findByReservation(
                reservation))
                .thenReturn(Optional.of(assignment));

        List<TodayDepartureResponseDTO> response =
                receptionService.getTodayDepartures();

        assertNotNull(response);

        assertEquals(
                1,
                response.size());

        assertEquals(
                "101",
                response.get(0).getRoomNumber());
    }

    /*
     * Verifies that reception
     * dashboard statistics are
     * calculated correctly.
     */
    @Test
    void getDashboard_ShouldReturnDashboardSummary() {

        when(reservationRepository
                .findByReservationStatusAndCheckInDate(
                        ReservationStatus.CONFIRMED,
                        LocalDate.now()))
                .thenReturn(List.of(new Reservation()));

        when(reservationRepository
                .findByReservationStatusAndCheckOutDate(
                        ReservationStatus.CHECKED_IN,
                        LocalDate.now()))
                .thenReturn(List.of(new Reservation()));

        when(roomAssignmentRepository.findByStatus(
                AssignmentStatus.CHECKED_IN))
                .thenReturn(List.of(
                        new RoomAssignment(),
                        new RoomAssignment()));

        when(roomRepository.countByRoomStatus(
                RoomStatus.AVAILABLE))
                .thenReturn(20L);

        when(roomRepository.countByRoomStatus(
                RoomStatus.OCCUPIED))
                .thenReturn(15L);

        ReceptionDashboardResponseDTO response =
                receptionService.getDashboard();

        assertNotNull(response);

        assertEquals(
                1,
                response.getTodayArrivals());

        assertEquals(
                1,
                response.getTodayDepartures());

        assertEquals(
                2,
                response.getCurrentGuests());

        assertEquals(
                20,
                response.getAvailableRooms());

        assertEquals(
                15,
                response.getOccupiedRooms());
    }

}

