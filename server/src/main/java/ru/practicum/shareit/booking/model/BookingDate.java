package ru.practicum.shareit.booking.model;

import java.time.LocalDateTime;

public interface BookingDate {
    Long getId();

    LocalDateTime getBookingDate();

    Long getBookerId();

}
