package com.infotact.project1.service;

import com.infotact.project1.dto.request.BookingHoldRequestDTO;
import com.infotact.project1.dto.response.BookingHoldResponseDTO;
import com.infotact.project1.enums.BookingHoldStatus;
import com.infotact.project1.model.BookingHold;
import com.infotact.project1.model.RoomType;
import com.infotact.project1.model.User;
import com.infotact.project1.repository.BookingHoldRepository;
import com.infotact.project1.repository.RoomTypeRepository;
import com.infotact.project1.repository.UserRepository;
import com.infotact.project1.service.BookingHoldService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/*
 * Unit tests for BookingHoldService.
 *
 * Covers:
 * - Booking hold creation
 * - Hold retrieval
 * - Hold cancellation
 * - Hold release
 *
 * External dependencies are mocked using Mockito.
 */

@ExtendWith(MockitoExtension.class)
class BookingHoldServiceTest {

    @Mock
    private BookingHoldRepository bookingHoldRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoomTypeRepository roomTypeRepository;

    @InjectMocks
    private BookingHoldService bookingHoldService;

    /*
     * Verifies that a booking hold
     * is created successfully.
     */
    @Test
    void createHold_ShouldCreateHoldSuccessfully() {

        BookingHoldRequestDTO request =
                new BookingHoldRequestDTO();

        request.setUserId(1L);
        request.setRoomTypeId(1L);
        request.setReservationId(10L);
        request.setCheckInDate(LocalDate.now().plusDays(1));
        request.setCheckOutDate(LocalDate.now().plusDays(3));

        User user = new User();
        user.setUserId(1L);

        RoomType roomType = new RoomType();
        roomType.setRoomTypeId(1L);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(roomTypeRepository.findById(1L))
                .thenReturn(Optional.of(roomType));

        BookingHold hold = new BookingHold();

        hold.setHoldId("10");
        hold.setUserId(1L);
        hold.setRoomTypeId(1L);
        hold.setReservationId(10L);
        hold.setCheckInDate(request.getCheckInDate());
        hold.setCheckOutDate(request.getCheckOutDate());
        hold.setStatus(BookingHoldStatus.ACTIVE);
        hold.setCreatedAt(LocalDateTime.now());
        hold.setExpiresAt(LocalDateTime.now().plusMinutes(5));

        when(bookingHoldRepository.save(any(BookingHold.class)))
                .thenReturn(hold);

        BookingHoldResponseDTO response =
                bookingHoldService.createHold(request);

        assertNotNull(response);

        assertEquals("10",
                response.getHoldId());

        assertEquals(1L,
                response.getUserId());

        assertEquals(BookingHoldStatus.ACTIVE,
                response.getStatus());

        verify(userRepository).findById(1L);
        verify(roomTypeRepository).findById(1L);
        verify(bookingHoldRepository).save(any(BookingHold.class));
    }

    /*
     * Verifies that hold creation
     * fails when the user
     * does not exist.
     */
    @Test
    void createHold_ShouldThrowException_WhenUserDoesNotExist() {

        BookingHoldRequestDTO request =
                new BookingHoldRequestDTO();

        request.setUserId(100L);

        when(userRepository.findById(100L))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> bookingHoldService.createHold(request));

        assertEquals(
                "User not found with id: 100",
                exception.getMessage());

        verify(userRepository).findById(100L);

        verify(bookingHoldRepository,
                never()).save(any());
    }

    /*
     * Verifies that hold creation
     * fails when the room type
     * does not exist.
     */
    @Test
    void createHold_ShouldThrowException_WhenRoomTypeDoesNotExist() {

        BookingHoldRequestDTO request =
                new BookingHoldRequestDTO();

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
                        () -> bookingHoldService.createHold(request));

        assertEquals(
                "Room Type not found with id: 100",
                exception.getMessage());

        verify(roomTypeRepository).findById(100L);
    }

    /*
     * Verifies that hold creation
     * fails for invalid dates.
     */
    @Test
    void createHold_ShouldThrowException_WhenDatesAreInvalid() {

        BookingHoldRequestDTO request =
                new BookingHoldRequestDTO();

        request.setUserId(1L);
        request.setRoomTypeId(1L);
        request.setCheckInDate(LocalDate.now().plusDays(5));
        request.setCheckOutDate(LocalDate.now().plusDays(5));

        User user = new User();
        user.setUserId(1L);

        RoomType roomType = new RoomType();
        roomType.setRoomTypeId(1L);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(roomTypeRepository.findById(1L))
                .thenReturn(Optional.of(roomType));

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> bookingHoldService.createHold(request));

        assertEquals(
                "Check-in date must be before check-out date",
                exception.getMessage());

        verify(bookingHoldRepository,
                never()).save(any());
    }

    /*
     * Verifies that an existing
     * booking hold is returned
     * successfully.
     */
    @Test
    void getHoldById_ShouldReturnHold() {

        BookingHold hold = new BookingHold();

        hold.setHoldId("10");
        hold.setUserId(1L);
        hold.setRoomTypeId(1L);
        hold.setCheckInDate(LocalDate.now().plusDays(1));
        hold.setCheckOutDate(LocalDate.now().plusDays(3));
        hold.setStatus(BookingHoldStatus.ACTIVE);
        hold.setCreatedAt(LocalDateTime.now());
        hold.setExpiresAt(LocalDateTime.now().plusMinutes(5));

        when(bookingHoldRepository.findById("10"))
                .thenReturn(Optional.of(hold));

        BookingHoldResponseDTO response =
                bookingHoldService.getHoldById("10");

        assertNotNull(response);
        assertEquals("10", response.getHoldId());
        assertEquals(1L, response.getUserId());
        assertEquals(BookingHoldStatus.ACTIVE,
                response.getStatus());

        verify(bookingHoldRepository).findById("10");
    }

    /*
     * Verifies that requesting a
     * non-existing booking hold
     * throws an exception.
     */
    @Test
    void getHoldById_ShouldThrowException_WhenHoldDoesNotExist() {

        when(bookingHoldRepository.findById("100"))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> bookingHoldService.getHoldById("100"));

        assertEquals(
                "Booking Hold not found with id: 100",
                exception.getMessage());

        verify(bookingHoldRepository).findById("100");
    }

    /*
     * Verifies that an active
     * booking hold can be
     * cancelled successfully.
     */
    @Test
    void cancelHold_ShouldCancelHold() {

        BookingHold hold = new BookingHold();

        hold.setHoldId("10");
        hold.setStatus(BookingHoldStatus.ACTIVE);

        when(bookingHoldRepository.findById("10"))
                .thenReturn(Optional.of(hold));

        when(bookingHoldRepository.save(any(BookingHold.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        bookingHoldService.cancelHold("10");

        assertEquals(
                BookingHoldStatus.CANCELLED,
                hold.getStatus());

        verify(bookingHoldRepository).findById("10");
        verify(bookingHoldRepository).save(hold);
    }

    /*
     * Verifies that cancelling
     * a non-existing booking hold
     * throws an exception.
     */
    @Test
    void cancelHold_ShouldThrowException_WhenHoldDoesNotExist() {

        when(bookingHoldRepository.findById("100"))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> bookingHoldService.cancelHold("100"));

        assertEquals(
                "Booking Hold not found with id: 100",
                exception.getMessage());

        verify(bookingHoldRepository).findById("100");

        verify(bookingHoldRepository,
                never()).save(any());
    }

    /*
     * Verifies that an active
     * booking hold is released
     * successfully.
     */
    @Test
    void releaseActiveHold_ShouldDeleteHold() {

        bookingHoldService.releaseActiveHold(10L);

        verify(bookingHoldRepository)
                .deleteById("10");
    }

}

