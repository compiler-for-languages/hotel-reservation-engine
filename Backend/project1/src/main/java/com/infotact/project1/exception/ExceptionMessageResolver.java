package com.infotact.project1.exception;

import org.springframework.dao.DataIntegrityViolationException;

import java.util.Locale;
import java.util.regex.Pattern;

public final class ExceptionMessageResolver {

    public static final String GENERIC_MESSAGE =
            "Operation could not be completed. Please try again later.";

    private static final Pattern TECHNICAL_PATTERN = Pattern.compile(
            "could not execute statement|sqlstate|sql exception|sqlexception|"
                    + "org\\.hibernate|hibernateexception|jdbc|postgresql|mysql|"
                    + "constraint violation|foreign key constraint|violates foreign key|"
                    + "violates unique constraint|duplicate key|data integrity|"
                    + "referential integrity|detached entity|persistence exception|"
                    + "org\\.springframework\\.dao|constraint \"|detail: key",
            Pattern.CASE_INSENSITIVE
    );

    private ExceptionMessageResolver() {
    }

    public static String resolveRuntimeMessage(String message) {
        if (message == null || message.isBlank()) {
            return GENERIC_MESSAGE;
        }

        String trimmed = message.trim();
        if (isTechnical(trimmed)) {
            return GENERIC_MESSAGE;
        }

        String resolvedByCode = resolveBusinessCode(trimmed);
        if (resolvedByCode != null) {
            return resolvedByCode;
        }

        return normalizeBusinessMessage(trimmed);
    }

    private static String resolveBusinessCode(String code) {
        switch (code) {
            // Authentication
            case "INVALID_CREDENTIALS":
                return "Invalid email or password.";
            case "ACCOUNT_INACTIVE":
                return "Your account is inactive. Please contact the administrator.";
            case "AUTHENTICATION_REQUIRED":
                return "Authentication required.";
            case "ACCESS_DENIED":
                return "You are not authorized to perform this action.";

            // Users
            case "USER_NOT_FOUND":
                return "User not found.";
            case "EMAIL_ALREADY_EXISTS":
                return "Email already registered.";
            case "PHONE_ALREADY_EXISTS":
                return "Phone number already registered.";
            case "SELF_DELETE_NOT_ALLOWED":
                return "Administrators cannot delete their own account.";
            case "SELF_DEACTIVATE_NOT_ALLOWED":
                return "Administrators cannot deactivate their own account.";

            // Reservations
            case "RESERVATION_NOT_FOUND":
                return "Reservation not found.";
            case "INVALID_DATE_RANGE":
                return "Check-out date must be after check-in date.";
            case "ROOM_CAPACITY_EXCEEDED":
                return "Guest count exceeds room capacity.";
            case "ROOM_UNAVAILABLE":
                return "Sorry, no rooms are available for the selected room type and dates.";
            case "RESERVATION_ALREADY_CHECKED_OUT":
                return "This reservation has already been checked out and cannot be modified.";
            case "ROOM_ASSIGNMENT_EXISTS":
                return "This reservation cannot be deleted because room assignments already exist.";
            case "RESERVATION_DELETE_NOT_ALLOWED":
                return "This reservation cannot be deleted.";

            // Rooms
            case "ROOM_NOT_FOUND":
                return "Room not found.";
            case "ROOM_NUMBER_EXISTS":
                return "Room number already exists.";
            case "ROOM_ALREADY_ASSIGNED":
                return "Room has already been assigned.";
            case "NO_AVAILABLE_ROOM":
                return "No available rooms found.";

            // Room Types
            case "ROOM_TYPE_NOT_FOUND":
                return "Room type not found.";
            case "ROOM_TYPE_EXISTS":
                return "Room type already exists.";
            case "ROOM_TYPE_DELETE_NOT_ALLOWED":
                return "This room type cannot be deleted because rooms are currently assigned to it. Suggestion: Deactivate instead of deleting.";

            // Payments
            case "PAYMENT_NOT_FOUND":
                return "Payment not found.";
            case "PAYMENT_ALREADY_EXISTS":
                return "Payment already exists for this reservation.";
            case "PAYMENT_DELETE_NOT_ALLOWED":
                return "Successful payments cannot be deleted.";
            case "PAYMENT_START_INVALID":
                return "Only pending payments can be processed.";
            case "PAYMENT_SUCCESS_INVALID":
                return "Only processing payments can be marked as successful.";
            case "PAYMENT_FAILED_INVALID":
                return "Only processing payments can be marked as failed.";
            case "PAYMENT_REFUND_INVALID":
                return "Only successful payments can be refunded.";

            // Guests
            case "GUEST_NOT_FOUND":
                return "Guest not found.";
            case "GUEST_LIMIT_REACHED":
                return "Maximum guest limit reached for this reservation.";
            case "GUEST_DETAILS_INCOMPLETE":
                return "Please enter details for all guests before check-in.";

            // Reception
            case "CHECKIN_NOT_ALLOWED":
                return "Reservation is not eligible for check-in.";
            case "CHECKOUT_NOT_ALLOWED":
                return "Guest is not checked in.";
            case "ROOM_NOT_ASSIGNED":
                return "A room has not been assigned to this reservation yet.";
            case "ALREADY_CHECKED_IN":
                return "This guest has already checked in.";
            case "NOT_CHECKED_IN":
                return "This guest has not checked in yet.";

            // Booking Hold
            case "BOOKING_HOLD_NOT_FOUND":
                return "Booking hold not found.";

            // Locks
            case "LOCK_FAILED":
                return "Could not acquire lock. Please try again.";
            case "THREAD_INTERRUPTED":
                return "Operation was interrupted. Please try again.";

            default:
                return null;
        }
    }

    public static String resolveDataIntegrityMessage(DataIntegrityViolationException ex) {
        String combined = collectMessages(ex).toLowerCase(Locale.ROOT);

        if (combined.contains("duplicate") || combined.contains("unique constraint")) {
            return "This record already exists.";
        }

        if (combined.contains("foreign key") || combined.contains("constraint")) {
            if (containsAny(combined, "room_type", "roomtype", "room type")) {
                return "Room type cannot be deleted because rooms are still using it.";
            }
            if (containsAny(combined, "room_assignment", "roomassignment", "assignment")) {
                return "Reservation cannot be deleted because it has assigned room records.";
            }
            if (combined.contains("reservation")) {
                return "Reservation cannot be deleted because it has assigned room records.";
            }
            if (combined.contains("payment")) {
                return "This record cannot be deleted because it is linked to one or more payments.";
            }
            if (combined.contains("guest")) {
                return "This record cannot be deleted because guest records are still linked to it.";
            }
            if (combined.contains("user")) {
                return "User cannot be deleted because related records still exist.";
            }
            if (combined.contains("room")) {
                return "Room cannot be deleted because it is still linked to other records.";
            }
            return "This record cannot be deleted because related records still exist.";
        }

        return GENERIC_MESSAGE;
    }

    public static boolean isTechnical(String message) {
        return TECHNICAL_PATTERN.matcher(message).find();
    }

    private static String normalizeBusinessMessage(String message) {
        String lower = message.toLowerCase(Locale.ROOT);

        if (lower.contains("email already registered")) {
            return "Email already registered.";
        }
        if (lower.contains("phone number already registered")) {
            return "Phone number already registered.";
        }
        if (lower.contains("invalid email or password")) {
            return "Invalid email or password.";
        }
        if (lower.contains("account is inactive")) {
            return "Account is inactive.";
        }
        if (lower.contains("room capacity exceeded") || lower.contains("capacity exceeded")) {
            return "Guest count exceeds room capacity.";
        }
        if (lower.contains("no rooms available") || lower.contains("room not available")) {
            return "Room not available for the selected dates.";
        }
        if (lower.contains("room type already exists")) {
            return "Room type already exists.";
        }
        if (lower.contains("room number already exists")) {
            return "Room number already exists.";
        }
        if (lower.contains("payment already exists")) {
            return "Payment already exists for this reservation.";
        }
        if (lower.contains("payment already completed") || lower.contains("successful payments cannot be deleted")) {
            return "Payment already completed.";
        }
        if (lower.contains("only pending payments can be moved to processing")) {
            return "Only pending payments can be processed.";
        }
        if (lower.contains("only processing payments can be marked as success")) {
            return "Only processing payments can be marked as successful.";
        }
        if (lower.contains("only processing payments can be marked as failed")) {
            return "Only processing payments can be marked as failed.";
        }
        if (lower.contains("only success payments can be refunded")) {
            return "Only successful payments can be refunded.";
        }
        if (lower.contains("check-out date must be after check-in date")) {
            return "Check-out date must be after check-in date.";
        }
        if (lower.contains("maximum guest limit reached")) {
            return "Maximum guest limit reached for this reservation.";
        }
        if (lower.contains("please enter details for all guests before check-in")) {
            return "Please enter details for all guests before check-in.";
        }
        if (lower.contains("only confirmed reservations can be assigned")) {
            return "Only confirmed reservations can be assigned a room.";
        }
        if (lower.contains("room has already been assigned")) {
            return "Room has already been assigned to this reservation.";
        }
        if (lower.contains("no available rooms found")) {
            return "No available rooms found for this reservation.";
        }
        if (lower.contains("reservation is not eligible for check-in")) {
            return "This reservation is not eligible for check-in.";
        }
        if (lower.contains("room has not been assigned")) {
            return "A room has not been assigned to this reservation yet.";
        }
        if (lower.contains("guest has already checked in")) {
            return "This guest has already checked in.";
        }
        if (lower.contains("guest is not checked in") || lower.contains("guest has not checked in")) {
            return "This guest has not checked in yet.";
        }
        if (lower.contains("room assignment not found")) {
            return "Room assignment not found for this reservation.";
        }
        if (lower.contains("administrators cannot deactivate their own account")) {
            return "Administrators cannot deactivate their own account.";
        }
        if (lower.contains("administrators cannot delete their own account")) {
            return "Administrators cannot delete their own account.";
        }
        if (lower.contains("not authorized to update this user")) {
            return "You are not authorized to update this user.";
        }
        if (lower.contains("not authorized to change account status")) {
            return "You are not authorized to change account status.";
        }
        if (lower.contains("authentication required")) {
            return "Authentication required.";
        }
        if (lower.contains("checked out")) {
            return "This reservation has already been checked out.";
        }

        if (lower.contains("reservation not found")) {
            return "Reservation not found.";
        }
        if (lower.contains("payment not found")) {
            return "Payment not found.";
        }
        if (lower.contains("guest not found")) {
            return "Guest not found.";
        }
        if (lower.contains("room type not found")) {
            return "Room type not found.";
        }
        if (lower.contains("room not found")) {
            return "Room not found.";
        }
        if (lower.contains("user not found")) {
            return "User not found.";
        }

        if (message.endsWith(".")) {
            return message;
        }

        return message + ".";
    }

    private static String collectMessages(Throwable throwable) {
        StringBuilder builder = new StringBuilder();
        Throwable current = throwable;

        while (current != null) {
            if (current.getMessage() != null) {
                if (builder.length() > 0) {
                    builder.append(' ');
                }
                builder.append(current.getMessage());
            }
            current = current.getCause();
        }

        return builder.toString();
    }

    private static boolean containsAny(String value, String... tokens) {
        for (String token : tokens) {
            if (value.contains(token)) {
                return true;
            }
        }
        return false;
    }
}
