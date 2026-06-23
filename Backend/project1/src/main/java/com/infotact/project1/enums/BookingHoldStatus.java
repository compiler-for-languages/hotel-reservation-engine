package com.infotact.project1.enums;

public enum BookingHoldStatus {

    ACTIVE,
    CONVERTED,// Successfully converted to reservation
    EXPIRED,// Automatically expired after TTL
    CANCELLED // User cancelled before payment

}