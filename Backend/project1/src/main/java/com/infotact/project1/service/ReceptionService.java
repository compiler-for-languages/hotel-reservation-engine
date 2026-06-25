package com.infotact.project1.service;

import com.infotact.project1.dto.request.AssignRoomRequestDTO;
import com.infotact.project1.dto.response.RoomAssignmentResponseDTO;
import com.infotact.project1.enums.AssignmentStatus;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReceptionService {

    private final ReservationRepository reservationRepository;

    private final RoomRepository roomRepository;

    private final RoomAssignmentRepository roomAssignmentRepository;

    private final UserRepository userRepository;


    public RoomAssignmentResponseDTO assignRoom(
            AssignRoomRequestDTO requestDTO) {

        // Fetch reservation
        Reservation reservation =
                reservationRepository.findById(
                                requestDTO.getReservationId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Reservation not found"));

        // Reservation must be confirmed
        if (reservation.getReservationStatus()
                != ReservationStatus.CONFIRMED) {

            throw new RuntimeException(
                    "Only confirmed reservations can be assigned a room");
        }

        // Prevent duplicate room assignment
        if (roomAssignmentRepository.existsByReservation(
                reservation)) {

            throw new RuntimeException(
                    "Room has already been assigned");
        }

        // Find available rooms of the reserved room type
        List<Room> availableRooms =
                roomRepository.findByRoomTypeAndRoomStatus(
                        reservation.getRoomType(),
                        RoomStatus.AVAILABLE);

        if (availableRooms.isEmpty()) {

            throw new RuntimeException(
                    "No available rooms found");
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
                                new RuntimeException(
                                        "Reservation not found"));

        // Reservation must be confirmed
        if (reservation.getReservationStatus()
                != ReservationStatus.CONFIRMED) {

            throw new RuntimeException(
                    "Reservation is not eligible for check-in");
        }

        // Fetch room assignment
        RoomAssignment assignment =
                roomAssignmentRepository
                        .findByReservation(reservation)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Room has not been assigned"));

        // Prevent duplicate check-in
        if (assignment.getStatus()
                != AssignmentStatus.ASSIGNED) {

            throw new RuntimeException(
                    "Guest has already checked in");
        }

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
                                new RuntimeException(
                                        "Reservation not found"));

        // Reservation must be checked in
        if (reservation.getReservationStatus()
                != ReservationStatus.CHECKED_IN) {

            throw new RuntimeException(
                    "Guest is not checked in");
        }

        // Fetch room assignment
        RoomAssignment assignment =
                roomAssignmentRepository
                        .findByReservation(reservation)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Room assignment not found"));

        // Validate assignment status
        if (assignment.getStatus()
                != AssignmentStatus.CHECKED_IN) {

            throw new RuntimeException(
                    "Guest has not checked in");
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










}