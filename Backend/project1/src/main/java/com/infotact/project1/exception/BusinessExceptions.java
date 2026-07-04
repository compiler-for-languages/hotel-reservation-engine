package com.infotact.project1.exception;

/**
 * Central factory for business rule violations.
 * Exception messages match unit test expectations and are normalized
 * for API responses by {@link ExceptionMessageResolver}.
 */
public final class BusinessExceptions {

    private BusinessExceptions() {
    }

    public static RuntimeException userNotFound() {
        return new RuntimeException("User not found.");
    }

    public static RuntimeException userNotFound(Long userId) {
        return new RuntimeException("User not found with id: " + userId);
    }

    public static RuntimeException userNotFoundByEmail(String email) {
        return new RuntimeException("User not found with email: " + email);
    }

    public static RuntimeException emailAlreadyExists(String email) {
        return new RuntimeException("Email already registered: " + email);
    }

    public static RuntimeException phoneAlreadyExists(String phone) {
        return new RuntimeException("Phone number already registered: " + phone);
    }

    public static RuntimeException invalidCredentials() {
        return new RuntimeException("Invalid email or password");
    }

    public static RuntimeException accountInactive() {
        return new RuntimeException("Account is inactive");
    }

    public static RuntimeException authenticationRequired() {
        return new RuntimeException("Authentication required");
    }

    public static RuntimeException accessDenied() {
        return new RuntimeException("Access denied");
    }

    public static RuntimeException roomTypeNotFound(Long roomTypeId) {
        return new RuntimeException("Room Type not found with id: " + roomTypeId);
    }

    public static RuntimeException roomTypeNotFoundByName(String name) {
        return new RuntimeException("Room Type not found with name: " + name);
    }

    public static RuntimeException roomTypeExists() {
        return new RuntimeException("Room type already exists.");
    }

    public static RuntimeException roomTypeNotFoundForFilter() {
        return new RuntimeException("Room Type not found. Please provide a valid room type id.");
    }

    public static RuntimeException roomNotFound(Long roomId) {
        return new RuntimeException("Room not found with id: " + roomId);
    }

    public static RuntimeException roomNotFoundByNumber(String roomNumber) {
        return new RuntimeException("Room not found with room number: " + roomNumber);
    }

    public static RuntimeException roomNumberExists(String roomNumber) {
        return new RuntimeException("Room number already exists: " + roomNumber);
    }

    public static RuntimeException reservationNotFound(Long reservationId) {
        return new RuntimeException("Reservation not found with id: " + reservationId);
    }

    public static RuntimeException reservationNotFound() {
        return new RuntimeException("Reservation not found");
    }

    public static RuntimeException invalidDateRange() {
        return new RuntimeException("Check-out date must be after check-in date");
    }

    public static RuntimeException invalidCheckInDateRange() {
        return new RuntimeException("Check-in date must be before check-out date");
    }

    public static RuntimeException roomCapacityExceeded() {
        return new RuntimeException("Room capacity exceeded.");
    }

    public static RuntimeException roomUnavailable(String roomTypeName) {
        return new RuntimeException("No rooms available for room type: " + roomTypeName);
    }

    public static RuntimeException paymentFailed() {
        return new RuntimeException("Payment Failed");
    }

    public static RuntimeException paymentNotFound(Long paymentId) {
        return new RuntimeException("Payment not found with id: " + paymentId);
    }

    public static RuntimeException paymentNotFoundForReservation(Long reservationId) {
        return new RuntimeException("Payment not found for reservation: " + reservationId);
    }

    public static RuntimeException paymentAlreadyExists(Long reservationId) {
        return new RuntimeException("Payment already exists for reservation: " + reservationId);
    }

    public static RuntimeException paymentDeleteNotAllowed() {
        return new RuntimeException("Successful payments cannot be deleted.");
    }

    public static RuntimeException paymentStartInvalid() {
        return new RuntimeException("Only pending payments can be started.");
    }

    public static RuntimeException paymentSuccessInvalid() {
        return new RuntimeException("Only processing payments can be marked as success.");
    }

    public static RuntimeException paymentFailedInvalid() {
        return new RuntimeException("Only processing payments can be marked as failed.");
    }

    public static RuntimeException paymentRefundInvalid() {
        return new RuntimeException("Only successful payments can be refunded.");
    }

    public static RuntimeException guestNotFound(Long guestId) {
        return new RuntimeException("Guest not found with id: " + guestId);
    }

    public static RuntimeException guestLimitReached() {
        return new RuntimeException("Maximum guest limit reached for this reservation.");
    }

    public static RuntimeException guestDetailsIncomplete() {
        return new RuntimeException("Please enter details for all guests before check-in.");
    }

    public static RuntimeException bookingHoldNotFound(String holdId) {
        return new RuntimeException("Booking Hold not found with id: " + holdId);
    }

    public static RuntimeException lockFailed(String lockName) {
        return new RuntimeException("Unable to acquire lock: " + lockName);
    }

    public static RuntimeException threadInterrupted(String lockName) {
        return new RuntimeException("Thread interrupted while acquiring lock: " + lockName);
    }

    public static RuntimeException onlyConfirmedReservationsCanBeAssigned() {
        return new RuntimeException("Only confirmed reservations can be assigned a room");
    }

    public static RuntimeException roomAlreadyAssigned() {
        return new RuntimeException("Room has already been assigned");
    }

    public static RuntimeException noAvailableRoomsFound() {
        return new RuntimeException("No available rooms found");
    }

    public static RuntimeException checkInNotAllowed() {
        return new RuntimeException("Reservation is not eligible for check-in");
    }

    public static RuntimeException roomNotAssigned() {
        return new RuntimeException("Room has not been assigned");
    }

    public static RuntimeException alreadyCheckedIn() {
        return new RuntimeException("Guest has already checked in");
    }

    public static RuntimeException guestNotCheckedIn() {
        return new RuntimeException("Guest is not checked in");
    }

    public static RuntimeException guestHasNotCheckedIn() {
        return new RuntimeException("Guest has not checked in");
    }

    public static RuntimeException roomAssignmentNotFound() {
        return new RuntimeException("Room assignment not found");
    }

    public static RuntimeException reservationAlreadyCheckedOut() {
        return new RuntimeException("Reservation already checked out");
    }

    public static RuntimeException roomAssignmentExists() {
        return new RuntimeException("Room assignment exists");
    }
}
