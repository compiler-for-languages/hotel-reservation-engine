package com.infotact.project1.service;

import com.infotact.project1.dto.request.AssignRoomRequestDTO;
import com.infotact.project1.dto.response.*;
import com.infotact.project1.enums.AssignmentStatus;
import com.infotact.project1.repository.GuestRepository;
import com.infotact.project1.enums.ReservationStatus;
import com.infotact.project1.enums.Role;
import com.infotact.project1.enums.RoomStatus;
import com.infotact.project1.model.Reservation;
import com.infotact.project1.model.Room;
import com.infotact.project1.model.RoomAssignment;
import com.infotact.project1.model.User;
import com.infotact.project1.repository.RoomAssignmentRepository;
import com.infotact.project1.repository.ReservationRepository;
import com.infotact.project1.repository.RoomRepository;
import com.infotact.project1.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.Collections;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReceptionService {

    private final ReservationRepository reservationRepository;

    private final RoomRepository roomRepository;

    private final RoomAssignmentRepository roomAssignmentRepository;

    private final UserRepository userRepository;

    private final GuestRepository guestRepository;

    private final GuestService guestService; //dependency injected


    public RoomAssignmentResponseDTO assignRoom(
            AssignRoomRequestDTO requestDTO) {

        // Fetch reservation
        Reservation reservation =
                reservationRepository.findById(
                                requestDTO.getReservationId())
                        .orElseThrow(() ->
                                new RuntimeException("RESERVATION_NOT_FOUND"));

        if (reservation.getReservationStatus() == ReservationStatus.CHECKED_OUT) {
            throw new RuntimeException("RESERVATION_ALREADY_CHECKED_OUT");
        }

        // Reservation must be confirmed
        if (reservation.getReservationStatus()
                != ReservationStatus.CONFIRMED) {

            throw new RuntimeException("CHECKIN_NOT_ALLOWED");
        }

        // Prevent duplicate room assignment
        if (roomAssignmentRepository.existsByReservation(
                reservation)) {

            throw new RuntimeException("ROOM_ALREADY_ASSIGNED");
        }

        // Find available rooms of the reserved room type
        List<Room> availableRooms =
                roomRepository.findByRoomTypeAndRoomStatus(
                        reservation.getRoomType(),
                        RoomStatus.AVAILABLE);

        if (availableRooms.isEmpty()) {

            throw new RuntimeException("NO_AVAILABLE_ROOM");
        }

        // Random room allocation
        Collections.shuffle(availableRooms);

        Room selectedRoom = availableRooms.get(0);

        // Create room assignment
        RoomAssignment assignment =
                new RoomAssignment();

        assignment.setReservation(reservation);

        assignment.setRoom(selectedRoom);

        assignment.setAssignedAt(LocalDateTime.now());

        assignment.setStatus(
                AssignmentStatus.ASSIGNED);

        // Room is now reserved for this guest
        selectedRoom.setRoomStatus(
                RoomStatus.OCCUPIED);

        roomRepository.save(selectedRoom);

        RoomAssignment savedAssignment =
                roomAssignmentRepository.save(
                        assignment);

        return mapToResponse(savedAssignment);
    }



    public RoomAssignmentResponseDTO checkIn(
            AssignRoomRequestDTO requestDTO) {

        // Fetch reservation
        Reservation reservation =
                reservationRepository.findById(
                                requestDTO.getReservationId())
                        .orElseThrow(() ->
                                new RuntimeException("RESERVATION_NOT_FOUND"));

        if (reservation.getReservationStatus() == ReservationStatus.CHECKED_OUT) {
            throw new RuntimeException("RESERVATION_ALREADY_CHECKED_OUT");
        }

        // Reservation must be confirmed
        if (reservation.getReservationStatus()
                != ReservationStatus.CONFIRMED) {

            throw new RuntimeException("CHECKIN_NOT_ALLOWED");
        }

        // Fetch room assignment
        RoomAssignment assignment =
                roomAssignmentRepository
                        .findByReservation(reservation)
                        .orElseThrow(() ->
                                new RuntimeException("ROOM_NOT_ASSIGNED"));

        // Prevent duplicate check-in
        if (assignment.getStatus()
                != AssignmentStatus.ASSIGNED) {

            throw new RuntimeException("ALREADY_CHECKED_IN");
        }

        guestService.validateGuestDetailsCompleted(
                reservation.getReservationId());
        //Here we validate, whether the no. of guests whose details are entered is equal to the guestCount entered while reservation
//ensured before checking them in

        // Update assignment
        assignment.setStatus(
                AssignmentStatus.CHECKED_IN);

        assignment.setActualCheckIn(
                LocalDateTime.now());

        // Update reservation
        reservation.setReservationStatus(
                ReservationStatus.CHECKED_IN);

        reservationRepository.save(reservation);

        RoomAssignment updatedAssignment =
                roomAssignmentRepository.save(
                        assignment);

        return mapToResponse(updatedAssignment);
    }





    public RoomAssignmentResponseDTO checkOut(
            AssignRoomRequestDTO requestDTO) {

        // Fetch reservation
        Reservation reservation =
                reservationRepository.findById(
                                requestDTO.getReservationId())
                        .orElseThrow(() ->
                                new RuntimeException("RESERVATION_NOT_FOUND"));

        if (reservation.getReservationStatus() == ReservationStatus.CHECKED_OUT) {
            throw new RuntimeException("RESERVATION_ALREADY_CHECKED_OUT");
        }

        // Reservation must be checked in
        if (reservation.getReservationStatus()
                != ReservationStatus.CHECKED_IN) {

            throw new RuntimeException("CHECKOUT_NOT_ALLOWED");
        }

        // Fetch room assignment
        RoomAssignment assignment =
                roomAssignmentRepository
                        .findByReservation(reservation)
                        .orElseThrow(() ->
                                new RuntimeException("ROOM_NOT_ASSIGNED"));

        // Validate assignment status
        if (assignment.getStatus()
                != AssignmentStatus.CHECKED_IN) {

            throw new RuntimeException("NOT_CHECKED_IN");
        }

        // Record actual checkout time
        assignment.setActualCheckOut(
                LocalDateTime.now());

        // Update assignment status
        assignment.setStatus(
                AssignmentStatus.CHECKED_OUT);

        // Update reservation status
        reservation.setReservationStatus(
                ReservationStatus.CHECKED_OUT);

        // Release the room
        Room room = assignment.getRoom();

        room.setRoomStatus(
                RoomStatus.AVAILABLE);

        roomRepository.save(room);

        reservationRepository.save(reservation);

        RoomAssignment updatedAssignment =
                roomAssignmentRepository.save(
                        assignment);

        return mapToResponse(updatedAssignment);
    }

    private RoomAssignmentResponseDTO mapToResponse(
            RoomAssignment assignment) {

        return RoomAssignmentResponseDTO.builder()

                .assignmentId(
                        assignment.getAssignmentId())

                .reservationId(
                        assignment.getReservation()
                                .getReservationId())

                .customerName(
                        assignment.getReservation()
                                .getUser()
                                .getFirstName()
                                + " "
                                + assignment.getReservation()
                                .getUser()
                                .getLastName())

                .roomNumber(
                        assignment.getRoom()
                                .getRoomNumber())

                .roomType(
                        assignment.getRoom()
                                .getRoomType()
                                .getName())

                .checkInDate(
                        assignment.getReservation()
                                .getCheckInDate())

                .checkOutDate(
                        assignment.getReservation()
                                .getCheckOutDate())
                .actualCheckIn(
                        assignment.getActualCheckIn())

                .actualCheckOut(
                        assignment.getActualCheckOut())

                .assignedAt(
                        assignment.getAssignedAt())

                .assignmentStatus(
                        assignment.getStatus())

                .build();
    }


    //read and get_data operations, Display on the reception dashboard

    public List<TodayArrivalResponseDTO> getTodayArrivals() {

        List<Reservation> reservations =
                reservationRepository
                        .findByReservationStatusAndCheckInDate(
                                ReservationStatus.CONFIRMED,
                                LocalDate.now());

        return reservations.stream()

                .map(reservation -> {

                    Optional<RoomAssignment> assignment =
                            roomAssignmentRepository
                                    .findByReservation(reservation);

                    return TodayArrivalResponseDTO.builder()

                            .reservationId(
                                    reservation.getReservationId())

                            .customerName(
                                    reservation.getUser().getFirstName()
                                            + " "
                                            + reservation.getUser().getLastName())

                            .phone(
                                    reservation.getUser().getPhone())

                            .roomType(
                                    reservation.getRoomType().getName())

                            .guestCount(
                                    reservation.getGuestCount())

                            .guestNames(
                                    formatGuestNames(reservation))

                            .checkInDate(
                                    reservation.getCheckInDate())

                            .checkOutDate(
                                    reservation.getCheckOutDate())

                            .roomAssigned(
                                    assignment.isPresent())

                            .assignmentStatus(
                                    assignment
                                            .map(RoomAssignment::getStatus)
                                            .orElse(null))

                            .build();

                })

                .toList();
    }





    public List<CurrentGuestResponseDTO> getCurrentGuests() {

        List<RoomAssignment> assignments =
                roomAssignmentRepository.findByStatus(
                        AssignmentStatus.CHECKED_IN);

        return assignments.stream()

                .map(assignment -> {

                    Reservation reservation =
                            assignment.getReservation();

                    List<GuestInfoResponseDTO> guests =
                            guestRepository
                                    .findByReservation(
                                            reservation)

                                    .stream()

                                    .map(guest ->

                                            GuestInfoResponseDTO
                                                    .builder()

                                                    .guestId(
                                                            guest.getGuestId())

                                                    .firstName(
                                                            guest.getFirstName())

                                                    .lastName(
                                                            guest.getLastName())

                                                    .phone(
                                                            guest.getPhone())

                                                    .gender(
                                                            guest.getGender())

                                                    .dateOfBirth(
                                                            guest.getDateOfBirth())

                                                    .build()

                                    )

                                    .toList();

                    return CurrentGuestResponseDTO
                            .builder()

                            .reservationId(
                                    reservation.getReservationId())

                            .primaryCustomerName(
                                    reservation.getUser()
                                            .getFirstName()
                                            + " "
                                            +
                                            reservation.getUser()
                                                    .getLastName())

                            .roomNumber(
                                    assignment.getRoom()
                                            .getRoomNumber())

                            .roomType(
                                    assignment.getRoom()
                                            .getRoomType()
                                            .getName())

                            .checkInDate(
                                    reservation.getCheckInDate())

                            .checkOutDate(
                                    reservation.getCheckOutDate())

                            .actualCheckIn(
                                    assignment.getActualCheckIn())

                            .guests(
                                    guests)

                            .build();

                })

                .toList();
    }



    public List<TodayDepartureResponseDTO> getTodayDepartures() {

        List<Reservation> reservations =
                reservationRepository
                        .findByReservationStatusAndCheckOutDate(
                                ReservationStatus.CHECKED_IN,
                                LocalDate.now());

        return reservations.stream()

                .map(reservation -> {

                    RoomAssignment assignment =
                            roomAssignmentRepository
                                    .findByReservation(reservation)
                                    .orElseThrow(() ->
                                            new RuntimeException(
                                                    "Room assignment not found"));

                    return TodayDepartureResponseDTO.builder()

                            .reservationId(
                                    reservation.getReservationId())

                            .customerName(
                                    reservation.getUser().getFirstName()
                                            + " "
                                            + reservation.getUser().getLastName())

                            .roomNumber(
                                    assignment.getRoom().getRoomNumber())

                            .roomType(
                                    assignment.getRoom()
                                            .getRoomType()
                                            .getName())

                            .checkOutDate(
                                    reservation.getCheckOutDate())

                            .actualCheckIn(
                                    assignment.getActualCheckIn())

                            .guestCount(
                                    reservation.getGuestCount())

                            .guestNames(
                                    formatGuestNames(reservation))

                            .build();

                })

                .toList();
    }



    public ReceptionDashboardResponseDTO getDashboard() {

        long todayArrivals =
                reservationRepository
                        .findByReservationStatusAndCheckInDate(
                                ReservationStatus.CONFIRMED,
                                LocalDate.now())
                        .size();

        long todayDepartures =
                reservationRepository
                        .findByReservationStatusAndCheckOutDate(
                                ReservationStatus.CHECKED_IN,
                                LocalDate.now())
                        .size();

        long currentGuests =
                roomAssignmentRepository
                        .findByStatus(
                                AssignmentStatus.CHECKED_IN)
                        .size();

        long availableRooms =
                roomRepository.countByRoomStatus(
                        RoomStatus.AVAILABLE);

        long occupiedRooms =
                roomRepository.countByRoomStatus(
                        RoomStatus.OCCUPIED);

        return ReceptionDashboardResponseDTO
                .builder()

                .todayArrivals(
                        todayArrivals)

                .todayDepartures(
                        todayDepartures)

                .currentGuests(
                        currentGuests)

                .availableRooms(
                        availableRooms)

                .occupiedRooms(
                        occupiedRooms)

                .build();
    }

    private String formatGuestNames(Reservation reservation) {

        return guestRepository.findByReservation(reservation)
                .stream()
                .map(guest -> guest.getFirstName() + " " + guest.getLastName())
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
    }

}