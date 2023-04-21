package ru.practicum.shareit.exceptions;

public class AlreadyUsedEmail extends RuntimeException {
    public AlreadyUsedEmail(String message) {
        super(message);
    }
}
