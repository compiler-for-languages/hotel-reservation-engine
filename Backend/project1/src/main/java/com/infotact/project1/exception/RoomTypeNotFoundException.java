package com.infotact.project1.exception;

/*
 * Thrown when a requested room type
 * does not exist in the system.
 */
public class RoomTypeNotFoundException
        extends RuntimeException {

    public RoomTypeNotFoundException(String message) {
        super(message);
    }
}