package ru.practicum.shareit.exceptions;

public class ItemIsUnavailable extends RuntimeException {
    public ItemIsUnavailable(String message) {
        super(message);
    }
}
