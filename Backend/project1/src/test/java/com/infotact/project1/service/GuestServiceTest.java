package com.infotact.project1.service;

import com.infotact.project1.dto.request.GuestPatchRequestDTO;
import com.infotact.project1.dto.request.GuestRequestDTO;
import com.infotact.project1.dto.response.GuestResponseDTO;
import com.infotact.project1.enums.Gender;
import com.infotact.project1.model.Guest;
import com.infotact.project1.model.Reservation;
import com.infotact.project1.repository.GuestRepository;
import com.infotact.project1.repository.ReservationRepository;
import com.infotact.project1.service.GuestService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/*
 * Unit tests for GuestService.
 *
 * Covers:
 * - Guest creation
 * - Guest retrieval
 * - Guest update
 * - Guest deletion
 * - Guest validation
 *
 * External dependencies are mocked using Mockito.
 */

@ExtendWith(MockitoExtension.class)
class GuestServiceTest {

    @Mock
    private GuestRepository guestRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private GuestService guestService;

    /*
     * Verifies that a guest is created
     * successfully.
     */
    @Test
    void createGuest_ShouldCreateGuestSuccessfully() {

        GuestRequestDTO request = new GuestRequestDTO();

        request.setReservationId(1L);
        request.setFirstName("Rahul");
        request.setLastName("Patil");
        request.setPhone("9876543210");
        request.setGender(Gender.MALE);
        request.setDateOfBirth(
                LocalDate.of(2002, 5, 10));

        Reservation reservation = new Reservation();
        reservation.setReservationId(1L);
        reservation.setGuestCount(2);

        when(reservationRepository.findById(1L))
                .thenReturn(Optional.of(reservation));

        when(guestRepository.countByReservation(reservation))
                .thenReturn(1L);

        Guest savedGuest = new Guest();

        savedGuest.setGuestId(1L);
        savedGuest.setReservation(reservation);
        savedGuest.setFirstName("Rahul");
        savedGuest.setLastName("Patil");
        savedGuest.setPhone("9876543210");
        savedGuest.setGender(Gender.MALE);
        savedGuest.setDateOfBirth(
                LocalDate.of(2002, 5, 10));

        when(guestRepository.save(any(Guest.class)))
                .thenReturn(savedGuest);

        GuestResponseDTO response =
                guestService.createGuest(request);

        assertNotNull(response);

        assertEquals(1L,
                response.getGuestId());

        assertEquals("Rahul",
                response.getFirstName());

        assertEquals(1L,
                response.getReservationId());

        verify(reservationRepository)
                .findById(1L);

        verify(guestRepository)
                .countByReservation(reservation);

        verify(guestRepository)
                .save(any(Guest.class));
    }

    /*
     * Verifies that guest creation
     * fails when reservation does
     * not exist.
     */
    @Test
    void createGuest_ShouldThrowException_WhenReservationDoesNotExist() {

        GuestRequestDTO request =
                new GuestRequestDTO();

        request.setReservationId(100L);

        when(reservationRepository.findById(100L))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> guestService.createGuest(request));

        assertEquals(
                "Reservation not found with id: 100",
                exception.getMessage());

        verify(reservationRepository)
                .findById(100L);

        verify(guestRepository,
                never()).save(any());
    }

    /*
     * Verifies that guest creation
     * fails when maximum guest
     * limit has been reached.
     */
    @Test
    void createGuest_ShouldThrowException_WhenGuestLimitReached() {

        GuestRequestDTO request =
                new GuestRequestDTO();

        request.setReservationId(1L);

        Reservation reservation =
                new Reservation();

        reservation.setReservationId(1L);
        reservation.setGuestCount(2);

        when(reservationRepository.findById(1L))
                .thenReturn(Optional.of(reservation));

        when(guestRepository.countByReservation(reservation))
                .thenReturn(2L);

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> guestService.createGuest(request));

        assertEquals(
                "Maximum guest limit reached for this reservation.",
                exception.getMessage());

        verify(guestRepository,
                never()).save(any());
    }

    /*
     * Verifies that all guests
     * are returned successfully.
     */
    @Test
    void getAllGuests_ShouldReturnAllGuests() {

        Reservation reservation =
                new Reservation();

        reservation.setReservationId(1L);

        Guest guest1 = new Guest();

        guest1.setGuestId(1L);
        guest1.setFirstName("Rahul");
        guest1.setReservation(reservation);

        Guest guest2 = new Guest();

        guest2.setGuestId(2L);
        guest2.setFirstName("Amit");
        guest2.setReservation(reservation);

        when(guestRepository.findAll())
                .thenReturn(List.of(guest1, guest2));

        List<GuestResponseDTO> response =
                guestService.getAllGuests();

        assertEquals(2,
                response.size());

        verify(guestRepository)
                .findAll();
    }

    /*
     * Verifies that a guest is
     * retrieved successfully
     * using guest id.
     */
    @Test
    void getGuestById_ShouldReturnGuest() {

        Reservation reservation =
                new Reservation();

        reservation.setReservationId(1L);

        Guest guest =
                new Guest();

        guest.setGuestId(1L);
        guest.setReservation(reservation);
        guest.setFirstName("Rahul");
        guest.setLastName("Patil");

        when(guestRepository.findById(1L))
                .thenReturn(Optional.of(guest));

        GuestResponseDTO response =
                guestService.getGuestById(1L);

        assertEquals(1L,
                response.getGuestId());

        assertEquals("Rahul",
                response.getFirstName());

        verify(guestRepository)
                .findById(1L);
    }

    /*
     * Verifies that requesting a
     * non-existing guest throws
     * an exception.
     */
    @Test
    void getGuestById_ShouldThrowException_WhenGuestDoesNotExist() {

        when(guestRepository.findById(100L))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> guestService.getGuestById(100L));

        assertEquals(
                "Guest not found with id: 100",
                exception.getMessage());

        verify(guestRepository)
                .findById(100L);
    }

    /*
     * Verifies that all guests
     * belonging to a reservation
     * are returned successfully.
     */
    @Test
    void getGuestsByReservation_ShouldReturnGuests() {

        Reservation reservation = new Reservation();
        reservation.setReservationId(1L);

        Guest guest = new Guest();
        guest.setGuestId(1L);
        guest.setReservation(reservation);
        guest.setFirstName("Rahul");
        guest.setLastName("Patil");

        when(reservationRepository.findById(1L))
                .thenReturn(Optional.of(reservation));

        when(guestRepository.findByReservation(reservation))
                .thenReturn(List.of(guest));

        List<GuestResponseDTO> response =
                guestService.getGuestsByReservation(1L);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("Rahul",
                response.get(0).getFirstName());

        verify(reservationRepository).findById(1L);
        verify(guestRepository).findByReservation(reservation);
    }

    /*
     * Verifies that requesting guests
     * for a non-existing reservation
     * throws an exception.
     */
    @Test
    void getGuestsByReservation_ShouldThrowException_WhenReservationDoesNotExist() {

        when(reservationRepository.findById(100L))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> guestService.getGuestsByReservation(100L));

        assertEquals(
                "Reservation not found",
                exception.getMessage());

        verify(reservationRepository).findById(100L);
    }

    /*
     * Verifies that guest details
     * are updated successfully.
     */
    @Test
    void updateGuest_ShouldUpdateGuestSuccessfully() {

        Reservation reservation = new Reservation();
        reservation.setReservationId(1L);

        Guest guest = new Guest();
        guest.setGuestId(1L);
        guest.setReservation(reservation);
        guest.setFirstName("Old");
        guest.setLastName("Name");
        guest.setPhone("9999999999");

        GuestPatchRequestDTO request =
                new GuestPatchRequestDTO();

        request.setFirstName("Rahul");
        request.setLastName("Patil");
        request.setPhone("9876543210");
        request.setGender(Gender.MALE);
        request.setDateOfBirth(
                LocalDate.of(2002, 5, 10));

        when(guestRepository.findById(1L))
                .thenReturn(Optional.of(guest));

        when(guestRepository.save(any(Guest.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        GuestResponseDTO response =
                guestService.updateGuest(1L, request);

        assertEquals("Rahul",
                response.getFirstName());

        assertEquals("Patil",
                response.getLastName());

        assertEquals("9876543210",
                response.getPhone());

        verify(guestRepository).save(any(Guest.class));
    }

    /*
     * Verifies that updating a
     * non-existing guest throws
     * an exception.
     */
    @Test
    void updateGuest_ShouldThrowException_WhenGuestDoesNotExist() {

        GuestPatchRequestDTO request =
                new GuestPatchRequestDTO();

        when(guestRepository.findById(100L))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> guestService.updateGuest(
                                100L,
                                request));

        assertEquals(
                "Guest not found with id: 100",
                exception.getMessage());

        verify(guestRepository).findById(100L);
    }

    /*
     * Verifies that a guest
     * is deleted successfully.
     */
    @Test
    void deleteGuest_ShouldDeleteGuestSuccessfully() {

        Guest guest = new Guest();
        guest.setGuestId(1L);

        when(guestRepository.findById(1L))
                .thenReturn(Optional.of(guest));

        guestService.deleteGuest(1L);

        verify(guestRepository).findById(1L);
        verify(guestRepository).delete(guest);
    }

    /*
     * Verifies that deleting a
     * non-existing guest throws
     * an exception.
     */
    @Test
    void deleteGuest_ShouldThrowException_WhenGuestDoesNotExist() {

        when(guestRepository.findById(100L))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> guestService.deleteGuest(100L));

        assertEquals(
                "Guest not found with id: 100",
                exception.getMessage());

        verify(guestRepository).findById(100L);

        verify(guestRepository,
                never()).delete(any(Guest.class));
    }

    /*
     * Verifies that guest validation
     * succeeds when all guest details
     * have been entered.
     */
    @Test
    void validateGuestDetailsCompleted_ShouldPass() {

        Reservation reservation = new Reservation();

        reservation.setReservationId(1L);
        reservation.setGuestCount(2);

        when(reservationRepository.findById(1L))
                .thenReturn(Optional.of(reservation));

        when(guestRepository.countByReservation(reservation))
                .thenReturn(2L);

        assertDoesNotThrow(() ->
                guestService.validateGuestDetailsCompleted(1L));

        verify(reservationRepository).findById(1L);
        verify(guestRepository)
                .countByReservation(reservation);
    }

    /*
     * Verifies that guest validation
     * fails when guest details are
     * incomplete.
     */
    @Test
    void validateGuestDetailsCompleted_ShouldThrowException_WhenGuestDetailsIncomplete() {

        Reservation reservation = new Reservation();

        reservation.setReservationId(1L);
        reservation.setGuestCount(3);

        when(reservationRepository.findById(1L))
                .thenReturn(Optional.of(reservation));

        when(guestRepository.countByReservation(reservation))
                .thenReturn(2L);

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> guestService
                                .validateGuestDetailsCompleted(1L));

        assertEquals(
                "Please enter details for all guests before check-in.",
                exception.getMessage());

        verify(reservationRepository).findById(1L);
        verify(guestRepository)
                .countByReservation(reservation);
    }

}