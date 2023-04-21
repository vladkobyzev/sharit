package ru.practicum.shareit.exceptions;

public class BookingStatusAlreadySet extends RuntimeException {
    public BookingStatusAlreadySet(String message) {
        super(message);
    }
}
