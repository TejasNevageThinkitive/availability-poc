package com.waystar.waystar.entity.enums;

public enum BookingWindowTimeZone {
    EST("America/New_York"),
    CT("America/Chicago"),
    MT("America/Denver"),
    PT("America/Los_Angeles"),
    AKT("America/Anchorage"),
    HST("Pacific/Honolulu"),
    HAST("Pacific/Honolulu"),
    MST("America/Phoenix"),
    IST("Asia/Kolkata");

    private final String value;

    BookingWindowTimeZone(String value) {
        this.value = value;
    }

    public String getValue(){
        return value;
    }
}
